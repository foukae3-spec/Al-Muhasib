package com.example.hadith

import androidx.compose.runtime.Immutable

/**
 * Authentic Islamic Heritage Collection.
 */
enum class HeritageBook(
    val id: String,
    val titleArabic: String,
    val titleEnglish: String,
    val authorArabic: String,
    val descriptionArabic: String,
    val approximateCount: Int
) {
    SAHIH_BUKHARI(
        id = "bukhari",
        titleArabic = "صحيح البخاري",
        titleEnglish = "Sahih al-Bukhari",
        authorArabic = "الإمام محمد بن إسماعيل البخاري (ت ٢٥٦ هـ)",
        descriptionArabic = "الجامع المسند الصحيح المختصر من أمور رسول الله ﷺ وسننه وأيامه، أصح كتاب بعد القرآن الكريم.",
        approximateCount = 7563
    ),
    SAHIH_MUSLIM(
        id = "muslim",
        titleArabic = "صحيح مسلم",
        titleEnglish = "Sahih Muslim",
        authorArabic = "الإمام مسلم بن الحجاج النيسابوري (ت ٢٦١ هـ)",
        descriptionArabic = "المسند الصحيح بنقل العدل عن العدل إلى رسول الله ﷺ، تميز بدقة السياق وحسن الترتيب والتبويب.",
        approximateCount = 7500
    ),
    SEERAH_NABAWYYAH(
        id = "seerah",
        titleArabic = "السيرة النبوية (الرحيق المختوم)",
        titleEnglish = "Prophetic Biography (Ar-Raheeq Al-Makhtum)",
        authorArabic = "الشيخ صفي الرحمن المباركفوري",
        descriptionArabic = "الكتاب الحائز على الجائزة الأولى في مسابقة رابطة العالم الإسلامي للسيرة النبوية الشريفة.",
        approximateCount = 16
    )
}

/**
 * Chapter / Kitab hierarchy within a book.
 */
@Immutable
data class HadithChapter(
    val chapterId: Int,
    val bookId: String,
    val chapterNumber: Int,
    val titleArabic: String,
    val hadithCount: Int
)

/**
 * Single Hadith or Seerah narrative entry.
 */
@Immutable
data class HadithItem(
    val id: String,
    val bookId: String,
    val chapterId: Int,
    val hadithNumber: Int,
    val chapterTitle: String,
    val babTitle: String,
    val sanad: String,
    val matn: String,
    val grade: String = "صحيح",
    val reference: String = "",
    val historicalMilestone: String = ""
)

/**
 * Bookmark for Hadith or Seerah section.
 */
@Immutable
data class HadithBookmark(
    val id: String,
    val bookId: String,
    val hadithId: String,
    val title: String,
    val snippet: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Download status for hadith & seerah volumes.
 */
sealed interface HadithDownloadState {
    data object Idle : HadithDownloadState
    data class Downloading(val progress: Float, val message: String) : HadithDownloadState
    data object Ready : HadithDownloadState
    data class Error(val message: String) : HadithDownloadState
}
