package com.grayguava.opentune.lite.ui.player

import android.os.Build
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer

/**
 * A cover on screen: which art, which song and where it sits in the queue.
 * The cover changes with the song, so a sharper copy of the same song's art
 * arriving later swaps in place rather than playing the change again.
 */
internal data class CoverShown(val url: String?, val index: Int, val id: String? = null)

/**
 * One cover dissolving into the next: the old one blurs away while the new
 * one comes into focus. With [animate] off it is a plain crossfade. [content]
 * draws a cover; the modifier it's handed carries the motion, so it goes on
 * whatever should move.
 */
@Composable
internal fun CoverSwap(
    shown: CoverShown,
    animate: Boolean = true,
    modifier: Modifier = Modifier,
    content: @Composable (url: String?, motion: Modifier) -> Unit,
) {
    AnimatedContent(
        targetState = shown,
        contentKey = { it.id ?: it.url },
        transitionSpec = { coverTransition(animate) },
        label = "cover",
        modifier = modifier,
    ) { s ->
        // 0 while waiting to come in, 1 on screen, 2 once gone: the drawn
        // change works from this, on its own clock.
        val phase = if (animate) {
            transition.animateFloat(
                transitionSpec = { tween(560, easing = FastOutSlowInEasing) },
                label = "coverPhase",
            ) { when (it) { EnterExitState.PreEnter -> 0f; EnterExitState.Visible -> 1f; EnterExitState.PostExit -> 2f } }
        } else {
            null
        }
        // Coming in or staying, as opposed to on the way out.
        val coming = transition.targetState == EnterExitState.Visible
        val motion = if (phase == null) Modifier else Modifier.graphicsLayer { dissolve(phase.value, coming) }
        content(s.url, motion)
    }
}

/** The dissolve: the old cover blurs away as the new one comes into focus. */
private fun GraphicsLayerScope.dissolve(p: Float, entering: Boolean) {
    val e = p.coerceIn(0f, 1f)           // how far in, while coming
    val x = (p - 1f).coerceIn(0f, 1f)    // how far out, while going
    // Graphics layers are reused. Clear effects left by an interrupted dissolve.
    renderEffect = null
    val r = (if (entering) 1f - e else x) * 28f * density
    if (r > 0.5f && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) renderEffect = BlurEffect(r, r, TileMode.Decal)
    alpha = if (entering) e else 1f - x
    val s = if (entering) 1.04f - 0.04f * e else 1f + 0.03f * x
    scaleX = s; scaleY = s
}

/** The two covers are kept until the dissolve has finished. */
private fun coverTransition(animate: Boolean): ContentTransform =
    if (animate) EnterTransition.None togetherWith fadeOut(tween(1, delayMillis = 700))
    else (fadeIn(tween(450)) + scaleIn(tween(450), initialScale = 0.94f)) togetherWith fadeOut(tween(300))
