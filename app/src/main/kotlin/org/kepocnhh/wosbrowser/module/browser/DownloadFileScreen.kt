package org.kepocnhh.wosbrowser.module.browser

import android.os.Environment
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.kepocnhh.wosbrowser.util.compose.STDBox
import org.kepocnhh.wosbrowser.util.showToast
import java.io.InputStream
import java.util.UUID

@Composable
internal fun DownloadFileScreen(
    onBack: () -> Unit,
    uri: String,
    body: InputStream,
) {
    val context = LocalContext.current
    val loadingState = remember { mutableStateOf(false) }
    DisposableEffect(Unit) {
        onDispose {
            runCatching {
                body.close()
            }
        }
    }
    LaunchedEffect(loadingState.value) {
        if (loadingState.value) {
            withContext(Dispatchers.Default) {
                val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = dir.resolve(UUID.randomUUID().toString())
                runCatching {
                    val bytes = body.readBytes()
                    file.writeBytes(bytes)
                    file
                }
            }.fold(
                onSuccess = { file ->
                    context.showToast("File $file downloaded.")
                },
                onFailure = { error ->
                    context.showToast("File download error: $error")
                },
            )
            onBack()
        }
    }
    STDBox(
        modifier = Modifier.fillMaxSize(),
        onDismissed = onBack,
        userSwipeEnabled = !loadingState.value,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
        ) {
            BasicText(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .align(Alignment.Center),
                text = "download file: $uri?",
                style = TextStyle(fontSize = 12.sp, textAlign = TextAlign.Center),
            )
            BasicText(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .align(Alignment.BottomCenter)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(enabled = !loadingState.value) {
                        loadingState.value = true
                    }
                    .wrapContentSize(),
                text = "ok",
                style = TextStyle(color = Color.White),
            )
        }
    }
}
