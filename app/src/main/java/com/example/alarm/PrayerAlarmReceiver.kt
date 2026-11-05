package com.example.alarm

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.MuhasibApplication
import com.example.R
import com.example.util.HapticFeedbackUtil
import com.example.widget.WidgetUpdateHelper

class PrayerAlarmReceiver : BroadcastReceiver() {

    companion object {
        const val NOTIFICATION_ID_PRAYER_WARN = 3001
        const val NOTIFICATION_ID_PRAYER = 3001
        const val NOTIFICATION_ID_PRE_ADHAN = 3002
    }

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action
        val prayerName = intent?.getStringExtra(PrayerAlarmScheduler.EXTRA_PRAYER_NAME) ?: "الصلاة"
        val isSnooze = intent?.getBooleanExtra(PrayerAlarmScheduler.EXTRA_IS_SNOOZE, false) ?: false
        val minutesLeft = intent?.getIntExtra(PrayerAlarmScheduler.EXTRA_MINUTES_LEFT, 0) ?: 0

        when (action) {
            PrayerAlarmScheduler.ACTION_SNOOZE_10_MIN -> {
                PrayerWarningActivity.dismissIfActive()
                // ponytail: immediate audio stop & cancel notification, then schedule 10 min snooze
                PrayerAdhanPlayer.stop()
                try {
                    NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID_PRAYER)
                } catch (e: Exception) {
                    // Ignore
                }
                PrayerAlarmScheduler.scheduleSnoozeAlarm(context, prayerName)
            }

            PrayerAlarmScheduler.ACTION_DISMISS_ADHAN -> {
                PrayerWarningActivity.dismissIfActive()
                // ponytail: stop adhan audio & clear notification
                PrayerAdhanPlayer.stop()
                try {
                    NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID_PRAYER)
                } catch (e: Exception) {
                    // Ignore
                }
            }

            PrayerAlarmScheduler.ACTION_MIDNIGHT_ROLLOVER -> {
                // ponytail: auto-rollover at midnight 00:00:05 - update all widgets and re-arm
                WidgetUpdateHelper.updateAllWidgets(context)
                PrayerAlarmScheduler.scheduleMidnightRollover(context, java.time.LocalDateTime.now().plusSeconds(10))
            }

            else -> {
                // Pre-Adhan Preparation Reminder (10/15 minutes before prayer): gentle notification only
                if (minutesLeft > 0) {
                    val openAppIntent = Intent(context, MainActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    }
                    val contentPendingIntent = PendingIntent.getActivity(
                        context,
                        2005,
                        openAppIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    val preAdhanNotification = NotificationCompat.Builder(context, MuhasibApplication.CHANNEL_ID_PRE_ADHAN)
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentTitle("اقترب وقت صلاة $prayerName")
                        .setContentText("متبقي $minutesLeft دقيقة على الأذان • استعد بالوضوء والصلاة")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setCategory(NotificationCompat.CATEGORY_REMINDER)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setContentIntent(contentPendingIntent)
                        .setAutoCancel(true)
                        .build()

                    try {
                        val notificationManager = NotificationManagerCompat.from(context)
                        notificationManager.notify(NOTIFICATION_ID_PRE_ADHAN, preAdhanNotification)
                    } catch (_: SecurityException) {}

                    HapticFeedbackUtil.performLightTick(context)
                    return
                }

                // ponytail: play adhan audio on USAGE_ALARM stream
                PrayerAdhanPlayer.play(context, prayerName)

                // Trigger severe alarm haptics
                HapticFeedbackUtil.performSevereAlarmVibration(context)

                // Full-Screen / Content Intent
                val fullScreenIntent = Intent(context, PrayerWarningActivity::class.java).apply {
                    putExtra(PrayerAlarmScheduler.EXTRA_PRAYER_NAME, prayerName)
                    putExtra(PrayerAlarmScheduler.EXTRA_MINUTES_LEFT, 0)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }

                val fullScreenPendingIntent = PendingIntent.getActivity(
                    context,
                    2001,
                    fullScreenIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                // Snooze 10 Minutes Action Intent
                val snoozeIntent = Intent(context, PrayerAlarmReceiver::class.java).apply {
                    this.action = PrayerAlarmScheduler.ACTION_SNOOZE_10_MIN
                    putExtra(PrayerAlarmScheduler.EXTRA_PRAYER_NAME, prayerName)
                }
                val snoozePendingIntent = PendingIntent.getBroadcast(
                    context,
                    2002,
                    snoozeIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                // Dismiss / Stop Adhan Action Intent
                val dismissIntent = Intent(context, PrayerAlarmReceiver::class.java).apply {
                    this.action = PrayerAlarmScheduler.ACTION_DISMISS_ADHAN
                    putExtra(PrayerAlarmScheduler.EXTRA_PRAYER_NAME, prayerName)
                }
                val dismissPendingIntent = PendingIntent.getBroadcast(
                    context,
                    2003,
                    dismissIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val title = if (isSnooze) {
                    "تذكير الصلاة: $prayerName"
                } else {
                    "حان الآن موعد أذان $prayerName"
                }

                val contentText = if (isSnooze) {
                    "مضت 10 دقائق على وقت $prayerName • أقم صلاتك يرحمك الله"
                } else {
                    "حيَّ على الصلاة • حيَّ على الفلاح"
                }

                val notification = NotificationCompat.Builder(context, MuhasibApplication.CHANNEL_ID_PRAYER_ALARM)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle(title)
                    .setContentText(contentText)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setFullScreenIntent(fullScreenPendingIntent, true)
                    .setContentIntent(fullScreenPendingIntent)
                    .setDeleteIntent(dismissPendingIntent)
                    .setAutoCancel(true)
                    .addAction(
                        R.drawable.ic_launcher_foreground,
                        "تذكير بعد 10 دقائق ⏱️",
                        snoozePendingIntent
                    )
                    .addAction(
                        R.drawable.ic_launcher_foreground,
                        "إيقاف الأذان ✓",
                        dismissPendingIntent
                    )
                    .build()

                try {
                    val notificationManager = NotificationManagerCompat.from(context)
                    notificationManager.notify(NOTIFICATION_ID_PRAYER, notification)
                } catch (e: SecurityException) {
                    // Notifications permission check
                }

                // Reschedule for the subsequent prayer if this was not a snooze
                if (!isSnooze) {
                    PrayerAlarmScheduler.scheduleNextPrayerAlarm(context)
                }
            }
        }
    }
}
