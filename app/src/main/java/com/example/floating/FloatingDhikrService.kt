package com.example.floating

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.provider.Settings
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.util.HapticFeedbackUtil
import java.lang.ref.WeakReference

/**
 * FloatingDhikrService — Sovereign System Overlay HUD & Live Niyyah Session Guardian.
 *
 * Upgraded so that when the user opens any external third-party app through the Niyyah Gatekeeper:
 * 1. Displays a draggable glassmorphic pill at the top of the screen with:
 *    - Live Niyyah countdown timer (e.g. "⏱ YouTube (04:52) • Focused Task")
 *    - Interactive 1-tap Dhikr counter ("سبحان الله وبحمده • 33")
 * 2. When the Niyyah time-box hits 00:00, triggers a double haptic alert and pulls the user
 *    back to the Al-Muhasib Home Screen so they never fall into an infinite scroll loop.
 * 3. Maintains full backward compatibility with clampToSafeBounds and notifyDhikrChanged.
 */
class FloatingDhikrService : Service() {

    companion object {
        const val CHANNEL_ID = "muhasib_floating_hud_channel"
        const val NOTIFICATION_ID = 3301
        const val SAFE_MARGIN_DP = 24

        const val ACTION_START_HUD = "com.example.floating.ACTION_START_HUD"
        const val ACTION_START_NIYYAH_TIMER = "com.example.floating.ACTION_START_NIYYAH_TIMER"
        const val ACTION_STOP_HUD = "com.example.floating.ACTION_STOP_HUD"

        const val EXTRA_APP_LABEL = "extra_app_label"
        const val EXTRA_NIYYAH_TEXT = "extra_niyyah_text"
        const val EXTRA_DURATION_MINUTES = "extra_duration_minutes"

        private var activeInstance: WeakReference<FloatingDhikrService>? = null

        fun notifyDhikrChanged(context: Context) {
            activeInstance?.get()?.refreshDhikr()
        }

        /**
         * Constrains drag coordinates within marginPx so HUD cannot slip off-screen or under cutouts.
         */
        fun clampToSafeBounds(
            targetX: Int,
            targetY: Int,
            viewWidth: Int,
            viewHeight: Int,
            screenWidth: Int,
            screenHeight: Int,
            marginPx: Int
        ): Pair<Int, Int> {
            val minX = marginPx
            val minY = marginPx
            val maxX = (screenWidth - viewWidth - marginPx).coerceAtLeast(minX)
            val maxY = (screenHeight - viewHeight - marginPx).coerceAtLeast(minY)
            return Pair(
                targetX.coerceIn(minX, maxX),
                targetY.coerceIn(minY, maxY)
            )
        }

        private val PHRASES = listOf(
            "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ",
            "أَسْتَغْفِرُ اللَّهَ وَأَتُوبُ إِلَيْهِ",
            "لَا إِلَٰهَ إِلَّا اللَّهُ",
            "اللَّهُمَّ صَلِّ وَسَلِّمْ عَلَى نَبِيِّنَا مُحَمَّدٍ",
            "لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ"
        )
    }

    private var windowManager: WindowManager? = null
    private var rootOverlayView: View? = null
    private var niyyahCountdownTextView: TextView? = null
    private var dhikrTextView: TextView? = null
    private var countBadgeView: TextView? = null

    private var countDownTimer: CountDownTimer? = null
    private var sessionDhikrCount = 0
    private var currentPhraseIndex = 0
    private var activeAppLabel: String? = null
    private var activeNiyyahReason: String? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        activeInstance = WeakReference(this)
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildForegroundNotification("Sovereign Dhikr & Niyyah HUD Active"))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!Settings.canDrawOverlays(this)) {
            stopSelf()
            return START_NOT_STICKY
        }

        when (intent?.action) {
            ACTION_STOP_HUD -> {
                removeOverlayView()
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_START_NIYYAH_TIMER -> {
                val appLabel = intent.getStringExtra(EXTRA_APP_LABEL) ?: "App"
                val niyyahText = intent.getStringExtra(EXTRA_NIYYAH_TEXT) ?: "Focused Task"
                val minutes = intent.getIntExtra(EXTRA_DURATION_MINUTES, 5).coerceIn(1, 120)
                activeAppLabel = appLabel
                activeNiyyahReason = niyyahText
                ensureOverlayAttached()
                startNiyyahCountdown(appLabel, niyyahText, minutes)
            }
            else -> {
                ensureOverlayAttached()
            }
        }
        return START_STICKY
    }

    fun refreshDhikr() {
        currentPhraseIndex = (currentPhraseIndex + 1) % PHRASES.size
        dhikrTextView?.text = PHRASES[currentPhraseIndex]
    }

    private fun ensureOverlayAttached() {
        if (rootOverlayView != null) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            return
        }
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = dpToPx(38)
        }

        // Programmatic Heavenly iOS 26 Liquid Glass Pill Container
        val pillContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dpToPx(14), dpToPx(8), dpToPx(14), dpToPx(8))

            val glassBg = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dpToPx(24).toFloat()
                setColor(Color.parseColor("#E60E131E")) // 90% Obsidian Twilight Velvet
                setStroke(dpToPx(1), Color.parseColor("#4DE5C185")) // Champagne Gold Specular Rim
            }
            background = glassBg
            elevation = dpToPx(16).toFloat()
        }

        // Niyyah Live Session Countdown Badge
        niyyahCountdownTextView = TextView(this).apply {
            textSize = 11f
            setTextColor(Color.parseColor("#E5C185")) // Champagne Gold
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            visibility = View.GONE
            setPadding(0, 0, dpToPx(8), 0)
        }
        pillContainer.addView(niyyahCountdownTextView)

        // Dhikr Calligraphy Phrase
        dhikrTextView = TextView(this).apply {
            text = PHRASES[currentPhraseIndex]
            textSize = 13f
            setTextColor(Color.parseColor("#F7F4EC")) // Pearl Alabaster
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setPadding(dpToPx(4), 0, dpToPx(8), 0)
        }
        pillContainer.addView(dhikrTextView)

        // 1-Tap Interactive Counter Badge
        countBadgeView = TextView(this).apply {
            text = "0"
            textSize = 11f
            setTextColor(Color.parseColor("#0B0E16"))
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setPadding(dpToPx(8), dpToPx(2), dpToPx(8), dpToPx(2))

            val badgeBg = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dpToPx(12).toFloat()
                setColor(Color.parseColor("#E5C185"))
            }
            background = badgeBg
        }
        pillContainer.addView(countBadgeView)

        // Drag & Tap Physics Touch Listener
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        var isDragging = false

        pillContainer.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isDragging = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - initialTouchX).toInt()
                    val dy = (event.rawY - initialTouchY).toInt()
                    if (kotlin.math.abs(dx) > 8 || kotlin.math.abs(dy) > 8) {
                        isDragging = true
                        val metrics = resources.displayMetrics
                        val (clampedX, clampedY) = clampToSafeBounds(
                            targetX = initialX + dx,
                            targetY = initialY + dy,
                            viewWidth = pillContainer.width.coerceAtLeast(1),
                            viewHeight = pillContainer.height.coerceAtLeast(1),
                            screenWidth = metrics.widthPixels,
                            screenHeight = metrics.heightPixels,
                            marginPx = dpToPx(SAFE_MARGIN_DP)
                        )
                        params.x = clampedX
                        params.y = clampedY
                        runCatching { windowManager?.updateViewLayout(pillContainer, params) }
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!isDragging) {
                        onPillTapped()
                    }
                    true
                }
                else -> false
            }
        }

        rootOverlayView = pillContainer
        runCatching { windowManager?.addView(rootOverlayView, params) }
    }

    private fun onPillTapped() {
        sessionDhikrCount++
        HapticFeedbackUtil.performTick(this)
        countBadgeView?.text = sessionDhikrCount.toString()
        if (sessionDhikrCount % 33 == 0) {
            currentPhraseIndex = (currentPhraseIndex + 1) % PHRASES.size
            dhikrTextView?.text = PHRASES[currentPhraseIndex]
            HapticFeedbackUtil.performHeavyClick(this)
        }
    }

    private fun startNiyyahCountdown(appLabel: String, niyyahText: String, durationMinutes: Int) {
        countDownTimer?.cancel()
        niyyahCountdownTextView?.visibility = View.VISIBLE

        val totalMillis = durationMinutes * 60_000L
        countDownTimer = object : CountDownTimer(totalMillis, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                val mins = (millisUntilFinished / 1000L) / 60L
                val secs = (millisUntilFinished / 1000L) % 60L
                niyyahCountdownTextView?.text =
                    "⏱ $appLabel (%02d:%02d) • \"%s\"".format(mins, secs, niyyahText.take(22))
                if (mins == 0L && secs <= 30L) {
                    niyyahCountdownTextView?.setTextColor(Color.parseColor("#FF5252"))
                    HapticFeedbackUtil.performTick(this@FloatingDhikrService)
                }
            }

            override fun onFinish() {
                niyyahCountdownTextView?.text = "⚠️ Niyyah Time Expired for $appLabel — Returning to Mihrab"
                HapticFeedbackUtil.performHeavyClick(this@FloatingDhikrService)
                pullUserBackToMihrabHome(appLabel)
            }
        }.start()
    }

    private fun pullUserBackToMihrabHome(expiredAppLabel: String) {
        val homeIntent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("extra_niyyah_expired_app", expiredAppLabel)
        }
        runCatching { startActivity(homeIntent) }
    }

    private fun removeOverlayView() {
        countDownTimer?.cancel()
        countDownTimer = null
        rootOverlayView?.let { view ->
            runCatching { windowManager?.removeView(view) }
        }
        rootOverlayView = null
    }

    override fun onDestroy() {
        removeOverlayView()
        if (activeInstance?.get() === this) {
            activeInstance = null
        }
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Al-Muhasib Floating Guardian HUD",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Displays live Niyyah session countdown and floating Dhikr counter over apps"
            }
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            nm?.createNotificationChannel(channel)
        }
    }

    private fun buildForegroundNotification(contentText: String): Notification {
        val openHomeIntent = Intent(this, MainActivity::class.java)
        val pendingHome = PendingIntent.getActivity(
            this,
            0,
            openHomeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Al-Muhasib Life OS Guardian")
            .setContentText(contentText)
            .setContentIntent(pendingHome)
            .setOngoing(true)
            .build()
    }

    private fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            resources.displayMetrics
        ).toInt()
    }
}
