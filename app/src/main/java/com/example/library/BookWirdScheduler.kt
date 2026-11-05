package com.example.library

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.util.IslamicCalendarUtil
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

object BookWirdScheduler {
    private const val TAG = "BookWirdScheduler"
    const val REQUEST_CODE_BASE = 5000

    /**
     * Schedules 5 daily serialized book reading notifications (15 minutes after each prayer).
     */
    fun scheduleDailyWirdAlarms(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val schedule = IslamicCalendarUtil.getDailyPrayerSchedule()
        if (schedule.isEmpty()) return

        val zone = ZoneId.systemDefault()
        val today = LocalDate.now()
        val now = LocalDateTime.now()

        for (i in 0 until minOf(5, schedule.size)) {
            val prayer = schedule[i]
            // 15 minutes after prayer time
            var triggerTime = LocalDateTime.of(today, prayer.time).plusMinutes(15)
            if (triggerTime.isBefore(now)) {
                triggerTime = triggerTime.plusDays(1)
            }

            val triggerEpochMs = triggerTime.atZone(zone).toInstant().toEpochMilli()
            val intent = Intent(context, BookNotificationReceiver::class.java).apply {
                action = BookNotificationReceiver.ACTION_TRIGGER_WIRD
                putExtra(BookNotificationReceiver.EXTRA_SLOT_INDEX, i)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE_BASE + i,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerEpochMs,
                        pendingIntent
                    )
                } else {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        triggerEpochMs,
                        pendingIntent
                    )
                }
            } catch (e: SecurityException) {
                Log.w(TAG, "Exact alarm permission restricted, falling back to inexact alarm", e)
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerEpochMs, pendingIntent)
            }
        }
    }
}
