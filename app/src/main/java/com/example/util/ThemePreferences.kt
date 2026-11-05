package com.example.util

import android.content.Context
import com.example.data.model.AppTheme

/**
 * SharedPreferences persistence for user-selected AppTheme across app restarts and activities.
 */
object ThemePreferences {
    private const val PREFS_NAME = "al_muhasib_theme_prefs"
    private const val KEY_THEME_ID = "selected_app_theme_id"
    private const val KEY_SOLAR_AUTO_THEME = "solar_auto_theme_enabled"

    fun getTheme(context: Context): AppTheme {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val themeId = prefs.getString(KEY_THEME_ID, AppTheme.DEFAULT.id)
        return AppTheme.fromId(themeId)
    }

    fun setTheme(context: Context, theme: AppTheme) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_THEME_ID, theme.id).apply()
    }

    fun isSolarAutoThemeEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_SOLAR_AUTO_THEME, false)
    }

    fun setSolarAutoThemeEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_SOLAR_AUTO_THEME, enabled).apply()
    }

    fun resolveEffectiveTheme(context: Context): AppTheme {
        if (isSolarAutoThemeEnabled(context)) {
            val now = java.time.LocalTime.now()
            val schedule = IslamicCalendarUtil.getDailyPrayerSchedule()
            val sunrise = schedule.getOrNull(1)?.time ?: java.time.LocalTime.of(6, 0)
            val maghrib = schedule.getOrNull(4)?.time ?: java.time.LocalTime.of(18, 30)
            val isDaylight = now.isAfter(sunrise) && now.isBefore(maghrib)
            return if (isDaylight) AppTheme.MIHRAB_PARCHMENT_LIGHT else AppTheme.MIHRAB_AMOLED_DARK
        }
        return getTheme(context)
    }
}
