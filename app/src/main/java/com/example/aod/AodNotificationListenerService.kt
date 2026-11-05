package com.example.aod

import android.app.Notification
import android.content.Context
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AodNotificationItem(
    val key: String,
    val packageName: String,
    val appName: String,
    val title: String,
    val text: String,
    val color: Color,
    val count: Int = 1,
    val postTime: Long = System.currentTimeMillis()
)

// ponytail: lightweight AOD notification listener tracking active notifications with brand colors
class AodNotificationListenerService : NotificationListenerService() {

    companion object {
        private val _notifications = MutableStateFlow<List<AodNotificationItem>>(emptyList())
        val notifications: StateFlow<List<AodNotificationItem>> = _notifications.asStateFlow()

        fun isNotificationAccessGranted(context: Context): Boolean {
            val enabledListeners = Settings.Secure.getString(
                context.contentResolver,
                "enabled_notification_listeners"
            ) ?: return false
            return enabledListeners.contains(context.packageName)
        }

        // ponytail: brand color map for popular communication & utility apps
        fun resolveAppColor(packageName: String): Color {
            return when {
                packageName.contains("whatsapp", ignoreCase = true) -> Color(0xFF25D366)
                packageName.contains("telegram", ignoreCase = true) -> Color(0xFF2AABEE)
                packageName.contains("messaging", ignoreCase = true) || packageName.contains("mms", ignoreCase = true) -> Color(0xFF3B82F6)
                packageName.contains("dialer", ignoreCase = true) || packageName.contains("phone", ignoreCase = true) -> Color(0xFF10B981)
                packageName.contains("mail", ignoreCase = true) || packageName.contains("gmail", ignoreCase = true) -> Color(0xFFEA4335)
                packageName.contains("instagram", ignoreCase = true) -> Color(0xFFE1306C)
                packageName.contains("twitter", ignoreCase = true) || packageName.contains("x.android", ignoreCase = true) -> Color(0xFF38BDF8)
                packageName.contains("muhasib", ignoreCase = true) || packageName.contains("example", ignoreCase = true) -> Color(0xFF10B981)
                else -> Color(0xFFF59E0B) // Warm Amber
            }
        }

        fun resolveAppName(packageName: String): String {
            return when {
                packageName.contains("whatsapp", ignoreCase = true) -> "واتساب"
                packageName.contains("telegram", ignoreCase = true) -> "تيليجرام"
                packageName.contains("messaging", ignoreCase = true) || packageName.contains("mms", ignoreCase = true) -> "رسائل"
                packageName.contains("dialer", ignoreCase = true) || packageName.contains("phone", ignoreCase = true) -> "مكالمات"
                packageName.contains("mail", ignoreCase = true) || packageName.contains("gmail", ignoreCase = true) -> "بريد"
                packageName.contains("instagram", ignoreCase = true) -> "إنستغرام"
                packageName.contains("twitter", ignoreCase = true) -> "إكس (تويتر)"
                packageName.contains("muhasib", ignoreCase = true) || packageName.contains("example", ignoreCase = true) -> "المُحَاسِب"
                else -> "تطبيق"
            }
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        refreshActiveNotifications()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        refreshActiveNotifications()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        refreshActiveNotifications()
    }

    private fun refreshActiveNotifications() {
        try {
            val active = activeNotifications ?: return
            val filtered = active.filter { sbn ->
                // Ignore ongoing background services or silent persistent notifications
                val isOngoing = (sbn.notification.flags and Notification.FLAG_ONGOING_EVENT) != 0
                val isGroupSummary = (sbn.notification.flags and Notification.FLAG_GROUP_SUMMARY) != 0
                !isOngoing && !isGroupSummary && sbn.packageName != packageName
            }

            val grouped = filtered.groupBy { it.packageName }
            val items = grouped.map { (pkg, sbns) ->
                val latest = sbns.maxByOrNull { it.postTime } ?: sbns.first()
                val extras = latest.notification.extras
                val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: resolveAppName(pkg)
                val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""

                AodNotificationItem(
                    key = latest.key ?: pkg,
                    packageName = pkg,
                    appName = resolveAppName(pkg),
                    title = title,
                    text = text,
                    color = resolveAppColor(pkg),
                    count = sbns.size,
                    postTime = latest.postTime
                )
            }
            _notifications.value = items
        } catch (_: Exception) {}
    }
}
