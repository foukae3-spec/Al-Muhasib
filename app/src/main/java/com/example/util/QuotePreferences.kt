package com.example.util

import android.content.Context

data class ActiveQuote(
    val title: String,
    val content: String,
    val bookTitle: String,
    val timestamp: Long,
    val bookId: String = "ibn_qayyim_daa_dawaa",
    val author: String = "الإمام ابن قيم الجوزية",
    val pageNumber: Int = 1,
    val dailyChunkIndex: Int = 1,
    val dailyTargetChunks: Int = 5
)

object QuotePreferences {
    private const val PREFS_NAME = "muhasib_quote_prefs"
    private const val KEY_TITLE = "pinned_quote_title"
    private const val KEY_CONTENT = "pinned_quote_content"
    private const val KEY_BOOK = "pinned_quote_book"
    private const val KEY_TIMESTAMP = "pinned_quote_timestamp"
    private const val KEY_ACTIVE_BOOK_ID = "active_reading_book_id"
    private const val KEY_AUTHOR = "pinned_quote_author"
    private const val KEY_PAGE_NUMBER = "pinned_quote_page_num"
    private const val KEY_DAILY_CHUNK_INDEX = "pinned_quote_chunk_idx"
    private const val KEY_DAILY_TARGET = "pinned_quote_daily_target"

    fun getActiveBookId(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_ACTIVE_BOOK_ID, "ibn_qayyim_daa_dawaa") ?: "ibn_qayyim_daa_dawaa"
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
        val bookId = prefs.getString(KEY_ACTIVE_BOOK_ID, "ibn_qayyim_daa_dawaa") ?: "ibn_qayyim_daa_dawaa"
        val author = prefs.getString(KEY_AUTHOR, "الإمام ابن قيم الجوزية") ?: "الإمام ابن قيم الجوزية"
        val pageNum = prefs.getInt(KEY_PAGE_NUMBER, 1)
        val chunkIdx = prefs.getInt(KEY_DAILY_CHUNK_INDEX, 1)
        val target = prefs.getInt(KEY_DAILY_TARGET, 5)

        if (title != null && content != null) {
            return ActiveQuote(
                title = title,
                content = content,
                bookTitle = book ?: "الداء والدواء",
                timestamp = time,
                bookId = bookId,
                author = author,
                pageNumber = pageNum,
                dailyChunkIndex = chunkIdx,
                dailyTargetChunks = target
            )
        }

        // Default foundational quote (ابن القيم)
        return ActiveQuote(
            title = "حقيقة المجاهدة",
            content = "الصبر عن الشهوة أيسر من الصبر على ما توجبه الشهوة؛ فإنها توجب ألماً وحسرة وتضيع وقتاً شريفاً لا يعوض.",
            bookTitle = "الداء والدواء",
            timestamp = System.currentTimeMillis(),
            bookId = "ibn_qayyim_daa_dawaa",
            author = "الإمام ابن قيم الجوزية",
            pageNumber = 1,
            dailyChunkIndex = 1,
            dailyTargetChunks = 5
        )
    }

    fun setPinnedQuote(
        context: Context,
        title: String,
        content: String,
        bookTitle: String,
        bookId: String = "ibn_qayyim_daa_dawaa",
        author: String = "الإمام ابن قيم الجوزية",
        pageNumber: Int = 1,
        dailyChunkIndex: Int = 1,
        dailyTargetChunks: Int = 5
    ) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_TITLE, title)
            .putString(KEY_CONTENT, content)
            .putString(KEY_BOOK, bookTitle)
            .putString(KEY_ACTIVE_BOOK_ID, bookId)
            .putString(KEY_AUTHOR, author)
            .putInt(KEY_PAGE_NUMBER, pageNumber)
            .putInt(KEY_DAILY_CHUNK_INDEX, dailyChunkIndex)
            .putInt(KEY_DAILY_TARGET, dailyTargetChunks)
            .putLong(KEY_TIMESTAMP, System.currentTimeMillis())
            .apply()
    }
}
