package com.example.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings

// ponytail: intent shortcuts for Xiaomi MIUI / HyperOS background pop-ups, autostart, and battery optimization
object PermissionHelper {

    fun isXiaomiOrMiui(): Boolean {
        val manufacturer = Build.MANUFACTURER.orEmpty()
        val brand = Build.BRAND.orEmpty()
        return manufacturer.contains("Xiaomi", ignoreCase = true) ||
                manufacturer.contains("Redmi", ignoreCase = true) ||
                brand.contains("POCO", ignoreCase = true) ||
                brand.contains("Xiaomi", ignoreCase = true)
    }

    /**
     * Opens Xiaomi "Display pop-up windows in background" / app permissions screen
     */
    fun openXiaomiPopupPermissions(context: Context): Boolean {
        return try {
            val intent = Intent("miui.intent.action.APP_PERM_EDITOR").apply {
                setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity")
                putExtra("extra_pkgname", context.packageName)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            try {
                val altIntent = Intent("miui.intent.action.APP_PERM_EDITOR").apply {
                    setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.AppPermissionsEditorActivity")
                    putExtra("extra_pkgname", context.packageName)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(altIntent)
                true
            } catch (e2: Exception) {
                openAppSettings(context)
            }
        }
    }

    /**
     * Opens Xiaomi Autostart management screen
     */
    fun openXiaomiAutostart(context: Context): Boolean {
        return try {
            val intent = Intent().apply {
                component = ComponentName(
                    "com.miui.securitycenter",
                    "com.miui.permcenter.autostart.AutoStartManagementActivity"
                )
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            openAppSettings(context)
        }
    }

    /**
     * Opens system battery optimization settings
     */
    fun openBatteryOptimizationSettings(context: Context): Boolean {
        return try {
            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            openAppSettings(context)
        }
    }

    /**
     * Checks if Full Screen Intent is granted (Android 14+)
     */
    fun canUseFullScreenIntent(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val notificationManager = context.getSystemService(android.app.NotificationManager::class.java)
            notificationManager?.canUseFullScreenIntent() ?: true
        } else {
            true
        }
    }

    /**
     * Opens Full Screen Intent permission settings (Android 14+)
     */
    fun openFullScreenIntentSettings(context: Context): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT).apply {
                    data = Uri.parse("package:${context.packageName}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                true
            } else {
                openAppSettings(context)
            }
        } catch (e: Exception) {
            openAppSettings(context)
        }
    }

    /**
     * Checks if application is whitelisted from battery optimizations
     */
    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? android.os.PowerManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            powerManager?.isIgnoringBatteryOptimizations(context.packageName) ?: true
        } else {
            true
        }
    }

    /**
     * Fallback: opens standard application info & details page
     */
    fun openAppSettings(context: Context): Boolean {
        return try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }
}
