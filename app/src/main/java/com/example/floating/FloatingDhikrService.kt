package com.example.floating

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.MuhasibApplication
import com.example.R
import com.example.aod.AodManager
import com.example.data.model.DhikrItem
import com.example.util.HapticFeedbackUtil
import com.example.util.IslamicCalendarUtil
import com.example.widget.WidgetUpdateHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

// ponytail: floating Adhkar HUD using native WindowManager and TYPE_APPLICATION_OVERLAY
class FloatingDhikrService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var windowManager: WindowManager? = null
    private var floatingView: View? = null
    private var windowParams: WindowManager.LayoutParams? = null

    private var currentItem: DhikrItem? = null

    companion object {
        private var activeInstance: WeakReference<FloatingDhikrService>? = null
        const val SAFE_MARGIN_DP = 24

        fun notifyDhikrChanged(context: Context) {
            activeInstance?.get()?.refreshDhikr()
        }

        // ponytail: constrain drag coordinates within 24dp safe margin so card/bubble cannot slip under cutouts or navbars
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
    }

    override fun onCreate() {
        super.onCreate()
        activeInstance = WeakReference(this)
        startForegroundNotification()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            stopSelf()
            return
        }

        initFloatingWindow()
        refreshDhikr()
    }

    private fun startForegroundNotification() {
        val openAppIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification: Notification = NotificationCompat.Builder(this, MuhasibApplication.CHANNEL_ID_AOD_SERVICE)
            .setContentTitle("النافذة العائمة للأذكار — المُحَاسِب")
            .setContentText("الأذكار العائمة فوق التطبيقات نشطة وتعمل الآن")
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
            startForeground(1008, notification, foregroundServiceType)
        } else {
            startForeground(1008, notification)
        }
    }

    private fun initFloatingWindow() {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as? WindowManager
        val inflater = LayoutInflater.from(this)
        floatingView = inflater.inflate(R.layout.layout_floating_dhikr, null)

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val metrics = resources.displayMetrics
        val marginPx = (SAFE_MARGIN_DP * metrics.density).toInt()
        val initialWidth = (300 * metrics.density).toInt()
        val initialHeight = (180 * metrics.density).toInt()
        val (initX, initY) = clampToSafeBounds(
            targetX = marginPx,
            targetY = marginPx + 160,
            viewWidth = initialWidth,
            viewHeight = initialHeight,
            screenWidth = metrics.widthPixels,
            screenHeight = metrics.heightPixels,
            marginPx = marginPx
        )
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = initX
            y = initY
        }
        windowParams = params

        val cardContainer = floatingView?.findViewById<LinearLayout>(R.id.floating_card_container)
        val bubbleContainer = floatingView?.findViewById<FrameLayout>(R.id.floating_bubble_container)
        val header = floatingView?.findViewById<LinearLayout>(R.id.floating_header)
        val btnMinimize = floatingView?.findViewById<TextView>(R.id.btn_floating_minimize)
        val btnClose = floatingView?.findViewById<TextView>(R.id.btn_floating_close)
        val btnCount = floatingView?.findViewById<Button>(R.id.btn_floating_count)
        val btnNext = floatingView?.findViewById<Button>(R.id.btn_floating_next)
        val tvArabic = floatingView?.findViewById<TextView>(R.id.tv_floating_arabic)

        // Dragging handler for expanded card header
        attachDragListener(header) {
            // Nothing extra on single tap of header
        }

        // Dragging & clicking handler for minimized bubble
        attachDragListener(bubbleContainer) {
            // ponytail: expand to card and re-clamp bounds so expanded 300dp card cannot be pushed off-screen
            bubbleContainer?.visibility = View.GONE
            cardContainer?.visibility = View.VISIBLE
            val currentMetrics = resources.displayMetrics
            val currentMargin = (SAFE_MARGIN_DP * currentMetrics.density).toInt()
            val cardWidth = (300 * currentMetrics.density).toInt()
            val (clampedX, clampedY) = clampToSafeBounds(
                targetX = params.x,
                targetY = params.y,
                viewWidth = cardWidth,
                viewHeight = floatingView?.height?.takeIf { it > 0 } ?: (180 * currentMetrics.density).toInt(),
                screenWidth = currentMetrics.widthPixels,
                screenHeight = currentMetrics.heightPixels,
                marginPx = currentMargin
            )
            params.x = clampedX
            params.y = clampedY
            try {
                windowManager?.updateViewLayout(floatingView, params)
            } catch (e: Exception) {
                // Ignore
            }
        }

        // Action when user taps the Dhikr text directly: counts or completes to next
        val onDhikrCardTapped: () -> Unit = {
            val item = currentItem
            if (item != null) {
                if (item.currentCount + 1 >= item.targetCount) {
                    HapticFeedbackUtil.performCompletionRumble(this)
                    serviceScope.launch {
                        MuhasibApplication.instance.repository.markDhikrDone(item.id)
                        refreshDhikr()
                        WidgetUpdateHelper.updateAllWidgets(this@FloatingDhikrService)
                    }
                } else {
                    HapticFeedbackUtil.performDhikrTick(this)
                    serviceScope.launch {
                        MuhasibApplication.instance.repository.incrementDhikr(item.id)
                        refreshDhikr()
                        WidgetUpdateHelper.updateAllWidgets(this@FloatingDhikrService)
                    }
                }
            } else {
                // Category completed: tap resets to recite again
                HapticFeedbackUtil.performLightTick(this)
                serviceScope.launch {
                    val cat = IslamicCalendarUtil.getCurrentTimeDhikrCategory()
                    MuhasibApplication.instance.repository.resetCategoryDhikr(cat.name)
                    refreshDhikr()
                    WidgetUpdateHelper.updateAllWidgets(this@FloatingDhikrService)
                }
            }
        }
        tvArabic?.setOnClickListener { onDhikrCardTapped() }

        // Minimize action
        btnMinimize?.setOnClickListener {
            HapticFeedbackUtil.performLightTick(this)
            cardContainer?.visibility = View.GONE
            bubbleContainer?.visibility = View.VISIBLE
        }

        // Close action
        btnClose?.setOnClickListener {
            HapticFeedbackUtil.performLightTick(this)
            AodManager.setFloatingDhikrEnabled(this, false)
            stopSelf()
        }

        // Tap "+1" count
        btnCount?.setOnClickListener {
            val item = currentItem ?: return@setOnClickListener
            val profile = AodManager.getMisbahaHapticProfile(this)
            HapticFeedbackUtil.performMisbahaFeedback(this, profile, item.currentCount + 1, item.targetCount)
            serviceScope.launch {
                MuhasibApplication.instance.repository.incrementDhikr(item.id)
                refreshDhikr()
                WidgetUpdateHelper.updateAllWidgets(this@FloatingDhikrService)
            }
        }

        // Tap "Next" button (or restart if completed)
        btnNext?.setOnClickListener {
            val item = currentItem
            if (item != null) {
                HapticFeedbackUtil.performCompletionRumble(this)
                serviceScope.launch {
                    MuhasibApplication.instance.repository.markDhikrDone(item.id)
                    refreshDhikr()
                    WidgetUpdateHelper.updateAllWidgets(this@FloatingDhikrService)
                }
            } else {
                HapticFeedbackUtil.performLightTick(this)
                serviceScope.launch {
                    val cat = IslamicCalendarUtil.getCurrentTimeDhikrCategory()
                    MuhasibApplication.instance.repository.resetCategoryDhikr(cat.name)
                    refreshDhikr()
                    WidgetUpdateHelper.updateAllWidgets(this@FloatingDhikrService)
                }
            }
        }

        try {
            windowManager?.addView(floatingView, params)
        } catch (e: Exception) {
            stopSelf()
        }
    }

    private fun attachDragListener(view: View?, onClick: () -> Unit) {
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        var hasMoved = false

        view?.setOnTouchListener { _, event ->
            val params = windowParams ?: return@setOnTouchListener false
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    hasMoved = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - initialTouchX).toInt()
                    val dy = (event.rawY - initialTouchY).toInt()
                    if (Math.abs(dx) > 10 || Math.abs(dy) > 10) {
                        hasMoved = true
                        val metrics = resources.displayMetrics
                        val marginPx = (SAFE_MARGIN_DP * metrics.density).toInt()
                        val vWidth = floatingView?.width?.takeIf { it > 0 } ?: (view?.width?.takeIf { it > 0 } ?: (200 * metrics.density).toInt())
                        val vHeight = floatingView?.height?.takeIf { it > 0 } ?: (view?.height?.takeIf { it > 0 } ?: (150 * metrics.density).toInt())
                        // ponytail: constrain drag coordinates within 24dp safe margin
                        val (clampedX, clampedY) = clampToSafeBounds(
                            targetX = initialX + dx,
                            targetY = initialY + dy,
                            viewWidth = vWidth,
                            viewHeight = vHeight,
                            screenWidth = metrics.widthPixels,
                            screenHeight = metrics.heightPixels,
                            marginPx = marginPx
                        )
                        params.x = clampedX
                        params.y = clampedY
                        windowManager?.updateViewLayout(floatingView, params)
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!hasMoved) {
                        onClick()
                    }
                    true
                }
                else -> false
            }
        }
    }

    fun refreshDhikr() {
        serviceScope.launch {
            try {
                val app = MuhasibApplication.instance
                val category = IslamicCalendarUtil.getCurrentTimeDhikrCategory()
                val items = app.repository.getDhikrByCategoryDirect(category.name)
                val active = items.firstOrNull { !it.isCompleted }
                val completedCount = items.count { it.isCompleted }
                val totalCount = items.size

                currentItem = active

                floatingView?.let { view ->
                    val tvCategory = view.findViewById<TextView>(R.id.tv_floating_category)
                    val tvProgress = view.findViewById<TextView>(R.id.tv_floating_progress)
                    val tvArabic = view.findViewById<TextView>(R.id.tv_floating_arabic)
                    val tvCount = view.findViewById<TextView>(R.id.tv_floating_count)
                    val tvBadge = view.findViewById<TextView>(R.id.tv_bubble_badge)
                    val btnCount = view.findViewById<Button>(R.id.btn_floating_count)
                    val btnNext = view.findViewById<Button>(R.id.btn_floating_next)

                    if (active != null) {
                        tvCategory?.text = "${category.displayName} 📿"
                        tvProgress?.text = "${completedCount + 1} / $totalCount"
                        tvArabic?.text = active.arabic
                        tvCount?.text = "${active.currentCount} / ${active.targetCount}"
                        tvBadge?.text = "${completedCount + 1}"
                        btnNext?.text = "التالي ❯"
                        btnCount?.visibility = View.VISIBLE
                    } else {
                        tvCategory?.text = "${category.displayName} ✓"
                        tvProgress?.text = "$totalCount / $totalCount"
                        tvArabic?.text = "ما شاء الله! تمت جميع أذكار ${category.displayName}. اضغط لإعادة القراءة."
                        tvCount?.text = "مكتمل ✓"
                        tvBadge?.text = "✓"
                        btnNext?.text = "إعادة ↻"
                        btnCount?.visibility = View.GONE
                    }
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // ponytail: re-clamp HUD within safe bounds when screen rotates between portrait and landscape
        val params = windowParams ?: return
        val metrics = resources.displayMetrics
        val marginPx = (SAFE_MARGIN_DP * metrics.density).toInt()
        val vWidth = floatingView?.width?.takeIf { it > 0 } ?: (200 * metrics.density).toInt()
        val vHeight = floatingView?.height?.takeIf { it > 0 } ?: (150 * metrics.density).toInt()
        val (clampedX, clampedY) = clampToSafeBounds(
            targetX = params.x,
            targetY = params.y,
            viewWidth = vWidth,
            viewHeight = vHeight,
            screenWidth = metrics.widthPixels,
            screenHeight = metrics.heightPixels,
            marginPx = marginPx
        )
        params.x = clampedX
        params.y = clampedY
        try {
            windowManager?.updateViewLayout(floatingView, params)
        } catch (e: Exception) {
            // Ignore
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        refreshDhikr()
        return START_STICKY
    }

    override fun onDestroy() {
        activeInstance = null
        serviceScope.cancel()
        floatingView?.let {
            try {
                windowManager?.removeView(it)
            } catch (e: Exception) {
                // Ignore
            }
        }
        floatingView = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
