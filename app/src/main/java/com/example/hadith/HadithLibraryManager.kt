package com.example.hadith

import android.content.Context
import android.content.SharedPreferences
import com.example.quran.book.WrittenQuranCatalog
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
 * Manager and repository for Authentic Seerah & As-Sahihayn Library (السيرة النبوية وصحيح البخاري ومسلم).
 * Features:
 * - Public download of authentic collections from verified repositories
 * - Offline caching in `filesDir/hadith_library/`
 * - Rich bundled authentic seeds (Bukhari, Muslim, and complete milestone chapters of Ar-Raheeq Al-Makhtum)
 * - Kitab & Bab chapter hierarchy navigation
 * - Sanad vs Matn separation
 * - Reading progress memory & bookmarking
 * - Search across all collections
 */
object HadithLibraryManager {

    private const val PREFS_NAME = "muhasib_hadith_library_prefs"
    private const val KEY_PROGRESS_PREFIX = "key_hadith_progress_"
    private const val KEY_LAST_HADITH_PREFIX = "key_last_hadith_"
    private const val KEY_BOOKMARKS = "key_hadith_bookmarks"

    // Verified public repositories
    private const val BUKHARI_PUBLIC_URL = "https://cdn.jsdelivr.net/gh/fawazahmed0/hadith-api@1/editions/ara-bukhari.json"
    private const val MUSLIM_PUBLIC_URL = "https://cdn.jsdelivr.net/gh/fawazahmed0/hadith-api@1/editions/ara-muslim.json"
    private const val SEERAH_PUBLIC_URL = "https://raw.githubusercontent.com/al-maktaba/seerah/main/ar-raheeq.json"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    private val _downloadStates = MutableStateFlow<Map<String, HadithDownloadState>>(emptyMap())
    val downloadStates: StateFlow<Map<String, HadithDownloadState>> = _downloadStates.asStateFlow()

    private val inMemoryHadiths = mutableMapOf<String, List<HadithItem>>()

    private fun getBookFile(context: Context, bookId: String): File {
        val dir = File(context.filesDir, "hadith_library")
        if (!dir.exists()) dir.mkdirs()
        return File(dir, "$bookId.json")
    }

    fun isBookDownloaded(context: Context, bookId: String): Boolean {
        val file = getBookFile(context, bookId)
        return file.exists() && file.length() > 50_000L
    }

    suspend fun downloadBook(context: Context, book: HeritageBook): Boolean = withContext(Dispatchers.IO) {
        val targetFile = getBookFile(context, book.id)
        val tempFile = File(targetFile.parentFile, "${book.id}.tmp")

        _downloadStates.value = _downloadStates.value + (book.id to HadithDownloadState.Downloading(0.05f, "الاتصال بمستودع السنة النبوية الشريفة..."))

        val url = when (book) {
            HeritageBook.SAHIH_BUKHARI -> BUKHARI_PUBLIC_URL
            HeritageBook.SAHIH_MUSLIM -> MUSLIM_PUBLIC_URL
            HeritageBook.SEERAH_NABAWYYAH -> SEERAH_PUBLIC_URL
        }

        try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "AlMuhasib-HadithLibrary/1.0")
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                _downloadStates.value = _downloadStates.value + (book.id to HadithDownloadState.Error("فشل الاتصال بالمستودع (${response.code})"))
                return@withContext false
            }

            val body = response.body ?: run {
                _downloadStates.value = _downloadStates.value + (book.id to HadithDownloadState.Error("استجابة فارغة"))
                return@withContext false
            }

            val totalBytes = body.contentLength()
            var downloaded = 0L

            body.byteStream().use { input ->
                FileOutputStream(tempFile).use { output ->
                    val buffer = ByteArray(8192)
                    var read: Int
                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                        downloaded += read
                        if (totalBytes > 0) {
                            val fraction = (downloaded.toFloat() / totalBytes.toFloat()).coerceIn(0.1f, 0.95f)
                            _downloadStates.value = _downloadStates.value + (book.id to HadithDownloadState.Downloading(
                                fraction,
                                "تحميل الأحاديث الشريفة (${downloaded / 1024} ك.ب)..."
                            ))
                        }
                    }
                    output.flush()
                }
            }

            if (tempFile.exists() && tempFile.length() > 20_000L) {
                if (targetFile.exists()) targetFile.delete()
                tempFile.renameTo(targetFile)
                inMemoryHadiths.remove(book.id)
                loadBookHadiths(context, book)
                _downloadStates.value = _downloadStates.value + (book.id to HadithDownloadState.Ready)
                true
            } else {
                tempFile.delete()
                _downloadStates.value = _downloadStates.value + (book.id to HadithDownloadState.Error("الملف المنزل غير مكتمل"))
                false
            }
        } catch (e: Exception) {
            tempFile.delete()
            _downloadStates.value = _downloadStates.value + (book.id to HadithDownloadState.Error(e.localizedMessage ?: "حدث خطأ أثناء التحميل"))
            false
        }
    }

    /**
     * Loads Hadiths for a specific book.
     */
    suspend fun loadBookHadiths(context: Context, book: HeritageBook): List<HadithItem> = withContext(Dispatchers.IO) {
        inMemoryHadiths[book.id]?.let { return@withContext it }

        val file = getBookFile(context, book.id)
        if (file.exists() && file.length() > 50_000L) {
            try {
                val json = JSONObject(file.readText())
                val hadithsArray = json.optJSONArray("hadiths")
                if (hadithsArray != null) {
                    val list = mutableListOf<HadithItem>()
                    for (i in 0 until hadithsArray.length()) {
                        val obj = hadithsArray.getJSONObject(i)
                        val num = obj.optInt("hadithnumber", i + 1)
                        val text = obj.optString("text", "")
                        val (sanad, matn) = splitSanadAndMatn(text)

                        list.add(
                            HadithItem(
                                id = "${book.id}_$num",
                                bookId = book.id,
                                chapterId = obj.optInt("chapter", 1),
                                hadithNumber = num,
                                chapterTitle = obj.optString("chapterTitle", "كتاب السنة"),
                                babTitle = obj.optString("babTitle", ""),
                                sanad = sanad,
                                matn = matn,
                                grade = obj.optString("grade", "صحيح"),
                                reference = "${book.titleArabic}، حديث رقم $num"
                            )
                        )
                    }
                    inMemoryHadiths[book.id] = list
                    return@withContext list
                }
            } catch (e: Exception) {
                // Fallback to seed on parse error
            }
        }

        val seed = getSeedHadithsForBook(book)
        inMemoryHadiths[book.id] = seed
        seed
    }

    /**
     * Extracts Sanad (chain of narrators) and Matn (noble prophetic text).
     */
    fun splitSanadAndMatn(text: String): Pair<String, String> {
        val markers = listOf(
            "قَالَ: سَمِعْتُ رَسُولَ اللَّهِ",
            "قال: سمعت رسول الله",
            "قَالَ سَمِعْتُ رَسُولَ اللَّهِ",
            "قال سمعت رسول الله",
            "قَالَ: قَالَ رَسُولُ اللَّهِ",
            "قال: قال رسول الله",
            "قَالَ رَسُولُ اللَّهِ",
            "قال رسول الله",
            "أَنَّ رَسُولَ اللَّهِ",
            "أن رسول الله",
            "عَنِ النَّبِيِّ",
            "عن النبي",
            "سَمِعْتُ رَسُولَ اللَّهِ",
            "سمعت رسول الله"
        )

        for (marker in markers) {
            val idx = text.indexOf(marker)
            if (idx in 1..250) {
                val sanad = text.substring(0, idx).trim()
                val matn = text.substring(idx).trim()
                return Pair(sanad, matn)
            }
        }

        // Fallback: look for قَالَ or قال
        val qalaIdx = text.indexOf("قَالَ:")
        if (qalaIdx in 1..200) {
            return Pair(text.substring(0, qalaIdx).trim(), text.substring(qalaIdx).trim())
        }

        return Pair("", text)
    }

    /**
     * Search across loaded collections (synchronous).
     */
    fun searchHadiths(query: String, bookId: String? = null): List<HadithItem> {
        val clean = WrittenQuranCatalog.normalizeArabic(query.trim())
        if (clean.isBlank()) return emptyList()

        val results = mutableListOf<HadithItem>()
        val targetBooks = if (bookId != null) {
            val b = HeritageBook.entries.find { it.id == bookId }
            listOfNotNull(inMemoryHadiths[bookId] ?: b?.let { getSeedHadithsForBook(it) })
        } else {
            HeritageBook.entries.map { b ->
                inMemoryHadiths[b.id] ?: getSeedHadithsForBook(b)
            }
        }

        for (bookList in targetBooks) {
            for (item in bookList) {
                val match = WrittenQuranCatalog.normalizeArabic(item.matn).contains(clean) ||
                        WrittenQuranCatalog.normalizeArabic(item.sanad).contains(clean) ||
                        WrittenQuranCatalog.normalizeArabic(item.babTitle).contains(clean) ||
                        WrittenQuranCatalog.normalizeArabic(item.chapterTitle).contains(clean) ||
                        item.hadithNumber.toString() == clean

                if (match) {
                    results.add(item)
                    if (results.size >= 80) return results
                }
            }
        }
        return results
    }

    /**
     * High-speed asynchronous search across Hadith and Seerah libraries offloaded to Dispatchers.Default.
     */
    suspend fun searchHadithsAsync(query: String, bookId: String? = null): List<HadithItem> = withContext(Dispatchers.Default) {
        searchHadiths(query, bookId)
    }

    // --- SharedPreferences Reading Progress & Bookmarks ---

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveReadingProgress(context: Context, bookId: String, hadithNumber: Int, totalCount: Int) {
        val fraction = if (totalCount > 0) (hadithNumber.toFloat() / totalCount.toFloat()).coerceIn(0f, 1f) else 0f
        getPrefs(context).edit()
            .putFloat(KEY_PROGRESS_PREFIX + bookId, fraction)
            .putInt(KEY_LAST_HADITH_PREFIX + bookId, hadithNumber)
            .apply()
    }

    fun getReadingProgressFraction(context: Context, bookId: String): Float {
        return getPrefs(context).getFloat(KEY_PROGRESS_PREFIX + bookId, 0f)
    }

    fun getLastReadHadithNumber(context: Context, bookId: String): Int {
        return getPrefs(context).getInt(KEY_LAST_HADITH_PREFIX + bookId, 1)
    }

    fun saveBookmark(context: Context, bookmark: HadithBookmark) {
        val current = getBookmarks(context).toMutableList()
        current.removeAll { it.bookId == bookmark.bookId && it.hadithId == bookmark.hadithId }
        current.add(0, bookmark)

        val array = org.json.JSONArray()
        for (b in current) {
            val obj = JSONObject()
            obj.put("id", b.id)
            obj.put("bookId", b.bookId)
            obj.put("hadithId", b.hadithId)
            obj.put("title", b.title)
            obj.put("snippet", b.snippet)
            obj.put("time", b.timestamp)
            array.put(obj)
        }
        getPrefs(context).edit().putString(KEY_BOOKMARKS, array.toString()).apply()
    }

    fun removeBookmark(context: Context, bookmarkId: String) {
        val current = getBookmarks(context).toMutableList()
        current.removeAll { it.id == bookmarkId }

        val array = org.json.JSONArray()
        for (b in current) {
            val obj = JSONObject()
            obj.put("id", b.id)
            obj.put("bookId", b.bookId)
            obj.put("hadithId", b.hadithId)
            obj.put("title", b.title)
            obj.put("snippet", b.snippet)
            obj.put("time", b.timestamp)
            array.put(obj)
        }
        getPrefs(context).edit().putString(KEY_BOOKMARKS, array.toString()).apply()
    }

    fun getBookmarks(context: Context): List<HadithBookmark> {
        val raw = getPrefs(context).getString(KEY_BOOKMARKS, null) ?: return emptyList()
        val list = mutableListOf<HadithBookmark>()
        try {
            val array = org.json.JSONArray(raw)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    HadithBookmark(
                        id = obj.getString("id"),
                        bookId = obj.getString("bookId"),
                        hadithId = obj.getString("hadithId"),
                        title = obj.getString("title"),
                        snippet = obj.getString("snippet"),
                        timestamp = obj.getLong("time")
                    )
                )
            }
        } catch (e: Exception) {
            // Ignore
        }
        return list
    }

    // --- Rich Built-In Seed Datasets ---

    private fun getSeedHadithsForBook(book: HeritageBook): List<HadithItem> {
        return when (book) {
            HeritageBook.SAHIH_BUKHARI -> getBukhariSeeds()
            HeritageBook.SAHIH_MUSLIM -> getMuslimSeeds()
            HeritageBook.SEERAH_NABAWYYAH -> getSeerahSeeds()
        }
    }

    private fun getBukhariSeeds(): List<HadithItem> = listOf(
        HadithItem(
            id = "bukhari_1",
            bookId = HeritageBook.SAHIH_BUKHARI.id,
            chapterId = 1,
            hadithNumber = 1,
            chapterTitle = "كتاب بدء الوحي",
            babTitle = "باب كَيْفَ كَانَ بَدْءُ الْوَحْيِ إِلَى رَسُولِ اللَّهِ ﷺ",
            sanad = "حَدَّثَنَا الْحُمَيْدِيُّ عَبْدُ اللَّهِ بْنُ الزُّبَيْرِ، قَالَ: حَدَّثَنَا سُفْيَانُ، قَالَ: حَدَّثَنَا يَحْيَى بْنُ سَعِيدٍ الأَنْصَارِيُّ، قَالَ: أَخْبَرَنِي مُحَمَّدُ بْنُ إِبْرَاهِيمَ التَّيْمِيُّ، أَنَّهُ سَمِعَ عَلْقَمَةَ بْنَ وَقَّاصٍ اللَّيْثِيَّ، يَقُولُ: سَمِعْتُ عُمَرَ بْنَ الْخَطَّابِ رَضِيَ اللَّهُ عَنْهُ عَلَى الْمِنْبَرِ",
            matn = "قَالَ: سَمِعْتُ رَسُولَ اللَّهِ ﷺ يَقُولُ: «إِنَّمَا الأَعْمَالُ بِالنِّيَّاتِ، وَإِنَّمَا لِكُلِّ امْرِئٍ مَا نَوَى، فَمَنْ كَانَتْ هِجْرَتُهُ إِلَى دُنْيَا يُصِيبُهَا، أَوْ إِلَى امْرَأَةٍ يَنْكِحُهَا، فَهِجْرَتُهُ إِلَى مَا هَاجَرَ إِلَيْهِ».",
            grade = "صحيح متفق عليه",
            reference = "صحيح البخاري، حديث رقم 1"
        ),
        HadithItem(
            id = "bukhari_2",
            bookId = HeritageBook.SAHIH_BUKHARI.id,
            chapterId = 1,
            hadithNumber = 2,
            chapterTitle = "كتاب بدء الوحي",
            babTitle = "باب كَيْفَ كَانَ بَدْءُ الْوَحْيِ إِلَى رَسُولِ اللَّهِ ﷺ",
            sanad = "حَدَّثَنَا عَبْدُ اللَّهِ بْنُ يُوسُفَ، قَالَ: أَخْبَرَنَا مَالِكٌ، عَنْ هِشَامِ بْنِ عُرْوَةَ، عَنْ أَبِيهِ، عَنْ عَائِشَةَ أُمِّ الْمُؤْمِنِينَ رَضِيَ اللَّهُ عَنْهَا",
            matn = "أَنَّ الْحَارِثَ بْنَ هِشَامٍ رَضِيَ اللَّهُ عَنْهُ سَأَلَ رَسُولَ اللَّهِ ﷺ فَقَالَ: يَا رَسُولَ اللَّهِ، كَيْفَ يَأْتِيكَ الْوَحْيُ؟ فَقَالَ رَسُولُ اللَّهِ ﷺ: «أَحْيَانًا يَأْتِينِي مِثْلَ صَلْصَلَةِ الْجَرَسِ، وَهُوَ أَشَدُّهُ عَلَيَّ، فَيُفْصَمُ عَنِّي وَقَدْ وَعَيْتُ عَنْهُ مَا قَالَ، وَأَحْيَانًا يَتَمَثَّلُ لِي الْمَلَكُ رَجُلًا فَيُكَلِّمُنِي فَأَعِي مَا يَقُولُ». قَالَتْ عَائِشَةُ رَضِيَ اللَّهُ عَنْهَا: وَلَقَدْ رَأَيْتُهُ يَنْزِلُ عَلَيْهِ الْوَحْيُ فِي الْيَوْمِ الشَّدِيدِ الْبَرْدِ، فَيَفْصِمُ عَنْهُ وَإِنَّ جَبِينَهُ لَيَتَفَصَّدُ عَرَقًا.",
            grade = "صحيح",
            reference = "صحيح البخاري، حديث رقم 2"
        ),
        HadithItem(
            id = "bukhari_8",
            bookId = HeritageBook.SAHIH_BUKHARI.id,
            chapterId = 2,
            hadithNumber = 8,
            chapterTitle = "كتاب الإيمان",
            babTitle = "باب دُعَاؤُكُمْ إِيمَانُكُمْ",
            sanad = "حَدَّثَنَا عَبْدُ اللَّهِ بْنُ مُوسَى، قَالَ: أَخْبَرَنَا حَنْظَلَةُ بْنُ أَبِي سُفْيَانَ، عَنْ عِكْرِمَةَ بْنِ خَالِدٍ، عَنِ ابْنِ عُمَرَ رَضِيَ اللَّهُ عَنْهُمَا",
            matn = "قَالَ: قَالَ رَسُولُ اللَّهِ ﷺ: «بُنِيَ الإِسْلاَمُ عَلَى خَمْسٍ: شَهَادَةِ أَنْ لاَ إِلَهَ إِلاَّ اللَّهُ وَأَنَّ مُحَمَّدًا رَسُولُ اللَّهِ، وَإِقَامِ الصَّلاَةِ، وَإِيتَاءِ الزَّكَاةِ، وَالحَجِّ، وَصَوْمِ رَمَضَانَ».",
            grade = "صحيح متفق عليه",
            reference = "صحيح البخاري، حديث رقم 8"
        ),
        HadithItem(
            id = "bukhari_9",
            bookId = HeritageBook.SAHIH_BUKHARI.id,
            chapterId = 2,
            hadithNumber = 9,
            chapterTitle = "كتاب الإيمان",
            babTitle = "باب أُمُورُ الإِيمَانِ",
            sanad = "حَدَّثَنَا عَبْدُ اللَّهِ بْنُ مُحَمَّدٍ، قَالَ: حَدَّثَنَا أَبُو عَامِرٍ الْعَقَدِيُّ، قَالَ: حَدَّثَنَا سُلَيْمَانُ بْنُ بِلَالٍ، عَنْ عَبْدِ اللَّهِ بْنِ دِينَارٍ، عَنْ أَبِي صَالِحٍ، عَنْ أَبِي هُرَيْرَةَ رَضِيَ اللَّهُ عَنْهُ، عَنِ النَّبِيِّ ﷺ",
            matn = "قَالَ: «الإِيمَانُ بِضْعٌ وَسِتُّونَ شُعْبَةً، وَالحَيَاءُ شُعْبَةٌ مِنَ الإِيمَانِ».",
            grade = "صحيح",
            reference = "صحيح البخاري، حديث رقم 9"
        ),
        HadithItem(
            id = "bukhari_10",
            bookId = HeritageBook.SAHIH_BUKHARI.id,
            chapterId = 2,
            hadithNumber = 10,
            chapterTitle = "كتاب الإيمان",
            babTitle = "باب المُسْلِمُ مَنْ سَلِمَ المُسْلِمُونَ مِنْ لِسَانِهِ وَيَدِهِ",
            sanad = "حَدَّثَنَا سَعِيدُ بْنُ يَحْيَى بْنِ سَعِيدٍ الْقُرَشِيُّ، قَالَ: حَدَّثَنَا أَبِي، قَالَ: حَدَّثَنَا أَبُو بُرْدَةَ بْنُ عَبْدِ اللَّهِ بْنِ أَبِي بُرْدَةَ، عَنْ أَبِي بُرْدَةَ، عَنْ أَبِي مُوسَى رَضِيَ اللَّهُ عَنْهُ",
            matn = "قَالَ: قَالُوا يَا رَسُولَ اللَّهِ، أَيُّ الإِسْلاَمِ أَفْضَلُ؟ قَالَ: «مَنْ سَلِمَ المُسْلِمُونَ مِنْ لِسَانِهِ وَيَدِهِ».",
            grade = "صحيح متفق عليه",
            reference = "صحيح البخاري، حديث رقم 10"
        ),
        HadithItem(
            id = "bukhari_71",
            bookId = HeritageBook.SAHIH_BUKHARI.id,
            chapterId = 3,
            hadithNumber = 71,
            chapterTitle = "كتاب العلم",
            babTitle = "باب مَنْ يُرِدِ اللَّهُ بِهِ خَيْرًا يُفَقِّهْهُ فِي الدِّينِ",
            sanad = "حَدَّثَنَا سَعِيدُ بْنُ عُفَيْرٍ، قَالَ: حَدَّثَنَا ابْنُ وَهْبٍ، عَنْ يُونُسَ، عَنِ ابْنِ شِهَابٍ، قَالَ: قَالَ حُمَيْدُ بْنُ عَبْدِ الرَّحْمَنِ: سَمِعْتُ مُعَاوِيَةَ خَطِيبًا يَقُولُ: سَمِعْتُ النَّبِيَّ ﷺ",
            matn = "يَقُولُ: «مَنْ يُرِدِ اللَّهُ بِهِ خَيْرًا يُفَقِّهْهُ فِي الدِّينِ، وَإِنَّمَا أَنَا قَاسِمٌ وَاللَّهُ يُعْطِي، وَلَنْ تَزَالَ هَذِهِ الأُمَّةُ قَائِمَةً عَلَى أَمْرِ اللَّهِ، لاَ يَضُرُّهُمْ مَنْ خَالَفَهُمْ، حَتَّى يَأْتِيَ أَمْرُ اللَّهِ».",
            grade = "صحيح",
            reference = "صحيح البخاري، حديث رقم 71"
        ),
        HadithItem(
            id = "bukhari_6412",
            bookId = HeritageBook.SAHIH_BUKHARI.id,
            chapterId = 81,
            hadithNumber = 6412,
            chapterTitle = "كتاب الرقاق",
            babTitle = "باب مَا جَاءَ فِي الرِّقَاقِ وَأَنْ لاَ عَيْشَ إِلَّا عَيْشُ الآخِرَةِ",
            sanad = "حَدَّثَنَا مَكِّيُّ بْنُ إِبْرَاهِيمَ، أَخْبَرَنَا عَبْدُ اللَّهِ بْنُ سَعِيدِ بْنِ أَبِي هِنْدٍ، عَنْ أَبِيهِ، عَنِ ابْنِ عَبَّاسٍ رَضِيَ اللَّهُ عَنْهُمَا",
            matn = "قَالَ: قَالَ النَّبِيُّ ﷺ: «نِعْمَتَانِ مَغْبُونٌ فِيهِمَا كَثِيرٌ مِنَ النَّاسِ: الصِّحَّةُ وَالفَرَاغُ».",
            grade = "صحيح",
            reference = "صحيح البخاري، حديث رقم 6412"
        ),
        HadithItem(
            id = "bukhari_6416",
            bookId = HeritageBook.SAHIH_BUKHARI.id,
            chapterId = 81,
            hadithNumber = 6416,
            chapterTitle = "كتاب الرقاق",
            babTitle = "باب قَوْلِ النَّبِيِّ ﷺ كُنْ فِي الدُّنْيَا كَأَنَّكَ غَرِيبٌ",
            sanad = "حَدَّثَنَا عَلِيُّ بْنُ عَبْدِ اللَّهِ، حَدَّثَنَا مُحَمَّدُ بْنُ عَبْدِ الرَّحْمَنِ أَبُو المُنْذِرِ الطُّفَاوِيُّ، عَنِ الأَعْمَشِ، قَالَ: حَدَّثَنِي مُجَاهِدٌ، عَنْ عَبْدِ اللَّهِ بْنِ عُمَرَ رَضِيَ اللَّهُ عَنْهُمَا",
            matn = "قَالَ: أَخَذَ رَسُولُ اللَّهِ ﷺ بِمَنْكِبِي، فَقَالَ: «كُنْ فِي الدُّنْيَا كَأَنَّكَ غَرِيبٌ أَوْ عَابِرُ سَبِيلٍ». وَكَانَ ابْنُ عُمَرَ يَقُولُ: إِذَا أَمْسَيْتَ فَلاَ تَنْتَظِرِ الصَّبَاحَ، وَإِذَا أَصْبَحْتَ فَلاَ تَنْتَظِرِ المَسَاءَ، وَخُذْ مِنْ صِحَّتِكَ لِمَرَضِكَ، وَمِنْ حَيَاتِكَ لِمَوْتِكَ.",
            grade = "صحيح",
            reference = "صحيح البخاري، حديث رقم 6416"
        )
    )

    private fun getMuslimSeeds(): List<HadithItem> = listOf(
        HadithItem(
            id = "muslim_8",
            bookId = HeritageBook.SAHIH_MUSLIM.id,
            chapterId = 1,
            hadithNumber = 8,
            chapterTitle = "كتاب الإيمان",
            babTitle = "باب بَيَانِ الإِيمَانِ وَالإِسْلاَمِ وَالإِحْسَانِ (حديث جبريل الطويل)",
            sanad = "حَدَّثَنِي زُهَيْرُ بْنُ حَرْبٍ، حَدَّثَنَا جَرِيرٌ، عَنْ عُمَارَةَ، عَنْ أَبِي زُرْعَةَ، عَنْ أَبِي هُرَيْرَةَ، وَعُمَرَ بْنِ الْخَطَّابِ رَضِيَ اللَّهُ عَنْهُ",
            matn = "بَيْنَمَا نَحْنُ عِنْدَ رَسُولِ اللَّهِ ﷺ ذَاتَ يَوْمٍ، إِذْ طَلَعَ عَلَيْنَا رَجُلٌ شَدِيدُ بَيَاضِ الثِّيَابِ، شَدِيدُ سَوَادِ الشَّعَرِ، لاَ يُرَى عَلَيْهِ أَثَرُ السَّفَرِ، وَلاَ يَعْرِفُهُ مِنَّا أَحَدٌ، حَتَّى جَلَسَ إِلَى النَّبِيِّ ﷺ، فَأَسْنَدَ رُكْبَتَيْهِ إِلَى رُكْبَتَيْهِ، وَوَضَعَ كَفَّيْهِ عَلَى فَخِذَيْهِ، وَقَالَ: يَا مُحَمَّدُ أَخْبِرْنِي عَنِ الإِسْلاَمِ، فَقَالَ رَسُولُ اللَّهِ ﷺ: «الإِسْلاَمُ أَنْ تَشْهَدَ أَنْ لاَ إِلَهَ إِلاَّ اللَّهُ وَأَنَّ مُحَمَّدًا رَسُولُ اللَّهِ، وَتُقِيمَ الصَّلاَةَ، وَتُؤْتِيَ الزَّكَاةَ، وَتَصُومَ رَمَضَانَ، وَتَحُجَّ الْبَيْتَ إِنِ اسْتَطَعْتَ إِلَيْهِ سَبِيلًا» قَالَ: صَدَقْتَ. قَالَ: فَأَخْبِرْنِي عَنِ الإِيمَانِ، قَالَ: «أَنْ تُؤْمِنَ بِاللَّهِ، وَمَلاَئِكَتِهِ، وَكُتُبِهِ، وَرُسُلِهِ، وَالْيَوْمِ الآخِرِ، وَتُؤْمِنَ بِالْقَدَرِ خَيْرِهِ وَشَرِّهِ». قَالَ: صَدَقْتَ. قَالَ: فَأَخْبِرْنِي عَنِ الإِحْسَانِ، قَالَ: «أَنْ تَعْبُدَ اللَّهَ كَأَنَّكَ تَرَاهُ، فَإِنْ لَمْ تَكُنْ تَرَاهُ فَإِنَّهُ يَرَاكَ»... ثُمَّ قَالَ ﷺ: «يَا عُمَرُ أَتَدْرِي مَنِ السَّائِلُ؟» قُلْتُ: اللَّهُ وَرَسُولُهُ أَعْلَمُ. قَالَ: «فَإِنَّهُ جِبْرِيلُ أَتَاكُمْ يُعَلِّمُكُمْ دِينَكُمْ».",
            grade = "صحيح مسلم",
            reference = "صحيح مسلم، حديث رقم 8"
        ),
        HadithItem(
            id = "muslim_55",
            bookId = HeritageBook.SAHIH_MUSLIM.id,
            chapterId = 1,
            hadithNumber = 55,
            chapterTitle = "كتاب الإيمان",
            babTitle = "باب بَيَانِ أَنَّ الدِّينَ النَّصِيحَةُ",
            sanad = "حَدَّثَنَا مُحَمَّدُ بْنُ عَبَّادٍ الْمَكِّيُّ، حَدَّثَنَا سُفْيَانُ، قَالَ: قُلْتُ لِسُهَيْلٍ: إِنَّ عَمْرًا حَدَّثَنَا عَنْ قَعْقَاعٍ، عَنْ أَبِيكَ، قَالَ: وَرَجَوْتُ أَنْ يُحَدِّثَنِي بِهِ، قَالَ: فَقَالَ سَمِعْتُهُ مِنْ الَّذِي سَمِعَهُ مِنْهُ أَبِي، كَانَ صَدِيقًا لَهُ بِالشَّامِ، ثُمَّ حَدَّثَنَا سُفْيَانُ عَنْ سُهَيْلٍ، عَنْ عَطَاءِ بْنِ يَزِيدَ، عَنْ تَمِيمٍ الدَّارِيِّ",
            matn = "أَنَّ النَّبِيَّ ﷺ قَالَ: «الدِّينُ النَّصِيحَةُ» قُلْنَا: لِمَنْ؟ قَالَ: «لِلَّهِ وَلِكِتَابِهِ وَلِرَسُولِهِ وَلأَئِمَّةِ الْمُسْلِمِينَ وَعَامَّتِهِمْ».",
            grade = "صحيح مسلم",
            reference = "صحيح مسلم، حديث رقم 55"
        ),
        HadithItem(
            id = "muslim_223",
            bookId = HeritageBook.SAHIH_MUSLIM.id,
            chapterId = 2,
            hadithNumber = 223,
            chapterTitle = "كتاب الطهارة",
            babTitle = "باب فَضْلِ الْوُضُوءِ",
            sanad = "حَدَّثَنَا إِسْحَاقُ بْنُ مَنْصُورٍ، حَدَّثَنَا حَبَّانُ بْنُ هِلاَلٍ، حَدَّثَنَا أَبَانُ، حَدَّثَنَا يَحْيَى، أَنَّ زَيْدًا حَدَّثَهُ، أَنَّ أَبَا سَلاَّمٍ حَدَّثَهُ، عَنْ أَبِي مَالِكٍ الأَشْعَرِيِّ",
            matn = "قَالَ: قَالَ رَسُولُ اللَّهِ ﷺ: «الطُّهُورُ شَطْرُ الإِيمَانِ، وَالْحَمْدُ لِلَّهِ تَمْلأُ الْمِيزَانَ، وَسُبْحَانَ اللَّهِ وَالْحَمْدُ لِلَّهِ تَمْلآنِ - أَوْ تَمْلأُ - مَا بَيْنَ السَّمَاوَاتِ وَالأَرْضِ، وَالصَّلاَةُ نُورٌ، وَالصَّدَقَةُ بُرْهَانٌ، وَالصَّبْرُ ضِيَاءٌ، وَالْقُرْآنُ حُجَّةٌ لَكَ أَوْ عَلَيْكَ، كُلُّ النَّاسِ يَغْدُو فَبَائِعٌ نَفْسَهُ فَمُعْتِقُهَا أَوْ مُوبِقُهَا».",
            grade = "صحيح مسلم",
            reference = "صحيح مسلم، حديث رقم 223"
        ),
        HadithItem(
            id = "muslim_2586",
            bookId = HeritageBook.SAHIH_MUSLIM.id,
            chapterId = 45,
            hadithNumber = 2586,
            chapterTitle = "كتاب البر والصلة والآداب",
            babTitle = "باب تَرَاحُمِ الْمُؤْمِنِينَ وَتَعَاطُفِهِمْ وَتَعَاضُدِهِمْ",
            sanad = "حَدَّثَنَا أَبُو بَكْرِ بْنُ أَبِي شَيْبَةَ، حَدَّثَنَا عَبْدُ اللَّهِ بْنُ نُمَيْرٍ، وَأَبُو مُعَاوِيَةَ، عَنِ الأَعْمَشِ، عَنِ الشَّعْبِيِّ، عَنِ النُّعْمَانِ بْنِ بَشِيرٍ",
            matn = "قَالَ: قَالَ رَسُولُ اللَّهِ ﷺ: «مَثَلُ الْمُؤْمِنِينَ فِي تَوَادِّهِمْ، وَتَرَاحُمِهِمْ، وَتَعَاطُفِهِمْ مَثَلُ الْجَسَدِ إِذَا اشْتَكَى مِنْهُ عُضْوٌ تَدَاعَى لَهُ سَائِرُ الْجَسَدِ بِالسَّهَرِ وَالْحُمَّى».",
            grade = "صحيح متفق عليه",
            reference = "صحيح مسلم، حديث رقم 2586"
        )
    )

    private fun getSeerahSeeds(): List<HadithItem> = listOf(
        HadithItem(
            id = "seerah_1",
            bookId = HeritageBook.SEERAH_NABAWYYAH.id,
            chapterId = 1,
            hadithNumber = 1,
            chapterTitle = "الفصل الأول: النسب الشريف والمولد المبارك",
            babTitle = "نسب سيد المرسلين ومولده في عام الفيل",
            sanad = "رواية الثقات وأئمة السير والمغازي، مستندة إلى نصوص الصحيحين والقرآن العظيم",
            matn = "هو سيّد الخلق وإمام الأنبياء، أبو القاسم محمّد بن عبد الله بن عبد المطلب بن هاشم بن عبد مناف بن قصي بن كلاب، ينتهي نسبه الشريف إلى إسماعيل بن إبراهيم الخليل عليهما الصلاة والسلام.\n\nوُلِدَ النبي ﷺ في صبيحة يوم الإثنين التاسع من شهر ربيع الأول في عام الفيل، بمكة المكرمة في دار بني هاشم. ولم يكن لأمه آمنة بنت وهب أن ترى ما تراه الحوامل من ثقل ونصب، وحين وضعته خرج منه نور أضاءت له قصور الشام في بصرى. وقد توفي والده عبد الله وهو جنين في بطن أمه، فكفله جده عبد المطلب وحماه وسمّاه محمداً رجاء أن يُحمد في الأرض والسماء.",
            grade = "متواتر ومجمع عليه بين أئمة الأمة",
            reference = "الرحيق المختوم، ص 54",
            historicalMilestone = "عام الفيل (٥٧١ م)"
        ),
        HadithItem(
            id = "seerah_2",
            bookId = HeritageBook.SEERAH_NABAWYYAH.id,
            chapterId = 2,
            hadithNumber = 2,
            chapterTitle = "الفصل الثاني: الرضاعة ونشأة الطهر",
            babTitle = "في ديار بني سعد وحادثة شق الصدر الشريف",
            sanad = "أخرجه الإمام مسلم في صحيحه عن أنس بن مالك رضي الله عنه",
            matn = "أُرضع النبي ﷺ أولاً من ثويبة مولاة أبي لهب، ثم التمست له أمه المراضع فأخذته حليمة السعدية إلى بادية بني سعد، فحلّت البركة في ديارها ومواشيها، وعاش ﷺ في البادية أربع سنوات يتكلم أفصح العربية وأعذبها.\n\nوفي سن الرابعة وقعت له حادثة شق الصدر المباركة؛ حيث أتاه جبريل عليه السلام وهو يلعب مع الغلمان، فشق صدره واستخرج قلبه وغسله في طست من ذهب بماء زمزم، ونزع منه علقة سوداء وقال: «هذا حظ الشيطان منك»، ثم لَأَمَهُ وأعاده إلى مكانه، فرجع الغلمان إلى أمه بالرضاع خائفين وقالوا: إن محمداً قد قُتل! فاستقبلته وهو منتقع اللون، فعاد سالماً معافى ومحفوظاً من كل سوء.",
            grade = "صحيح مسلم (١٦٢)",
            reference = "الرحيق المختوم، ص 58",
            historicalMilestone = "البادية (٥٧٥ م)"
        ),
        HadithItem(
            id = "seerah_3",
            bookId = HeritageBook.SEERAH_NABAWYYAH.id,
            chapterId = 3,
            hadithNumber = 3,
            chapterTitle = "الفصل الثالث: كفالة أبي طالب وحلف الفضول",
            babTitle = "وفاة الأم والجد، رعاية العم، وحلف نصرة المظلوم",
            sanad = "أئمة السير والمغازي، وسنن البيهقي وأحمد",
            matn = "توفيت أمه آمنة بالأبواء وهو في السادسة من عمره، ثم لحق بها جده عبد المطلب وهو في الثامنة، فكفله عمه الشفيق أبو طالب وقدمه على أولاده وأحبه حباً شديداً. ورعى ﷺ الغنم لأهل مكة على قراريط ليعين عمه، فتعلم الصبر والسكينة والحكمة.\n\nوفي العشرين من عمره شهد حلف الفضول في دار عبد الله بن جدعان، حيث تعاهدت بطون قريش على ألا يجدوا بمكة مظلوماً إلا قاموا معه حتى ترد مظلمته. وقد قال ﷺ عنه بعد النبوة: «لَقَدْ شَهِدْتُ فِي دَارِ عَبْدِ اللَّهِ بْنِ جُدْعَانَ حِلْفًا مَا أُحِبُّ أَنَّ لِيَ بِهِ حُمْرَ النَّعَمِ، وَلَوْ أُدْعَى بِهِ فِي الإِسْلاَمِ لأَجَبْتُ».",
            grade = "حسن وصحيح بشواهده",
            reference = "الرحيق المختوم، ص 62",
            historicalMilestone = "حلف الفضول (٥٩٠ م)"
        ),
        HadithItem(
            id = "seerah_4",
            bookId = HeritageBook.SEERAH_NABAWYYAH.id,
            chapterId = 4,
            hadithNumber = 4,
            chapterTitle = "الفصل الرابع: التجارة إلى الشام والزواج المبارك",
            babTitle = "رحلة بصرى الشام مع ميسرة، والاقتران بالسيدة خديجة رضي الله عنها",
            sanad = "توثيق ابن إسحاق وابن هشام والمباركفوري",
            matn = "لُقّب النبي ﷺ في شبابه بالصادق الأمين لما اشتُهر به من عفة وطهارة ووفاء بالعهود. ولما بلغ خمساً وعشرين سنة عرضت عليه السيدة خديجة بنت خويلد رضي الله عنها أن يخرج بمالها في تجارة إلى بصرى الشام بصحبة غلامها ميسرة.\n\nفرأى ميسرة في تلك الرحلة من كراماته وأمانته وبركته ما بهر لبه، وعاد بربح وفير لم يُعهد مثله. فلما حدث ميسرة سيدته بما رأى، رغبت خديجة وهي سيدة نساء قريش حسَباً وشرفاً في الاقتران به، فخطبها أعمامه وتزوجها ﷺ، وكانت له نعم الزوجة والوزير الصادق والسكن المؤيد.",
            grade = "مشهور ومعتمد عند أهل السير",
            reference = "الرحيق المختوم، ص 65",
            historicalMilestone = "الزواج المبارك (٥٩٥ م)"
        ),
        HadithItem(
            id = "seerah_5",
            bookId = HeritageBook.SEERAH_NABAWYYAH.id,
            chapterId = 5,
            hadithNumber = 5,
            chapterTitle = "الفصل الخامس: إعادة بناء الكعبة والتحكيم الفذ",
            babTitle = "حكم وضع الحجر الأسود وحقن دماء قبائل قريش",
            sanad = "مسند أحمد وسنن أئمة المغازي",
            matn = "لما بلغ النبي ﷺ خمساً وثلاثين سنة، تصدعت جدران الكعبة المشرفة بسيل عارم، فأجمعت قريش على إعادة بنائها وألا يُدخلوا في بنائها إلا مالاً حلالاً طيباً.\n\nفلما انتهى البناء إلى موضع الحجر الأسود تنازعوا فيمن يحظى بشرف وضعه، وكادت تشتعل بينهم حرب ضروس أعدوا لها السلاح، حتى اتفقوا على تحكيم أول داخل من باب الصفا، فكان رسول الله ﷺ، فلما رأوه هتفوا: «هَذَا الأَمِينُ، رَضِينَا، هَذَا مُحَمَّدٌ». فطلب رداءً فبسطه، وأخذ الحجر ووضعه فيه بيده الكريمة، ثم أمر رؤساء القبائل أن يأخذوا جميعاً بأطراف الرداء فرفعوه، ثم تناوله هو ووضعه في مكانه، فحقن الله به دماءهم بحكمته الفريدة.",
            grade = "صحيح وثابت تاريخياً",
            reference = "الرحيق المختوم، ص 68",
            historicalMilestone = "بناء الكعبة (٦٠٥ م)"
        ),
        HadithItem(
            id = "seerah_6",
            bookId = HeritageBook.SEERAH_NABAWYYAH.id,
            chapterId = 6,
            hadithNumber = 6,
            chapterTitle = "الفصل السادس: في غار حراء ومبتدأ نزول الوحي",
            babTitle = "التحنث في جبل النور، ونزول: اقْرَأْ بِاسْمِ رَبِّكَ",
            sanad = "صحيح البخاري (حديث رقم 3) وصحيح مسلم عن عائشة أم المؤمنين",
            matn = "حُبّب إلى النبي ﷺ الخلوة والانقطاع عن أوثان الجاهلية ورذائلها، فكان يخلو بغار حراء في جبل النور الليالي ذوات العدد يتحنث ويتعبد ويتأمل في عظمة الخالق سبحانه.\n\nحتى إذا فاجأه الحق في شهر رمضان المبارك وهو ابن أربعين سنة، جاءه الروح الأمين جبريل عليه السلام فقال له: «اقْرَأْ»، قال: «مَا أَنَا بِقَارِئٍ»، فأخذه فغطّه حتى بلغ منه الجهد ثم أرسله، وتكرر ذلك ثلاثاً، ثم قال:\n\n﴿اقْرَأْ بِاسْمِ رَبِّكَ الَّذِي خَلَقَ ۝ خَلَقَ الْإِنسَانَ مِنْ عَلَقٍ ۝ اقْرَأْ وَرَبُّكَ الْأَكْرَمُ ۝ الَّذِي عَلَّمَ بِالْقَلَمِ ۝ عَلَّمَ الْإِنسَانَ مَا لَمْ يَعْلَمْ﴾.\n\nفرجع بها ترجف بوادره إلى خديجة فقال: «زَمِّلُونِي»، فثبتته قائلة: «كَلَّا، فَوَاللَّهِ لَا يُخْزِيكَ اللَّهُ أَبَدًا؛ إِنَّكَ لَتَصِلُ الرَّحِمَ، وَتَصْدُقُ الْحَدِيثَ، وَتَحْمِلُ الْكَلَّ، وَتَكْسِبُ الْمَعْدُومَ، وَتَقْرِي الضَّيْفَ، وَتُعِينُ عَلَى نَوَائِبِ الْحَقِّ».",
            grade = "صحيح متفق عليه",
            reference = "صحيح البخاري، حديث رقم 3",
            historicalMilestone = "مبتدأ الوحي (رمضان ٦١٠ م)"
        ),
        HadithItem(
            id = "seerah_7",
            bookId = HeritageBook.SEERAH_NABAWYYAH.id,
            chapterId = 7,
            hadithNumber = 7,
            chapterTitle = "الفصل السابع: الدعوة السرية والجهر بالحق",
            babTitle = "السابقون الأولون، دار الأرقم، ونداء جبل الصفا",
            sanad = "الصحيحان وأهل السير والمغازي",
            matn = "بدأ النبي ﷺ دعوته سراً لثلاث سنين، فكان أول من آمن من النساء خديجة، ومن الرجال أبو بكر الصديق، ومن الصبيان علي بن أبي طالب، ومن الموالي زيد بن حارثة، ومن الأرقاء بلال بن رباح. وكانت دار الأرقم بن أبي الأرقم منطلق الإيمان والتربية وتدارس القرآن.\n\nحتى نزل قوله تعالى: ﴿وَأَنذِرْ عَشِيرَتَكَ الْأَقْرَبِينَ﴾ و﴿فَاصْدَعْ بِمَا تُؤْمَرُ﴾، فصعد ﷺ جبل الصفا وهتف: «يَا صَبَاحَاهُ!» فاجتمعت إليه قريش، فقال: «أَرَأَيْتَكُمْ لَوْ أَخْبَرْتُكُمْ أَنَّ خَيْلًا بِالْوَادِي تُرِيدُ أَنْ تُغِيرَ عَلَيْكُمْ، أَكُنْتُمْ مُصَدِّقِيَّ؟» قَالُوا: نَعَمْ، مَا جَرَّبْنَا عَلَيْكَ إِلَّا صِدْقًا. قَالَ: «فَإِنِّي نَذِيرٌ لَكُمْ بَيْنَ يَدَيْ عَذَابٍ شَدِيدٍ»، فبدأت قريش مواجهتها بالعداء والتكذيب والتنكيل بالمستضعفين.",
            grade = "صحيح متفق عليه",
            reference = "صحيح البخاري، حديث رقم 4770",
            historicalMilestone = "الصدع بالدعوة (٦١٣ م)"
        ),
        HadithItem(
            id = "seerah_8",
            bookId = HeritageBook.SEERAH_NABAWYYAH.id,
            chapterId = 8,
            hadithNumber = 8,
            chapterTitle = "الفصل الثامن: الهجرة إلى الحبشة ومقاطعة الشّعب",
            babTitle = "صبر المؤمنين، عدالة النجاشي، وصحيفة الحصار الجائر",
            sanad = "سيرة ابن هشام ومسند أحمد بإسناد صحيح",
            matn = "حين اشتد أذى قريش وتعذيبها لآل ياسر وبلال وخباب، قال ﷺ لأصحابه: «لَوْ خَرَجْتُمْ إِلَى أَرْضِ الْحَبَشَةِ، فَإِنَّ بِهَا مَلِكًا لاَ يُظْلَمُ عِنْدَهُ أَحَدٌ»، فهاجر الرعيل الأول وقرأ جعفر بن أبي طالب سورة مريم بحضرة النجاشي فبكى الملك حتى اخضلت لحيته وقال: «إن هذا والذي جاء به عيسى ليخرج من مشكاة واحدة»، وردهم آمنين مكرّمين.\n\nولما رأت قريش ثبات المسلمين كتبت صحيفة ظالمة بمقاطعة بني هاشم وبني المطلب في شِعب أبي طالب، فقطعوا عنهم الطعام والشراب ثلاث سنين حتى أكلوا أوراق الشجر، ثم أرسل الله حشرة الأَرَضَة فأكلت الصحيفة الظالمة إلا ما كان فيها من ذكر اسم الله، فانفك الحصار بالحق المبين.",
            grade = "صحيح ومشهور",
            reference = "الرحيق المختوم، ص 105",
            historicalMilestone = "الهجرة للحبشة والحصار (٦١٥ - ٦١٩ م)"
        ),
        HadithItem(
            id = "seerah_9",
            bookId = HeritageBook.SEERAH_NABAWYYAH.id,
            chapterId = 9,
            hadithNumber = 9,
            chapterTitle = "الفصل التاسع: عام الحزن، الطائف، والإسراء والمعراج",
            babTitle = "وفاة خديجة وأبي طالب، رمية الطائف بالحجارة، والرحلة السماوية",
            sanad = "صحيح البخاري ومسلم والقرآن الكريم (سورة الإسراء والنجم)",
            matn = "توفي أبو طالب والسيدة خديجة في عام واحد فسُمّي «عام الحزن». فخرج النبي ﷺ إلى الطائف يرجو نصرة ثقيف، فردوه بأقبح رد وأغروا به سفهاءهم فرموه بالحجارة حتى دميت قدماه الشريفتان، فلجأ إلى بستان ودعا دعاءه الخالد المليء بالرضا والعبودية: «اللَّهُمَّ إِلَيْكَ أَشْكُو ضَعْفَ قُوَّتِي، وَقِلَّةَ حِيلَتِي، وَهَوَانِي عَلَى النَّاسِ... إِنْ لَمْ يَكُنْ بِكَ عَلَيَّ غَضَبٌ فَلاَ أُبَالِي».\n\nوبعد هذا البلاء العظيم جاء التكريم الإلهي الأكبر؛ فأُسري به ﷺ ليلاً من المسجد الحرام إلى المسجد الأقصى، ثم عُرِج به إلى السموات العُلا وسدرة المنتهى، وفرض الله عليه وعلى أمته الصلوات الخمس في اليوم والليلة، ورأى من آيات ربه الكبرى وعاد في ليلته ثابتاً منصوراً.",
            grade = "صحيح متفق عليه وتواتر في القرآن والسنة",
            reference = "الرحيق المختوم، ص 130",
            historicalMilestone = "الإسراء والمعراج (٦٢١ م)"
        ),
        HadithItem(
            id = "seerah_10",
            bookId = HeritageBook.SEERAH_NABAWYYAH.id,
            chapterId = 10,
            hadithNumber = 10,
            chapterTitle = "الفصل العاشر: بيعتا العقبة والهجرة النبوية المباركة",
            babTitle = "لقاء يثرب، حماية غار ثور، ودخول طيبة الطيبة",
            sanad = "الصحيحان وسيرة ابن إسحاق",
            matn = "عرض النبي ﷺ نفسه على وفود الحجيج فبايعه ستة من أهل يثرب، ثم كانت بيعة العقبة الأولى، وتلتها بيعة العقبة الكبرى مع ثلاثة وسبعين رجلاً وامرأتين من الأوس والخزرج عاهدوه على السمع والطاعة والنصرة.\n\nفلما أجمعت قريش في دار الندوة على قتله، أمره ربه بالهجرة، فخرج ومعه الصديق أبو بكر وترك علياً في فراشه ليرد الودائع. وكمنا في غار ثور ثلاثة أيام تحوطهما العناية الربانية، وقال ﷺ لرفيقه الخائف: «يَا أَبَا بَكْرٍ، مَا ظَنُّكَ بِاثْنَيْنِ اللَّهُ ثَالِثُهُمَا؟».\n\nووصل الموكب النبوي المدينة المنورة فأشرق بنوره كل شيء، واستقبلته الأنصار بالأناشيد والبشرى، فبدأت أعظم مرحلة في تاريخ الإسلام وبناء الأمة الشاهدة.",
            grade = "متواتر قطعي الثبوت",
            reference = "الرحيق المختوم، ص 165",
            historicalMilestone = "الهجرة النبوية (١ هـ / ٦٢٢ م)"
        ),
        HadithItem(
            id = "seerah_11",
            bookId = HeritageBook.SEERAH_NABAWYYAH.id,
            chapterId = 11,
            hadithNumber = 11,
            chapterTitle = "الفصل الحادي عشر: بناء المجتمع المدني والدولة",
            babTitle = "المسجد النبوي الشريف، المؤاخاة التاريخية، ووثيقة المدينة",
            sanad = "أهل المغازي والسنن الكبرى",
            matn = "كان أول عمل لرسول الله ﷺ في المدينة تأسيس المسجد النبوي الشريف، ليكون مركزاً للعبادة ومقراً للشورى ومدرسة للعلم ومنطلقاً للأخوة والتكافل. وشارك ﷺ أصحابه بيده الشريفة في حمل اللبن والحجارة.\n\nثم آخى ﷺ بين المهاجرين والأنصار في دار أنس بن مالك؛ في مشهد إيثار وتضحية فريد تقاسم فيه الأنصاري ماله وبيته مع أخيه المهاجر دون منٍّ أو أذى.\n\nثم كتب وثيقة المدينة الدستورية التاريخية التي نظمت العلاقات بين المسلمين من جهة، وبينهم وبين يهود المدينة والقبائل المجاورة من جهة أخرى، مرسخة العدل المطلق، وحرية الاعتقاد، وحرمة الدماء والأموال، والدفاع المشترك عن الوطن.",
            grade = "صحيح وثابت تاريخياً",
            reference = "الرحيق المختوم، ص 180",
            historicalMilestone = "تأسيس المجتمع المدني (١ هـ / ٦٢٣ م)"
        ),
        HadithItem(
            id = "seerah_12",
            bookId = HeritageBook.SEERAH_NABAWYYAH.id,
            chapterId = 12,
            hadithNumber = 12,
            chapterTitle = "الفصل الثاني عشر: غزوة بدر الكبرى وغزوة أحد",
            babTitle = "يوم الفرقان الخالد، ودرس طاعة الأمر النبوي في أحد",
            sanad = "القرآن الكريم (سورة الأنفال وآل عمران) والصحيحان",
            matn = "في السابع عشر من رمضان في السنة الثانية للهجرة وقعت غزوة بدر الكبرى (يوم الفرقان). وقف ثلاثمائة وبضعة عشر مسلماً أمام ألف مقاتل من صناديد قريش، فاستغاث النبي ﷺ ربه طوال الليل رافعاً يديه، فأنزل الله ملائكة مردفين ونصر عبده وأعز جنده وقتل قادة الشرك كأبي جهل وأمية بن خلف.\n\nوفي شوال من السنة الثالثة كانت غزوة أحد؛ حيث التقى الجيشان وقاتل المسلمون بشجاعة وكسروا شوكة العدو، فلما خالف الرماة أمر النبي ﷺ بالبقاء على جبل عينين ونزلوا لجمع الغنائم، استدار فرسان قريش بقيادة خالد بن الوليد وأصابوا المسلمين واستشهد سبعون من خيار الصحابة كحمزة بن عبد المطلب ومصعب بن عمير، وشُجَّ وجه النبي ﷺ وكُسرت رباعيته، فكانت موعظة ربانية بليغة في شؤم معصية القائد.",
            grade = "متواتر ومفصل في القرآن العظيم",
            reference = "الرحيق المختوم، ص 210",
            historicalMilestone = "بدر وأحد (٢ - ٣ هـ)"
        ),
        HadithItem(
            id = "seerah_13",
            bookId = HeritageBook.SEERAH_NABAWYYAH.id,
            chapterId = 13,
            hadithNumber = 13,
            chapterTitle = "الفصل الثالث عشر: غزوة الخندق وصلح الحديبية",
            babTitle = "حصار الأحزاب، فكرة سلمان الفارسي، والفتح المبين في الحديبية",
            sanad = "القرآن الكريم (سورة الأحزاب والفتح) والصحيحان",
            matn = "في شوال من السنة الخامسة تجمع عشرة آلاف مقاتل من قريش وغطفان وقبائل العرب بتحريض من يهود خيبر لاستئصال المسلمين في المدينة. فأشار سلمان الفارسي بحفر خندق يحمي المدينة، وشارك النبي ﷺ في الحفر وضرب المعول. وحوصرت المدينة في برد شديد وزلزال، حتى أرسل الله ريحاً وجنوداً كفأت قدورهم واقتلعت خيامهم وقذف الرعب في قلوبهم فانصرفوا.\n\nوفي ذي القعدة من السنة السادسة خرج ﷺ مع ألف وأربعمائة صحابي معتمرين، فصدتهم قريش عند الحديبية. فتمت بيعة الرضوان تحت الشجرة، وعُقد صلح الحديبية الذي بدا في ظاهره إجحافاً لكنه كان نصراً عظيماً وفتحاً مبيناً، حيث أتاح حرية الدعوة ودخل الناس في دين الله أفواجاً، وأرسل ﷺ رسائل الدعوة إلى ملوك كسرى وقيصر والمقوقس والنجاشي.",
            grade = "متواتر وقطعي الثبوت",
            reference = "الرحيق المختوم، ص 280",
            historicalMilestone = "الأحزاب والحديبية (٥ - ٦ هـ)"
        ),
        HadithItem(
            id = "seerah_14",
            bookId = HeritageBook.SEERAH_NABAWYYAH.id,
            chapterId = 14,
            hadithNumber = 14,
            chapterTitle = "الفصل الرابع عشر: فتح مكة الأعظم وتطهير البيت الحرام",
            babTitle = "دخول مكة فاتحاً متواضعاً، تحطيم الأصنام، وإعلان العفو العام",
            sanad = "صحيح البخاري وصحيح مسلم والمغازي",
            matn = "نقضت قريش عهد الحديبية بإعانتها بني بكر على خزاعة حلفاء المسلمين، فجهز رسول الله ﷺ جيشاً من عشرة آلاف مقاتل وسار إلى مكة في رمضان سنة ثمان للهجرة.\n\nودخل النبي ﷺ مكة فاتحاً رأسه مطأطئ تواضعاً لله وعظمة لانتصاره، حتى كادت لحيته تمس قربوس سرجه، وطاف بالبيت الحرام وحطم ثلاثمائة وستين صنماً وهو يقرأ: ﴿وَقُلْ جَاءَ الْحَقُّ وَزَهَقَ الْبَاطِلُ إِنَّ الْبَاطِلَ كَانَ زَهُوقًا﴾.\n\nثم وقف أهل مكة يترقبون حكمه فيهم بعد عشرين عاماً من التعذيب والطرد والحروب، فقال لهم: «يَا مَعْشَرَ قُرَيْشٍ، مَا تَرَوْنَ أَنِّي فَاعِلٌ بِكُمْ؟» قَالُوا: خَيْرًا، أَخٌ كَرِيمٌ وَابْنُ أَخٍ كَرِيمٍ. فَقَالَ كلمته الخالدة في تاريخ البشرية: «اذْهَبُوا فَأَنْتُمُ الطُّلَقَاءُ»، فدخل أهل مكة في دين الله راضين مطمئنين.",
            grade = "صحيح متفق عليه",
            reference = "الرحيق المختوم، ص 380",
            historicalMilestone = "فتح مكة (رمضان ٨ هـ / ٦٣٠ م)"
        ),
        HadithItem(
            id = "seerah_15",
            bookId = HeritageBook.SEERAH_NABAWYYAH.id,
            chapterId = 15,
            hadithNumber = 15,
            chapterTitle = "الفصل الخامس عشر: حجة الوداع ووفاة خير الأنام ﷺ",
            babTitle = "إكمال الدين، إعلان حقوق الإنسان، ووداع الرفيق الأعلى",
            sanad = "الصحيحان وسنن الأئمة ومجمع الزوائد",
            matn = "في السنة العاشرة للهجرة أذن رسول الله ﷺ في الناس بالحج، فاجتمع معه أكثر من مائة ألف من الصحابة في حجة الوداع. وخطب على جبل عرفات خطبته الشاملة التي قرر فيها حرمة الدماء والأموال والأعراض، وحرم الربا وثارات الجاهلية، وأوصى بالنساء خيراً وأوصى بالتمسك بكتاب الله وسنته.\n\nوأنزل الله تعالى على نبيه في ذلك اليوم المشهود: ﴿الْيَوْمَ أَكْمَلْتُ لَكُمْ دِينَكُمْ وَأَتْمَمْتُ عَلَيْكُمْ نِعْمَتِي وَرَضِيتُ لَكُمُ الْإِسْلَامَ دِينًا﴾.\n\nوفي أوائل صفر سنة إحدى عشرة للهجرة بدأ المرض برسول الله ﷺ، حتى اشتد به الوجع في بيت أم المؤمنين عائشة رضي الله عنها، وفي ضحى يوم الإثنين الثاني عشر من ربيع الأول رفع إصبعه الشريفة إلى السماء وكان آخر كلامه رطباً بذكر الله: «بَلِ الرَّفِيقَ الأَعْلَى مِنَ الْجَنَّةِ... مَعَ الَّذِينَ أَنْعَمَ اللَّهُ عَلَيْهِمْ مِنَ النَّبِيِّينَ وَالصِّدِّيقِينَ وَالشُّهَدَاءِ وَالصَّالِحِينَ». فصعدت روحه الطاهرة الزكية إلى بارئها بعد أن بلغ الرسالة وأدى الأمانة ونصح الأمة وتركها على المحجة البيضاء ليلها كنهارها لا يزيغ عنها إلا هالك.",
            grade = "متواتر وقطعي المرويات عند كافة أئمة المسلمين",
            reference = "الرحيق المختوم، ص 440",
            historicalMilestone = "حجة الوداع والوفاة (١٠ - ١١ هـ)"
        )
    )
}

