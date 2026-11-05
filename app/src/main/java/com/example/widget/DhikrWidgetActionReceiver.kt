package com.example.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.MuhasibApplication
import com.example.util.HapticFeedbackUtil
import kotlinx.coroutines.launch

class DhikrWidgetActionReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_INCREMENT_DHIKR = "com.example.widget.ACTION_INCREMENT_DHIKR"
        const val ACTION_MARK_DONE_AND_NEXT = "com.example.widget.ACTION_MARK_DONE_AND_NEXT"
        const val ACTION_RESET_CATEGORY = "com.example.widget.ACTION_RESET_CATEGORY"
        const val EXTRA_DHIKR_ID = "extra_dhikr_id"
        const val EXTRA_CATEGORY = "extra_category"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        val pendingResult = goAsync()
        val app = (context.applicationContext as? MuhasibApplication) ?: MuhasibApplication.instance
        val action = intent.action

        app.applicationScope.launch {
            try {
                when (action) {
                    ACTION_INCREMENT_DHIKR -> {
                        HapticFeedbackUtil.performMechanicalClick(context)
                        val dhikrId = intent.getIntExtra(EXTRA_DHIKR_ID, -1)
                        if (dhikrId != -1) {
                            app.repository.incrementDhikr(dhikrId)
                        } else {
                            app.repository.incrementIstighfarCount()
                        }
                    }
                    ACTION_MARK_DONE_AND_NEXT -> {
                        HapticFeedbackUtil.performCompletionRumble(context)
                        val dhikrId = intent.getIntExtra(EXTRA_DHIKR_ID, -1)
                        if (dhikrId != -1) {
                            app.repository.markDhikrDone(dhikrId)
                        }
                    }
                    ACTION_RESET_CATEGORY -> {
                        HapticFeedbackUtil.performLightTick(context)
                        val category = intent.getStringExtra(EXTRA_CATEGORY)
                        if (!category.isNullOrBlank()) {
                            app.repository.resetCategoryDhikr(category)
                        }
                    }
                }
                WidgetUpdateHelper.updateAllWidgets(context)
                // ponytail: notify floating HUD if active
                try {
                    com.example.floating.FloatingDhikrService.notifyDhikrChanged(context)
                } catch (e: Exception) {
                    // Ignore
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
