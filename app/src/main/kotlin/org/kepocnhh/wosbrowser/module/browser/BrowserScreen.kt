package org.kepocnhh.wosbrowser.module.browser

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoView

@Composable
internal fun BrowserScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        val context = LocalContext.current
        val runtime = remember { mutableStateOf(GeckoRuntime.getDefault(context)) }.value
        val session = remember {
            val session = GeckoSession()
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
        BasicText(
            modifier = Modifier
                .fillMaxWidth()
                .height(124.dp)
                .background(Color.Black.copy(alpha = 0.5f))
                .align(Alignment.BottomCenter)
                .clickable {
                    requested.value = "github.com"
                }
                .wrapContentSize(),
            text = "<",
            style = TextStyle(color = Color.White),
        )
    }
}
