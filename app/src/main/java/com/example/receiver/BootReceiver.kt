package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.alarm.PrayerAlarmScheduler
import com.example.aod.AodManager

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED ||
            intent?.action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            // Re-arm Always-On Display if enabled
            if (AodManager.isAodEnabled(context)) {
                AodManager.startAodLockService(context)
            }

            // Re-start Floating Adhkar HUD if enabled
            if (AodManager.isFloatingDhikrEnabled(context)) {
                AodManager.startFloatingDhikrService(context)
            }

            // Re-schedule prayer threshold alarm and midnight auto-rollover
            PrayerAlarmScheduler.scheduleNextPrayerThresholdAlarm(context)
            PrayerAlarmScheduler.scheduleMidnightRollover(context)
        }
    }
}
