package com.example
 
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.example.aod.AodManager
import com.example.data.AppDatabase
import com.example.data.repository.MuhasibRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MuhasibApplication : Application() {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val database by lazy { AppDatabase.getDatabase(this, applicationScope) }
    val repository by lazy { MuhasibRepository(database) }

    companion object {
        const val CHANNEL_ID_PRAYER_ALARM = "channel_prayer_alarm"
        const val CHANNEL_ID_PRE_ADHAN = "channel_pre_adhan"
        const val CHANNEL_ID_AOD_SERVICE = "channel_aod_service"
        const val CHANNEL_ID_AOD_LAUNCHER = "channel_aod_launcher"
        const val CHANNEL_ID_BOOK_WIRD = "channel_book_wird"
        lateinit var instance: MuhasibApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Warm up and initialize prayer preferences and settings
        com.example.util.prayer.PrayerPreferences.init(this)

        // Subsecond exact prayer alarm schedule and midnight auto-rollover
        try {
            com.example.alarm.PrayerAlarmScheduler.scheduleNextPrayerAlarm(this)
            com.example.alarm.PrayerAlarmScheduler.scheduleMidnightRollover(this)
            com.example.library.BookWirdScheduler.scheduleDailyWirdAlarms(this)
        } catch (_: Exception) {}

        createNotificationChannels()

        applicationScope.launch {
            repository.ensureSeeded()
        }

        // Automatically activate AOD Background Service if enabled
        if (AodManager.isAodEnabled(this)) {
            try {
                AodManager.startAodLockService(this)
            } catch (e: Exception) {
                // Ignore if started before boot completes
            }
        }

        // Automatically activate Floating Dhikr HUD if enabled
        if (AodManager.isFloatingDhikrEnabled(this)) {
            try {
                AodManager.startFloatingDhikrService(this)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)

            // High Priority Emergency Alarm Channel for Prayer Warnings
            val alarmChannel = NotificationChannel(
                CHANNEL_ID_PRAYER_ALARM,
                "Emergency Salah Alarms",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Unmissable full-screen warnings for approaching Salah deadlines"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 800)
                setBypassDnd(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                setSound(null, null)
            }

            // Low Priority Channel for Ambient AOD Lock Service
            val aodChannel = NotificationChannel(
                CHANNEL_ID_AOD_SERVICE,
                "AOD Lock Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Background supervisor for hardware lock screen AOD transition"
                setShowBadge(false)
            }

            // High Priority Channel for AOD Lockscreen Trigger
            val aodLauncherChannel = NotificationChannel(
                CHANNEL_ID_AOD_LAUNCHER,
                "AOD Automatic Display",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Immediate full-screen wake upon device lock"
                setShowBadge(false)
                setSound(null, null)
                enableVibration(false)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }

            // Gentle Preparation Channel for Pre-Adhan Reminders
            val preAdhanChannel = NotificationChannel(
                CHANNEL_ID_PRE_ADHAN,
                "Pre-Adhan Preparation",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Gentle reminders before prayer times for wudu and preparation"
                enableVibration(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }

            notificationManager?.createNotificationChannel(alarmChannel)
            notificationManager?.createNotificationChannel(aodChannel)
            notificationManager?.createNotificationChannel(aodLauncherChannel)
            notificationManager?.createNotificationChannel(preAdhanChannel)

            val bookWirdChannel = NotificationChannel(
                CHANNEL_ID_BOOK_WIRD,
                "وِرد العلم والقراءة اليومية",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "إشعارات قراءة شذرات الكتب الإسلامية ومزامنة موضع القراءة"
                enableVibration(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }
            notificationManager?.createNotificationChannel(bookWirdChannel)
        }
    }
}
