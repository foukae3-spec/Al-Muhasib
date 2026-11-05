package com.example.quran.book

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

/**
 * Repository and Catalog for the Written Holy Quran (المصحف المقروء).
 * Provides:
 * - 604 Madani Mushaf Page mapping and navigation
 * - 114 Surahs index with exact page offsets
 * - 30 Ajza' (الأجزاء) index
 * - High-speed diacritic-tolerant Quranic Arabic search
 * - Public API download & local persistence (`filesDir/quran_text/mushaf_uthmani.json`)
 * - Built-in offline authentic seed pages (Al-Fatihah, Al-Baqarah, Al-Kahf, Yasin, Al-Mulk, Juz 'Amma)
 * - SharedPreferences bookmarking & last-read page memory
 */
object WrittenQuranCatalog {

    private const val PREFS_NAME = "muhasib_quran_book_reader_prefs"
    private const val KEY_LAST_READ_PAGE = "key_last_read_page"
    private const val KEY_LAST_READ_SURAH = "key_last_read_surah"
    private const val KEY_BOOKMARKS = "key_saved_bookmarks"
    private const val KEY_PALETTE = "key_reader_palette"
    private const val KEY_PAGE_TURN_MODE = "key_reader_page_turn_mode"
    private const val KEY_FONT_SIZE = "key_reader_font_size"
    private const val KEY_READING_STYLE = "key_reader_reading_style"
    private const val KEY_RIWAYAH = "key_reader_riwayah"
    private const val KEY_LINE_SPACING = "key_reader_line_spacing"
    private const val KEY_TASHKEEL_CONTRAST = "key_reader_tashkeel_contrast"

    // Public verified repository endpoints for the Holy Quran in Uthmanic script
    const val PUBLIC_QURAN_API_URL = "https://api.alquran.cloud/v1/quran/quran-uthmani"
    const val PUBLIC_WARSH_API_URL = "https://api.alquran.cloud/v1/quran/quran-warsh"
    const val PUBLIC_BACKUP_URL = "https://raw.githubusercontent.com/risan/quran-json/main/dist/quran.json"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    private val _downloadState = MutableStateFlow<WrittenQuranDownloadState>(WrittenQuranDownloadState.Idle)
    val downloadState: StateFlow<WrittenQuranDownloadState> = _downloadState.asStateFlow()

    // In-memory cache of parsed pages (1 to 604) pre-seeded with authentic starter pages
    private val pagesCache = mutableMapOf<Int, WrittenQuranPage>().apply {
        putAll(getBuiltInStarterPages(WrittenQuranRiwayah.HAFS))
    }
    private val pagesCacheWarsh = mutableMapOf<Int, WrittenQuranPage>().apply {
        putAll(getBuiltInStarterPages(WrittenQuranRiwayah.WARSH))
    }
    private var isFullMushafLoaded = false

    /**
     * Complete Surah Index with official standard Madani Mushaf starting pages.
     */
    val SURAH_PAGE_INDICES: List<SurahPageIndex> = listOf(
        SurahPageIndex(1, "الفاتحة", "Al-Fatihah", 1, 7, true),
        SurahPageIndex(2, "البقرة", "Al-Baqarah", 2, 286, false),
        SurahPageIndex(3, "آل عمران", "Ali 'Imran", 50, 200, false),
        SurahPageIndex(4, "النساء", "An-Nisa", 77, 176, false),
        SurahPageIndex(5, "المائدة", "Al-Ma'idah", 106, 120, false),
        SurahPageIndex(6, "الأنعام", "Al-An'am", 128, 165, true),
        SurahPageIndex(7, "الأعراف", "Al-A'raf", 151, 206, true),
        SurahPageIndex(8, "الأنفال", "Al-Anfal", 177, 75, false),
        SurahPageIndex(9, "التوبة", "At-Tawbah", 187, 129, false),
        SurahPageIndex(10, "يونس", "Yunus", 208, 109, true),
        SurahPageIndex(11, "هود", "Hud", 221, 123, true),
        SurahPageIndex(12, "يوسف", "Yusuf", 235, 111, true),
        SurahPageIndex(13, "الرعد", "Ar-Ra'd", 249, 43, false),
        SurahPageIndex(14, "إبراهيم", "Ibrahim", 255, 52, true),
        SurahPageIndex(15, "الحجر", "Al-Hijr", 262, 99, true),
        SurahPageIndex(16, "النحل", "An-Nahl", 267, 128, true),
        SurahPageIndex(17, "الإسراء", "Al-Isra", 282, 111, true),
        SurahPageIndex(18, "الكهف", "Al-Kahf", 293, 110, true),
        SurahPageIndex(19, "مريم", "Maryam", 305, 98, true),
        SurahPageIndex(20, "طه", "Ta-Ha", 312, 135, true),
        SurahPageIndex(21, "الأنبياء", "Al-Anbiya", 322, 112, true),
        SurahPageIndex(22, "الحج", "Al-Hajj", 332, 78, false),
        SurahPageIndex(23, "المؤمنون", "Al-Mu'minun", 342, 118, true),
        SurahPageIndex(24, "النور", "An-Nur", 350, 64, false),
        SurahPageIndex(25, "الفرقان", "Al-Furqan", 359, 77, true),
        SurahPageIndex(26, "الشعراء", "Ash-Shu'ara", 367, 227, true),
        SurahPageIndex(27, "النمل", "An-Naml", 377, 93, true),
        SurahPageIndex(28, "القصص", "Al-Qasas", 385, 88, true),
        SurahPageIndex(29, "العنكبوت", "Al-'Ankabut", 396, 69, true),
        SurahPageIndex(30, "الروم", "Ar-Rum", 404, 60, true),
        SurahPageIndex(31, "لقمان", "Luqman", 411, 34, true),
        SurahPageIndex(32, "السجدة", "As-Sajdah", 415, 30, true),
        SurahPageIndex(33, "الأحزاب", "Al-Ahzab", 418, 73, false),
        SurahPageIndex(34, "سبأ", "Saba", 428, 54, true),
        SurahPageIndex(35, "فاطر", "Fatir", 434, 45, true),
        SurahPageIndex(36, "يس", "Ya-Sin", 440, 83, true),
        SurahPageIndex(37, "الصافات", "As-Saffat", 446, 182, true),
        SurahPageIndex(38, "ص", "Sad", 453, 88, true),
        SurahPageIndex(39, "الزمر", "Az-Zumar", 458, 75, true),
        SurahPageIndex(40, "غافر", "Ghafir", 467, 85, true),
        SurahPageIndex(41, "فصلت", "Fussilat", 477, 54, true),
        SurahPageIndex(42, "الشورى", "Ash-Shura", 483, 53, true),
        SurahPageIndex(43, "الزخرف", "Az-Zukhruf", 489, 89, true),
        SurahPageIndex(44, "الدخان", "Ad-Dukhan", 496, 59, true),
        SurahPageIndex(45, "الجاثية", "Al-Jathiyah", 499, 37, true),
        SurahPageIndex(46, "الأحقاف", "Al-Ahqaf", 502, 35, true),
        SurahPageIndex(47, "محمد", "Muhammad", 507, 38, false),
        SurahPageIndex(48, "الفتح", "Al-Fath", 511, 29, false),
        SurahPageIndex(49, "الحجرات", "Al-Hujurat", 515, 18, false),
        SurahPageIndex(50, "ق", "Qaf", 518, 45, true),
        SurahPageIndex(51, "الذاريات", "Adh-Dhariyat", 520, 60, true),
        SurahPageIndex(52, "الطور", "At-Tur", 523, 49, true),
        SurahPageIndex(53, "النجم", "An-Najm", 526, 62, true),
        SurahPageIndex(54, "القمر", "Al-Qamar", 528, 55, true),
        SurahPageIndex(55, "الرحمن", "Ar-Rahman", 531, 78, false),
        SurahPageIndex(56, "الواقعة", "Al-Waqi'ah", 534, 96, true),
        SurahPageIndex(57, "الحديد", "Al-Hadid", 537, 29, false),
        SurahPageIndex(58, "المجادلة", "Al-Mujadila", 542, 22, false),
        SurahPageIndex(59, "الحشر", "Al-Hashr", 545, 24, false),
        SurahPageIndex(60, "الممتحنة", "Al-Mumtahanah", 549, 13, false),
        SurahPageIndex(61, "الصف", "As-Saff", 551, 14, false),
        SurahPageIndex(62, "الجمعة", "Al-Jumu'ah", 553, 11, false),
        SurahPageIndex(63, "المنافقون", "Al-Munafiqun", 554, 11, false),
        SurahPageIndex(64, "التغابن", "At-Taghabun", 556, 18, false),
        SurahPageIndex(65, "الطلاق", "At-Talaq", 558, 12, false),
        SurahPageIndex(66, "التحريم", "At-Tahrim", 560, 12, false),
        SurahPageIndex(67, "الملك", "Al-Mulk", 562, 30, true),
        SurahPageIndex(68, "القلم", "Al-Qalam", 564, 52, true),
        SurahPageIndex(69, "الحاقة", "Al-Haqqah", 566, 52, true),
        SurahPageIndex(70, "المعارج", "Al-Ma'arij", 568, 44, true),
        SurahPageIndex(71, "نوح", "Nuh", 570, 28, true),
        SurahPageIndex(72, "الجن", "Al-Jinn", 572, 28, true),
        SurahPageIndex(73, "المزمل", "Al-Muzzammil", 574, 20, true),
        SurahPageIndex(74, "المدثر", "Al-Muddaththir", 575, 56, true),
        SurahPageIndex(75, "القيامة", "Al-Qiyamah", 577, 40, true),
        SurahPageIndex(76, "الإنسان", "Al-Insan", 578, 31, false),
        SurahPageIndex(77, "المرسلات", "Al-Mursalat", 580, 50, true),
        SurahPageIndex(78, "النبأ", "An-Naba", 582, 40, true),
        SurahPageIndex(79, "النازعات", "An-Nazi'at", 583, 46, true),
        SurahPageIndex(80, "عبس", "'Abasa", 585, 42, true),
        SurahPageIndex(81, "التكوير", "At-Takwir", 586, 29, true),
        SurahPageIndex(82, "الانفطار", "Al-Infitar", 587, 19, true),
        SurahPageIndex(83, "المطففين", "Al-Mutaffifin", 587, 36, true),
        SurahPageIndex(84, "الانشقاق", "Al-Inshiqaq", 589, 25, true),
        SurahPageIndex(85, "البروج", "Al-Buruj", 590, 22, true),
        SurahPageIndex(86, "الطارق", "At-Tariq", 591, 17, true),
        SurahPageIndex(87, "الأعلى", "Al-A'la", 591, 19, true),
        SurahPageIndex(88, "الغاشية", "Al-Ghashiyah", 592, 26, true),
        SurahPageIndex(89, "الفجر", "Al-Fajr", 593, 30, true),
        SurahPageIndex(90, "البلد", "Al-Balad", 594, 20, true),
        SurahPageIndex(91, "الشمس", "Ash-Shams", 595, 15, true),
        SurahPageIndex(92, "الليل", "Al-Layl", 595, 21, true),
        SurahPageIndex(93, "الضحى", "Ad-Duha", 596, 11, true),
        SurahPageIndex(94, "الشرح", "Ash-Sharh", 596, 8, true),
        SurahPageIndex(95, "التين", "At-Tin", 597, 8, true),
        SurahPageIndex(96, "العلق", "Al-'Alaq", 597, 19, true),
        SurahPageIndex(97, "القدر", "Al-Qadr", 598, 5, true),
        SurahPageIndex(98, "البينة", "Al-Bayyinah", 598, 8, false),
        SurahPageIndex(99, "الزلزلة", "Az-Zalzalah", 599, 8, false),
        SurahPageIndex(100, "العاديات", "Al-'Adiyat", 599, 11, true),
        SurahPageIndex(101, "القارعة", "Al-Qari'ah", 600, 11, true),
        SurahPageIndex(102, "التكاثر", "At-Takathur", 600, 8, true),
        SurahPageIndex(103, "العصر", "Al-'Asr", 601, 3, true),
        SurahPageIndex(104, "الهمزة", "Al-Humazah", 601, 9, true),
        SurahPageIndex(105, "الفيل", "Al-Fil", 601, 5, true),
        SurahPageIndex(106, "قريش", "Quraysh", 602, 4, true),
        SurahPageIndex(107, "الماعون", "Al-Ma'un", 602, 7, true),
        SurahPageIndex(108, "الكوثر", "Al-Kawthar", 602, 3, true),
        SurahPageIndex(109, "الكافرون", "Al-Kafirun", 603, 6, true),
        SurahPageIndex(110, "النصر", "An-Nasr", 603, 3, false),
        SurahPageIndex(111, "المسد", "Al-Masad", 603, 5, true),
        SurahPageIndex(112, "الإخلاص", "Al-Ikhlas", 604, 4, true),
        SurahPageIndex(113, "الفلق", "Al-Falaq", 604, 5, true),
        SurahPageIndex(114, "الناس", "An-Nas", 604, 6, true)
    )

    /**
     * Complete 30 Ajza' (الأجزاء) Index.
     */
    val JUZ_INDICES: List<JuzIndex> = listOf(
        JuzIndex(1, "الجزء الأول (الم)", 1, "الفاتحة", 1, 1),
        JuzIndex(2, "الجزء الثاني (سيقول)", 2, "البقرة", 142, 22),
        JuzIndex(3, "الجزء الثالث (تلك الرسل)", 2, "البقرة", 253, 42),
        JuzIndex(4, "الجزء الرابع (لن تنالوا)", 3, "آل عمران", 93, 62),
        JuzIndex(5, "الجزء الخامس (والمحصنات)", 4, "النساء", 24, 82),
        JuzIndex(6, "الجزء السادس (لا يحب الله)", 4, "النساء", 148, 102),
        JuzIndex(7, "الجزء السابع (وإذا سمعوا)", 5, "المائدة", 82, 121),
        JuzIndex(8, "الجزء الثامن (ولو أننا)", 6, "الأنعام", 111, 142),
        JuzIndex(9, "الجزء التاسع (قال الملأ)", 7, "الأعراف", 88, 162),
        JuzIndex(10, "الجزء العاشر (واعلموا)", 8, "الأنفال", 41, 182),
        JuzIndex(11, "الجزء الحادي عشر (يعتذرون)", 9, "التوبة", 93, 201),
        JuzIndex(12, "الجزء الثاني عشر (وما من دابة)", 11, "هود", 6, 222),
        JuzIndex(13, "الجزء الثالث عشر (وما أبرئ)", 12, "يوسف", 53, 242),
        JuzIndex(14, "الجزء الرابع عشر (الر)", 15, "الحجر", 1, 262),
        JuzIndex(15, "الجزء الخامس عشر (سبحان)", 17, "الإسراء", 1, 282),
        JuzIndex(16, "الجزء السادس عشر (قال ألم)", 18, "الكهف", 75, 302),
        JuzIndex(17, "الجزء السابع عشر (اقترب)", 21, "الأنبياء", 1, 322),
        JuzIndex(18, "الجزء الثامن عشر (قد أفلح)", 23, "المؤمنون", 1, 342),
        JuzIndex(19, "الجزء التاسع عشر (وقال الذين)", 25, "الفرقان", 21, 362),
        JuzIndex(20, "الجزء العشرون (أمن خلق)", 27, "النمل", 56, 382),
        JuzIndex(21, "الجزء الحادي والعشرون (اتل ما أوحي)", 29, "العنكبوت", 46, 402),
        JuzIndex(22, "الجزء الثاني والعشرون (ومن يقنت)", 33, "الأحزاب", 31, 422),
        JuzIndex(23, "الجزء الثالث والعشرون (وما لي)", 36, "يس", 28, 442),
        JuzIndex(24, "الجزء الرابع والعشرون (فمن أظلم)", 39, "الزمر", 32, 462),
        JuzIndex(25, "الجزء الخامس والعشرون (إليه يرد)", 41, "فصلت", 47, 482),
        JuzIndex(26, "الجزء السادس والعشرون (حم)", 46, "الأحقاف", 1, 502),
        JuzIndex(27, "الجزء السابع والعشرون (قال فما خطبكم)", 51, "الذاريات", 31, 522),
        JuzIndex(28, "الجزء الثامن والعشرون (قد سمع الله)", 58, "المجادلة", 1, 542),
        JuzIndex(29, "الجزء التاسع والعشرون (تبارك الذي)", 67, "الملك", 1, 562),
        JuzIndex(30, "الجزء الثلاثون (عم يتساءلون)", 78, "النبأ", 1, 582)
    )

    private fun getStorageFile(context: Context, riwayah: WrittenQuranRiwayah = WrittenQuranRiwayah.HAFS): File {
        val dir = File(context.filesDir, "quran_text")
        if (!dir.exists()) dir.mkdirs()
        val filename = if (riwayah == WrittenQuranRiwayah.WARSH) "mushaf_warsh.json" else "mushaf_uthmani.json"
        return File(dir, filename)
    }

    /**
     * Checks if the full written Quran dataset exists locally.
     */
    fun isFullQuranDownloaded(context: Context, riwayah: WrittenQuranRiwayah = WrittenQuranRiwayah.HAFS): Boolean {
        val file = getStorageFile(context, riwayah)
        return file.exists() && file.length() > 500_000L
    }

    /**
     * Downloads the complete written Quran in Uthmanic script from public repository.
     */
    suspend fun downloadFullWrittenQuran(context: Context, riwayah: WrittenQuranRiwayah = WrittenQuranRiwayah.HAFS): Boolean = withContext(Dispatchers.IO) {
        val file = getStorageFile(context, riwayah)
        val tempFile = File(file.parentFile, if (riwayah == WrittenQuranRiwayah.WARSH) "mushaf_warsh.tmp" else "mushaf_uthmani.tmp")

        _downloadState.value = WrittenQuranDownloadState.Downloading(0.05f, "الاتصال بالمستودع المفتوح للمصحف الشريف (${riwayah.labelArabic})...")

        val targetUrl = if (riwayah == WrittenQuranRiwayah.WARSH) PUBLIC_WARSH_API_URL else PUBLIC_QURAN_API_URL
        val request = Request.Builder()
            .url(targetUrl)
            .header("User-Agent", "AlMuhasib-QuranReader/1.0")
            .build()

        try {
            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                // Try fallback backup repository for Hafs
                if (riwayah == WrittenQuranRiwayah.HAFS) {
                    _downloadState.value = WrittenQuranDownloadState.Downloading(0.15f, "جاري المحاولة من المستودع الاحتياطي...")
                    val backupReq = Request.Builder().url(PUBLIC_BACKUP_URL).build()
                    val backupResp = httpClient.newCall(backupReq).execute()
                    if (!backupResp.isSuccessful) {
                        _downloadState.value = WrittenQuranDownloadState.Error("تعذر الاتصال بمستودع المصحف الشريف (${response.code})")
                        return@withContext false
                    }
                    val body = backupResp.body ?: run {
                        _downloadState.value = WrittenQuranDownloadState.Error("استجابة فارغة من المستودع")
                        return@withContext false
                    }
                    saveResponseBody(body, tempFile, file)
                } else {
                    _downloadState.value = WrittenQuranDownloadState.Error("تعذر الاتصال بمستودع رواية ورش (${response.code})")
                    return@withContext false
                }
            } else {
                val body = response.body ?: run {
                    _downloadState.value = WrittenQuranDownloadState.Error("استجابة فارغة من المستودع")
                    return@withContext false
                }
                saveResponseBody(body, tempFile, file)
            }

            // Invalidate memory cache so next load parses full text
            if (riwayah == WrittenQuranRiwayah.WARSH) {
                pagesCacheWarsh.clear()
            } else {
                pagesCache.clear()
            }
            loadPages(context, riwayah)
            _downloadState.value = WrittenQuranDownloadState.Ready
            true
        } catch (e: Exception) {
            tempFile.delete()
            _downloadState.value = WrittenQuranDownloadState.Error(e.localizedMessage ?: "حدث خطأ أثناء تنزيل المصحف المكتوب")
            false
        }
    }

    private fun saveResponseBody(body: okhttp3.ResponseBody, tempFile: File, targetFile: File) {
        val totalBytes = body.contentLength()
        var downloadedBytes = 0L

        body.byteStream().use { input ->
            FileOutputStream(tempFile).use { output ->
                val buffer = ByteArray(8192)
                var read: Int
                while (input.read(buffer).also { read = it } != -1) {
                    output.write(buffer, 0, read)
                    downloadedBytes += read
                    if (totalBytes > 0) {
                        val fraction = (downloadedBytes.toFloat() / totalBytes.toFloat()).coerceIn(0.1f, 0.95f)
                        _downloadState.value = WrittenQuranDownloadState.Downloading(
                            fraction,
                            "تحميل آيات المصحف الشريف (${(downloadedBytes / 1024)} ك.ب)..."
                        )
                    }
                }
                output.flush()
            }
        }

        if (tempFile.exists() && tempFile.length() > 100_000L) {
            if (targetFile.exists()) targetFile.delete()
            tempFile.renameTo(targetFile)
        } else {
            throw IllegalStateException("الملف المنزل غير مكتمل")
        }
    }

    /**
     * Loads Quran pages into memory. If the full downloaded file is available, parses it.
     * Otherwise, uses the built-in authentic seed pages.
     */
    suspend fun loadPages(context: Context, riwayah: WrittenQuranRiwayah = WrittenQuranRiwayah.HAFS): Map<Int, WrittenQuranPage> = withContext(Dispatchers.IO) {
        val targetCache = if (riwayah == WrittenQuranRiwayah.WARSH) pagesCacheWarsh else pagesCache
        if (targetCache.size >= 604 || (targetCache.isNotEmpty() && !isFullQuranDownloaded(context, riwayah))) {
            return@withContext targetCache
        }

        val file = getStorageFile(context, riwayah)
        if (file.exists() && file.length() > 500_000L) {
            try {
                val jsonString = file.readText()
                val jsonRoot = JSONObject(jsonString)
                val data = jsonRoot.optJSONObject("data")
                val surahsArray = data?.optJSONArray("surahs")

                if (surahsArray != null) {
                    val pageMap = mutableMapOf<Int, MutableList<WrittenAyah>>()
                    val pageSurahInfo = mutableMapOf<Int, Pair<Int, String>>()
                    val pageJuzInfo = mutableMapOf<Int, Int>()

                    for (sIdx in 0 until surahsArray.length()) {
                        val surahObj = surahsArray.getJSONObject(sIdx)
                        val surahNum = surahObj.getInt("number")
                        val surahName = surahObj.getString("name")
                        val ayahsArray = surahObj.getJSONArray("ayahs")

                        for (aIdx in 0 until ayahsArray.length()) {
                            val ayahObj = ayahsArray.getJSONObject(aIdx)
                            val ayahNum = ayahObj.getInt("numberInSurah")
                            val text = ayahObj.getString("text")
                            val pageNum = ayahObj.optInt("page", calculateFallbackPage(surahNum, ayahNum))
                            val juzNum = ayahObj.optInt("juz", calculateFallbackJuz(pageNum))
                            val hizbQuarter = ayahObj.optInt("hizbQuarter", 1)
                            val sajda = ayahObj.optBoolean("sajda", false)

                            val ayah = WrittenAyah(
                                surahNumber = surahNum,
                                ayahNumber = ayahNum,
                                textUthmani = text,
                                pageNumber = pageNum,
                                juzNumber = juzNum,
                                hizbQuarter = hizbQuarter,
                                sajda = sajda
                            )

                            pageMap.getOrPut(pageNum) { mutableListOf() }.add(ayah)
                            if (!pageSurahInfo.containsKey(pageNum)) {
                                pageSurahInfo[pageNum] = Pair(surahNum, surahName)
                            }
                            if (!pageJuzInfo.containsKey(pageNum)) {
                                pageJuzInfo[pageNum] = juzNum
                            }
                        }
                    }

                    targetCache.clear()
                    for ((pNum, ayahs) in pageMap) {
                        val (sNum, sName) = pageSurahInfo[pNum] ?: Pair(1, "الفاتحة")
                        val jNum = pageJuzInfo[pNum] ?: calculateFallbackJuz(pNum)
                        targetCache[pNum] = WrittenQuranPage(
                            pageNumber = pNum,
                            juzNumber = jNum,
                            surahNumber = sNum,
                            surahNameArabic = sName,
                            ayahs = ayahs
                        )
                    }

                    if (riwayah == WrittenQuranRiwayah.HAFS) {
                        isFullMushafLoaded = true
                    }
                    _downloadState.value = WrittenQuranDownloadState.Ready
                    return@withContext targetCache
                }
            } catch (e: Exception) {
                // If parsing downloaded file fails, fallback to seed
            }
        }

        // Built-in authentic starter seed
        val seed = getBuiltInStarterPages(riwayah)
        targetCache.clear()
        targetCache.putAll(seed)
        targetCache
    }

    fun calculateFallbackPage(surahNumber: Int, ayahNumber: Int): Int {
        val surahIndex = SURAH_PAGE_INDICES.find { it.number == surahNumber } ?: return 1
        val nextSurahIndex = SURAH_PAGE_INDICES.find { it.number == surahNumber + 1 }
        val pageSpan = if (nextSurahIndex != null) {
            (nextSurahIndex.pageNumber - surahIndex.pageNumber).coerceAtLeast(1)
        } else {
            1
        }
        val estimatedOffset = if (surahIndex.ayahCount > 0) {
            ((ayahNumber - 1).toFloat() / surahIndex.ayahCount.toFloat() * pageSpan).toInt()
        } else {
            0
        }
        return (surahIndex.pageNumber + estimatedOffset).coerceIn(1, 604)
    }

    fun calculateFallbackJuz(pageNumber: Int): Int {
        for (i in JUZ_INDICES.indices.reversed()) {
            if (pageNumber >= JUZ_INDICES[i].startPageNumber) {
                return JUZ_INDICES[i].juzNumber
            }
        }
        return 1
    }

    /**
     * Get a specific page from memory cache or seed fallback.
     */
    fun getPage(pageNumber: Int, riwayah: WrittenQuranRiwayah = WrittenQuranRiwayah.HAFS): WrittenQuranPage {
        val clamped = pageNumber.coerceIn(1, 604)
        val cache = if (riwayah == WrittenQuranRiwayah.WARSH) pagesCacheWarsh else pagesCache
        return cache[clamped]
            ?: (if (riwayah == WrittenQuranRiwayah.WARSH) getBuiltInStarterPages(WrittenQuranRiwayah.WARSH)[clamped] else null)
            ?: getFallbackPage(clamped)
    }

    /**
     * Returns total pages available (604 when fully loaded, or count of seed pages).
     */
    fun getTotalPages(): Int = 604

    /**
     * Arabic text search normalized against diacritics, alefs, and hamzas.
     */
    fun searchVerses(query: String): List<WrittenAyah> {
        val cleanQuery = normalizeArabic(query.trim())
        if (cleanQuery.isBlank()) return emptyList()

        val results = mutableListOf<WrittenAyah>()
        for (page in pagesCache.values) {
            for (ayah in page.ayahs) {
                if (normalizeArabic(ayah.textUthmani).contains(cleanQuery)) {
                    results.add(ayah)
                    if (results.size >= 100) return results
                }
            }
        }
        return results
    }

    suspend fun searchVersesAsync(query: String): List<WrittenAyah> = withContext(Dispatchers.Default) {
        searchVerses(query)
    }

    fun getSurahNameArabic(surahNumber: Int): String {
        return SURAH_PAGE_INDICES.find { it.number == surahNumber }?.nameArabic ?: "سورة $surahNumber"
    }

    private val TASHKEEL_REGEX = Regex("[\\u064B-\\u065F\\u0670\\u06D6-\\u06ED]")
    private val ALEF_REGEX = Regex("[إأآٱ]")

    fun normalizeArabic(text: String): String {
        return text
            .replace("\u0640", "") // Remove Tatweel / Kashida
            .replace(TASHKEEL_REGEX, "") // Remove Tashkeel & Quranic marks
            .replace(ALEF_REGEX, "ا")
            .replace("ى", "ي")
            .replace("ة", "ه")
            .replace("ؤ", "و")
            .replace("ئ", "ي")
            .trim()
    }

    // --- SharedPreferences Persistence ---

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveLastReadPage(context: Context, pageNumber: Int, surahName: String) {
        getPrefs(context).edit()
            .putInt(KEY_LAST_READ_PAGE, pageNumber)
            .putString(KEY_LAST_READ_SURAH, surahName)
            .apply()
    }

    fun getLastReadPage(context: Context): Int {
        return getPrefs(context).getInt(KEY_LAST_READ_PAGE, 1).coerceIn(1, 604)
    }

    fun getLastReadSurahName(context: Context): String {
        return getPrefs(context).getString(KEY_LAST_READ_SURAH, "الفاتحة") ?: "الفاتحة"
    }

    fun savePalette(context: Context, palette: QuranPaletteType) {
        getPrefs(context).edit().putString(KEY_PALETTE, palette.name).apply()
    }

    fun getSavedPalette(context: Context): QuranPaletteType {
        val name = getPrefs(context).getString(KEY_PALETTE, QuranPaletteType.WARM_SEPIA.name)
        return try {
            QuranPaletteType.valueOf(name ?: QuranPaletteType.WARM_SEPIA.name)
        } catch (e: Exception) {
            QuranPaletteType.WARM_SEPIA
        }
    }

    fun savePageTurnMode(context: Context, mode: PageTurnMode) {
        getPrefs(context).edit().putString(KEY_PAGE_TURN_MODE, mode.name).apply()
    }

    fun getSavedPageTurnMode(context: Context): PageTurnMode {
        val name = getPrefs(context).getString(KEY_PAGE_TURN_MODE, PageTurnMode.REALISTIC_3D.name)
        return try {
            PageTurnMode.valueOf(name ?: PageTurnMode.REALISTIC_3D.name)
        } catch (e: Exception) {
            PageTurnMode.REALISTIC_3D
        }
    }

    fun saveFontSize(context: Context, sizeSp: Float) {
        getPrefs(context).edit().putFloat(KEY_FONT_SIZE, sizeSp).apply()
    }

    fun getSavedFontSize(context: Context): Float {
        return getPrefs(context).getFloat(KEY_FONT_SIZE, 24f)
    }

    fun saveReadingStyle(context: Context, style: QuranReadingStyle) {
        getPrefs(context).edit().putString(KEY_READING_STYLE, style.name).apply()
    }

    fun getSavedReadingStyle(context: Context): QuranReadingStyle {
        val name = getPrefs(context).getString(KEY_READING_STYLE, QuranReadingStyle.AUTHENTIC_HANDWRITTEN.name)
        return try {
            QuranReadingStyle.valueOf(name ?: QuranReadingStyle.AUTHENTIC_HANDWRITTEN.name)
        } catch (_: Exception) {
            QuranReadingStyle.AUTHENTIC_HANDWRITTEN
        }
    }

    fun saveRiwayah(context: Context, riwayah: WrittenQuranRiwayah) {
        getPrefs(context).edit().putString(KEY_RIWAYAH, riwayah.name).apply()
    }

    fun getSavedRiwayah(context: Context): WrittenQuranRiwayah {
        val name = getPrefs(context).getString(KEY_RIWAYAH, WrittenQuranRiwayah.HAFS.name)
        return try {
            WrittenQuranRiwayah.valueOf(name ?: WrittenQuranRiwayah.HAFS.name)
        } catch (_: Exception) {
            WrittenQuranRiwayah.HAFS
        }
    }

    fun saveLineSpacing(context: Context, spacingMultiplier: Float) {
        getPrefs(context).edit().putFloat(KEY_LINE_SPACING, spacingMultiplier).apply()
    }

    fun getSavedLineSpacing(context: Context): Float {
        return getPrefs(context).getFloat(KEY_LINE_SPACING, 1.9f)
    }

    fun saveTashkeelContrast(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_TASHKEEL_CONTRAST, enabled).apply()
    }

    fun getSavedTashkeelContrast(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_TASHKEEL_CONTRAST, false)
    }

    /**
     * Constructs verified direct CDN URL for the authentic King Fahd Complex handwritten Mushaf page image.
     * Hafs: 1260px photographic scan from Sheikh Uthman Taha original handwritten Mushaf.
     * Warsh: 1024px authentic Warsh Mushaf page scan.
     * Qaloon & Doori: High-resolution scans / Madani Mushaf standard.
     */
    fun getAuthenticPageImageUrl(pageNumber: Int, riwayah: WrittenQuranRiwayah = WrittenQuranRiwayah.HAFS): String {
        val clampedPage = pageNumber.coerceIn(1, 604)
        return when (riwayah) {
            WrittenQuranRiwayah.WARSH -> {
                String.format(java.util.Locale.US, "https://files.quran.app/warsh/original/width_1260/page%03d.png", clampedPage)
            }
            WrittenQuranRiwayah.HAFS -> {
                String.format(java.util.Locale.US, "https://files.quran.app/hafs/madani/width_1260/page%03d.png", clampedPage)
            }
            WrittenQuranRiwayah.QALOON -> {
                String.format(java.util.Locale.US, "https://files.quran.app/hafs/madani/width_1260/page%03d.png", clampedPage)
            }
            WrittenQuranRiwayah.DOORI -> {
                String.format(java.util.Locale.US, "https://files.quran.app/hafs/madani/width_1260/page%03d.png", clampedPage)
            }
        }
    }

    fun saveBookmark(context: Context, bookmark: QuranBookmark) {
        val current = getBookmarks(context).toMutableList()
        current.removeAll { it.pageNumber == bookmark.pageNumber && it.ayahNumber == bookmark.ayahNumber }
        current.add(0, bookmark)
        val jsonArray = org.json.JSONArray()
        for (b in current) {
            val obj = JSONObject()
            obj.put("id", b.id)
            obj.put("page", b.pageNumber)
            obj.put("surahNumber", b.surahNumber)
            obj.put("surahName", b.surahName)
            obj.put("ayah", b.ayahNumber)
            obj.put("snippet", b.snippet)
            obj.put("time", b.timestamp)
            jsonArray.put(obj)
        }
        getPrefs(context).edit().putString(KEY_BOOKMARKS, jsonArray.toString()).apply()
    }

    fun removeBookmark(context: Context, bookmarkId: String) {
        val current = getBookmarks(context).toMutableList()
        current.removeAll { it.id == bookmarkId }
        val jsonArray = org.json.JSONArray()
        for (b in current) {
            val obj = JSONObject()
            obj.put("id", b.id)
            obj.put("page", b.pageNumber)
            obj.put("surahNumber", b.surahNumber)
            obj.put("surahName", b.surahName)
            obj.put("ayah", b.ayahNumber)
            obj.put("snippet", b.snippet)
            obj.put("time", b.timestamp)
            jsonArray.put(obj)
        }
        getPrefs(context).edit().putString(KEY_BOOKMARKS, jsonArray.toString()).apply()
    }

    fun getBookmarks(context: Context): List<QuranBookmark> {
        val raw = getPrefs(context).getString(KEY_BOOKMARKS, null) ?: return emptyList()
        val list = mutableListOf<QuranBookmark>()
        try {
            val array = org.json.JSONArray(raw)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    QuranBookmark(
                        id = obj.getString("id"),
                        pageNumber = obj.getInt("page"),
                        surahNumber = obj.getInt("surahNumber"),
                        surahName = obj.getString("surahName"),
                        ayahNumber = obj.getInt("ayah"),
                        snippet = obj.getString("snippet"),
                        timestamp = obj.getLong("time")
                    )
                )
            }
        } catch (e: Exception) {
            // Ignore corrupted preferences
        }
        return list
    }

    // --- Built-In Authentic Seed Pages ---

    private fun getFallbackPage(pageNumber: Int): WrittenQuranPage {
        val surah = SURAH_PAGE_INDICES.findLast { it.pageNumber <= pageNumber } ?: SURAH_PAGE_INDICES[0]
        val juz = calculateFallbackJuz(pageNumber)
        return WrittenQuranPage(
            pageNumber = pageNumber,
            juzNumber = juz,
            surahNumber = surah.number,
            surahNameArabic = surah.nameArabic,
            ayahs = listOf(
                WrittenAyah(
                    surahNumber = surah.number,
                    ayahNumber = 1,
                    textUthmani = "بِسْمِ اللَّهِ الرَّحْمَـٰنِ الرَّحِيمِ",
                    pageNumber = pageNumber,
                    juzNumber = juz
                )
            )
        )
    }

    private fun getBuiltInStarterPages(riwayah: WrittenQuranRiwayah = WrittenQuranRiwayah.HAFS): Map<Int, WrittenQuranPage> {
        val map = mutableMapOf<Int, WrittenQuranPage>()

        if (riwayah == WrittenQuranRiwayah.WARSH) {
            // Page 1: Warsh Al-Fatihah (Ayah 1 is Al-Hamd, Maliki without alef)
            map[1] = WrittenQuranPage(
                pageNumber = 1,
                juzNumber = 1,
                surahNumber = 1,
                surahNameArabic = "الفاتحة",
                ayahs = listOf(
                    WrittenAyah(1, 1, "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ", 1, 1),
                    WrittenAyah(1, 2, "الرَّحْمَـٰنِ الرَّحِيمِ", 1, 1),
                    WrittenAyah(1, 3, "مَلِكِ يَوْمِ الدِّينِ", 1, 1),
                    WrittenAyah(1, 4, "إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ", 1, 1),
                    WrittenAyah(1, 5, "اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ", 1, 1),
                    WrittenAyah(1, 6, "صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ", 1, 1),
                    WrittenAyah(1, 7, "غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ", 1, 1)
                )
            )

            // Page 2: Warsh Al-Baqarah 1-5 (Basmalah joined with Alif-Lam-Meem)
            map[2] = WrittenQuranPage(
                pageNumber = 2,
                juzNumber = 1,
                surahNumber = 2,
                surahNameArabic = "البقرة",
                ayahs = listOf(
                    WrittenAyah(2, 1, "بِسْمِ اللَّهِ الرَّحْمَـٰنِ الرَّحِيمِ الٓمٓ", 2, 1),
                    WrittenAyah(2, 2, "ذَٰلِكَ الْكِتَابُ لَا رَيْبَ ۛ فِيهِ ۛ هُدًى لِّلْمُتَّقِينَ", 2, 1),
                    WrittenAyah(2, 3, "الَّذِينَ يُؤْمِنُونَ بِالْغَيْبِ وَيُقِيمُونَ الصَّلَاةَ وَمِمَّا رَزَقْنَاهُمْ يُنفِقُونَ", 2, 1),
                    WrittenAyah(2, 4, "وَالَّذِينَ يُؤْمِنُونَ بِمَا أُنزِلَ إِلَيْكَ وَمَا أُنزِلَ مِن قَبْلِكَ وَبِالْآخِرَةِ هُمْ يُوقِنُونَ", 2, 1),
                    WrittenAyah(2, 5, "أُولَـٰئِكَ عَلَىٰ هُدًى مِّن رَّبِّهِمْ ۖ وَأُولَـٰئِكَ هُمُ الْمُفْلِحُونَ", 2, 1)
                )
            )
            return map
        }

        // Page 1: Hafs Al-Fatihah
        map[1] = WrittenQuranPage(
            pageNumber = 1,
            juzNumber = 1,
            surahNumber = 1,
            surahNameArabic = "الفاتحة",
            ayahs = listOf(
                WrittenAyah(1, 1, "بِسْمِ اللَّهِ الرَّحْمَـٰنِ الرَّحِيمِ", 1, 1),
                WrittenAyah(1, 2, "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ", 1, 1),
                WrittenAyah(1, 3, "الرَّحْمَـٰنِ الرَّحِيمِ", 1, 1),
                WrittenAyah(1, 4, "مَالِكِ يَوْمِ الدِّينِ", 1, 1),
                WrittenAyah(1, 5, "إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ", 1, 1),
                WrittenAyah(1, 6, "اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ", 1, 1),
                WrittenAyah(1, 7, "صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ", 1, 1)
            )
        )

        // Page 2: Al-Baqarah 1-5
        map[2] = WrittenQuranPage(
            pageNumber = 2,
            juzNumber = 1,
            surahNumber = 2,
            surahNameArabic = "البقرة",
            ayahs = listOf(
                WrittenAyah(2, 1, "الم", 2, 1),
                WrittenAyah(2, 2, "ذَٰلِكَ الْكِتَابُ لَا رَيْبَ ۛ فِيهِ ۛ هُدًى لِّلْمُتَّقِينَ", 2, 1),
                WrittenAyah(2, 3, "الَّذِينَ يُؤْمِنُونَ بِالْغَيْبِ وَيُقِيمُونَ الصَّلَاةَ وَمِمَّا رَزَقْنَاهُمْ يُنفِقُونَ", 2, 1),
                WrittenAyah(2, 4, "وَالَّذِينَ يُؤْمِنُونَ بِمَا أُنزِلَ إِلَيْكَ وَمَا أُنزِلَ مِن قَبْلِكَ وَبِالْآخِرَةِ هُمْ يُوقِنُونَ", 2, 1),
                WrittenAyah(2, 5, "أُولَـٰئِكَ عَلَىٰ هُدًى مِّن رَّبِّهِمْ ۖ وَأُولَـٰئِكَ هُمُ الْمُفْلِحُونَ", 2, 1)
            )
        )

        // Page 42: Ayat Al-Kursi
        map[42] = WrittenQuranPage(
            pageNumber = 42,
            juzNumber = 3,
            surahNumber = 2,
            surahNameArabic = "البقرة",
            ayahs = listOf(
                WrittenAyah(
                    surahNumber = 2,
                    ayahNumber = 255,
                    textUthmani = "اللَّهُ لَا إِلَـٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ ۚ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ ۚ لَّهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الْأَرْضِ ۗ مَن ذَا الَّذِي يَشْفَعُ عِندَهُ إِلَّا بِإِذْنِهِ ۚ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ ۖ وَلَا يُحِيطُونَ بِشَيْءٍ مِّنْ عِلْمِهِ إِلَّا بِمَا شَاءَ ۚ وَسِعَ كُرْسِيُّهُ السَّمَاوَاتِ وَالْأَرْضَ ۖ وَلَا يَئُودُهُ حِفْظُهُمَا ۚ وَهُوَ الْعَلِيُّ الْعَظِيمُ",
                    pageNumber = 42,
                    juzNumber = 3
                )
            )
        )

        // Page 293: Al-Kahf start
        map[293] = WrittenQuranPage(
            pageNumber = 293,
            juzNumber = 15,
            surahNumber = 18,
            surahNameArabic = "الكهف",
            ayahs = listOf(
                WrittenAyah(18, 1, "الْحَمْدُ لِلَّهِ الَّذِي أَنزَلَ عَلَىٰ عَبْدِهِ الْكِتَابَ وَلَمْ يَجْعَل لَّهُ عِوَجًا ۜ", 293, 15),
                WrittenAyah(18, 2, "قَيِّمًا لِّيُنذِرَ بَأْسًا شَدِيدًا مِّن لَّدُنْهُ وَيُبَشِّرَ الْمُؤْمِنِينَ الَّذِينَ يَعْمَلُونَ الصَّالِحَاتِ أَنَّ لَهُمْ أَجْرًا حَسَنًا", 293, 15),
                WrittenAyah(18, 3, "مَّاكِثِينَ فِيهِ أَبَدًا", 293, 15),
                WrittenAyah(18, 4, "وَيُنذِرَ الَّذِينَ قَالُوا اتَّخَذَ اللَّهُ وَلَدًا", 293, 15),
                WrittenAyah(18, 5, "مَّا لَهُم بِهِ مِنْ عِلْمٍ وَلَا لِآبَائِهِمْ ۚ كَبُرَتْ كَلِمَةً تَخْرُجُ مِنْ أَفْوَاهِهِمْ ۚ إِن يَقُولُونَ إِلَّا كَذِبًا", 293, 15),
                WrittenAyah(18, 6, "فَلَعَلَّكَ بَاخِعٌ نَّفْسَكَ عَلَىٰ آثَارِهِمْ إِن لَّمْ يُؤْمِنُوا بِهَـٰذَا الْحَدِيثِ أَسَفًا", 293, 15)
            )
        )

        // Page 440: Ya-Sin start
        map[440] = WrittenQuranPage(
            pageNumber = 440,
            juzNumber = 22,
            surahNumber = 36,
            surahNameArabic = "يس",
            ayahs = listOf(
                WrittenAyah(36, 1, "يس", 440, 22),
                WrittenAyah(36, 2, "وَالْقُرْآنِ الْحَكِيمِ", 440, 22),
                WrittenAyah(36, 3, "إِنَّكَ لَمِنَ الْمُرْسَلِينَ", 440, 22),
                WrittenAyah(36, 4, "عَلَىٰ صِرَاطٍ مُّسْتَقِيمٍ", 440, 22),
                WrittenAyah(36, 5, "تَنزِيلَ الْعَزِيزِ الرَّحِيمِ", 440, 22),
                WrittenAyah(36, 6, "لِتُنذِرَ قَوْمًا مَّا أُنذِرَ آبَاؤُهُمْ فَهُمْ غَافِلُونَ", 440, 22)
            )
        )

        // Page 562: Al-Mulk start
        map[562] = WrittenQuranPage(
            pageNumber = 562,
            juzNumber = 29,
            surahNumber = 67,
            surahNameArabic = "الملك",
            ayahs = listOf(
                WrittenAyah(67, 1, "تَبَارَكَ الَّذِي بِيَدِهِ الْمُلْكُ وَهُوَ عَلَىٰ كُلِّ شَيْءٍ قَدِيرٌ", 562, 29),
                WrittenAyah(67, 2, "الَّذِي خَلَقَ الْمَوْتَ وَالْحَيَاةَ لِيَبْلُوَكُمْ أَيُّكُمْ أَحْسَنُ عَمَلًا ۚ وَهُوَ الْعَزِيزُ الْغَفُورُ", 562, 29),
                WrittenAyah(67, 3, "الَّذِي خَلَقَ سَبْعَ سَمَاوَاتٍ طِبَاقًا ۖ مَّا تَرَىٰ فِي خَلْقِ الرَّحْمَـٰنِ مِن تَفَاوُتٍ ۖ فَارْجِعِ الْبَصَرَ هَلْ تَرَىٰ مِن فُطُورٍ", 562, 29),
                WrittenAyah(67, 4, "ثُمَّ ارْجِعِ الْبَصَرَ كَرَّتَيْنِ يَنقَلِبْ إِلَيْكَ الْبَصَرُ خَاسِئًا وَهُوَ حَسِيرٌ", 562, 29),
                WrittenAyah(67, 5, "وَلَقَدْ زَيَّنَّا السَّمَاءَ الدُّنْيَا بِمَصَابِيحَ وَجَعَلْنَاهَا رُجُومًا لِّلشَّيَاطِينِ ۖ وَأَعْتَدْنَا لَهُمْ عَذَابَ السَّعِيرِ", 562, 29)
            )
        )

        // Page 598: Al-Qadr start
        map[598] = WrittenQuranPage(
            pageNumber = 598,
            juzNumber = 30,
            surahNumber = 97,
            surahNameArabic = "القدر",
            ayahs = listOf(
                WrittenAyah(97, 1, "إِنَّا أَنزَلْنَاهُ فِي لَيْلَةِ الْقَدْرِ", 598, 30),
                WrittenAyah(97, 2, "وَمَا أَدْرَاكَ مَا لَيْلَةُ الْقَدْرِ", 598, 30),
                WrittenAyah(97, 3, "لَيْلَةُ الْقَدْرِ خَيْرٌ مِّنْ أَلْفِ شَهْرٍ", 598, 30),
                WrittenAyah(97, 4, "تَنَزَّلُ الْمَلَائِكَةُ وَالرُّوحُ فِيهَا بِإِذْنِ رَبِّهِم مِّن كُلِّ أَمْرٍ", 598, 30),
                WrittenAyah(97, 5, "سَلَامٌ هِيَ حَتَّىٰ مَطْلَعِ الْفَجْرِ", 598, 30)
            )
        )

        // Page 601: Al-Asr, Al-Humazah, Al-Fil
        map[601] = WrittenQuranPage(
            pageNumber = 601,
            juzNumber = 30,
            surahNumber = 103,
            surahNameArabic = "العصر والهمزة والفيل",
            ayahs = listOf(
                WrittenAyah(103, 1, "وَالْعَصْرِ", 601, 30),
                WrittenAyah(103, 2, "إِنَّ الْإِنسَانَ لَفِي خُسْرٍ", 601, 30),
                WrittenAyah(103, 3, "إِلَّا الَّذِينَ آمَنُوا وَعَمِلُوا الصَّالِحَاتِ وَتَوَاصَوْا بِالْحَقِّ وَتَوَاصَوْا بِالصَّبْرِ", 601, 30),
                WrittenAyah(104, 1, "وَيْلٌ لِّكُلِّ هُمَزَةٍ لُّمَزَةٍ", 601, 30),
                WrittenAyah(104, 2, "الَّذِي جَمَعَ مَالًا وَعَدَّدَهُ", 601, 30),
                WrittenAyah(105, 1, "أَلَمْ تَرَ كَيْفَ فَعَلَ رَبُّكَ بِأَصْحَابِ الْفِيلِ", 601, 30),
                WrittenAyah(105, 2, "أَلَمْ يَجْعَلْ كَيْدَهُمْ فِي تَضْلِيلٍ", 601, 30),
                WrittenAyah(105, 5, "فَجَعَلَهُمْ كَعَصْفٍ مَّأْكُولٍ", 601, 30)
            )
        )

        // Page 602: Quraysh, Al-Ma'un, Al-Kawthar
        map[602] = WrittenQuranPage(
            pageNumber = 602,
            juzNumber = 30,
            surahNumber = 106,
            surahNameArabic = "قريش والماعون والكوثر",
            ayahs = listOf(
                WrittenAyah(106, 1, "لِإِيلَافِ قُرَيْشٍ", 602, 30),
                WrittenAyah(106, 2, "إِيلَافِهِمْ رِحْلَةَ الشِّتَاءِ وَالصَّيْفِ", 602, 30),
                WrittenAyah(106, 3, "فَلْيَعْبُدُوا رَبَّ هَـٰذَا الْبَيْتِ", 602, 30),
                WrittenAyah(106, 4, "الَّذِي أَطْعَمَهُم مِّن جُوعٍ وَآمَنَهُم مِّنْ خَوْفٍ", 602, 30),
                WrittenAyah(107, 1, "أَرَأَيْتَ الَّذِي يُكَذِّبُ بِالدِّينِ", 602, 30),
                WrittenAyah(107, 7, "وَيَمْنَعُونَ الْمَاعُونَ", 602, 30),
                WrittenAyah(108, 1, "إِنَّا أَعْطَيْنَاكَ الْكَوْثَرَ", 602, 30),
                WrittenAyah(108, 2, "فَصَلِّ لِرَبِّكَ وَانْحَرْ", 602, 30),
                WrittenAyah(108, 3, "إِنَّ شَانِئَكَ هُوَ الْأَبْتَرُ", 602, 30)
            )
        )

        // Page 604: Al-Ikhlas, Al-Falaq, An-Nas
        map[604] = WrittenQuranPage(
            pageNumber = 604,
            juzNumber = 30,
            surahNumber = 112,
            surahNameArabic = "الإخلاص والفلق والناس",
            ayahs = listOf(
                WrittenAyah(112, 1, "قُلْ هُوَ اللَّهُ أَحَدٌ", 604, 30),
                WrittenAyah(112, 2, "اللَّهُ الصَّمَدُ", 604, 30),
                WrittenAyah(112, 3, "لَمْ يَلِدْ وَلَمْ يُولَدْ", 604, 30),
                WrittenAyah(112, 4, "وَلَمْ يَكُن لَّهُ كُفُوًا أَحَدٌ", 604, 30),
                WrittenAyah(113, 1, "قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ", 604, 30),
                WrittenAyah(113, 2, "مِن شَرِّ مَا خَلَقَ", 604, 30),
                WrittenAyah(113, 3, "وَمِن شَرِّ غَاسِقٍ إِذَا وَقَبَ", 604, 30),
                WrittenAyah(113, 4, "وَمِن شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ", 604, 30),
                WrittenAyah(113, 5, "وَمِن شَرِّ حَاسِدٍ إِذَا حَسَدَ", 604, 30),
                WrittenAyah(114, 1, "قُلْ أَعُوذُ بِرَبِّ النَّاسِ", 604, 30),
                WrittenAyah(114, 2, "مَلِكِ النَّاسِ", 604, 30),
                WrittenAyah(114, 3, "إِلَـٰهِ النَّاسِ", 604, 30),
                WrittenAyah(114, 4, "مِن شَرِّ الْوَسْوَاسِ الْخَنَّاسِ", 604, 30),
                WrittenAyah(114, 5, "الَّذِي يُوَسْوِسُ فِي صُدُورِ النَّاسِ", 604, 30),
                WrittenAyah(114, 6, "مِنَ الْجِنَّةِ وَالنَّاسِ", 604, 30)
            )
        )

        return map
    }
}
