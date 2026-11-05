package com.example.library

import android.app.NotificationManager
import android.app.PendingIntent
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

/**
 * منسق إشعارات وِرد القراءة اليومي وتحديث العلامة المرجعية الموحدة (Unified Bookmark Cursor)
 */
object BookWirdNotificationWorker {
    private const val TAG = "BookWirdWorker"
    const val NOTIFICATION_ID = 7701

    fun dispatchNextWirdNotification(context: Context) {
        val app = context.applicationContext as? MuhasibApplication ?: return
        val repository = app.repository
        val scope = CoroutineScope(Dispatchers.IO)

        scope.launch {
            try {
                val activeBookId = QuotePreferences.getActiveBookId(context)
                val book = repository.getBookByIdDirect(activeBookId)
                    ?: IslamicBooksSeedData.getInitialBooks().first()
                val cursor = book.currentChunkCursor

                var chunk = repository.getChunkByIndexDirect(book.id, cursor)
                if (chunk == null) {
                    chunk = repository.getNextUnreadChunk(book.id)
                }
                if (chunk == null) {
                    val catalogEntry = OpenSourceBookCatalog.findEntry(book.id)
                    chunk = catalogEntry?.sampleChunks?.firstOrNull()
                        ?: IslamicBooksSeedData.getInitialChunks().find { it.bookId == book.id }
                        ?: IslamicBooksSeedData.getInitialChunks().first()
                }

                deliverNotification(
                    context = context,
                    bookTitle = book.title,
                    bookId = book.id,
                    chunkId = chunk.id,
                    chunkIndex = chunk.chunkIndex,
                    pageNumber = chunk.pageNumber,
                    title = chunk.title,
                    content = chunk.content
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to dispatch wird notification", e)
            }
        }
    }

    private fun deliverNotification(
        context: Context,
        bookTitle: String,
        bookId: String,
        chunkId: String,
        chunkIndex: Int,
        pageNumber: Int,
        title: String,
        content: String
    ) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        // Intent to open app reader
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("route", "books")
            putExtra("book_id", bookId)
            putExtra("page_number", pageNumber)
        }
        val pContentIntent = PendingIntent.getActivity(
            context,
            101,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Mark as read action (Advances unified bookmark cursor)
        val markReadIntent = Intent(context, BookNotificationReceiver::class.java).apply {
            action = BookNotificationReceiver.ACTION_MARK_READ
            putExtra(BookNotificationReceiver.EXTRA_BOOK_ID, bookId)
            putExtra(BookNotificationReceiver.EXTRA_CHUNK_ID, chunkId)
            putExtra(BookNotificationReceiver.EXTRA_CHUNK_INDEX, chunkIndex)
        }
        val pMarkReadIntent = PendingIntent.getBroadcast(
            context,
            102,
            markReadIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Pin to AOD action
        val pinIntent = Intent(context, BookNotificationReceiver::class.java).apply {
            action = BookNotificationReceiver.ACTION_PIN_QUOTE
            putExtra(BookNotificationReceiver.EXTRA_TITLE, title)
            putExtra(BookNotificationReceiver.EXTRA_CONTENT, content)
            putExtra(BookNotificationReceiver.EXTRA_BOOK_TITLE, bookTitle)
        }
        val pPinIntent = PendingIntent.getBroadcast(
            context,
            103,
            pinIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, MuhasibApplication.CHANNEL_ID_BOOK_WIRD)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("📖 وِرد العلم: $bookTitle (ص $pageNumber)")
            .setContentText(content)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("« $content »\n\n— $title")
                    .setBigContentTitle("📖 وِرد العلم: $bookTitle (ص $pageNumber)")
                    .setSummaryText("وِرد الصالح • صفحتان يومياً")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pContentIntent)
            .addAction(0, "✓ أتممتُها (تحديث العلامة)", pMarkReadIntent)
            .addAction(0, "📌 تثبيت بـ AOD", pPinIntent)
            .build()

        nm.notify(NOTIFICATION_ID, notification)
    }

    suspend fun advanceBookmark(context: Context, bookId: String) {
        val app = context.applicationContext as? MuhasibApplication ?: return
        app.repository.advanceBookChunkCursor(bookId)
    }
}
