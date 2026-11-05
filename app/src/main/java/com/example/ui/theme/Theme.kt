package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.data.model.AppTheme
import com.example.util.ThemePreferences

data class AppColors(
    val isDark: Boolean,
    val background: Color,
    val surface: Color,
    val glassCard: Color,
    val glassCardElevated: Color,
    val glassBorder: Color,
    val glassHighlight: Color,
    val shadow: Color,
    val inset: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val emerald: Color,
    val emeraldSoft: Color,
    val blue: Color,
    val blueSoft: Color,
    val amber: Color,
    val amberSoft: Color,
    val crimson: Color,
    val crimsonSoft: Color,
    val primaryAccent: Color = emerald,
    val primaryAccentBright: Color = emerald,
    val currentTheme: AppTheme = AppTheme.DEFAULT
)

val LocalAppColors = staticCompositionLocalOf {
    getAppColors(AppTheme.DEFAULT, isDark = true)
}

fun getAppColors(theme: AppTheme, isDark: Boolean): AppColors {
    if (!isDark) {
        return AppColors(
            isDark = false,
            background = LightBackground,
            surface = LightSurface,
            glassCard = LightCard,
            glassCardElevated = LightCardElevated,
            glassBorder = theme.primaryColor.copy(alpha = 0.15f),
            glassHighlight = LightHighlight,
            shadow = LightShadow,
            inset = LightInset,
            textPrimary = LightTextPrimary,
            textSecondary = LightTextSecondary,
            textTertiary = LightTextTertiary,
            emerald = theme.primaryColor,
            emeraldSoft = theme.primaryColor.copy(alpha = 0.18f),
            blue = SpiritualCyan,
            blueSoft = SpiritualCyanSoft,
            amber = SpiritualGold,
            amberSoft = SpiritualGoldSoft,
            crimson = AppleCrimson,
            crimsonSoft = AppleCrimsonSoft,
            primaryAccent = theme.primaryColor,
            primaryAccentBright = theme.accentColor,
            currentTheme = theme
        )
    }

    return when (theme) {
        AppTheme.EMERALD_NIGHT -> AppColors(
            isDark = true,
            background = EmeraldNightBackground,
            surface = EmeraldNightSurface,
            glassCard = EmeraldNightCard,
            glassCardElevated = EmeraldNightCardElevated,
            glassBorder = EmeraldNightBorder,
            glassHighlight = EmeraldNightHighlight,
            shadow = Color(0xCC000000),
            inset = Color(0xFF040A07),
            textPrimary = theme.textPrimaryColor,
            textSecondary = theme.textSecondaryColor,
            textTertiary = theme.textTertiaryColor,
            emerald = EmeraldNightPrimary,
            emeraldSoft = EmeraldNightPrimary.copy(alpha = 0.18f),
            blue = SpiritualCyan,
            blueSoft = SpiritualCyanSoft,
            amber = SpiritualGold,
            amberSoft = SpiritualGoldSoft,
            crimson = AppleCrimson,
            crimsonSoft = AppleCrimsonSoft,
            primaryAccent = EmeraldNightPrimary,
            primaryAccentBright = EmeraldNightAccent,
            currentTheme = theme
        )
        AppTheme.ROYAL_NAVY -> AppColors(
            isDark = true,
            background = RoyalNavyBackground,
            surface = RoyalNavySurface,
            glassCard = RoyalNavyCard,
            glassCardElevated = RoyalNavyCardElevated,
            glassBorder = RoyalNavyBorder,
            glassHighlight = RoyalNavyHighlight,
            shadow = Color(0xCC020408),
            inset = Color(0xFF04070E),
            textPrimary = theme.textPrimaryColor,
            textSecondary = theme.textSecondaryColor,
            textTertiary = theme.textTertiaryColor,
            emerald = RoyalNavyPrimary,
            emeraldSoft = RoyalNavyPrimary.copy(alpha = 0.18f),
            blue = RoyalNavyAccent,
            blueSoft = RoyalNavyAccent.copy(alpha = 0.18f),
            amber = SpiritualGold,
            amberSoft = SpiritualGoldSoft,
            crimson = AppleCrimson,
            crimsonSoft = AppleCrimsonSoft,
            primaryAccent = RoyalNavyPrimary,
            primaryAccentBright = RoyalNavyAccent,
            currentTheme = theme
        )
        AppTheme.WARM_SEPIA -> AppColors(
            isDark = true,
            background = WarmSepiaBackground,
            surface = WarmSepiaSurface,
            glassCard = WarmSepiaCard,
            glassCardElevated = WarmSepiaCardElevated,
            glassBorder = WarmSepiaBorder,
            glassHighlight = WarmSepiaHighlight,
            shadow = Color(0xCC0A0806),
            inset = Color(0xFF100E0B),
            textPrimary = theme.textPrimaryColor,
            textSecondary = theme.textSecondaryColor,
            textTertiary = theme.textTertiaryColor,
            emerald = WarmSepiaPrimary,
            emeraldSoft = WarmSepiaPrimary.copy(alpha = 0.18f),
            blue = Color(0xFFD97706),
            blueSoft = Color(0x26D97706),
            amber = WarmSepiaPrimary,
            amberSoft = WarmSepiaPrimary.copy(alpha = 0.18f),
            crimson = AppleCrimson,
            crimsonSoft = AppleCrimsonSoft,
            primaryAccent = WarmSepiaPrimary,
            primaryAccentBright = WarmSepiaAccent,
            currentTheme = theme
        )
        AppTheme.KAABA_GOLD -> AppColors(
            isDark = true,
            background = KaabaGoldBackground,
            surface = KaabaGoldSurface,
            glassCard = KaabaGoldCard,
            glassCardElevated = KaabaGoldCardElevated,
            glassBorder = KaabaGoldBorder,
            glassHighlight = KaabaGoldHighlight,
            shadow = Color(0xCC000000),
            inset = Color(0xFF050505),
            textPrimary = theme.textPrimaryColor,
            textSecondary = theme.textSecondaryColor,
            textTertiary = theme.textTertiaryColor,
            emerald = KaabaGoldPrimary,
            emeraldSoft = KaabaGoldPrimary.copy(alpha = 0.18f),
            blue = Color(0xFFCA8A04),
            blueSoft = Color(0x26CA8A04),
            amber = KaabaGoldPrimary,
            amberSoft = KaabaGoldPrimary.copy(alpha = 0.18f),
            crimson = AppleCrimson,
            crimsonSoft = AppleCrimsonSoft,
            primaryAccent = KaabaGoldPrimary,
            primaryAccentBright = KaabaGoldAccent,
            currentTheme = theme
        )
        AppTheme.MONOCHROME -> AppColors(
            isDark = true,
            background = MonochromeBackground,
            surface = MonochromeSurface,
            glassCard = MonochromeCard,
            glassCardElevated = MonochromeCardElevated,
            glassBorder = MonochromeBorder,
            glassHighlight = MonochromeHighlight,
            shadow = Color(0xCC000000),
            inset = Color(0xFF080808),
            textPrimary = theme.textPrimaryColor,
            textSecondary = theme.textSecondaryColor,
            textTertiary = theme.textTertiaryColor,
            emerald = MonochromePrimary,
            emeraldSoft = MonochromePrimary.copy(alpha = 0.18f),
            blue = MonochromeAccent,
            blueSoft = MonochromeAccent.copy(alpha = 0.18f),
            amber = Color(0xFFD4D4D8),
            amberSoft = Color(0x26D4D4D8),
            crimson = AppleCrimson,
            crimsonSoft = AppleCrimsonSoft,
            primaryAccent = MonochromePrimary,
            primaryAccentBright = MonochromeAccent,
            currentTheme = theme
        )
    }
}

fun getThemeColorScheme(theme: AppTheme, isDark: Boolean): ColorScheme {
    if (!isDark) {
        return lightColorScheme(
            primary = theme.primaryColor,
            onPrimary = Color.White,
            primaryContainer = theme.primaryColor.copy(alpha = 0.18f),
            onPrimaryContainer = theme.primaryColor,
            secondary = theme.accentColor,
            onSecondary = Color.White,
            secondaryContainer = theme.accentColor.copy(alpha = 0.18f),
            onSecondaryContainer = theme.accentColor,
            tertiary = SpiritualGold,
            onTertiary = Color.White,
            background = LightBackground,
            onBackground = LightTextPrimary,
            surface = LightSurface,
            onSurface = LightTextPrimary,
            surfaceVariant = LightCard,
            onSurfaceVariant = LightTextSecondary,
            outline = LightBorder,
            error = AppleCrimson,
            onError = Color.White
        )
    }

    return darkColorScheme(
        primary = theme.primaryColor,
        onPrimary = if (theme == AppTheme.MONOCHROME) Color.Black else Color(0xFF041008),
        primaryContainer = theme.primaryColor.copy(alpha = 0.22f),
        onPrimaryContainer = theme.accentColor,
        secondary = theme.accentColor,
        onSecondary = Color(0xFF041008),
        secondaryContainer = theme.accentColor.copy(alpha = 0.22f),
        onSecondaryContainer = theme.accentColor,
        tertiary = SpiritualGold,
        onTertiary = Color(0xFF041008),
        background = theme.backgroundColor,
        onBackground = theme.textPrimaryColor,
        surface = theme.surfaceColor,
        onSurface = theme.textPrimaryColor,
        surfaceVariant = theme.cardColor,
        onSurfaceVariant = theme.textSecondaryColor,
        outline = theme.borderColor,
        error = AppleCrimson,
        onError = Color.White
    )
}

@Composable
fun AlMuhasibTheme(
    appTheme: AppTheme? = null,
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val currentTheme = appTheme ?: remember(context) { ThemePreferences.getTheme(context) }
    val colorScheme = getThemeColorScheme(currentTheme, darkTheme)
    val customColors = getAppColors(currentTheme, darkTheme)

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val isAod = view.context.javaClass.simpleName == "AodActivity" ||
                        view.context::class.java.name.contains("AodActivity")
                if (isAod) {
                    window.statusBarColor = android.graphics.Color.TRANSPARENT
                    window.navigationBarColor = android.graphics.Color.TRANSPARENT
                    val controller = WindowCompat.getInsetsController(window, view)
                    controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    controller.hide(WindowInsetsCompat.Type.systemBars())
                } else {
                    window.statusBarColor = customColors.background.toArgb()
                    window.navigationBarColor = customColors.background.toArgb()
                    val controller = WindowCompat.getInsetsController(window, view)
                    controller.isAppearanceLightStatusBars = !darkTheme
                    controller.isAppearanceLightNavigationBars = !darkTheme
                }
            }
        }
    }

    // Wrap with RTL LayoutDirection for authentic Arabic experience
    CompositionLocalProvider(
        LocalAppColors provides customColors,
        LocalLayoutDirection provides LayoutDirection.Rtl
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

