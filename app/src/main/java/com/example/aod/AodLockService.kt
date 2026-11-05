package com.example.aod

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.media.AudioManager
import android.os.BatteryManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.MuhasibApplication
import com.example.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalTime

class AodLockService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var screenOffJob: Job? = null
    private var screenReceiver: BroadcastReceiver? = null
    private var wakeLock: PowerManager.WakeLock? = null

    companion object {
        const val ACTION_DISMISS_AOD = "com.example.aod.ACTION_DISMISS_AOD"

        @Volatile
        var lastAodLaunchTimeMs: Long = 0L

        // ponytail: evaluate battery saver & low battery (< 15%) restriction rule
        fun shouldBlockAodDueToBattery(
            isBatterySaverCutoffEnabled: Boolean,
            isPowerSaveMode: Boolean,
            batteryPct: Float
        ): Boolean {
            return isBatterySaverCutoffEnabled && (isPowerSaveMode || batteryPct < 15f)
        }

        // ponytail: check if within scheduled quiet hours (23:30 to 05:00)
        fun isWithinScheduledQuietHours(
            time: LocalTime,
            quietStart: LocalTime = LocalTime.of(23, 30),
            quietEnd: LocalTime = LocalTime.of(5, 0)
        ): Boolean {
            return if (quietStart.isAfter(quietEnd)) {
                time >= quietStart || time < quietEnd
            } else {
                time >= quietStart && time < quietEnd
            }
        }

        @Volatile
        private var activeServiceInstance: AodLockService? = null

        fun releaseWakeLock() {
            try {
                activeServiceInstance?.let { service ->
                    if (service.wakeLock?.isHeld == true) {
                        service.wakeLock?.release()
                    }
                }
            } catch (_: Exception) {}
        }
    }

    override fun onCreate() {
        super.onCreate()
        activeServiceInstance = this
        startForegroundWithNotification()
        registerScreenStateReceiver()
    }

    private fun startForegroundWithNotification() {
        val openAppIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification: Notification = NotificationCompat.Builder(this, MuhasibApplication.CHANNEL_ID_AOD_SERVICE)
            .setContentTitle("الحارس المحيطي — المُحَاسِب")
            .setContentText("خدمة الشاشة الدائمة (AOD) نشطة وتعمل تلقائياً فور قفل الهاتف")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_SECRET)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val foregroundServiceType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            } else {
                0
            }
            startForeground(1001, notification, foregroundServiceType)
        } else {
            startForeground(1001, notification)
        }
    }

    private fun registerScreenStateReceiver() {
        if (screenReceiver != null) return

        screenReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    Intent.ACTION_SCREEN_OFF -> {
                        // ponytail: if user pressed power button while AOD was active, dismiss AOD and do NOT relaunch it,
                        // so device stays off and next power-on press goes directly to the system Lock Screen!
                        if (AodActivity.isActive() || AodActivity.isAodResumed) {
                            screenOffJob?.cancel()
                            screenOffJob = null
                            AodActivity.finishIfActive()
                            return
                        }

                        // AUTOMATIC ACTIVATION: Launch AOD when device locks / screen turns off
                        // Skip if phone is currently engaged in an active call
                        val audioManager = context?.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
                        val inCall = audioManager?.mode == AudioManager.MODE_IN_CALL ||
                                audioManager?.mode == AudioManager.MODE_IN_COMMUNICATION

                        if (!inCall && context != null && AodManager.isAodEnabled(context)) {
                            // ponytail: 200ms debounce ensures OS finishes screen-off broadcast before waking AOD
                            screenOffJob?.cancel()
                            screenOffJob = serviceScope.launch {
                                delay(200)
                                launchAodActivity(context)
                            }
                        }
                    }
                    Intent.ACTION_SCREEN_ON -> {
                        // Check if SCREEN_ON is from our own AOD launch waking the screen vs user manual wake
                        val timeSinceLaunch = System.currentTimeMillis() - lastAodLaunchTimeMs
                        if (timeSinceLaunch > 2000L) {
                            // If user woke phone manually from sleep/screen-off, abort pending AOD launch and dismiss active AOD
                            // so pressing hardware power button reveals native Android Lock Screen directly!
                            screenOffJob?.cancel()
                            screenOffJob = null
                            AodActivity.finishIfActive()
                        }
                    }
                    Intent.ACTION_USER_PRESENT -> {
                        screenOffJob?.cancel()
                        screenOffJob = null
                        // Device was successfully unlocked (fingerprint/PIN/face) -> Dismiss AOD
                        if (context != null) {
                            val dismissIntent = Intent(ACTION_DISMISS_AOD).apply {
                                setPackage(context.packageName)
                            }
                            context.sendBroadcast(dismissIntent)
                        }
                    }
                }
            }
        }

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_USER_PRESENT)
        }
        ContextCompat.registerReceiver(
            this,
            screenReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    private fun launchAodActivity(context: Context) {
        lastAodLaunchTimeMs = System.currentTimeMillis()
        // ponytail: check battery status before launching AOD
        if (AodManager.isBatterySaverDisableEnabled(context)) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            val isPowerSave = powerManager?.isPowerSaveMode == true

            var batteryPct = 100f
            try {
                val batteryFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
                val batteryStatus = context.registerReceiver(null, batteryFilter)
                val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
                val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
                if (level >= 0 && scale > 0) {
                    batteryPct = level * 100f / scale
                }
            } catch (e: Exception) {
                // Ignore receiver error
            }

            if (shouldBlockAodDueToBattery(true, isPowerSave, batteryPct)) {
                return
            }
        }

        // ponytail: scheduled quiet hours check (23:30 to 05:00)
        if (AodManager.getAodDisplayMode(context) == AodManager.MODE_SCHEDULED) {
            if (isWithinScheduledQuietHours(LocalTime.now())) {
                return
            }
        }

        // Low-power dim wake lock to transition screen smoothly without high-brightness power surge
        try {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
            @Suppress("DEPRECATION")
            wakeLock = powerManager?.newWakeLock(
                PowerManager.SCREEN_DIM_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE,
                "AlMuhasib:AodScreenWake"
            )?.apply {
                setReferenceCounted(false)
                acquire(1000)
            }
        } catch (e: Exception) {
            try {
                val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
                wakeLock = powerManager?.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK,
                    "AlMuhasib:AodCpuWake"
                )?.apply {
                    setReferenceCounted(false)
                    acquire(1000)
                }
            } catch (e2: Exception) {
                // Ignore
            }
        }

        val aodIntent = Intent(context, AodActivity::class.java).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_SINGLE_TOP or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
                Intent.FLAG_ACTIVITY_NO_USER_ACTION or
                Intent.FLAG_ACTIVITY_NO_ANIMATION
            )
        }

        // 1. Direct Start Activity
        try {
            context.startActivity(aodIntent)
        } catch (e: Exception) {
            // If background start is restricted by OS
        }

        // 2. High-priority FullScreenIntent fallback (Ensures wake on MIUI / Android 10+)
        try {
            val fullScreenPendingIntent = PendingIntent.getActivity(
                context,
                1005,
                aodIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val fullScreenNotification = NotificationCompat.Builder(context, MuhasibApplication.CHANNEL_ID_AOD_LAUNCHER)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("AOD Active")
                .setContentText("عرض الشاشة المحيطية")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setFullScreenIntent(fullScreenPendingIntent, true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setAutoCancel(true)
                .build()

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.notify(1005, fullScreenNotification)
        } catch (e: Exception) {
            // Ignore
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        serviceScope.cancel()
        screenReceiver?.let {
            try {
                unregisterReceiver(it)
            } catch (e: Exception) {
                // Ignore
            }
            screenReceiver = null
        }
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (e: Exception) {
            // Ignore
        }
        if (activeServiceInstance == this) {
            activeServiceInstance = null
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
