package com.example.quran.tajweed

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

object TajweedSettings {
    private const val PREFS_NAME = "al_muhasib_tajweed_prefs"
    private const val KEY_TAJWEED_ENABLED = "key_tajweed_enabled"
    private const val KEY_TAJWEED_THEME = "key_tajweed_theme"
    private const val KEY_COLOR_PREFIX = "key_tajweed_color_"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun isTajweedEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_TAJWEED_ENABLED, true)
    }

    fun setTajweedEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_TAJWEED_ENABLED, enabled).apply()
    }

    fun getTajweedTheme(context: Context): TajweedTheme {
        val name = getPrefs(context).getString(KEY_TAJWEED_THEME, TajweedTheme.STANDARD.name)
        return try {
            TajweedTheme.valueOf(name ?: TajweedTheme.STANDARD.name)
        } catch (_: Exception) {
            TajweedTheme.STANDARD
        }
    }

    fun setTajweedTheme(context: Context, theme: TajweedTheme) {
        getPrefs(context).edit().putString(KEY_TAJWEED_THEME, theme.name).apply()
    }

    fun getCustomColor(context: Context, ruleId: String): Color? {
        val key = KEY_COLOR_PREFIX + ruleId
        val argb = getPrefs(context).getInt(key, -1)
        return if (argb != -1) Color(argb) else null
    }

    fun setCustomColor(context: Context, ruleId: String, color: Color) {
        val key = KEY_COLOR_PREFIX + ruleId
        getPrefs(context).edit().putInt(key, color.toArgb()).apply()
    }

    fun resetCustomColors(context: Context) {
        val editor = getPrefs(context).edit()
        TajweedRule.entries.forEach { rule ->
            editor.remove(KEY_COLOR_PREFIX + rule.id)
        }
        editor.apply()
    }

    fun getAllCustomColors(context: Context): Map<String, Color> {
        val map = mutableMapOf<String, Color>()
        TajweedRule.entries.forEach { rule ->
            getCustomColor(context, rule.id)?.let { color ->
                map[rule.id] = color
            }
        }
        return map
    }
}
