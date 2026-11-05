package com.example.aod

import android.app.KeyguardManager
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import com.example.util.HapticFeedbackUtil
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.example.MuhasibApplication
import com.example.ui.theme.AlMuhasibTheme
import com.example.widget.WidgetUpdateHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

class AodActivity : ComponentActivity(), SensorEventListener {

    private var sensorManager: SensorManager? = null
    private var proximitySensor: Sensor? = null
    private var lightSensor: Sensor? = null
    private var lastKnownLux: Float? = null

    private var isPocketActive = mutableStateOf(false)
    private var dismissReceiver: BroadcastReceiver? = null
    private var tap10sJob: Job? = null
    private var pocketJob: Job? = null
    private var lastAppliedBrightness: Float = -1f
    private var lastLightSampleTime: Long = 0L
    private var currentDhikrIncrementAction: (() -> Unit)? = null
    private var currentDhikrTarget: Int = 100
    private var currentDhikrIndex: Int = 0

    companion object {
        @Volatile
        var isAodResumed: Boolean = false
        @Volatile
        var activeInstance: AodActivity? = null

        fun isActive(): Boolean {
            val inst = activeInstance
            return inst != null && !inst.isFinishing
        }

        fun finishIfActive(): Boolean {
            val inst = activeInstance
            if (inst != null && !inst.isFinishing) {
                inst.finish()
                activeInstance = null
                isAodResumed = false
                return true
            }
            activeInstance = null
            isAodResumed = false
            return false
        }

        // ponytail: ambient light sensor lux curve mapping for OLED efficiency (guaranteed minimum visible threshold)
        fun calculateAutoBrightness(lux: Float): Float {
            return when {
                lux < 5f -> 0.03f       // Ultra-dark room: 3% is gentle yet clearly legible
                lux <= 50f -> 0.05f     // Dim room: 5%
                lux <= 300f -> 0.09f    // Normal indoor room: 9%
                lux <= 1000f -> 0.14f   // Bright office: 14%
                else -> 0.22f           // Outdoor / direct sunlight: 22%
            }
        }

        // ponytail: proximity pocket detection formula
        fun calculateIsCovered(distance: Float, maxRange: Float): Boolean {
            return distance < 1.0f || (distance <= 2.0f && distance < maxRange)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        activeInstance = this
        // ponytail: enable showWhenLocked and turnScreenOn prior to super.onCreate for Xiaomi/Android keyguard display
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        super.onCreate(savedInstanceState)

        // Setup complete immersive fullscreen to eliminate phone heading / status bar
        configureAodWindow()

        // Setup Proximity & Light Sensors
        initSensors()

        // Register dismissal receiver when phone is unlocked (Android 14+ safe)
        registerDismissReceiver()

        val repository = MuhasibApplication.instance.repository

        setContent {
            AlMuhasibTheme(darkTheme = true) {
                val sobrietyState by repository.getSobrietyState()
                    .collectAsState(initial = null)

                var burnInOffsetX by remember { mutableIntStateOf(0) }
                var burnInOffsetY by remember { mutableIntStateOf(0) }

                AodDisplayContent(
                    istighfarCount = sobrietyState?.istighfarTodayCount ?: 0,
                    onIncrementIstighfar = {
                        lifecycleScope.launch {
                            repository.incrementIstighfarCount()
                            WidgetUpdateHelper.updateAllWidgets(this@AodActivity)
                        }
                    },
                    subhanAllahCount = sobrietyState?.subhanAllahTodayCount ?: 0,
                    onIncrementSubhanAllah = {
                        lifecycleScope.launch {
                            repository.incrementSubhanAllahCount()
                            WidgetUpdateHelper.updateAllWidgets(this@AodActivity)
                        }
                    },
                    alhamdulillahCount = sobrietyState?.alhamdulillahTodayCount ?: 0,
                    onIncrementAlhamdulillah = {
                        lifecycleScope.launch {
                            repository.incrementAlhamdulillahCount()
                            WidgetUpdateHelper.updateAllWidgets(this@AodActivity)
                        }
                    },
                    allahuAkbarCount = sobrietyState?.allahuAkbarTodayCount ?: 0,
                    onIncrementAllahuAkbar = {
                        lifecycleScope.launch {
                            repository.incrementAllahuAkbarCount()
                            WidgetUpdateHelper.updateAllWidgets(this@AodActivity)
                        }
                    },
                    laIlahaIllallahCount = sobrietyState?.laIlahaIllallahTodayCount ?: 0,
                    onIncrementLaIlahaIllallah = {
                        lifecycleScope.launch {
                            repository.incrementLaIlahaIllallahCount()
                            WidgetUpdateHelper.updateAllWidgets(this@AodActivity)
                        }
                    },
                    tasbi7Count = sobrietyState?.tasbi7TodayCount ?: 0,
                    onIncrementTasbi7 = {
                        lifecycleScope.launch {
                            repository.incrementTasbi7Count()
                            WidgetUpdateHelper.updateAllWidgets(this@AodActivity)
                        }
                    },
                    onUnlock = {
                        unlockDeviceAndExit()
                    },
                    isInPocket = isPocketActive.value,
                    burnInOffsetX = burnInOffsetX,
                    burnInOffsetY = burnInOffsetY,
                    clockStyle = AodManager.getClockStyle(this@AodActivity),
                    accentColorStyle = AodManager.getAccentColor(this@AodActivity),
                    batteryStyle = AodManager.getBatteryStyle(this@AodActivity),
                    isNotificationPrivacy = AodManager.isNotificationPrivacyEnabled(this@AodActivity),
                    onMinuteTick = { now ->
                        // Synchronous 1-minute tick: Pixel shifting & Scheduled quiet hours & Ambient light sampling
                        if (AodManager.isBurnInProtectionEnabled(this@AodActivity)) {
                            burnInOffsetX = Random.nextInt(-3, 4)
                            burnInOffsetY = Random.nextInt(-3, 4)
                        }
                        if (AodManager.getAodDisplayMode(this@AodActivity) == AodManager.MODE_SCHEDULED) {
                            if (AodLockService.isWithinScheduledQuietHours(now)) {
                                finish()
                            }
                        }
                        sampleAmbientLightOnce()
                    },
                    onSelectedDhikrChanged = { index, _, onIncrement, target ->
                        currentDhikrIndex = index
                        currentDhikrIncrementAction = onIncrement
                        currentDhikrTarget = target
                    }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        configureAodWindow()
    }

    override fun onRestart() {
        super.onRestart()
        configureAodWindow()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        // ponytail: re-affirm showWhenLocked and turnScreenOn when window attaches
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
    }

    override fun onResume() {
        super.onResume()
        isAodResumed = true
        activeInstance = this
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
        hideSystemBars()

        // Release transition wake lock immediately now that AodActivity is visible
        AodLockService.releaseWakeLock()

        // ponytail: initialize 10s countdown if in MIUI Tap-to-Show mode
        if (AodManager.getAodDisplayMode(this) == AodManager.MODE_TAP_10S) {
            resetTap10sTimer()
        }
    }

    override fun onPause() {
        super.onPause()
        isAodResumed = false
    }

    // ponytail: MIUI "Tap to Show for 10s" Mode touch dispatcher
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev?.action == MotionEvent.ACTION_DOWN && !isPocketActive.value) {
            resetTap10sTimer()
            sampleAmbientLightOnce()
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun resetTap10sTimer() {
        if (AodManager.getAodDisplayMode(this) != AodManager.MODE_TAP_10S) return
        tap10sJob?.cancel()
        tap10sJob = lifecycleScope.launch {
            delay(10_000L)
            finish()
        }
    }

    private fun configureAodWindow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }

        // Apply OLED dimming based on preferences or ambient light
        val brightness = if (AodManager.isAutoBrightnessEnabled(this) && lastKnownLux != null) {
            calculateAutoBrightness(lastKnownLux!!)
        } else {
            AodManager.getScreenBrightness(this)
        }

        val lp = window.attributes
        lp.screenBrightness = if (isPocketActive.value) 0.0f else brightness
        lastAppliedBrightness = lp.screenBrightness

        // Limit display refresh rate to 60Hz or lowest supported mode (saving ~50% display power on 120Hz AMOLED like Poco F4)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val currentDisplay = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    display
                } else {
                    @Suppress("DEPRECATION")
                    windowManager.defaultDisplay
                }
                val modes = currentDisplay?.supportedModes
                val lowestMode = modes?.minByOrNull { it.refreshRate }
                if (lowestMode != null) {
                    lp.preferredDisplayModeId = lowestMode.modeId
                    lp.preferredRefreshRate = lowestMode.refreshRate.coerceAtMost(60f)
                } else {
                    lp.preferredRefreshRate = 60f
                }
            } catch (_: Exception) {}
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            lp.layoutInDisplayCutoutMode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
            } else {
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }
        window.attributes = lp

        window.setBackgroundDrawableResource(android.R.color.black)
        @Suppress("DEPRECATION")
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        @Suppress("DEPRECATION")
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        // Note: Strictly do NOT add FLAG_ALLOW_LOCK_WHILE_SCREEN_ON as it instructs Keyguard to draw over AOD
        val keepScreenFlag = if (!isPocketActive.value) WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON else 0
        @Suppress("DEPRECATION")
        window.addFlags(
            keepScreenFlag or
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        WindowCompat.setDecorFitsSystemWindows(window, false)
        hideSystemBars()

        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { _, _ ->
            hideSystemBars()
            WindowInsetsCompat.CONSUMED
        }
    }

    private fun hideSystemBars() {
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        insetsController.hide(WindowInsetsCompat.Type.systemBars())

        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_FULLSCREEN
            or View.SYSTEM_UI_FLAG_LOW_PROFILE
        )
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemBars()
            // Clear launcher notification once AOD is safely rendered in foreground
            try {
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                notificationManager?.cancel(1005)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun sampleAmbientLightOnce() {
        if (!AodManager.isAutoBrightnessEnabled(this) || isPocketActive.value) return
        try {
            lightSensor?.let {
                sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
            }
        } catch (_: Exception) {}
    }

    private fun initSensors() {
        try {
            sensorManager = getSystemService(Context.SENSOR_SERVICE) as? SensorManager

            // Only register proximity sensor if pocket mode is enabled
            if (AodManager.isPocketModeEnabled(this)) {
                proximitySensor = sensorManager?.getDefaultSensor(Sensor.TYPE_PROXIMITY)
                proximitySensor?.let {
                    sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
                }
            }

            // Hardware light sensor: sample ambient light once at startup, then unregister immediately
            if (AodManager.isAutoBrightnessEnabled(this)) {
                lightSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_LIGHT)
                sampleAmbientLightOnce()
            }
        } catch (e: Exception) {
            // Ignore if device lacks sensor
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        when (event.sensor.type) {
            Sensor.TYPE_PROXIMITY -> {
                if (!AodManager.isPocketModeEnabled(this)) {
                    isPocketActive.value = false
                    return
                }
                val distance = event.values[0]
                val maxRange = proximitySensor?.maximumRange ?: 5f
                val isCovered = calculateIsCovered(distance, maxRange)

                // debounce pocket state to prevent virtual proximity sensor calibration glitch
                pocketJob?.cancel()
                if (isCovered) {
                    pocketJob = lifecycleScope.launch {
                        delay(600) // Must stay covered for at least 600ms
                        isPocketActive.value = true
                        try {
                            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                            val lp = window.attributes
                            lp.screenBrightness = 0.0f
                            lastAppliedBrightness = 0.0f
                            window.attributes = lp
                        } catch (e: Exception) {
                            // Ignore
                        }
                    }
                } else {
                    isPocketActive.value = false
                    try {
                        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                        val lp = window.attributes
                        val restoredBrightness = if (AodManager.isAutoBrightnessEnabled(this@AodActivity)) {
                            lastKnownLux?.let { calculateAutoBrightness(it) } ?: AodManager.getScreenBrightness(this@AodActivity)
                        } else {
                            AodManager.getScreenBrightness(this@AodActivity)
                        }
                        lp.screenBrightness = restoredBrightness
                        lastAppliedBrightness = restoredBrightness
                        window.attributes = lp
                    } catch (e: Exception) {
                        // Ignore
                    }
                }
            }

            Sensor.TYPE_LIGHT -> {
                // Ignore light readings while in pocket
                if (isPocketActive.value) return

                val lux = event.values[0]
                lastKnownLux = lux

                if (AodManager.isAutoBrightnessEnabled(this)) {
                    val targetBrightness = calculateAutoBrightness(lux)
                    // Deadband threshold: Only touch WindowManager if brightness difference >= 0.02f
                    if (kotlin.math.abs(targetBrightness - lastAppliedBrightness) >= 0.02f) {
                        lastAppliedBrightness = targetBrightness
                        try {
                            val lp = window.attributes
                            lp.screenBrightness = targetBrightness
                            window.attributes = lp
                        } catch (e: Exception) {
                            // Ignore
                        }
                    }
                }

                // ULTRA-LOW POWER: Unregister light sensor immediately once lux reading is captured!
                // Eliminates continuous 5-10 Hz sensor HAL wakeups and CPU interrupts.
                try {
                    sensorManager?.unregisterListener(this, lightSensor)
                } catch (e: Exception) {
                    // Ignore
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun registerDismissReceiver() {
        dismissReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == AodLockService.ACTION_DISMISS_AOD ||
                    intent?.action == Intent.ACTION_USER_PRESENT) {
                    finish()
                }
            }
        }
        val filter = IntentFilter().apply {
            addAction(AodLockService.ACTION_DISMISS_AOD)
            addAction(Intent.ACTION_USER_PRESENT)
        }
        ContextCompat.registerReceiver(
            this,
            dismissReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    private fun unlockDeviceAndExit() {
        // ponytail: finish AodActivity to seamlessly reveal native Android Lock Screen (NOT password prompt)
        finish()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            // Count with hardware volume rocker (blind pocket counting)
            if (!isPocketActive.value) {
                val action = currentDhikrIncrementAction
                val target = currentDhikrTarget
                val dhikrIdx = currentDhikrIndex
                val repository = MuhasibApplication.instance.repository
                lifecycleScope.launch {
                    val current = repository.getSobrietyStateDirect()
                    val currentCount = when (dhikrIdx) {
                        0 -> current?.istighfarTodayCount ?: 0
                        1 -> current?.subhanAllahTodayCount ?: 0
                        2 -> current?.alhamdulillahTodayCount ?: 0
                        3 -> current?.allahuAkbarTodayCount ?: 0
                        4 -> current?.laIlahaIllallahTodayCount ?: 0
                        else -> current?.istighfarTodayCount ?: 0
                    }
                    val nextCount = currentCount + 1
                    if (action != null) {
                        action.invoke()
                    } else {
                        repository.incrementIstighfarCount()
                        WidgetUpdateHelper.updateAllWidgets(this@AodActivity)
                    }
                    val profile = AodManager.getMisbahaHapticProfile(this@AodActivity)
                    HapticFeedbackUtil.performMisbahaFeedback(this@AodActivity, profile, nextCount, target)
                }
                resetTap10sTimer()
            }
            return true // Suppress Android system volume overlay
        }
        return super.onKeyDown(keyCode, event)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        @Suppress("DEPRECATION")
        super.onBackPressed()
        // Unlock on back press
        unlockDeviceAndExit()
    }

    override fun onDestroy() {
        tap10sJob?.cancel()
        pocketJob?.cancel()
        try {
            sensorManager?.unregisterListener(this)
        } catch (e: Exception) {
            // Ignore
        }
        dismissReceiver?.let {
            try {
                unregisterReceiver(it)
            } catch (e: Exception) {
                // Ignore
            }
            dismissReceiver = null
        }
        if (activeInstance == this) {
            activeInstance = null
        }
        isAodResumed = false
        super.onDestroy()
    }
}
