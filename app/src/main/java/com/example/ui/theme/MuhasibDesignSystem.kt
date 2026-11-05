package com.example.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp

/**
 * أوضاع الإضاءة في نظام الصالح:
 * - AMOLED_DARK: أسود نقي #000000 لتوفير طاقة الشاشة وحماية العين ليلاً
 * - PARCHMENT_LIGHT: عاجي دافئ #FAF6EE كلون ورق المصحف للقراءة النهارية
 * - SOLAR_AUTO: يتبدل تلقائياً مع الشروق والمغرب عبر SolarCalculator
 */
enum class MuhasibThemeMode {
    AMOLED_DARK,
    PARCHMENT_LIGHT,
    SOLAR_AUTO
}

/**
 * الباليتات الروحانية الست المتوافقة مع AodManager.kt
 */
enum class SacredAccentPalette(
    val titleArabic: String,
    val darkPrimary: Color,
    val darkGlow: Color,
    val lightPrimary: Color,
    val lightGlow: Color
) {
    EMERALD(
        titleArabic = "زُمُرُّد الرَّوضَة",
        darkPrimary = Color(0xFF10B981),
        darkGlow = Color(0x2610B981),
        lightPrimary = Color(0xFF086646),
        lightGlow = Color(0x1F086646)
    ),
    GOLD(
        titleArabic = "ذَهَب المِيزَاب",
        darkPrimary = Color(0xFFD4AF37),
        darkGlow = Color(0x26D4AF37),
        lightPrimary = Color(0xFF9A7418),
        lightGlow = Color(0x1F9A7418)
    ),
    CYAN(
        titleArabic = "نُور الفَجْر",
        darkPrimary = Color(0xFF38BDF8),
        darkGlow = Color(0x2638BDF8),
        lightPrimary = Color(0xFF0369A1),
        lightGlow = Color(0x1F0369A1)
    ),
    WHITE(
        titleArabic = "صَفَاء النُّور",
        darkPrimary = Color(0xFFE2E8F0),
        darkGlow = Color(0x22E2E8F0),
        lightPrimary = Color(0xFF334155),
        lightGlow = Color(0x1A334155)
    ),
    ROSE(
        titleArabic = "وَرْد المَدِينَة",
        darkPrimary = Color(0xFFFB7185),
        darkGlow = Color(0x26FB7185),
        lightPrimary = Color(0xFFBE123C),
        lightGlow = Color(0x1FBE123C)
    ),
    PURPLE(
        titleArabic = "سَكِينَة السَّحَر",
        darkPrimary = Color(0xFFC084FC),
        darkGlow = Color(0x26C084FC),
        lightPrimary = Color(0xFF6B21A8),
        lightGlow = Color(0x1F6B21A8)
    );

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
    // ألوان الجبهات الأربع للتعافي وحفظ الصلاة
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
            canvas = Color(0xFF000000),              // True OLED Pitch Black (0mA current)
            cardSurface = Color(0xFF0A0C0B),         // Obsidian Glass Tier 1
            elevatedPill = Color(0xFF131715),        // Tactile Pill Surface Tier 2
            glassSpecularTop = Color(0x24FFFFFF),    // 14% top specular reflection
            glassBorderBottom = Color(0x06FFFFFF),   // 2.5% bottom edge shadow
            divider = Color(0xFF1A1F1C),
            primaryInk = Color(0xFFF5F2EB),          // Warm Alabaster White
            secondaryInk = Color(0xFF949C97),        // Balanced Sage Grey
            mutedInk = Color(0xFF555E59),            // Subtle Metadata Grey
            accent = palette.darkPrimary,
            accentSoftGlow = palette.darkGlow,
            salahGuardAccent = Color(0xFF10B981),    // Emerald
            purityPornAccent = Color(0xFF38BDF8),    // Celestial Sapphire
            clarityWeedAccent = Color(0xFFF59E0B),   // Sovereign Amber
            breathSmokingAccent = Color(0xFF14B8A6), // Pure Teal
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
            accent = palette.lightPrimary,
            accentSoftGlow = palette.lightGlow,
            salahGuardAccent = Color(0xFF086646),
            purityPornAccent = Color(0xFF0284C7),
            clarityWeedAccent = Color(0xFFB45309),
            breathSmokingAccent = Color(0xFF0F766E),
            dangerAlert = Color(0xFFDC2626)
        )
    }
}

/**
 * نظام الخطوط الهندسي مع تفعيل Tabular Numerals (tnum) لمنع اهتزاز العدادات
 */
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
        fontFamily = FontFamily.Serif, // مثالي لقراءة فقرات الكتب والاقتباسات
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

    CompositionLocalProvider(LocalMuhasibColors provides effectiveColors) {
        MaterialTheme(
            colorScheme = materialColorScheme,
            typography = MuhasibTypography,
            content = content
        )
    }
}
