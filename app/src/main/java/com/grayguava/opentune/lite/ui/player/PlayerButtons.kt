package com.grayguava.opentune.lite.ui.player

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Back, play/pause and forward: play in an accent pill that stretches wide
 * while it plays, and the skips in tinted circles that tilt toward where they
 * go. The drawn glyphs below fold play into pause, and the skip arrows roll
 * a step on with each tap.
 */
@Composable
fun PlayerControls(
    isPlaying: Boolean,
    isBuffering: Boolean,
    hasNext: Boolean,
    onTogglePlay: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    modifier: Modifier = Modifier,
    animate: Boolean = true,
) {
    var nextRolls by remember { mutableIntStateOf(0) }
    var previousRolls by remember { mutableIntStateOf(0) }
    // Deep, strong feels for the transport, each its own (see Haptics.Pattern).
    val haptics = com.grayguava.opentune.lite.ui.components.rememberHaptics()
    val back = { haptics.pattern(com.grayguava.opentune.lite.ui.components.Haptics.Pattern.PREVIOUS); previousRolls++; onPrevious() }
    val ahead = { haptics.pattern(com.grayguava.opentune.lite.ui.components.Haptics.Pattern.NEXT); nextRolls++; onNext() }
    val playPause = {
        haptics.pattern(if (isPlaying) com.grayguava.opentune.lite.ui.components.Haptics.Pattern.PAUSE else com.grayguava.opentune.lite.ui.components.Haptics.Pattern.PLAY)
        onTogglePlay()
    }
    Row(
        modifier.fillMaxWidth().height(86.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CapsuleSkip(forward = false, previousRolls, enabled = true, animate, back)
        Spacer(Modifier.width(21.dp))
        CapsulePlay(isPlaying, isBuffering, animate, playPause)
        Spacer(Modifier.width(21.dp))
        CapsuleSkip(forward = true, nextRolls, hasNext, animate, ahead)
    }
}

/** How far a press has gone, 0 to 1, springing in under the finger and easing back out. */
@Composable
private fun pressAmount(pressed: Boolean, animate: Boolean): Float {
    val p by animateFloatAsState(
        if (pressed) 1f else 0f,
        when {
            !animate -> snap()
            pressed -> spring(dampingRatio = 0.8f, stiffness = 900f)
            else -> spring(dampingRatio = 0.55f, stiffness = 380f)
        },
        label = "press",
    )
    return p
}

private fun Modifier.button(interaction: MutableInteractionSource, enabled: Boolean, label: String, onClick: () -> Unit) =
    clickable(interactionSource = interaction, indication = com.grayguava.opentune.lite.ui.components.FocusRing(androidx.compose.foundation.shape.CircleShape), enabled = enabled, onClick = onClick)
        .semantics { role = Role.Button; contentDescription = label }

/**
 * A value that travels from one end to the other and back, for the loading
 * sheen. It runs only while [on] is true, so a still button costs nothing.
 */
@Composable
private fun looping(on: Boolean, from: Float, to: Float, ms: Int, reverse: Boolean): Float {
    if (!on) return from
    val v by rememberInfiniteTransition(label = "loop").animateFloat(
        from,
        to,
        infiniteRepeatable(
            tween(ms, easing = if (reverse) FastOutSlowInEasing else LinearEasing),
            if (reverse) RepeatMode.Reverse else RepeatMode.Restart,
        ),
        label = "loopValue",
    )
    return v
}

// ---- Capsule -----------------------------------------------------------------

@Composable
private fun CapsulePlay(isPlaying: Boolean, isBuffering: Boolean, animate: Boolean, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val interaction = remember { MutableInteractionSource() }
    val press = pressAmount(interaction.collectIsPressedAsState().value, animate)
    // 0 is a circle (paused), 1 a wide pill (playing), on a spring that overshoots a touch.
    val stretch by animateFloatAsState(
        if (isPlaying) 1f else 0f,
        if (animate) spring(dampingRatio = 0.55f, stiffness = 260f) else snap(),
        label = "capsule",
    )
    val sheen = looping(isBuffering && animate, -0.4f, 1.4f, 1_100, reverse = false)
    val h = 68.dp
    val w = h + 42.dp * stretch
    Box(
        Modifier
            .size(width = w.coerceAtLeast(49.dp), height = h)
            .graphicsLayer {
                // Squashes a little as it stretches, like something soft.
                val s = 1f - 0.06f * press
                scaleX = s
                scaleY = s * (1f - 0.05f * sin(PI.toFloat() * stretch.coerceIn(0f, 1f)))
            }
            .drawBehindCompat { size ->
                val radius = CornerRadius(size.height / 2f)
                drawRoundRect(scheme.primary, cornerRadius = radius)
                // A band of light sweeps across while the song loads.
                if (isBuffering && animate) {
                    val x = sheen * size.width
                    drawRoundRect(
                        Brush.horizontalGradient(
                            listOf(scheme.onPrimary.copy(alpha = 0f), scheme.onPrimary.copy(alpha = 0.28f), scheme.onPrimary.copy(alpha = 0f)),
                            startX = x - size.width * 0.35f,
                            endX = x + size.width * 0.35f,
                        ),
                        cornerRadius = radius,
                    )
                }
            }
            .button(interaction, true, if (isPlaying) "Pause" else "Play", onClick),
        contentAlignment = Alignment.Center,
    ) {
        PlayPauseGlyph(isPlaying, scheme.onPrimary, animate, Modifier.size(32.dp))
    }
}

@Composable
private fun CapsuleSkip(forward: Boolean, rolls: Int, enabled: Boolean, animate: Boolean, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val interaction = remember { MutableInteractionSource() }
    val press = pressAmount(interaction.collectIsPressedAsState().value, animate)
    // A lean toward where the tap is going, that wobbles back once let go.
    val lean = remember { Animatable(0f) }
    LaunchedEffect(rolls) {
        if (rolls == 0 || !animate) return@LaunchedEffect
        lean.snapTo(1f)
        lean.animateTo(0f, spring(dampingRatio = 0.35f, stiffness = 260f))
    }
    val dir = if (forward) 1f else -1f
    val alpha = if (enabled) 1f else 0.35f
    Box(
        Modifier
            .size(51.dp)
            .graphicsLayer {
                val s = 1f - 0.1f * press
                scaleX = s
                scaleY = s
                rotationZ = dir * (14f * lean.value + 8f * press)
                translationX = dir * (6.dp.toPx() * lean.value)
                this.alpha = alpha
            }
            .drawBehindCompat { size -> drawCircle(scheme.primary.copy(alpha = 0.16f + 0.1f * press), radius = size.minDimension / 2f) }
            .button(interaction, enabled, if (forward) "Next" else "Previous", onClick),
        contentAlignment = Alignment.Center,
    ) {
        SkipGlyph(forward, rolls, scheme.primary, animate, Modifier.size(27.dp))
    }
}

/** How deep the Morph button's waves are, as a part of its radius, when fully wavy. */
private const val MORPH_DEPTH = 0.075f
private const val MORPH_WAVES = 9

/**
 * Sets [path] to a circle of [radius] whose edge rises and falls in
 * [MORPH_WAVES] rounded waves, [depth] of the radius deep (0 is a plain
 * circle), turned by [degrees]. The crests touch [radius], so the shape
 * never grows past its box as the waves come in.
 */
// ---- Shared ------------------------------------------------------------------

/** Draws behind the content, with the drawing's size. */
private fun Modifier.drawBehindCompat(block: DrawScope.(Size) -> Unit) = drawBehind { block(size) }

/**
 * Play and pause as one shape. Each half of the triangle is a four-point
 * piece that travels to one of the pause bars, so a tap folds one into the
 * other on a spring that carries a touch past and settles.
 */
@Composable
fun PlayPauseGlyph(playing: Boolean, color: Color, animate: Boolean, modifier: Modifier = Modifier) {
    val p by animateFloatAsState(
        if (playing) 1f else 0f,
        if (animate) spring(dampingRatio = 0.62f, stiffness = 420f) else snap(),
        label = "playPause",
    )
    Canvas(modifier) { drawPlayPause(p.coerceIn(-0.1f, 1.1f), color) }
}

// The two pieces, as points in a unit square: top-left, top-right, bottom-right, bottom-left.
// Play is a triangle cut down the middle; pause is two bars.
private val PLAY_LEFT = floatArrayOf(0.20f, 0.13f, 0.53f, 0.315f, 0.53f, 0.685f, 0.20f, 0.87f)
private val PLAY_RIGHT = floatArrayOf(0.53f, 0.315f, 0.86f, 0.5f, 0.86f, 0.5f, 0.53f, 0.685f)
private val PAUSE_LEFT = floatArrayOf(0.21f, 0.14f, 0.39f, 0.14f, 0.39f, 0.86f, 0.21f, 0.86f)
private val PAUSE_RIGHT = floatArrayOf(0.61f, 0.14f, 0.79f, 0.14f, 0.79f, 0.86f, 0.61f, 0.86f)

private fun DrawScope.drawPlayPause(p: Float, color: Color) {
    val w = size.minDimension
    // A slight squeeze through the middle of the fold.
    val squeeze = 1f - 0.07f * sin(PI.toFloat() * p.coerceIn(0f, 1f))
    val stroke = Stroke(width = w * 0.09f, join = StrokeJoin.Round)
    scale(squeeze, squeeze) {
        for ((from, to) in listOf(PLAY_LEFT to PAUSE_LEFT, PLAY_RIGHT to PAUSE_RIGHT)) {
            val path = Path()
            for (i in 0 until 4) {
                val x = (from[2 * i] + (to[2 * i] - from[2 * i]) * p) * w
                val y = (from[2 * i + 1] + (to[2 * i + 1] - from[2 * i + 1]) * p) * w
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()
            // Filled, then stroked with round joins in the same colour, so the corners are soft.
            drawPath(path, color, style = Fill)
            drawPath(path, color, style = stroke)
        }
    }
}

/**
 * Two arrowheads, like ⏩. Each tap rolls them a step on: the front one is
 * pushed on and shrinks away, the back one moves up to take its place, and
 * a new one grows in behind, while the whole glyph nudges in the direction
 * of travel.
 * [rolls] counts taps; every change plays one roll.
 */
@Composable
fun SkipGlyph(forward: Boolean, rolls: Int, color: Color, animate: Boolean, modifier: Modifier = Modifier) {
    // At rest a full roll and none look the same, so resting at 1 lets every tap start from 0.
    val t = remember { Animatable(1f) }
    LaunchedEffect(rolls) {
        if (rolls == 0 || !animate) return@LaunchedEffect
        t.snapTo(0f)
        t.animateTo(1f, tween(420, easing = FastOutSlowInEasing))
    }
    Canvas(modifier) {
        val w = size.minDimension
        val nudge = sin(PI.toFloat() * t.value) * w * 0.06f
        scale(if (forward) 1f else -1f, 1f) {
            translate(left = nudge) { drawArrows(t.value, color) }
        }
    }
}

private fun DrawScope.drawArrows(t: Float, color: Color) {
    val w = size.minDimension
    val head = 0.42f * w
    val height = 0.6f * w
    val cy = size.height / 2f
    val start = (size.width - 2 * head) / 2f
    val shift = t * head
    val stroke = Stroke(width = w * 0.06f, join = StrokeJoin.Round)
    fun arrow(left: Float, scale: Float, alpha: Float) {
        if (scale <= 0.01f || alpha <= 0.01f) return
        val hw = head * scale
        val hh = height * scale
        val path = Path().apply {
            moveTo(left, cy - hh / 2f)
            lineTo(left + hw, cy)
            lineTo(left, cy + hh / 2f)
            close()
        }
        // Fill and the soft-cornered stroke go down opaque in a layer that's
        // faded as one, so a half-faded arrow doesn't show its outline.
        val bounds = androidx.compose.ui.geometry.Rect(left - w * 0.1f, cy - hh, left + hw + w * 0.1f, cy + hh)
        drawContext.canvas.saveLayer(bounds, androidx.compose.ui.graphics.Paint().apply { this.alpha = alpha })
        drawPath(path, color, style = Fill)
        drawPath(path, color, style = stroke)
        drawContext.canvas.restore()
    }
    // The new one grows in at the back.
    arrow(start, t, t)
    // The back one moves up to the front.
    arrow(start + shift, 1f, 1f)
    // The front one is pushed on ahead of it, shrinking as it goes.
    arrow(start + head + shift, 1f - t, 1f - t)
}
