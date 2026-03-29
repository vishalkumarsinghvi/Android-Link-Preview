package com.jaincomapny.android_link_preview

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material3.placeholder
import com.google.accompanist.placeholder.material3.shimmer
import com.google.android.gms.ads.MobileAds
import com.jaincomapny.android_link_preview.ui.theme.AndroidLinkPreviewTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {

    private lateinit var adManager: AdManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MobileAds.initialize(this)
        adManager = AdManager(this)
        enableEdgeToEdge()
        setContent {
            AndroidLinkPreviewTheme {
                val viewModel = ViewModelProvider(this)[LinkPreviewViewModel::class.java]
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LinkPreviewApp(
                        modifier = Modifier.padding(innerPadding),
                        viewModel = viewModel,
                        onDownloadItem = { item -> downloadWithAd(item) }
                    )
                }
            }
        }
    }

    private fun downloadWithAd(item: BulkPreviewItem) {
        adManager.showAdThenDownload(this) {
            lifecycleScope.launch(Dispatchers.IO) {
                val success = PdfExporter.export(this@MainActivity, item)
                withContext(Dispatchers.Main) {
                    val msg = if (success) "PDF saved to Downloads" else "Failed to save PDF"
                    Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

// ─── Root ─────────────────────────────────────────────────────────────────────

@Composable
fun LinkPreviewApp(
    modifier: Modifier = Modifier,
    viewModel: LinkPreviewViewModel,
    onDownloadItem: (BulkPreviewItem) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }

    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = "Link Preview",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Single") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Bulk") }
            )
        }
        when (selectedTab) {
            0 -> SingleLinkPreviewContent(viewModel = viewModel, onDownloadItem = onDownloadItem)
            1 -> BulkLinkPreviewContent(viewModel = viewModel, onDownloadItem = onDownloadItem)
        }
    }
}

// ─── Single tab ───────────────────────────────────────────────────────────────

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun SingleLinkPreviewContent(viewModel: LinkPreviewViewModel, onDownloadItem: (BulkPreviewItem) -> Unit) {
    var url by remember { mutableStateOf(TextFieldValue("")) }
    val previewData = viewModel.linkPreviewData.collectAsState(null)
    val isLoading = viewModel.isLoading.collectAsState()
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text("Enter a URL to preview", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(8.dp))
        BasicTextField(
            value = url,
            onValueChange = { url = it; errorMessage = null },
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.LightGray, RoundedCornerShape(8.dp))
                .padding(12.dp)
        )
        errorMessage?.let {
            Text(
                it,
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        Button(
            onClick = {
                val raw = url.text.trim()
                val formatted = when {
                    raw.isBlank() -> { errorMessage = "URL cannot be empty."; null }
                    raw.startsWith("http://") -> { errorMessage = "Use https:// instead of http://"; null }
                    raw.startsWith("https://") -> raw
                    else -> "https://$raw"
                }
                if (formatted != null) viewModel.fetchLinkPreviewData(formatted)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) { Text("Submit") }

        Spacer(Modifier.height(16.dp))

        if (isLoading.value) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .placeholder(
                        visible = true,
                        highlight = PlaceholderHighlight.shimmer(),
                        color = Color.Gray.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(12.dp)
                    )
            )
        } else {
            previewData.value?.let { data ->
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            GlideImage(
                                model = data.imageUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(100.dp)
                                    .background(Color.Gray)
                            )
                            Column(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .padding(end = 44.dp)
                            ) {
                                Text(data.title ?: "No Title", style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    data.description ?: "No Description",
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(data.url ?: "No URL", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        IconButton(
                            onClick = {
                                onDownloadItem(BulkPreviewItem(url = data.url ?: "", data = data))
                            },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(6.dp)
                                .size(36.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SaveAlt,
                                contentDescription = "Save as PDF",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── Bulk tab ─────────────────────────────────────────────────────────────────

@Composable
fun BulkLinkPreviewContent(
    viewModel: LinkPreviewViewModel,
    onDownloadItem: (BulkPreviewItem) -> Unit
) {
    val context = LocalContext.current
    var inputText by remember { mutableStateOf("") }
    val bulkPreviews by viewModel.bulkPreviews.collectAsState()
    val bulkProgress by viewModel.bulkProgress.collectAsState()
    val isBulkLoading = bulkProgress != null

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        Text("Paste URLs — one per line", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(8.dp))

        BasicTextField(
            value = inputText,
            onValueChange = { inputText = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .background(Color.LightGray, RoundedCornerShape(8.dp))
                .padding(12.dp)
        )
        Spacer(Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    val urls = inputText.lines()
                        .map { it.trim() }
                        .filter { it.isNotBlank() && !it.startsWith("http://") }
                        .map { if (it.startsWith("https://")) it else "https://$it" }
                    if (urls.isNotEmpty()) viewModel.fetchBulkPreviews(urls)
                    else Toast.makeText(context, "No valid URLs found", Toast.LENGTH_SHORT).show()
                },
                enabled = !isBulkLoading,
                modifier = Modifier.weight(1f)
            ) {
                Text(if (isBulkLoading) "Fetching…" else "Fetch All")
            }
            OutlinedButton(
                onClick = {
                    inputText = ""
                    viewModel.clearBulkPreviews()
                },
                enabled = !isBulkLoading,
                modifier = Modifier.width(90.dp)
            ) {
                Text("Clear")
            }
        }

        bulkProgress?.let { (done, total) ->
            Spacer(Modifier.height(8.dp))
            Text("Fetching $done / $total…", style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { if (total > 0) done.toFloat() / total else 0f },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(bulkPreviews) { item ->
                BulkPreviewCard(item = item, onDownload = { onDownloadItem(item) })
            }
        }
    }
}

// ─── Bulk preview card ────────────────────────────────────────────────────────

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun BulkPreviewCard(item: BulkPreviewItem, onDownload: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        if (item.data == null) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Failed to load preview",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.error
                )
                Text(
                    text = item.url,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        } else {
            Box(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    GlideImage(
                        model = item.data.imageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(80.dp)
                            .background(Color.Gray)
                    )
                    Column(
                        modifier = Modifier
                            .padding(10.dp)
                            .padding(end = 44.dp)
                            .weight(1f)
                    ) {
                        Text(
                            text = item.data.title ?: "No Title",
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = item.data.description ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = item.url,
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                IconButton(
                    onClick = onDownload,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SaveAlt,
                        contentDescription = "Save as PDF",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
