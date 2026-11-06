package com.example.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * MuhasibDesignSystem.kt — Heavenly iOS 26 Liquid Glass & Apple HIG / Taste-Skill Design System.
 *
 * Combines:
 * - Low-saturation (<75%), warm celestial metals & alabaster tones:
 *   Champagne Gold (#E5C185), Pearl Alabaster (#F5F2EB), Dawn Silk Rose (#DFB2A9),
 *   Celestial Mist (#9BBCD6), Sage Pearl (#9CC5B5), and Amethyst Dusk (#C3A6E8).
 * - Multi-stop atmospheric sky & horizon canvases (Twilight Velvet, Golden Hour Cloud, Alabaster Sanctuary).
 * - Physics-based Apple spring specifications (dampingRatio = 0.76f, stiffness = MediumLow).
 * - Full backward compatibility with existing MuhasibColors and LocalMuhasibColors calls.
 */
enum class MuhasibThemeMode {
    AMOLED_DARK,
    PARCHMENT_LIGHT,
    SOLAR_AUTO
}

enum class SacredAccentPalette(
    val displayName: String,
    val primaryTint: Color,
    val secondaryTint: Color,
    val specularHighlight: Color,
    val ambientGlow: Color,
    val canvasTop: Color,
    val canvasMid: Color,
    val canvasBottom: Color,
    val titleArabic: String = displayName
) {
    EMERALD(
        displayName = "Celestial Sage & Champagne",
        primaryTint = Color(0xFFE5C185),       // Warm Champagne Gold
        secondaryTint = Color(0xFF9CC5B5),     // Soft Heavenly Sage Pearl
        specularHighlight = Color(0xFFFBF8F1), // Warm Alabaster Specular Rim
        ambientGlow = Color(0x40E5C185),
        canvasTop = Color(0xFF141B29),         // Velvet Twilight Sky
        canvasMid = Color(0xFF1C2230),         // Soft Horizon Indigo
        canvasBottom = Color(0xFF0A0D14),      // Deep Warm Obsidian
        titleArabic = "زُمُرُّد الرَّوضَة"
    ),
    GOLD(
        displayName = "Golden Hour Sanctuary",
        primaryTint = Color(0xFFE8C78E),       // Sunlit Champagne
        secondaryTint = Color(0xFFF5EFE6),     // Silk Alabaster
        specularHighlight = Color(0xFFFFFFFF),
        ambientGlow = Color(0x45E8C78E),
        canvasTop = Color(0xFF1F1B24),
        canvasMid = Color(0xFF2B2329),
        canvasBottom = Color(0xFF0D0B10),
        titleArabic = "ذَهَب المِيزَاب"
    ),
    CYAN(
        displayName = "Fajr Horizon Mist",
        primaryTint = Color(0xFF9BBCD6),       // Ethereal Fajr Sky Blue
        secondaryTint = Color(0xFFE5C185),     // Dawn Gold Horizon
        specularHighlight = Color(0xFFF0F7FC),
        ambientGlow = Color(0x409BBCD6),
        canvasTop = Color(0xFF0F1926),
        canvasMid = Color(0xFF182638),
        canvasBottom = Color(0xFF080D14),
        titleArabic = "نُور الفَجْر"
    ),
    WHITE(
        displayName = "Alabaster Archway",
        primaryTint = Color(0xFFF5F2EB),       // Pure Warm Pearl
        secondaryTint = Color(0xFFD6C3A5),     // Limestone Gold
        specularHighlight = Color(0xFFFFFFFF),
        ambientGlow = Color(0x38F5F2EB),
        canvasTop = Color(0xFF181A20),
        canvasMid = Color(0xFF22252E),
        canvasBottom = Color(0xFF0C0E12),
        titleArabic = "صَفَاء النُّور"
    ),
    ROSE(
        displayName = "Maghrib Silk Rose",
        primaryTint = Color(0xFFDFB2A9),       // Soft Maghrib Rose Gold
        secondaryTint = Color(0xFFE5C185),     // Warm Amber
        specularHighlight = Color(0xFFFDF6F4),
        ambientGlow = Color(0x40DFB2A9),
        canvasTop = Color(0xFF1F1621),
        canvasMid = Color(0xFF2B1E28),
        canvasBottom = Color(0xFF0C090F),
        titleArabic = "وَرْد المَدِينَة"
    ),
    PURPLE(
        displayName = "Isha Starlight Velvet",
        primaryTint = Color(0xFFC3A6E8),       // Soft Celestial Lavender
        secondaryTint = Color(0xFFE5C185),     // Starlight Gold
        specularHighlight = Color(0xFFF8F4FC),
        ambientGlow = Color(0x40C3A6E8),
        canvasTop = Color(0xFF131124),
        canvasMid = Color(0xFF1C1933),
        canvasBottom = Color(0xFF080710),
        titleArabic = "سَكِينَة السَّحَر"
    );

    val darkPrimary: Color get() = primaryTint
    val darkGlow: Color get() = ambientGlow
    val lightPrimary: Color get() = primaryTint
    val lightGlow: Color get() = ambientGlow

    fun atmosphericBackgroundBrush(): Brush = Brush.verticalGradient(
        colors = listOf(canvasTop, canvasMid, canvasBottom)
    )

    companion object {
        fun fromAodAccent(accent: Int): SacredAccentPalette = when (accent) {
            0 -> EMERALD
            1 -> GOLD
            2 -> CYAN
            3 -> WHITE
            4 -> ROSE
            5 -> PURPLE
            else -> EMERALD
        }
    }
}

val LocalSacredPalette = staticCompositionLocalOf { SacredAccentPalette.EMERALD }

object AppleLiquidGlassTokens {
    val CardCornerRadius: Dp = 28.dp
    val PillCornerRadius: Dp = 999.dp
    val SheetCornerRadius: Dp = 36.dp
    val AmbientShadowElevation: Dp = 24.dp

    val GlassSurfaceFill = Color(0x24FFFFFF)
    val GlassSurfaceElevatedFill = Color(0x30FFFFFF)
    val GlassWarmTintFill = Color(0x1AE5C185)
    val SpecularTopRim = Color(0x66FFFFFF)
    val SpecularBottomRim = Color(0x12FFFFFF)
    val DeepAmbientShadow = Color(0x9903060C)

    fun <T> fluidSpring() = spring<T>(
        dampingRatio = 0.76f,
        stiffness = Spring.StiffnessMediumLow
    )
}

typealias HeavenlyLiquidGlassTokens = AppleLiquidGlassTokens

@Immutable
data class MuhasibColors(
    val isDark: Boolean,
    val canvas: Color,
    val cardSurface: Color,
    val elevatedPill: Color,
    val glassSpecularTop: Color,
    val glassBorderBottom: Color,
    val divider: Color,
    val primaryInk: Color,
    val secondaryInk: Color,
    val mutedInk: Color,
    val accent: Color,
    val accentSoftGlow: Color,
    val salahGuardAccent: Color,
    val purityPornAccent: Color,
    val clarityWeedAccent: Color,
    val breathSmokingAccent: Color,
    val dangerAlert: Color
)

val LocalMuhasibColors = staticCompositionLocalOf<MuhasibColors> {
    buildMuhasibColors(isDark = true, palette = SacredAccentPalette.EMERALD)
}

fun buildMuhasibColors(
    isDark: Boolean,
    palette: SacredAccentPalette
): MuhasibColors {
    return if (isDark) {
        MuhasibColors(
            isDark = true,
            canvas = Color(0xFF0A0D14),              // Obsidian Twilight Velvet
            cardSurface = Color(0xFF121722),         // Liquid Glass Surface Tier 1
            elevatedPill = Color(0xFF1A2130),        // Elevated Pill Surface Tier 2
            glassSpecularTop = Color(0x38FFFFFF),    // 22% top specular rim
            glassBorderBottom = Color(0x10FFFFFF),   // subtle bottom edge refraction
            divider = Color(0x1FFFFFFF),
            primaryInk = Color(0xFFF7F4EC),          // Pure Warm Pearl Alabaster
            secondaryInk = Color(0xFFBAC3CE),        // Balanced Celestial Sage Grey
            mutedInk = Color(0xFF6B7788),            // Subtle Horizon Metadata Grey
            accent = palette.primaryTint,
            accentSoftGlow = palette.ambientGlow,
            salahGuardAccent = Color(0xFFE5C185),    // Champagne Gold
            purityPornAccent = Color(0xFF9BBCD6),    // Celestial Mist Blue
            clarityWeedAccent = Color(0xFFDFB2A9),   // Dawn Silk Rose
            breathSmokingAccent = Color(0xFF9CC5B5), // Sage Pearl
            dangerAlert = Color(0xFFEF4444)
        )
    } else {
        MuhasibColors(
            isDark = false,
            canvas = Color(0xFFFAF6EE),              // Warm Madinah Mushaf Parchment
            cardSurface = Color(0xFFF2ECE1),         // Limestone Elevated Card
            elevatedPill = Color(0xFFE7DFD0),        // Pressed / Secondary Sand Surface
            glassSpecularTop = Color(0xD9FFFFFF),    // Crisp 85% inner ivory bevel
            glassBorderBottom = Color(0xFFE0D7C6),   // Warm subtle stone border
            divider = Color(0xFFE4DCCB),
            primaryInk = Color(0xFF141311),          // Deep Calligraphic Sumi Ink
            secondaryInk = Color(0xFF5A554C),        // Warm Umber
            mutedInk = Color(0xFF8C8579),            // Soft Sandstone
            accent = palette.primaryTint,
            accentSoftGlow = palette.ambientGlow,
            salahGuardAccent = Color(0xFF9A7418),
            purityPornAccent = Color(0xFF0369A1),
            clarityWeedAccent = Color(0xFFB45309),
            breathSmokingAccent = Color(0xFF0F766E),
            dangerAlert = Color(0xFFDC2626)
        )
    }
}

val MuhasibTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Light,
        fontSize = 48.sp,
        lineHeight = 54.sp,
        letterSpacing = (-1.2).sp,
        fontFeatureSettings = "tnum"
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 34.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 19.sp,
        lineHeight = 28.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp,
        lineHeight = 31.sp,
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Proportional,
            trim = LineHeightStyle.Trim.None
        )
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 22.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
        fontFeatureSettings = "tnum"
    )
)

@Composable
fun MuhasibSalehTheme(
    themeMode: MuhasibThemeMode = MuhasibThemeMode.AMOLED_DARK,
    palette: SacredAccentPalette = SacredAccentPalette.EMERALD,
    isDaylightFromSolarCalc: Boolean = false,
    content: @Composable () -> Unit
) {
    val useDark = when (themeMode) {
        MuhasibThemeMode.AMOLED_DARK -> true
        MuhasibThemeMode.PARCHMENT_LIGHT -> false
        MuhasibThemeMode.SOLAR_AUTO -> !isDaylightFromSolarCalc
    }

    val targetColors = remember(useDark, palette) {
        buildMuhasibColors(isDark = useDark, palette = palette)
    }

    val animatedCanvas by animateColorAsState(targetColors.canvas, tween(350), label = "canvas")
    val animatedSurface by animateColorAsState(targetColors.cardSurface, tween(350), label = "surface")
    val animatedInk by animateColorAsState(targetColors.primaryInk, tween(350), label = "ink")

    val effectiveColors = targetColors.copy(
        canvas = animatedCanvas,
        cardSurface = animatedSurface,
        primaryInk = animatedInk
    )

    val materialColorScheme = if (useDark) {
        darkColorScheme(
            background = effectiveColors.canvas,
            surface = effectiveColors.cardSurface,
            primary = effectiveColors.accent,
            onBackground = effectiveColors.primaryInk,
            onSurface = effectiveColors.primaryInk
        )
    } else {
        lightColorScheme(
            background = effectiveColors.canvas,
            surface = effectiveColors.cardSurface,
            primary = effectiveColors.accent,
            onBackground = effectiveColors.primaryInk,
            onSurface = effectiveColors.primaryInk
        )
    }

    CompositionLocalProvider(
        LocalSacredPalette provides palette,
        LocalMuhasibColors provides effectiveColors
    ) {
        MaterialTheme(
            colorScheme = materialColorScheme,
            typography = MuhasibTypography,
            content = content
        )
    }
}
