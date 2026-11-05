package com.example.quran.book

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Single Ayah in the written Quran (المصحف الشريف).
 */
@Immutable
data class WrittenAyah(
    val surahNumber: Int,
    val ayahNumber: Int,
    val textUthmani: String,
    val pageNumber: Int,
    val juzNumber: Int,
    val hizbQuarter: Int = 1,
    val sajda: Boolean = false
) {
    /**
     * Formats ayah number with the traditional decorative Quranic floral bracket ۝.
     */
    val decoratedNumber: String
        get() = " ۝$ayahNumber "
}

/**
 * A full page of the Holy Quran matching the 604-page Madani Mushaf standard.
 */
@Immutable
data class WrittenQuranPage(
    val pageNumber: Int,
    val juzNumber: Int,
    val surahNumber: Int,
    val surahNameArabic: String,
    val ayahs: List<WrittenAyah>
) {
    val titleDisplay: String
        get() = "سورة $surahNameArabic • الجزء $juzNumber"
}

/**
 * Surah metadata with Madani Mushaf starting page.
 */
@Immutable
data class SurahPageIndex(
    val number: Int,
    val nameArabic: String,
    val nameEnglish: String,
    val pageNumber: Int,
    val ayahCount: Int,
    val isMeccan: Boolean
)

/**
 * Juz (الجزء) index entry.
 */
@Immutable
data class JuzIndex(
    val juzNumber: Int,
    val nameArabic: String,
    val startSurahNumber: Int,
    val startSurahName: String,
    val startAyahNumber: Int,
    val startPageNumber: Int
)

/**
 * Reading palette options engineered for eye comfort.
 */
enum class QuranPaletteType(
    val labelArabic: String,
    val description: String,
    val backgroundColor: Color,
    val pageColor: Color,
    val textColor: Color,
    val accentColor: Color,
    val borderColor: Color,
    val isDark: Boolean
) {
    WARM_SEPIA(
        labelArabic = "ورق دافئ (سيبيا)",
        description = "مريح للعين ليلاً ومحاكي لورق المصاحف القديمة",
        backgroundColor = Color(0xFFF4ECE1),
        pageColor = Color(0xFFFAF4EB),
        textColor = Color(0xFF2C2219),
        accentColor = Color(0xFFB45309),
        borderColor = Color(0xFFE2D6C3),
        isDark = false
    ),
    CREAM_PAPER(
        labelArabic = "ورق كريمي هادئ",
        description = "ألوان كلاسيكية هادئة ومريحة للعين في القراءة المطولة",
        backgroundColor = Color(0xFFF8F5EE),
        pageColor = Color(0xFFFFFDF8),
        textColor = Color(0xFF1E242B),
        accentColor = Color(0xFF047857),
        borderColor = Color(0xFFE8E2D2),
        isDark = false
    ),
    AMOLED_DARK(
        labelArabic = "أموليد أسود نقي",
        description = "أسود نقي لشاشات AMOLED مع لمسات خضراء ملكية وتوفير فائق للبطارية",
        backgroundColor = Color(0xFF000000),
        pageColor = Color(0xFF070B08),
        textColor = Color(0xFFECFDF5),
        accentColor = Color(0xFF10B981),
        borderColor = Color(0xFF132A1C),
        isDark = true
    ),
    DEEP_FOREST(
        labelArabic = "أخضر زمردي داكن",
        description = "خلفية داكنة بلمسات خضراء عميقة مريحة للأعصاب ومفضلة للقراءة الليلية",
        backgroundColor = Color(0xFF030D08),
        pageColor = Color(0xFF081810),
        textColor = Color(0xFFD1FAE5),
        accentColor = Color(0xFF34D399),
        borderColor = Color(0xFF1B432E),
        isDark = true
    ),
    DAYLIGHT_WHITE(
        labelArabic = "أبيض نهاري ناصع",
        description = "وضوح فائق للقراءة النهارية بنقاء حبري ناصع وتباين عالٍ",
        backgroundColor = Color(0xFFF1F5F9),
        pageColor = Color(0xFFFFFFFF),
        textColor = Color(0xFF0F172A),
        accentColor = Color(0xFF0D9488),
        borderColor = Color(0xFFCBD5E1),
        isDark = false
    )
}

/**
 * Page turn display mode: 3D paper curl vs normal horizontal/vertical scroll.
 */
enum class PageTurnMode(val labelArabic: String, val description: String) {
    REALISTIC_3D("تقليب ثلاثي الأبعاد (3D)", "محاكاة واقعية لتقليب صفحات الورق مع ظلال حية وانحناء فيزيائي"),
    NORMAL_SWIPE("تمرير أفقي سلس", "تقليب سلس وسريع بين الصفحات للشاشات عالية التردد 120Hz"),
    VERTICAL_SCROLL("تمرير رأسي مستمر", "قراءة رأسية متتابعة للمصحف الشريف صفحة تلو الأخرى")
}

/**
 * Saved Ayah / Page Bookmark.
 */
@Immutable
data class QuranBookmark(
    val id: String,
    val pageNumber: Int,
    val surahNumber: Int,
    val surahName: String,
    val ayahNumber: Int,
    val snippet: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Reading visual style: Authentic Handwritten calligraphy page scans vs Digital Uthmanic font.
 */
enum class QuranReadingStyle(val labelArabic: String, val description: String) {
    AUTHENTIC_HANDWRITTEN(
        labelArabic = "المصحف المخطوط الأصلي (عثمان طه)",
        description = "صفحات المصحف الشريف المخطوطة بِيَدِ الشيخ عثمان طه (مجمع الملك فهد) بدقة 1260px فائقة النقاء"
    ),
    DIGITAL_UTHMANI(
        labelArabic = "النص العثماني الرقمي",
        description = "نص عثماني رقمي متجاوب مع إمكانية تكبير الخط ونسخ الآيات وتغيير القوالب اللونية"
    )
}


/**
 * Download status for the written Quran text bundle.
 */
sealed interface WrittenQuranDownloadState {
    data object Idle : WrittenQuranDownloadState
    data class Downloading(val progressFraction: Float, val statusMessage: String) : WrittenQuranDownloadState
    data object Ready : WrittenQuranDownloadState
    data class Error(val message: String) : WrittenQuranDownloadState
}
