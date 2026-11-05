package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.MuhasibApplication
import com.example.R
import com.example.data.model.DhikrCategory
import com.example.data.model.DhikrItem
import com.example.util.IslamicCalendarUtil
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

object WidgetUpdateHelper {

    fun updateAllWidgets(context: Context) {
        val app = MuhasibApplication.instance
        app.applicationScope.launch {
            try {
                val prayerLog = app.repository.getTodayPrayerLog().firstOrNull()
                val sobrietyLog = app.repository.getSobrietyState().firstOrNull()

                // ponytail: dynamically query current time period's Adhkar (Morning/Evening/Post-Salah/Sleep)
                val currentCategory = IslamicCalendarUtil.getCurrentTimeDhikrCategory()
                val dhikrList = app.repository.getDhikrByCategoryDirect(currentCategory.name)
                val activeDhikr = dhikrList.firstOrNull { !it.isCompleted }
                val completedCount = dhikrList.count { it.isCompleted }
                val totalCount = dhikrList.size

                updatePrayerWidgets(context, prayerLog)
                updateDynamicDhikrWidgets(
                    context = context,
                    category = currentCategory,
                    activeDhikr = activeDhikr,
                    totalInCat = totalCount,
                    completedInCat = completedCount,
                    fallbackCount = sobrietyLog?.istighfarTodayCount ?: 0,
                    fallbackTarget = sobrietyLog?.istighfarDailyTarget ?: 100
                )
                updateSobrietyWidgets(context, sobrietyLog?.cleanDaysCount ?: 0, sobrietyLog?.workoutCompletedToday ?: false, sobrietyLog?.coldShowerToday ?: false)
                updateQuoteWidgets(context)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun updatePrayerWidgets(context: Context, prayerLog: com.example.data.model.PrayerLog?) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val componentName = ComponentName(context, PrayerAppWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
        if (appWidgetIds.isEmpty()) return

        val nextPrayer = IslamicCalendarUtil.getNextPrayer()
        val hijriDate = IslamicCalendarUtil.getFormattedHijriDate()

        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_prayer)

            views.setTextViewText(R.id.tv_widget_hijri_date, hijriDate)
            views.setTextViewText(R.id.tv_widget_next_prayer_title, "الصلاة القادمة: ${nextPrayer.arabicName}")
            views.setTextViewText(R.id.tv_widget_next_prayer_time, "${nextPrayer.timeFormatted} • خلال ${nextPrayer.timeRemainingFormatted}")

            // Update 5 prayer dots
            views.setImageViewResource(
                R.id.dot_fajr,
                if (prayerLog?.fajrDone == true) R.drawable.widget_dot_green else R.drawable.widget_dot_gray
            )
            views.setImageViewResource(
                R.id.dot_dhuhr,
                if (prayerLog?.dhuhrDone == true) R.drawable.widget_dot_green else R.drawable.widget_dot_gray
            )
            views.setImageViewResource(
                R.id.dot_asr,
                if (prayerLog?.asrDone == true) R.drawable.widget_dot_green else R.drawable.widget_dot_gray
            )
            views.setImageViewResource(
                R.id.dot_maghrib,
                if (prayerLog?.maghribDone == true) R.drawable.widget_dot_green else R.drawable.widget_dot_gray
            )
            views.setImageViewResource(
                R.id.dot_isha,
                if (prayerLog?.ishaDone == true) R.drawable.widget_dot_green else R.drawable.widget_dot_gray
            )

            // Open App on Click
            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                1101,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_prayer_root, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    // ponytail: context-aware dynamic adhkar widget renderer
    fun updateDynamicDhikrWidgets(
        context: Context,
        category: DhikrCategory,
        activeDhikr: DhikrItem?,
        totalInCat: Int,
        completedInCat: Int,
        fallbackCount: Int,
        fallbackTarget: Int
    ) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val componentName = ComponentName(context, DhikrAppWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
        if (appWidgetIds.isEmpty()) return

        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_dhikr)

            // Open app Intent (wired to header title)
            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingOpen = PendingIntent.getActivity(
                context,
                1103,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.tv_widget_dhikr_title, pendingOpen)

            if (activeDhikr != null) {
                views.setTextViewText(R.id.tv_widget_dhikr_title, category.displayName)
                views.setTextViewText(R.id.tv_widget_dhikr_target, "الذكر ${completedInCat + 1} من $totalInCat")
                views.setTextViewText(R.id.tv_widget_dhikr_arabic, activeDhikr.arabic)
                views.setTextViewText(R.id.tv_widget_dhikr_count, "${activeDhikr.currentCount} / ${activeDhikr.targetCount}")
                views.setTextViewText(R.id.btn_widget_increment_dhikr, "+1")
                views.setTextViewText(R.id.btn_widget_done_dhikr, "تم ✓")

                // Tap "+1" button
                val incIntent = Intent(context, DhikrWidgetActionReceiver::class.java).apply {
                    action = DhikrWidgetActionReceiver.ACTION_INCREMENT_DHIKR
                    putExtra(DhikrWidgetActionReceiver.EXTRA_DHIKR_ID, activeDhikr.id)
                }
                val pendingInc = PendingIntent.getBroadcast(
                    context,
                    1200 + activeDhikr.id,
                    incIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.btn_widget_increment_dhikr, pendingInc)

                // Tap "تم ✓" button or card body (mark done and advance)
                val doneIntent = Intent(context, DhikrWidgetActionReceiver::class.java).apply {
                    action = DhikrWidgetActionReceiver.ACTION_MARK_DONE_AND_NEXT
                    putExtra(DhikrWidgetActionReceiver.EXTRA_DHIKR_ID, activeDhikr.id)
                }
                val pendingDone = PendingIntent.getBroadcast(
                    context,
                    1300 + activeDhikr.id,
                    doneIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.btn_widget_done_dhikr, pendingDone)
                views.setOnClickPendingIntent(R.id.tv_widget_dhikr_arabic, pendingDone)
                views.setOnClickPendingIntent(R.id.ll_widget_dhikr_body, pendingDone)
                views.setOnClickPendingIntent(R.id.widget_dhikr_root, pendingDone)
            } else if (totalInCat > 0 && completedInCat >= totalInCat) {
                // Category Completed state
                views.setTextViewText(R.id.tv_widget_dhikr_title, "${category.displayName} ✓")
                views.setTextViewText(R.id.tv_widget_dhikr_target, "مكتمل $completedInCat/$totalInCat")
                views.setTextViewText(R.id.tv_widget_dhikr_arabic, "ما شاء الله! تمت قراءة جميع أذكار ${category.displayName} بنجاح. اضغط لإعادة القراءة.")
                views.setTextViewText(R.id.tv_widget_dhikr_count, "تقبل الله")
                views.setTextViewText(R.id.btn_widget_increment_dhikr, "↻")
                views.setTextViewText(R.id.btn_widget_done_dhikr, "إعادة")

                // Reset category button
                val resetIntent = Intent(context, DhikrWidgetActionReceiver::class.java).apply {
                    action = DhikrWidgetActionReceiver.ACTION_RESET_CATEGORY
                    putExtra(DhikrWidgetActionReceiver.EXTRA_CATEGORY, category.name)
                }
                val pendingReset = PendingIntent.getBroadcast(
                    context,
                    1400,
                    resetIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.btn_widget_increment_dhikr, pendingReset)
                views.setOnClickPendingIntent(R.id.btn_widget_done_dhikr, pendingReset)
                views.setOnClickPendingIntent(R.id.tv_widget_dhikr_arabic, pendingReset)
                views.setOnClickPendingIntent(R.id.ll_widget_dhikr_body, pendingReset)
                views.setOnClickPendingIntent(R.id.widget_dhikr_root, pendingReset)
            } else {
                // Fallback state
                views.setTextViewText(R.id.tv_widget_dhikr_title, "خَاتَمُ الاسْتِغْفَارِ")
                views.setTextViewText(R.id.tv_widget_dhikr_target, "الهدف: $fallbackTarget")
                views.setTextViewText(R.id.tv_widget_dhikr_arabic, "أَسْتَغْفِرُ اللَّهَ الَّذِي لَا إِلَهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ وَأَتُوبُ إِلَيْهِ")
                views.setTextViewText(R.id.tv_widget_dhikr_count, "$fallbackCount / $fallbackTarget")
                views.setTextViewText(R.id.btn_widget_increment_dhikr, "+1")
                views.setTextViewText(R.id.btn_widget_done_dhikr, "تم ✓")

                val incIntent = Intent(context, DhikrWidgetActionReceiver::class.java).apply {
                    action = DhikrWidgetActionReceiver.ACTION_INCREMENT_DHIKR
                }
                val pendingInc = PendingIntent.getBroadcast(
                    context,
                    1102,
                    incIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.btn_widget_increment_dhikr, pendingInc)
                views.setOnClickPendingIntent(R.id.btn_widget_done_dhikr, pendingInc)
                views.setOnClickPendingIntent(R.id.tv_widget_dhikr_arabic, pendingInc)
                views.setOnClickPendingIntent(R.id.ll_widget_dhikr_body, pendingInc)
                views.setOnClickPendingIntent(R.id.widget_dhikr_root, pendingInc)
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    fun updateDhikrWidgets(context: Context, currentCount: Int, targetCount: Int) {
        val currentCategory = IslamicCalendarUtil.getCurrentTimeDhikrCategory()
        updateDynamicDhikrWidgets(
            context = context,
            category = currentCategory,
            activeDhikr = null,
            totalInCat = 0,
            completedInCat = 0,
            fallbackCount = currentCount,
            fallbackTarget = targetCount
        )
    }

    fun updateSobrietyWidgets(context: Context, cleanDays: Int, workoutDone: Boolean, coldShowerDone: Boolean) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val componentName = ComponentName(context, SobrietyAppWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
        if (appWidgetIds.isEmpty()) return

        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_sobriety)

            views.setTextViewText(R.id.tv_widget_streak_days, "$cleanDays يوماً")
            views.setTextViewText(
                R.id.tv_widget_workout_status,
                if (workoutDone) "تمرين: مكتمل ✓" else "تمرين: لم يكتمل"
            )
            views.setTextViewText(
                R.id.tv_widget_shower_status,
                if (coldShowerDone) "دش بارد: مكتمل ✓" else "دش بارد: لم يكتمل"
            )

            // Direct Emergency Panic Action
            val panicIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("EXTRA_TRIGGER_PANIC", true)
            }
            val pendingPanic = PendingIntent.getActivity(
                context,
                1104,
                panicIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_widget_panic, pendingPanic)

            // Open app on body click
            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingOpen = PendingIntent.getActivity(
                context,
                1105,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_sobriety_root, pendingOpen)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    fun updateQuoteWidgets(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val componentName = ComponentName(context, QuoteAppWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
        if (appWidgetIds.isEmpty()) return

        val quote = com.example.util.QuotePreferences.getPinnedQuote(context)

        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_quote)
            views.setTextViewText(R.id.tv_widget_quote_title, quote.title)
            views.setTextViewText(R.id.tv_widget_quote_content, quote.content)
            views.setTextViewText(R.id.tv_widget_quote_source, quote.bookTitle)

            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("route", "books")
            }
            val pendingOpen = PendingIntent.getActivity(
                context,
                1106,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_quote_root, pendingOpen)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
