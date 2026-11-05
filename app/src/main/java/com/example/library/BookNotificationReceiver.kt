package com.example.library

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.MuhasibApplication
import com.example.R
import com.example.data.seed.IslamicBooksSeedData
import com.example.util.QuotePreferences
import com.example.widget.WidgetUpdateHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BookNotificationReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BookNotificationRx"
        const val NOTIFICATION_ID_WIRD = 7701

        const val ACTION_TRIGGER_WIRD = "com.example.action.TRIGGER_BOOK_WIRD"
        const val ACTION_MARK_READ = "com.example.action.BOOK_MARK_READ"
        const val ACTION_PIN_QUOTE = "com.example.action.BOOK_PIN_QUOTE"

        const val EXTRA_SLOT_INDEX = "extra_slot_index"
        const val EXTRA_BOOK_ID = "extra_book_id"
        const val EXTRA_CHUNK_ID = "extra_chunk_id"
        const val EXTRA_CHUNK_INDEX = "extra_chunk_index"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_CONTENT = "extra_content"
        const val EXTRA_BOOK_TITLE = "extra_book_title"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        Log.d(TAG, "Received action: $action")

        val app = context.applicationContext as? MuhasibApplication ?: return
        val repository = app.repository
        val scope = CoroutineScope(Dispatchers.IO)

        when (action) {
            ACTION_TRIGGER_WIRD -> {
                scope.launch {
                    val activeBookId = QuotePreferences.getActiveBookId(context)
                    val book = repository.getBookByIdDirect(activeBookId)
                        ?: IslamicBooksSeedData.getInitialBooks().first()
                    val cursor = book.currentChunkCursor

                    // Fetch next chunk at cursor or next unread
                    var chunk = repository.getChunkByIndexDirect(book.id, cursor)
                    if (chunk == null) {
                        chunk = repository.getNextUnreadChunk(book.id)
                    }
                    if (chunk == null) {
                        chunk = IslamicBooksSeedData.getInitialChunks().find { it.bookId == book.id }
                            ?: IslamicBooksSeedData.getInitialChunks().first()
                    }

                    deliverWirdNotification(context, book.title, book.id, chunk.id, chunk.chunkIndex, chunk.pageNumber, chunk.title, chunk.content, chunk.reflectionQuestion)
                }
            }

            ACTION_MARK_READ -> {
                val bookId = intent.getStringExtra(EXTRA_BOOK_ID) ?: return
                val chunkId = intent.getStringExtra(EXTRA_CHUNK_ID) ?: return
                val chunkIndex = intent.getIntExtra(EXTRA_CHUNK_INDEX, 0)

                scope.launch {
                    repository.markChunkRead(chunkId, true)
                    repository.updateBookCursor(bookId, chunkIndex + 1)

                    val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                    nm?.cancel(NOTIFICATION_ID_WIRD)

                    // Also schedule next cycle of alarms if needed
                    BookWirdScheduler.scheduleDailyWirdAlarms(context)
                    WidgetUpdateHelper.updateAllWidgets(context)
                }
            }

            ACTION_PIN_QUOTE -> {
                val title = intent.getStringExtra(EXTRA_TITLE) ?: "فائدة إيمانية"
                val content = intent.getStringExtra(EXTRA_CONTENT) ?: ""
                val bookTitle = intent.getStringExtra(EXTRA_BOOK_TITLE) ?: "وِرد العلم"

                QuotePreferences.setPinnedQuote(context, title, content, bookTitle)
                WidgetUpdateHelper.updateAllWidgets(context)
            }
        }
    }

    private fun deliverWirdNotification(
        context: Context,
        bookTitle: String,
        bookId: String,
        chunkId: String,
        chunkIndex: Int,
        pageNumber: Int,
        chunkTitle: String,
        content: String,
        reflectionQuestion: String?
    ) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        // Action 1: [✓ قرأتُ وحفظت الموضع]
        val markReadIntent = Intent(context, BookNotificationReceiver::class.java).apply {
            action = ACTION_MARK_READ
            putExtra(EXTRA_BOOK_ID, bookId)
            putExtra(EXTRA_CHUNK_ID, chunkId)
            putExtra(EXTRA_CHUNK_INDEX, chunkIndex)
        }
        val markReadPendingIntent = PendingIntent.getBroadcast(
            context,
            1001,
            markReadIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action 2: [📖 فتح في الكتاب]
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("route", "books")
            putExtra("bookId", bookId)
            putExtra("chunkIndex", chunkIndex)
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            1002,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action 3: [📌 تثبيت على الشاشة]
        val pinIntent = Intent(context, BookNotificationReceiver::class.java).apply {
            action = ACTION_PIN_QUOTE
            putExtra(EXTRA_TITLE, chunkTitle)
            putExtra(EXTRA_CONTENT, content)
            putExtra(EXTRA_BOOK_TITLE, bookTitle)
        }
        val pinPendingIntent = PendingIntent.getBroadcast(
            context,
            1003,
            pinIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val fullBody = buildString {
            append(content)
            if (!reflectionQuestion.isNullOrBlank()) {
                append("\n\n💡 سؤال التفكّر:\n")
                append(reflectionQuestion)
            }
        }

        val notification = NotificationCompat.Builder(context, MuhasibApplication.CHANNEL_ID_BOOK_WIRD)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("📚 وِرد العلم | $bookTitle (ص $pageNumber)")
            .setContentText(chunkTitle)
            .setStyle(NotificationCompat.BigTextStyle().bigText(fullBody).setSummaryText("شذرة اليوم"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(false)
            .setContentIntent(openAppPendingIntent)
            .addAction(0, "✓ قرأتُ وحفظت الموضع", markReadPendingIntent)
            .addAction(0, "📖 فتح في الكتاب", openAppPendingIntent)
            .addAction(0, "📌 تثبيت على الشاشة", pinPendingIntent)
            .build()

        nm.notify(NOTIFICATION_ID_WIRD, notification)
    }
}
