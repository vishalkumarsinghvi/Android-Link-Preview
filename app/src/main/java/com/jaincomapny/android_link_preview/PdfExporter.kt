package com.jaincomapny.android_link_preview

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import okhttp3.OkHttpClient
import okhttp3.Request

object PdfExporter {

    /**
     * Generates a PDF for [item] and saves it to the Downloads folder.
     * Must be called from a background thread (performs network + file I/O).
     * Returns true on success.
     */
    fun export(context: Context, item: BulkPreviewItem): Boolean {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        draw(page.canvas, item)
        document.finishPage(page)
        return save(context, document, "preview_${System.currentTimeMillis()}.pdf")
    }

    private fun draw(canvas: Canvas, item: BulkPreviewItem) {
        val margin = 40f
        val width = 595f - 2 * margin
        var y = margin

        // ── Header ──────────────────────────────────────────────
        val headerPaint = paint(22f, Color.BLACK, bold = true)
        canvas.drawText("Link Preview", margin, y + headerPaint.textSize, headerPaint)
        y += headerPaint.textSize + 10f
        canvas.drawLine(margin, y, margin + width, y, paint(1f, Color.LTGRAY))
        y += 16f

        // ── Thumbnail ────────────────────────────────────────────
        val bitmap = loadBitmap(item.data?.imageUrl)
        if (bitmap != null) {
            val imgH = (width * bitmap.height.toFloat() / bitmap.width).coerceAtMost(180f)
            canvas.drawBitmap(bitmap, null, RectF(margin, y, margin + width, y + imgH), null)
            y += imgH + 14f
        }

        // ── Title ────────────────────────────────────────────────
        y = drawWrapped(canvas, item.data?.title ?: "No Title", paint(18f, Color.BLACK, bold = true), margin, y, width)
        y += 6f

        // ── URL ──────────────────────────────────────────────────
        y = drawWrapped(canvas, item.url, paint(11f, Color.rgb(0, 100, 210)), margin, y, width)
        y += 10f

        // ── Description ──────────────────────────────────────────
        drawWrapped(canvas, item.data?.description ?: "No description available.", paint(13f, Color.DKGRAY), margin, y, width)

        // ── Branding footer (pinned to bottom of page) ───────────
        val footerPaint = paint(10f, Color.GRAY)
        val footerText = "Powered by Jain Company"
        val footerX = (595f - footerPaint.measureText(footerText)) / 2f
        canvas.drawLine(margin, 810f, margin + width, 810f, paint(0.5f, Color.LTGRAY))
        canvas.drawText(footerText, footerX, 828f, footerPaint)
    }

    // Returns the y position after the last drawn line
    private fun drawWrapped(canvas: Canvas, text: String, p: Paint, x: Float, startY: Float, maxW: Float): Float {
        val lineH = p.textSize + 4f
        var y = startY + p.textSize
        var line = ""
        for (word in text.split(" ")) {
            val test = if (line.isEmpty()) word else "$line $word"
            if (p.measureText(test) > maxW) {
                if (line.isNotEmpty()) { canvas.drawText(line, x, y, p); y += lineH }
                line = word
            } else {
                line = test
            }
        }
        if (line.isNotEmpty()) canvas.drawText(line, x, y, p)
        return y + 4f
    }

    private fun paint(size: Float, color: Int, bold: Boolean = false) = Paint().apply {
        textSize = size
        this.color = color
        isFakeBoldText = bold
        isAntiAlias = true
    }

    private fun loadBitmap(url: String?): Bitmap? {
        if (url.isNullOrBlank()) return null
        return try {
            val response = OkHttpClient().newCall(Request.Builder().url(url).build()).execute()
            val bytes = response.body.bytes()
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (e: Exception) {
            null
        }
    }

    private fun save(context: Context, document: PdfDocument, fileName: String): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                    put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }
                val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                    ?: return false
                context.contentResolver.openOutputStream(uri)?.use { document.writeTo(it) }
                values.clear()
                values.put(MediaStore.Downloads.IS_PENDING, 0)
                context.contentResolver.update(uri, values, null, null)
            } else {
                @Suppress("DEPRECATION")
                val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                dir.mkdirs()
                document.writeTo(java.io.File(dir, fileName).outputStream())
            }
            true
        } catch (e: Exception) {
            false
        } finally {
            document.close()
        }
    }
}
