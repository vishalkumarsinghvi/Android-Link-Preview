package com.jaincomapny.android_link_preview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jaincomapny.androidlinkpreview.LinkMetadataParser
import com.jaincomapny.androidlinkpreview.LinkPreviewData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class BulkPreviewItem(
    val url: String,
    val data: LinkPreviewData?,
    val isError: Boolean = false
)

class LinkPreviewViewModel : ViewModel() {

    private val _linkPreviewData = MutableStateFlow<LinkPreviewData?>(null)
    val linkPreviewData: StateFlow<LinkPreviewData?> = _linkPreviewData

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _bulkPreviews = MutableStateFlow<List<BulkPreviewItem>>(emptyList())
    val bulkPreviews: StateFlow<List<BulkPreviewItem>> = _bulkPreviews

    // null = idle, non-null = (completed, total)
    private val _bulkProgress = MutableStateFlow<Pair<Int, Int>?>(null)
    val bulkProgress: StateFlow<Pair<Int, Int>?> = _bulkProgress

    fun fetchLinkPreviewData(url: String) {
        viewModelScope.launch {
            _isLoading.emit(true)
            val preview = LinkMetadataParser().parse(url)
            _linkPreviewData.emit(preview)
            _isLoading.emit(false)
        }
    }

    fun clearBulkPreviews() {
        viewModelScope.launch {
            _bulkPreviews.emit(emptyList())
            _bulkProgress.emit(null)
        }
    }

    fun fetchBulkPreviews(urls: List<String>) {
        viewModelScope.launch {
            _bulkPreviews.emit(emptyList())
            _bulkProgress.emit(0 to urls.size)

            // All fetches start concurrently
            val deferreds = urls.map { url ->
                async(Dispatchers.IO) {
                    val data = try {
                        LinkMetadataParser().parse(url)
                    } catch (e: Exception) {
                        null
                    }
                    BulkPreviewItem(url = url, data = data, isError = data == null)
                }
            }

            // Collect results in order, updating UI as each one finishes
            val collected = mutableListOf<BulkPreviewItem>()
            deferreds.forEachIndexed { index, deferred ->
                collected.add(deferred.await())
                _bulkPreviews.emit(collected.toList())
                _bulkProgress.emit((index + 1) to urls.size)
            }
            _bulkProgress.emit(null)
        }
    }
}
