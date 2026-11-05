package com.example.util

import android.content.Context

data class ActiveQuote(
    val title: String,
    val content: String,
    val bookTitle: String,
    val timestamp: Long
)

object QuotePreferences {
    private const val PREFS_NAME = "muhasib_quote_prefs"
    private const val KEY_TITLE = "pinned_quote_title"
    private const val KEY_CONTENT = "pinned_quote_content"
    private const val KEY_BOOK = "pinned_quote_book"
    private const val KEY_TIMESTAMP = "pinned_quote_timestamp"
    private const val KEY_ACTIVE_BOOK_ID = "active_reading_book_id"

    fun getActiveBookId(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_ACTIVE_BOOK_ID, "al_daa_wal_dawaa") ?: "al_daa_wal_dawaa"
    }

    fun setActiveBookId(context: Context, bookId: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_ACTIVE_BOOK_ID, bookId).apply()
    }

    fun getPinnedQuote(context: Context): ActiveQuote {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val title = prefs.getString(KEY_TITLE, null)
        val content = prefs.getString(KEY_CONTENT, null)
        val book = prefs.getString(KEY_BOOK, null)
        val time = prefs.getLong(KEY_TIMESTAMP, 0L)

        if (title != null && content != null) {
            return ActiveQuote(
                title = title,
                content = content,
                bookTitle = book ?: "الفوائد",
                timestamp = time
            )
        }

        // Default foundational quote (ابن القيم)
        return ActiveQuote(
            title = "حقيقة المجاهدة",
            content = "الصبر عن الشهوة أيسر من الصبر على ما توجبه الشهوة؛ فإنها توجب ألماً وحسرة وتضيع وقتاً شريفاً لا يعوض.",
            bookTitle = "الفوائد — ابن القيم",
            timestamp = System.currentTimeMillis()
        )
    }

    fun setPinnedQuote(context: Context, title: String, content: String, bookTitle: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_TITLE, title)
            .putString(KEY_CONTENT, content)
            .putString(KEY_BOOK, bookTitle)
            .putLong(KEY_TIMESTAMP, System.currentTimeMillis())
            .apply()
    }
}
