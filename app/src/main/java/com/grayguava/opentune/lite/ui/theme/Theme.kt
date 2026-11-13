package com.grayguava.opentune.lite.ui.theme

import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.materialkolor.PaletteStyle
import com.materialkolor.rememberDynamicColorScheme
import com.grayguava.opentune.lite.data.settings.SEED_COLOR
import com.grayguava.opentune.lite.data.settings.ThemeSettings

/**
 * The app theme. The scheme comes from, in order: the playing track's artwork
 * (when that option is on and a color has been read from it), Material You
 * wallpaper colors (Android 12+), or the chosen accent. Color changes animate,
 * so a new track's colors ease in rather than snap.
 */
/** The colour read from the playing song's cover, or null with nothing playing; set whatever the theme follows. */
val LocalArtworkSeed = compositionLocalOf<Color?> { null }

/**
 * The playing song's own accent pair (primary, tertiary) from a scheme seeded
 * by its cover, for small touches that follow the music even when the app's
 * theme doesn't; the theme's own when nothing is playing. Eases between songs.
 */
@Composable
fun songAccents(): Pair<Color, Color> {
    val seed = LocalArtworkSeed.current
    val theme = MaterialTheme.colorScheme
    if (seed == null) return theme.primary to theme.tertiary
    val scheme = rememberDynamicColorScheme(
        seedColor = seed,
        isDark = theme.background.red + theme.background.green + theme.background.blue < 1.5f,
        isAmoled = false,
        style = PaletteStyle.Vibrant,
    )
    val primary by animateColorAsState(scheme.primary, tween(600), label = "songPrimary")
    val tertiary by animateColorAsState(scheme.tertiary, tween(600), label = "songTertiary")
    return primary to tertiary
}

@Composable
fun OpenTuneTheme(
    settings: ThemeSettings,
    artworkSeed: Color? = null,
    content: @Composable () -> Unit,
) {
    val seed = artworkSeed?.takeIf { settings.colorFromArtwork }
    val useWallpaper = seed == null && settings.dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    // The app's own colours are monochrome; the playing cover keeps its own.
    val generated = rememberDynamicColorScheme(
        seedColor = seed ?: Color(SEED_COLOR),
        isDark = true,
        isAmoled = settings.pureBlack,
        style = if (seed != null) PaletteStyle.TonalSpot else PaletteStyle.Monochrome,
    )
    val base = if (useWallpaper) {
        val context = LocalContext.current
        dynamicDarkColorScheme(context)
    } else {
        generated
    }
    val scheme = if (settings.pureBlack) base.toPureBlack() else base

    MaterialTheme(
        colorScheme = scheme.animated(),
        typography = AppTypography,
        shapes = AppShapes,
    ) {
        CompositionLocalProvider(
            LocalArtworkSeed provides artworkSeed,
            com.grayguava.opentune.lite.ui.components.LocalFocusRingColor provides MaterialTheme.colorScheme.primary,
        ) {
            content()
        }
    }
}

/**
 * A dark scheme seeded from artwork, for the full player: it always reads as
 * a dark, immersive surface whatever the app theme is.
 */
@Composable
fun PlayerTheme(seed: Color?, settings: ThemeSettings, neutral: Boolean = false, active: Boolean = true, content: @Composable () -> Unit) {
    val scheme = rememberDynamicColorScheme(
        seedColor = seed ?: Color(SEED_COLOR),
        isDark = true,
        isAmoled = settings.pureBlack,
        style = PaletteStyle.Vibrant,
    )
    val amoled = settings.playerBackground == com.grayguava.opentune.lite.data.settings.PlayerBackground.AMOLED
    // Keep artwork-derived background tones even when the layout uses white controls.
    val playerScheme = if (neutral) scheme.copy(
        primary = Color.White, onPrimary = Color.Black, secondary = Color.White, tertiary = Color.White,
        background = Color(0xFF202023), surface = Color(0xFF202023), onSurface = Color.White,
        onBackground = Color.White, onSurfaceVariant = Color(0xFFCACACE),
        surfaceContainer = Color(0xFF2B2B2E), surfaceContainerHigh = Color(0xFF36363A),
    ) else scheme
    MaterialTheme(
        colorScheme = if (!active) MaterialTheme.colorScheme else if (amoled) playerScheme.toPureBlack() else playerScheme.animated(),
        typography = AppTypography,
        shapes = AppShapes,
    ) {
        content()
    }
}

/**
 * Black backgrounds with neutral gray cards, so color comes only from the
 * accent and the artwork, never from tinted surfaces.
 */
private fun ColorScheme.toPureBlack(): ColorScheme = copy(
    background = Color.Black,
    surface = Color.Black,
    surfaceVariant = Color(0xFF242426),
    surfaceContainerLowest = Color.Black,
    surfaceContainerLow = Color(0xFF0E0E0F),
    surfaceContainer = Color(0xFF1B1B1D),
    surfaceContainerHigh = Color(0xFF232325),
    surfaceContainerHighest = Color(0xFF2D2D30),
    onSurface = Color(0xFFF2F2F3),
    onBackground = Color(0xFFF2F2F3),
    onSurfaceVariant = Color(0xFF9A9AA0),
    outline = Color(0xFF4A4A4F),
    outlineVariant = Color(0xFF2E2E32),
)

/** The scheme with every colour easing to its new value, so a change of seed fades rather than snaps. */
@Composable
internal fun ColorScheme.animated(): ColorScheme {
    val spec = tween<Color>(durationMillis = 600)
    @Composable
    fun Color.a() = animateColorAsState(this, spec, label = "scheme").value
    return copy(
        primary = primary.a(),
        onPrimary = onPrimary.a(),
        primaryContainer = primaryContainer.a(),
        onPrimaryContainer = onPrimaryContainer.a(),
        secondary = secondary.a(),
        onSecondary = onSecondary.a(),
        secondaryContainer = secondaryContainer.a(),
        onSecondaryContainer = onSecondaryContainer.a(),
        tertiary = tertiary.a(),
        tertiaryContainer = tertiaryContainer.a(),
        onTertiaryContainer = onTertiaryContainer.a(),
        background = background.a(),
        onBackground = onBackground.a(),
        surface = surface.a(),
        onSurface = onSurface.a(),
        surfaceVariant = surfaceVariant.a(),
        onSurfaceVariant = onSurfaceVariant.a(),
        surfaceContainerLowest = surfaceContainerLowest.a(),
        surfaceContainerLow = surfaceContainerLow.a(),
        surfaceContainer = surfaceContainer.a(),
        surfaceContainerHigh = surfaceContainerHigh.a(),
        surfaceContainerHighest = surfaceContainerHighest.a(),
        outline = outline.a(),
        outlineVariant = outlineVariant.a(),
    )
}

private val baseType = Typography()

private fun TextStyle.scaled(weight: FontWeight? = null, spacing: Float? = null) =
    copy(fontWeight = weight ?: fontWeight, letterSpacing = spacing?.sp ?: letterSpacing)

val AppTypography = Typography(
    displayLarge = baseType.displayLarge.scaled(FontWeight.Bold, -1.2f),
    displayMedium = baseType.displayMedium.scaled(FontWeight.Bold, -1f),
    displaySmall = baseType.displaySmall.scaled(FontWeight.Bold, -0.6f),
    headlineLarge = baseType.headlineLarge.scaled(FontWeight.Bold, -0.4f),
    headlineMedium = baseType.headlineMedium.scaled(FontWeight.SemiBold, -0.3f),
    headlineSmall = baseType.headlineSmall.scaled(FontWeight.SemiBold, -0.2f),
    titleLarge = baseType.titleLarge.scaled(FontWeight.SemiBold, -0.1f),
    titleMedium = baseType.titleMedium.scaled(FontWeight.Medium),
    titleSmall = baseType.titleSmall.scaled(FontWeight.Medium),
    bodyLarge = baseType.bodyLarge.scaled(),
    bodyMedium = baseType.bodyMedium.scaled(),
    bodySmall = baseType.bodySmall.scaled(),
    labelLarge = baseType.labelLarge.scaled(FontWeight.Medium),
    labelMedium = baseType.labelMedium.scaled(FontWeight.Medium),
    labelSmall = baseType.labelSmall.scaled(FontWeight.Medium),
)

/** Large, heavy lyric lines, read at a glance from arm's length. */
val LyricsTextStyle = TextStyle(
    fontSize = 34.sp,
    lineHeight = 41.sp,
    fontWeight = FontWeight.ExtraBold,
    letterSpacing = (-0.6).sp,
)

val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

@Composable
fun rememberSeedSwatches(colors: List<Int>): List<Color> = remember(colors) { colors.map { Color(it) } }
