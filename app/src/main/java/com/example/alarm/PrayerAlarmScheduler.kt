package com.example.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.util.IslamicCalendarUtil
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

object PrayerAlarmScheduler {

    const val EXTRA_PRAYER_NAME = "extra_prayer_name"
    const val EXTRA_MINUTES_LEFT = "extra_minutes_left"
    const val EXTRA_IS_SNOOZE = "extra_is_snooze"
    const val ACTION_PRAYER_ALARM = "com.example.alarm.ACTION_PRAYER_ALARM"
    const val ACTION_SNOOZE_10_MIN = "com.example.alarm.ACTION_SNOOZE_10_MIN"
    const val ACTION_DISMISS_ADHAN = "com.example.alarm.ACTION_DISMISS_ADHAN"
    const val ACTION_MIDNIGHT_ROLLOVER = "com.example.alarm.ACTION_MIDNIGHT_ROLLOVER"

    // ponytail: calculate next midnight rollover epoch ms at 00:00:05
    fun calculateNextMidnightEpochMs(
        now: LocalDateTime = LocalDateTime.now(),
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Long {
        val rolloverTime = LocalTime.of(0, 0, 5)
        val targetDate = if (now.toLocalTime().isBefore(rolloverTime)) {
            now.toLocalDate()
        } else {
            now.toLocalDate().plusDays(1)
        }
        return targetDate.atTime(rolloverTime).atZone(zoneId).toInstant().toEpochMilli()
    }

    // ponytail: schedule midnight rollover at 00:00:05 to invoke WidgetUpdateHelper.updateAllWidgets
    fun scheduleMidnightRollover(context: Context, now: LocalDateTime = LocalDateTime.now()) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val triggerEpochMs = calculateNextMidnightEpochMs(now)

        val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            action = ACTION_MIDNIGHT_ROLLOVER
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            1005,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        scheduleInexactOrIdle(alarmManager, triggerEpochMs, pendingIntent)
    }

    // Pure calculation helper for next prayer target
    fun calculateNextPrayerTarget(
        schedule: List<IslamicCalendarUtil.PrayerSchedule>,
        now: LocalTime,
        today: LocalDate
    ): Pair<LocalDateTime, String> {
        val nextPrayer = schedule.firstOrNull { it.time.isAfter(now.plusSeconds(30)) }
        val targetDateTime = if (nextPrayer != null) {
            LocalDateTime.of(today, nextPrayer.time)
        } else {
            // Tomorrow's Fajr
            val firstPrayer = schedule.first()
            LocalDateTime.of(today.plusDays(1), firstPrayer.time)
        }
        val prayerName = nextPrayer?.arabicName ?: schedule.first().arabicName
        return Pair(targetDateTime, prayerName)
    }

    fun calculateTriggerEpochMs(targetDateTime: LocalDateTime, zone: ZoneId): Long {
        return targetDateTime.atZone(zone).toInstant().toEpochMilli()
    }

    // ponytail: schedule next prayer EXACTLY on time at scheduled minute
    fun scheduleNextPrayerAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val settings = runCatching { com.example.util.prayer.PrayerPreferences.getSettings(context) }.getOrNull()
        val prayerZone = runCatching {
            if (settings?.timezoneId?.isNotBlank() == true) ZoneId.of(settings.timezoneId) else ZoneId.systemDefault()
        }.getOrDefault(ZoneId.systemDefault())

        val schedule = IslamicCalendarUtil.getDailyPrayerSchedule()
        val now = LocalTime.now(prayerZone)
        val today = LocalDate.now(prayerZone)

        val (targetDateTime, prayerName) = calculateNextPrayerTarget(schedule, now, today)
        var triggerEpochMs = calculateTriggerEpochMs(targetDateTime, prayerZone)

        // Safety: ensure alarm trigger is in the future
        if (triggerEpochMs <= System.currentTimeMillis()) {
            triggerEpochMs = System.currentTimeMillis() + 1000L
        }

        val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            action = ACTION_PRAYER_ALARM
            putExtra(EXTRA_PRAYER_NAME, prayerName)
            putExtra(EXTRA_IS_SNOOZE, false)
            putExtra(EXTRA_MINUTES_LEFT, 0)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            1002,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        scheduleExact(context, alarmManager, triggerEpochMs, pendingIntent)

        // Pre-Adhan Reminder (10/15 min) gentle alert if configured
        val preAdhanMinutes = settings?.preAdhanMinutes ?: 0
        if (preAdhanMinutes > 0) {
            schedulePreAdhanAlarm(context, alarmManager, targetDateTime, prayerName, preAdhanMinutes, prayerZone)
        }

        // ponytail: also ensure midnight auto-rollover is scheduled
        scheduleMidnightRollover(context)
    }

    fun schedulePreAdhanAlarm(
        context: Context,
        alarmManager: AlarmManager,
        targetDateTime: LocalDateTime,
        prayerName: String,
        minutesBefore: Int,
        zone: ZoneId
    ) {
        val preTarget = targetDateTime.minusMinutes(minutesBefore.toLong())
        val preEpochMs = preTarget.atZone(zone).toInstant().toEpochMilli()
        val currentMs = System.currentTimeMillis()

        if (preEpochMs > currentMs + 60_000L) {
            val preIntent = Intent(context, PrayerAlarmReceiver::class.java).apply {
                action = ACTION_PRAYER_ALARM
                putExtra(EXTRA_PRAYER_NAME, prayerName)
                putExtra(EXTRA_IS_SNOOZE, false)
                putExtra(EXTRA_MINUTES_LEFT, minutesBefore)
            }
            val prePendingIntent = PendingIntent.getBroadcast(
                context,
                1004,
                preIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            scheduleExact(context, alarmManager, preEpochMs, prePendingIntent)
        }
    }

    // ponytail: backwards compatibility alias
    fun scheduleNextPrayerThresholdAlarm(context: Context) {
        scheduleNextPrayerAlarm(context)
    }

    // ponytail: 10-minute snooze alarm
    fun schedule10MinuteSnooze(context: Context, prayerName: String) {
        scheduleSnoozeAlarm(context, prayerName)
    }

    fun scheduleSnoozeAlarm(context: Context, prayerName: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val triggerEpochMs = System.currentTimeMillis() + (10 * 60 * 1000L)

        val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            action = ACTION_PRAYER_ALARM
            putExtra(EXTRA_PRAYER_NAME, prayerName)
            putExtra(EXTRA_IS_SNOOZE, true)
            putExtra(EXTRA_MINUTES_LEFT, 0)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            1003,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        scheduleExact(context, alarmManager, triggerEpochMs, pendingIntent)
    }

    private fun scheduleExact(context: Context, alarmManager: AlarmManager, triggerEpochMs: Long, pendingIntent: PendingIntent) {
        val showIntent = PendingIntent.getActivity(
            context,
            1001,
            Intent(context, com.example.MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerEpochMs, showIntent)

        try {
            // AlarmClock API: Top-priority, exempt from Doze mode delays, subsecond-perfect precision
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
        } catch (_: SecurityException) {
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
            } catch (_: SecurityException) {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    triggerEpochMs,
                    pendingIntent
                )
            }
        }
    }

    private fun scheduleInexactOrIdle(alarmManager: AlarmManager, triggerEpochMs: Long, pendingIntent: PendingIntent) {
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
        } catch (_: SecurityException) {
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerEpochMs,
                pendingIntent
            )
        }
    }

    /**
     * For manual testing or demo trigger
     */
    fun triggerInstantAlarmTest(context: Context, prayerName: String = "العصر") {
        val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            action = ACTION_PRAYER_ALARM
            putExtra(EXTRA_PRAYER_NAME, prayerName)
            putExtra(EXTRA_IS_SNOOZE, false)
            putExtra(EXTRA_MINUTES_LEFT, 0)
        }
        context.sendBroadcast(intent)
    }
}
