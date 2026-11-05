package com.example.aod

import android.content.Context
import android.content.Intent
import android.os.Build

object AodManager {

    const val MODE_ALWAYS_ON = 0
    const val MODE_TAP_10S = 1
    const val MODE_SCHEDULED = 2

    const val HAPTIC_MECHANICAL = 0
    const val HAPTIC_TICK = 1
    const val HAPTIC_SILENT = 2

    // AOD Clock Styles
    const val CLOCK_DIGITAL_MINIMAL = 0
    const val CLOCK_ARABIC_INDIC = 1
    const val CLOCK_ANALOG = 2
    const val CLOCK_STACKED = 3
    const val CLOCK_GEOMETRIC = 4

    // AOD Accent Color Palettes
    const val ACCENT_EMERALD = 0
    const val ACCENT_GOLD = 1
    const val ACCENT_CYAN = 2
    const val ACCENT_WHITE = 3
    const val ACCENT_ROSE = 4
    const val ACCENT_PURPLE = 5

    // AOD Battery Indicator Styles
    const val BATTERY_PERCENTAGE = 0
    const val BATTERY_BAR = 1
    const val BATTERY_RING = 2
    const val BATTERY_HIDDEN = 3

    private const val PREFS_NAME = "al_muhasib_aod_prefs"
    private const val KEY_AOD_ENABLED = "aod_hardware_lock_enabled"
    private const val KEY_BURN_IN_PROTECTION = "aod_burn_in_protection"
    private const val KEY_BRIGHTNESS = "aod_screen_brightness"
    private const val KEY_AUTO_BRIGHTNESS = "aod_auto_brightness_enabled"
    private const val KEY_DISPLAY_MODE = "aod_display_mode"
    private const val KEY_BATTERY_SAVER_DISABLE = "aod_battery_saver_disable_enabled"
    private const val KEY_MISBAHA_HAPTIC = "aod_misbaha_haptic_profile"
    private const val KEY_POCKET_MODE = "aod_pocket_mode_enabled"
    private const val KEY_CLOCK_STYLE = "aod_clock_style"
    private const val KEY_ACCENT_COLOR = "aod_accent_color"
    private const val KEY_BATTERY_STYLE = "aod_battery_style"
    private const val KEY_NOTIFICATION_PRIVACY = "aod_notification_privacy"

    fun isAodEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_AOD_ENABLED, true)
    }

    fun setAodEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_AOD_ENABLED, enabled).apply()

        if (enabled) {
            startAodLockService(context)
        } else {
            stopAodLockService(context)
        }
    }

    fun isBurnInProtectionEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_BURN_IN_PROTECTION, true)
    }

    fun setBurnInProtectionEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_BURN_IN_PROTECTION, enabled).apply()
    }

    fun getScreenBrightness(context: Context): Float {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getFloat(KEY_BRIGHTNESS, 0.06f)
    }

    fun setScreenBrightness(context: Context, brightness: Float) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putFloat(KEY_BRIGHTNESS, brightness.coerceIn(0.02f, 0.25f)).apply()
    }

    // ponytail: auto-brightness with hardware ambient light sensor
    fun isAutoBrightnessEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_AUTO_BRIGHTNESS, true)
    }

    fun setAutoBrightnessEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_AUTO_BRIGHTNESS, enabled).apply()
    }

    // ponytail: AOD display mode: ALWAYS_ON, TAP_10S, SCHEDULED
    fun getAodDisplayMode(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_DISPLAY_MODE, MODE_ALWAYS_ON)
    }

    fun setAodDisplayMode(context: Context, mode: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_DISPLAY_MODE, mode).apply()
    }

    // ponytail: battery saver cut-off toggle (default false so MIUI battery optimization doesn't block AOD)
    fun isBatterySaverDisableEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_BATTERY_SAVER_DISABLE, false)
    }

    fun setBatterySaverDisableEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_BATTERY_SAVER_DISABLE, enabled).apply()
    }

    // ponytail: pocket mode sensor guard toggle
    fun isPocketModeEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_POCKET_MODE, true)
    }

    fun setPocketModeEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_POCKET_MODE, enabled).apply()
    }

    // ponytail: Misbaha digital haptic profiles: MECHANICAL, TICK, SILENT
    fun getMisbahaHapticProfile(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_MISBAHA_HAPTIC, HAPTIC_MECHANICAL)
    }

    fun setMisbahaHapticProfile(context: Context, profile: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_MISBAHA_HAPTIC, profile).apply()
    }

    fun startAodLockService(context: Context) {
        val intent = Intent(context, AodLockService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    fun stopAodLockService(context: Context) {
        val intent = Intent(context, AodLockService::class.java)
        context.stopService(intent)
    }

    private const val KEY_FLOATING_DHIKR_ENABLED = "floating_dhikr_hud_enabled"

    fun isFloatingDhikrEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_FLOATING_DHIKR_ENABLED, false)
    }

    fun setFloatingDhikrEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_FLOATING_DHIKR_ENABLED, enabled).apply()

        if (enabled) {
            startFloatingDhikrService(context)
        } else {
            stopFloatingDhikrService(context)
        }
    }

    fun startFloatingDhikrService(context: Context) {
        val intent = Intent(context, com.example.floating.FloatingDhikrService::class.java)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } catch (e: Exception) {
            // Ignore if overlay permission missing
        }
    }

    fun stopFloatingDhikrService(context: Context) {
        val intent = Intent(context, com.example.floating.FloatingDhikrService::class.java)
        try {
            context.stopService(intent)
        } catch (e: Exception) {
            // Ignore
        }
    }

    // AOD Clock Style
    fun getClockStyle(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_CLOCK_STYLE, CLOCK_DIGITAL_MINIMAL)
    }

    fun setClockStyle(context: Context, style: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_CLOCK_STYLE, style).apply()
    }

    // AOD Accent Color Palette
    fun getAccentColor(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_ACCENT_COLOR, ACCENT_EMERALD)
    }

    fun setAccentColor(context: Context, color: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_ACCENT_COLOR, color).apply()
    }

    // AOD Battery Indicator Style
    fun getBatteryStyle(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_BATTERY_STYLE, BATTERY_PERCENTAGE)
    }

    fun setBatteryStyle(context: Context, style: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_BATTERY_STYLE, style).apply()
    }

    // AOD Notification Privacy Mode (Glowing Dots vs Full Badges)
    fun isNotificationPrivacyEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_NOTIFICATION_PRIVACY, false)
    }

    fun setNotificationPrivacyEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_NOTIFICATION_PRIVACY, enabled).apply()
    }
}
