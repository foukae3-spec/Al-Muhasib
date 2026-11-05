package com.example.quran

import android.content.Context
import android.os.PowerManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

/**
 * Progress tracker for batch full-Quran downloading.
 */
data class BatchDownloadProgress(
    val reciterId: Int = 0,
    val completedCount: Int = 0,
    val totalCount: Int = 114,
    val currentSurahName: String = "",
    val totalBytesDownloaded: Long = 0L,
    val isCancelled: Boolean = false
) {
    val progressFraction: Float
        get() = if (totalCount > 0) (completedCount.toFloat() / totalCount.toFloat()).coerceIn(0f, 1f) else 0f
}

/**
 * Manages downloading Quran audio tracks (single surahs or full 114 surahs with one button)
 * for offline listening.
 */
class QuranDownloadManager private constructor(private val appContext: Context) {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val powerManager = appContext.getSystemService(Context.POWER_SERVICE) as? PowerManager
    private val downloadWakeLock = powerManager?.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "AlMuhasib:QuranBatchDownloadWakeLock")?.apply {
        setReferenceCounted(false)
    }

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    // State key: "${reciterId}_${surahNumber}"
    private val _downloadStates = MutableStateFlow<Map<String, SurahDownloadState>>(emptyMap())
    val downloadStates: StateFlow<Map<String, SurahDownloadState>> = _downloadStates.asStateFlow()

    private val _downloadedSurahsCache = MutableStateFlow<Map<Int, Set<Int>>>(emptyMap())
    val downloadedSurahsCache: StateFlow<Map<Int, Set<Int>>> = _downloadedSurahsCache.asStateFlow()

    private val _isBatchDownloading = MutableStateFlow(false)
    val isBatchDownloading: StateFlow<Boolean> = _isBatchDownloading.asStateFlow()

    private val _batchProgress = MutableStateFlow<BatchDownloadProgress?>(null)
    val batchProgress: StateFlow<BatchDownloadProgress?> = _batchProgress.asStateFlow()

    private var batchDownloadJob: Job? = null
    private val singleJobs = mutableMapOf<String, Job>()

    init {
        scope.launch {
            refreshDownloadedCache()
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: QuranDownloadManager? = null

        fun getInstance(context: Context): QuranDownloadManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: QuranDownloadManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private fun getReciterDirectory(reciterId: Int): File {
        val baseDir = appContext.getExternalFilesDir("quran_audio") ?: File(appContext.filesDir, "quran_audio")
        val dir = File(baseDir, "reciter_$reciterId")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    fun refreshDownloadedCache(reciterId: Int? = null) {
        val baseDir = appContext.getExternalFilesDir("quran_audio") ?: File(appContext.filesDir, "quran_audio")
        if (!baseDir.exists()) return

        val reciterDirs = if (reciterId != null) {
            listOfNotNull(File(baseDir, "reciter_$reciterId").takeIf { it.exists() })
        } else {
            baseDir.listFiles { f -> f.isDirectory && f.name.startsWith("reciter_") }?.toList() ?: emptyList()
        }

        val map = mutableMapOf<Int, Set<Int>>()
        for (rDir in reciterDirs) {
            val rId = rDir.name.removePrefix("reciter_").toIntOrNull() ?: continue
            val downloadedSet = rDir.listFiles { file -> file.isFile && file.name.endsWith(".mp3") && file.length() > 1024L }
                ?.mapNotNull { it.name.removeSuffix(".mp3").toIntOrNull() }
                ?.toSet() ?: emptySet()
            map[rId] = downloadedSet
        }

        _downloadedSurahsCache.update { current ->
            if (reciterId != null) {
                current + map
            } else {
                map
            }
        }
    }

    fun getDownloadedSurahNumbers(reciterId: Int): Set<Int> {
        val cached = _downloadedSurahsCache.value[reciterId]
        if (cached != null) return cached
        val set = getReciterDirectory(reciterId).listFiles { file -> file.isFile && file.name.endsWith(".mp3") && file.length() > 1024L }
            ?.mapNotNull { it.name.removeSuffix(".mp3").toIntOrNull() }
            ?.toSet() ?: emptySet()
        _downloadedSurahsCache.update { it + (reciterId to set) }
        return set
    }

    fun getLocalSurahFile(reciterId: Int, surahNumber: Int): File? {
        val dir = getReciterDirectory(reciterId)
        val paddedNumber = surahNumber.toString().padStart(3, '0')
        val file = File(dir, "$paddedNumber.mp3")
        return if (file.exists() && file.length() > 1024L) file else null
    }

    fun isSurahDownloaded(reciterId: Int, surahNumber: Int): Boolean {
        val cached = _downloadedSurahsCache.value[reciterId]
        if (cached != null) {
            return surahNumber in cached
        }
        return getLocalSurahFile(reciterId, surahNumber) != null
    }

    fun getDownloadedSurahsCount(reciterId: Int): Int {
        val cached = _downloadedSurahsCache.value[reciterId]
        if (cached != null) return cached.size
        val dir = getReciterDirectory(reciterId)
        if (!dir.exists()) return 0
        val files = dir.listFiles { file -> file.isFile && file.name.endsWith(".mp3") && file.length() > 1024L }
        return files?.size ?: 0
    }

    fun getDownloadedTotalSize(reciterId: Int): Long {
        val dir = getReciterDirectory(reciterId)
        if (!dir.exists()) return 0L
        return dir.listFiles()?.sumOf { if (it.isFile) it.length() else 0L } ?: 0L
    }

    /**
     * Download a single Surah.
     */
    fun downloadSurah(reciter: Reciter, surah: Surah) {
        val key = "${reciter.id}_${surah.number}"
        if (isSurahDownloaded(reciter.id, surah.number)) {
            _downloadStates.update { it + (key to SurahDownloadState.Downloaded) }
            return
        }

        singleJobs[key]?.cancel()
        val job = scope.launch {
            executeDownload(reciter, surah)
        }
        singleJobs[key] = job
    }

    /**
     * One-click download of the complete 114 Surahs for the specified reciter.
     */
    fun downloadFullQuran(reciter: Reciter) {
        if (_isBatchDownloading.value) return

        batchDownloadJob?.cancel()
        batchDownloadJob = scope.launch {
            try {
                downloadWakeLock?.acquire(60 * 60 * 1000L) // 1-hour safe hold
            } catch (e: Exception) {
                // Ignore wake lock acquire error
            }

            _isBatchDownloading.value = true
            val total = SurahCatalog.SURAHS.size
            var completed = getDownloadedSurahsCount(reciter.id)

            _batchProgress.value = BatchDownloadProgress(
                reciterId = reciter.id,
                completedCount = completed,
                totalCount = total,
                currentSurahName = "بدء التحميل..."
            )

            try {
                for (surah in SurahCatalog.SURAHS) {
                    if (isSurahDownloaded(reciter.id, surah.number)) {
                        val key = "${reciter.id}_${surah.number}"
                        _downloadStates.update { it + (key to SurahDownloadState.Downloaded) }
                        continue
                    }

                    _batchProgress.value = _batchProgress.value?.copy(
                        currentSurahName = surah.nameArabic
                    )

                    val success = executeDownload(reciter, surah)
                    if (success) {
                        completed++
                        _batchProgress.value = _batchProgress.value?.copy(
                            completedCount = completed
                        )
                    }
                }
            } catch (e: CancellationException) {
                _batchProgress.value = _batchProgress.value?.copy(isCancelled = true)
            } finally {
                try {
                    if (downloadWakeLock?.isHeld == true) {
                        downloadWakeLock?.release()
                    }
                } catch (e: Exception) {
                    // Ignore
                }
                _isBatchDownloading.value = false
            }
        }
    }

    fun cancelBatchDownload() {
        batchDownloadJob?.cancel()
        try {
            if (downloadWakeLock?.isHeld == true) {
                downloadWakeLock?.release()
            }
        } catch (e: Exception) {
            // Ignore
        }
        _isBatchDownloading.value = false
        _batchProgress.value = _batchProgress.value?.copy(isCancelled = true)
    }

    fun deleteSurah(reciterId: Int, surahNumber: Int): Boolean {
        val key = "${reciterId}_${surahNumber}"
        val file = getLocalSurahFile(reciterId, surahNumber)
        val deleted = file?.delete() ?: false
        if (deleted) {
            _downloadStates.update { it + (key to SurahDownloadState.NotDownloaded) }
            _downloadedSurahsCache.update { current ->
                val existing = current[reciterId] ?: emptySet()
                current + (reciterId to (existing - surahNumber))
            }
        }
        return deleted
    }

    fun deleteAllForReciter(reciterId: Int): Boolean {
        val dir = getReciterDirectory(reciterId)
        val deleted = dir.deleteRecursively()
        if (deleted) {
            dir.mkdirs()
            _downloadStates.update { current ->
                current.filterKeys { !it.startsWith("${reciterId}_") }
            }
            _downloadedSurahsCache.update { current ->
                current + (reciterId to emptySet())
            }
        }
        return deleted
    }

    private suspend fun executeDownload(reciter: Reciter, surah: Surah): Boolean = withContext(Dispatchers.IO) {
        val key = "${reciter.id}_${surah.number}"
        _downloadStates.update { it + (key to SurahDownloadState.Downloading(0f)) }

        val dir = getReciterDirectory(reciter.id)
        val paddedNumber = surah.number.toString().padStart(3, '0')
        val targetFile = File(dir, "$paddedNumber.mp3")
        val tempFile = File(dir, "$paddedNumber.mp3.tmp")

        val url = ReciterCatalog.getSurahAudioUrl(reciter, surah.number)
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "AlMuhasib-QuranPlayer/1.0")
            .build()

        try {
            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                _downloadStates.update { it + (key to SurahDownloadState.Failed("خطأ في الخادم: ${response.code}")) }
                return@withContext false
            }

            val body = response.body ?: run {
                _downloadStates.update { it + (key to SurahDownloadState.Failed("الملف غير متوفر")) }
                return@withContext false
            }

            val totalBytes = body.contentLength()
            var downloadedBytes = 0L

            body.byteStream().use { input ->
                FileOutputStream(tempFile).use { output ->
                    val buffer = ByteArray(8192)
                    var read: Int
                    var lastEmittedProgress = 0f

                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                        downloadedBytes += read

                        if (totalBytes > 0) {
                            val progress = downloadedBytes.toFloat() / totalBytes.toFloat()
                            if (progress - lastEmittedProgress >= 0.05f || progress >= 0.99f) {
                                lastEmittedProgress = progress
                                _downloadStates.update { it + (key to SurahDownloadState.Downloading(progress)) }
                            }
                        }
                    }
                    output.flush()
                }
            }

            if (tempFile.exists() && tempFile.length() > 1024L) {
                if (targetFile.exists()) targetFile.delete()
                val renamed = tempFile.renameTo(targetFile)
                if (renamed) {
                    _downloadStates.update { it + (key to SurahDownloadState.Downloaded) }
                    _downloadedSurahsCache.update { current ->
                        val existing = current[reciter.id] ?: emptySet()
                        current + (reciter.id to (existing + surah.number))
                    }
                    return@withContext true
                }
            }
            tempFile.delete()
            _downloadStates.update { it + (key to SurahDownloadState.Failed("فشل حفظ الملف")) }
            false
        } catch (e: CancellationException) {
            tempFile.delete()
            _downloadStates.update { it + (key to SurahDownloadState.NotDownloaded) }
            throw e
        } catch (e: Exception) {
            tempFile.delete()
            _downloadStates.update { it + (key to SurahDownloadState.Failed(e.localizedMessage ?: "فشل التحميل")) }
            false
        }
    }
}
