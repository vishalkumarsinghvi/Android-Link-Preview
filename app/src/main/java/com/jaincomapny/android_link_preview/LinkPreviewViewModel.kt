package com.jaincomapny.android_link_preview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jaincomapny.androidlinkpreview.LinkMetadataParser
import com.jaincomapny.androidlinkpreview.LinkPreviewData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LinkPreviewViewModel : ViewModel() {

    private val _linkPreviewData = MutableStateFlow<LinkPreviewData?>(null)
    val linkPreviewData: StateFlow<LinkPreviewData?> = _linkPreviewData

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun fetchLinkPreviewData(url: String) {
        viewModelScope.launch {
            _isLoading.emit(true)
            val preview = LinkMetadataParser().parse(url)
            _linkPreviewData.emit(preview)
            _isLoading.emit(false)
        }
    }
}