package org.kepocnhh.wosbrowser.module.browser

import android.util.Log
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoRuntimeSettings
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoView
import org.mozilla.geckoview.WebResponse
import java.io.InputStream

private const val TAG = "[Browser]"

@Composable
internal fun BrowserScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        val canGoBackState = remember { mutableStateOf(false) }
        val context = LocalContext.current
        val runtime = remember {
            val settings = GeckoRuntimeSettings.Builder()
                .build()
            val value = GeckoRuntime
                .create(context, settings)
            mutableStateOf(value)
        }.value
        val download = remember { mutableStateOf<Pair<String, InputStream>?>(null) }
        val session = remember {
            val value = GeckoSession()
            value.contentDelegate = object : GeckoSession.ContentDelegate {
                override fun onExternalResponse(session: GeckoSession, response: WebResponse) {
                    Log.d(TAG, "on external response: ${response.uri}")
                    val body = response.body
                    if (download.value == null && body != null) {
                        download.value = response.uri to body
                    }
                }
            }
            value.navigationDelegate = object : GeckoSession.NavigationDelegate {
                override fun onLocationChange(
                    session: GeckoSession,
                    url: String?,
                    perms: MutableList<GeckoSession.PermissionDelegate.ContentPermission>,
                    hasUserGesture: Boolean,
                ) {
                    Log.d(TAG, "onLocationChange: $url")
                }

                override fun onCanGoBack(session: GeckoSession, canGoBack: Boolean) {
                    canGoBackState.value = canGoBack
                }
            }
            value.open(runtime)
            mutableStateOf(value)
        }.value
        val requested = remember { mutableStateOf<String?>(null) }
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                GeckoView(context).also {
                    it.layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    )
                    it.setSession(session)
                }
            },
            update = {
                val uri = requested.value
                // todo
            }
        )
        val uri = requested.value
        if (uri == null) {
            val target = "google.com"
            val text = "go to:\n$target"
            BasicText(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(enabled = requested.value == null) {
                        requested.value = target
                        session.loadUri(target)
                    }
                    .wrapContentSize(),
                text = text,
                style = TextStyle(color = Color.White, textAlign = TextAlign.Center),
            )
        }
        if (canGoBackState.value) {
            BasicText(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .align(Alignment.BottomCenter)
                    .clickable(enabled = canGoBackState.value) {
                        session.goBack()
                    }
                    .wrapContentSize(),
                text = "<",
                style = TextStyle(color = Color.White),
            )
        }
        val pair = download.value
        if (pair != null) {
            DownloadFileScreen(
                onBack = {
                    download.value = null
                },
                uri = pair.first,
                body = pair.second,
            )
        }
    }
}
