package com.grayguava.opentune.lite.ui

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.ui.unit.IntOffset

/**
 * How pages come and go: the page opened, the one it covers, and the same
 * two on the way back. Pages move along the Z axis, coming forward from
 * behind and sinking away on the way back. [still] (reduced animation) is a
 * short cross-fade.
 */
internal object PageMotion {
    private val ease = FastOutSlowInEasing

    fun enter(still: Boolean): EnterTransition = if (still) fadeIn(tween(160))
    else fadeIn(tween(220)) + scaleIn(spring(dampingRatio = 1f, stiffness = 420f), initialScale = 0.86f)

    fun exit(still: Boolean): ExitTransition = if (still) fadeOut(tween(120))
    else fadeOut(tween(200)) + scaleOut(tween(320, easing = ease), targetScale = 1.08f)

    fun popEnter(still: Boolean): EnterTransition = if (still) fadeIn(tween(160))
    else fadeIn(tween(240)) + scaleIn(tween(320, easing = ease), initialScale = 1.08f)

    fun popExit(still: Boolean): ExitTransition = if (still) fadeOut(tween(120))
    else fadeOut(tween(200)) + scaleOut(tween(280, easing = ease), targetScale = 0.86f)

    /** The full player rising over the page. */
    fun playerIn(still: Boolean): EnterTransition = if (still) fadeIn(tween(160))
    else slideInVertically(playerRise) { it } + fadeIn()

    /** The full player dropping away. */
    fun playerOut(still: Boolean): ExitTransition {
        if (still) return fadeOut(tween(140))
        return slideOutVertically(tween(260, easing = FastOutSlowInEasing)) { it } + fadeOut(tween(260))
    }

    // Apple Music's sheet: no bounce, short and responsive.
    private val playerRise = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = 550f,
        visibilityThreshold = IntOffset.VisibilityThreshold,
    )
}
