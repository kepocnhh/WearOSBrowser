package org.kepocnhh.wosbrowser.util.compose

import androidx.compose.animation.core.snap
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.wear.compose.foundation.BasicSwipeToDismissBox
import androidx.wear.compose.foundation.SwipeToDismissBoxState
import androidx.wear.compose.foundation.rememberSwipeToDismissBoxState

@Composable
internal fun STDBox(
    modifier: Modifier,
    state: SwipeToDismissBoxState = rememberSwipeToDismissBoxState(
        animationSpec = snap(delayMillis = 0),
    ),
    userSwipeEnabled: Boolean = true,
    onDismissed: () -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    BasicSwipeToDismissBox(
        modifier = modifier,
        state = state,
        userSwipeEnabled = userSwipeEnabled,
        onDismissed = onDismissed,
    ) { isBackground: Boolean ->
        if (!isBackground) content()
    }
}
