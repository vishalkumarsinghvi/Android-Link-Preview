package com.jaincomapny.android_link_preview

import android.os.Bundle
import android.util.Log
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material3.placeholder
import com.google.accompanist.placeholder.material3.shimmer
import com.jaincomapny.android_link_preview.ui.theme.AndroidLinkPreviewTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidLinkPreviewTheme {
                val linkPreviewViewModel = ViewModelProvider(this@MainActivity)[LinkPreviewViewModel::class.java]
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LinkPreviewScreen(
                        modifier = Modifier.padding(innerPadding), viewModel = linkPreviewViewModel
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun LinkPreviewScreen(modifier: Modifier = Modifier, viewModel: LinkPreviewViewModel) {
    var url by remember { mutableStateOf(TextFieldValue("")) }
    val previewData = viewModel.linkPreviewData.collectAsState(null)
    val isLoading = viewModel.isLoading.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Link Preview",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            text = "Enter a URL to preview",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        BasicTextField(
            value = url,
            onValueChange = { url = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .background(Color.LightGray, RoundedCornerShape(8.dp))
                .padding(12.dp)
        )

        Button(
            onClick = {
                viewModel.fetchLinkPreviewData(url.text)
            }, modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("Submit")
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (isLoading.value) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .placeholder(
                        visible = true, highlight = PlaceholderHighlight.shimmer(), // ✅ no deprecation
                        color = Color.Gray.copy(alpha = 0.3f), shape = RoundedCornerShape(12.dp)
                    ), contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Gray.copy(alpha = 0.3f))
                )
            }
        } else {
            previewData.value?.let {
                Card(
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)
                ) {
                    Log.d("TAG", "LinkPreviewScreen: " + it.imageUrl)
                    Row {
                        GlideImage(
                            model = it.imageUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(100.dp)
                                .background(color = Color.Gray)
                        )
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = it.title ?: "No Title", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = it.description ?: "No Description",
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = it.url ?: "No URL", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}
