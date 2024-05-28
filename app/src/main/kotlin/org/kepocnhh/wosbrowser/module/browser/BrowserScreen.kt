package org.kepocnhh.wosbrowser.module.browser

import android.os.Environment
import android.util.Log
import android.view.ViewGroup
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
import java.util.UUID

private const val TAG = "[Browser]"

private fun onWebResponse(response: WebResponse) {
    Log.d(TAG, "on web response: $response: ${response.uri}")
    val body = response.body
    if (body == null) {
        Log.d(TAG, "body of ${response.uri} is null")
        return
    }
    val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    val file = dir.resolve(UUID.randomUUID().toString())
    Log.d(TAG, "try download ${response.uri} to ${file.absolutePath}")
    file.writeBytes(body.readBytes())
    Log.d(TAG, "write ${file.absolutePath} ok")
}

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
//                .allowInsecureConnections(GeckoRuntimeSettings.ALLOW_ALL)
                .build()
            val value = GeckoRuntime
                .create(context, settings)
            mutableStateOf(value)
        }.value
        val session = remember {
            val session = GeckoSession()
            session.contentDelegate = object : GeckoSession.ContentDelegate {
                override fun onContextMenu(
                    session: GeckoSession,
                    screenX: Int,
                    screenY: Int,
                    element: GeckoSession.ContentDelegate.ContextElement,
                ) {
                    val message = """
                        onContextMenu:
                        element:
                        type: ${element.type}
                        link: ${element.linkUri}
                    """.trimIndent()
                    Log.d(TAG, message)
                }

                override fun onExternalResponse(session: GeckoSession, response: WebResponse) {
                    onWebResponse(response)
                }
            }
            session.navigationDelegate = object : GeckoSession.NavigationDelegate {
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

//                override fun onLoadRequest(
//                    session: GeckoSession,
//                    request: GeckoSession.NavigationDelegate.LoadRequest,
//                ): GeckoResult<AllowOrDeny> {
//                    if (request.target == GeckoSession.NavigationDelegate.TARGET_WINDOW_NEW) {
//                        session.loadUri(request.uri)
//                    }
//                    return GeckoResult.fromValue(AllowOrDeny.ALLOW)
//                }
            }
//            session.settings.allowJavascript = true
//            session.settings.viewportMode = GeckoSessionSettings.VIEWPORT_MODE_MOBILE
//            session.settings.userAgentMode = GeckoSessionSettings.USER_AGENT_MODE_MOBILE
//            session.settings.userAgentOverride = null
            session.open(runtime)
            mutableStateOf(session)
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
                if (uri != null) {
                    session.loadUri(uri)
                }
            }
        )
        val uri = requested.value
        if (uri == null) {
//            val expected = "about:config"
//            val expected = "about:buildconfig"
//            val expected = "https://unsplash.com/wallpapers/animals/panda"
            val expected = "google.com"
//            val expected = "github.com"
            val text = "go to:\n$expected"
            BasicText(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .align(Alignment.Center)
                    .clickable(enabled = requested.value == null) {
                        requested.value = expected
                    }
                    .padding(16.dp)
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
    }
}
