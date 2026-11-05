package com.example.util

import android.content.Context
import com.example.data.model.AppTheme

/**
 * SharedPreferences persistence for user-selected AppTheme across app restarts and activities.
 */
object ThemePreferences {
    private const val PREFS_NAME = "al_muhasib_theme_prefs"
    private const val KEY_THEME_ID = "selected_app_theme_id"

    fun getTheme(context: Context): AppTheme {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val themeId = prefs.getString(KEY_THEME_ID, AppTheme.DEFAULT.id)
        return AppTheme.fromId(themeId)
    }

    fun setTheme(context: Context, theme: AppTheme) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_THEME_ID, theme.id).apply()
    }
}
