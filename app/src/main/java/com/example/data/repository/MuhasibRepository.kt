package com.example.data.repository

import android.content.Context
import com.example.data.AppDatabase
import com.example.data.model.BookChunk
import com.example.data.model.DhikrItem
import com.example.data.model.IslamicBook
import com.example.data.model.MuhasabahScore
import com.example.data.model.PrayerLog
import com.example.data.model.RecoveryTrack
import com.example.data.model.SobrietyLog
import com.example.data.seed.AdhkarSeedData
import com.example.data.seed.IslamicBooksSeedData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDate

class MuhasibRepository(private val database: AppDatabase) {

    private val dhikrDao = database.dhikrDao()
    private val prayerDao = database.prayerDao()
    private val sobrietyDao = database.sobrietyDao()
    private val muhasabahDao = database.muhasabahDao()
    private val islamicBookDao = database.islamicBookDao()
    private val bookChunkDao = database.bookChunkDao()
    private val recoveryTrackDao = database.recoveryTrackDao()

    // Islamic Books Library
    val allBooks: Flow<List<IslamicBook>> = islamicBookDao.getAllBooks()

    fun getBooksByCategory(category: String): Flow<List<IslamicBook>> =
        islamicBookDao.getBooksByCategory(category)

    fun getBookById(id: String): Flow<IslamicBook?> =
        islamicBookDao.getBookById(id)

    suspend fun getBookByIdDirect(id: String): IslamicBook? = withContext(Dispatchers.IO) {
        islamicBookDao.getBookByIdDirect(id)
    }

    fun getChunksForBook(bookId: String): Flow<List<BookChunk>> =
        bookChunkDao.getChunksForBook(bookId)

    fun getChunkByIndex(bookId: String, chunkIndex: Int): Flow<BookChunk?> =
        bookChunkDao.getChunkByIndex(bookId, chunkIndex)

    suspend fun getChunkByIndexDirect(bookId: String, chunkIndex: Int): BookChunk? = withContext(Dispatchers.IO) {
        bookChunkDao.getChunkByIndexDirect(bookId, chunkIndex)
    }

    suspend fun getNextUnreadChunk(bookId: String): BookChunk? = withContext(Dispatchers.IO) {
        bookChunkDao.getNextUnreadChunk(bookId)
    }

    suspend fun updateBookCursor(bookId: String, cursor: Int) = withContext(Dispatchers.IO) {
        islamicBookDao.updateCursor(bookId, cursor, System.currentTimeMillis())
    }

    suspend fun markChunkRead(chunkId: String, isRead: Boolean) = withContext(Dispatchers.IO) {
        bookChunkDao.markChunkRead(chunkId, isRead, System.currentTimeMillis())
    }

    suspend fun markBookDownloaded(bookId: String, downloaded: Boolean) = withContext(Dispatchers.IO) {
        islamicBookDao.markDownloaded(bookId, downloaded)
    }

    suspend fun getRandomBookQuote(): BookChunk? = withContext(Dispatchers.IO) {
        bookChunkDao.getRandomQuote()
    }

    suspend fun insertBookChunks(chunks: List<BookChunk>) = withContext(Dispatchers.IO) {
        bookChunkDao.insertAll(chunks)
    }

    // 4-Front Recovery Engine
    val allRecoveryTracks: Flow<List<RecoveryTrack>> = recoveryTrackDao.getAllTracks()

    fun getRecoveryTrack(trackType: String): Flow<RecoveryTrack?> =
        recoveryTrackDao.getTrack(trackType)

    suspend fun getRecoveryTrackDirect(trackType: String): RecoveryTrack? = withContext(Dispatchers.IO) {
        recoveryTrackDao.getTrackDirect(trackType)
    }

    suspend fun updateRecoveryStreak(trackType: String, streakSeconds: Long, highestStreak: Long) = withContext(Dispatchers.IO) {
        recoveryTrackDao.updateStreak(trackType, streakSeconds, highestStreak, System.currentTimeMillis())
    }

    suspend fun recordTrackRelapse(trackType: String) = withContext(Dispatchers.IO) {
        recoveryTrackDao.recordRelapse(trackType, System.currentTimeMillis())
    }

    suspend fun toggleNightGuard(trackType: String, enabled: Boolean) = withContext(Dispatchers.IO) {
        recoveryTrackDao.toggleNightGuard(trackType, enabled)
    }

    suspend fun toggleSalahLock(trackType: String, enabled: Boolean) = withContext(Dispatchers.IO) {
        recoveryTrackDao.toggleSalahLock(trackType, enabled)
    }

    suspend fun updateMoneyAndCigarettes(trackType: String, moneySaved: Double, cigarettesAvoided: Int) = withContext(Dispatchers.IO) {
        recoveryTrackDao.updateMoneyAndCigarettes(trackType, moneySaved, cigarettesAvoided)
    }

    val allDhikr: Flow<List<DhikrItem>> = dhikrDao.getAllDhikr()

    fun getDhikrByCategory(category: String): Flow<List<DhikrItem>> =
        dhikrDao.getDhikrByCategory(category)

    suspend fun getDhikrByCategoryDirect(category: String): List<DhikrItem> = withContext(Dispatchers.IO) {
        dhikrDao.getDhikrByCategoryDirect(category)
    }

    suspend fun incrementDhikr(id: Int) = withContext(Dispatchers.IO) {
        val item = dhikrDao.getDhikrById(id) ?: return@withContext
        val newCount = item.currentCount + 1
        val isCompleted = newCount >= item.targetCount
        dhikrDao.updateCount(id, newCount, isCompleted)
        val text = item.arabic + " " + item.title + " " + item.category
        when {
            text.contains("أستغفر") || text.contains("استغفار") || text.contains("أَسْتَغْفِرُ") -> {
                sobrietyDao.incrementIstighfar()
            }
            text.contains("سبحان الله") || text.contains("سُبْحَانَ اللَّه") -> {
                sobrietyDao.incrementSubhanAllah()
            }
            text.contains("الحمد لله") || text.contains("الْحَمْدُ لِلَّه") -> {
                sobrietyDao.incrementAlhamdulillah()
            }
            text.contains("الله أكبر") || text.contains("اللَّهُ أَكْبَر") -> {
                sobrietyDao.incrementAllahuAkbar()
            }
            text.contains("لا إله إلا الله") || text.contains("لَا إِلَهَ إِلَّا اللَّه") -> {
                sobrietyDao.incrementLaIlahaIllallah()
            }
            else -> {
                sobrietyDao.incrementTasbi7()
            }
        }
    }

    // ponytail: mark dhikr as completed directly and advance to next
    suspend fun markDhikrDone(id: Int) = withContext(Dispatchers.IO) {
        val item = dhikrDao.getDhikrById(id) ?: return@withContext
        dhikrDao.updateCount(id, item.targetCount, true)
        val text = item.arabic + " " + item.title + " " + item.category
        when {
            text.contains("أستغفر") || text.contains("استغفار") || text.contains("أَسْتَغْفِرُ") -> {
                sobrietyDao.incrementIstighfar()
            }
            text.contains("سبحان الله") || text.contains("سُبْحَانَ اللَّه") -> {
                sobrietyDao.incrementSubhanAllah()
            }
            text.contains("الحمد لله") || text.contains("الْحَمْدُ لِلَّه") -> {
                sobrietyDao.incrementAlhamdulillah()
            }
            text.contains("الله أكبر") || text.contains("اللَّهُ أَكْبَر") -> {
                sobrietyDao.incrementAllahuAkbar()
            }
            text.contains("لا إله إلا الله") || text.contains("لَا إِلَهَ إِلَّا اللَّه") -> {
                sobrietyDao.incrementLaIlahaIllallah()
            }
            else -> {
                sobrietyDao.incrementTasbi7()
            }
        }
    }

    suspend fun resetCategoryDhikr(category: String) = withContext(Dispatchers.IO) {
        dhikrDao.resetCategoryCounts(category)
    }

    suspend fun resetAllDhikr() = withContext(Dispatchers.IO) {
        dhikrDao.resetAllCounts()
    }

    fun getTodayPrayerLog(): Flow<PrayerLog> {
        val today = LocalDate.now().toString()
        return prayerDao.getPrayerLogForDate(today).map { log ->
            log ?: PrayerLog(dateString = today)
        }
    }

    suspend fun togglePrayer(prayerName: String, done: Boolean) = withContext(Dispatchers.IO) {
        val today = LocalDate.now().toString()
        val current = prayerDao.getPrayerLogDirect(today) ?: PrayerLog(dateString = today)
        val updated = when (prayerName.lowercase()) {
            "fajr" -> current.copy(fajrDone = done)
            "dhuhr" -> current.copy(dhuhrDone = done)
            "asr" -> current.copy(asrDone = done)
            "maghrib" -> current.copy(maghribDone = done)
            "isha" -> current.copy(ishaDone = done)
            else -> current
        }
        prayerDao.insertOrUpdate(updated)
    }

    fun getSobrietyState(): Flow<SobrietyLog> =
        sobrietyDao.getSobrietyState().map { it ?: SobrietyLog() }

    suspend fun getSobrietyStateDirect(): SobrietyLog? = withContext(Dispatchers.IO) {
        sobrietyDao.getSobrietyStateDirect()
    }

    suspend fun incrementIstighfarCount() = withContext(Dispatchers.IO) {
        sobrietyDao.incrementIstighfar()
    }

    suspend fun setIstighfarCount(count: Int) = withContext(Dispatchers.IO) {
        sobrietyDao.setIstighfarCount(count)
    }

    // ponytail: tasbih is its own counter, never mixed with istighfar
    suspend fun incrementTasbi7Count() = withContext(Dispatchers.IO) {
        sobrietyDao.incrementTasbi7()
    }

    suspend fun incrementSubhanAllahCount() = withContext(Dispatchers.IO) {
        sobrietyDao.incrementSubhanAllah()
    }

    suspend fun incrementAlhamdulillahCount() = withContext(Dispatchers.IO) {
        sobrietyDao.incrementAlhamdulillah()
    }

    suspend fun incrementAllahuAkbarCount() = withContext(Dispatchers.IO) {
        sobrietyDao.incrementAllahuAkbar()
    }

    suspend fun incrementLaIlahaIllallahCount() = withContext(Dispatchers.IO) {
        sobrietyDao.incrementLaIlahaIllallah()
    }

    suspend fun toggleWorkout(done: Boolean) = withContext(Dispatchers.IO) {
        val current = sobrietyDao.getSobrietyStateDirect() ?: SobrietyLog()
        sobrietyDao.insertOrUpdate(current.copy(workoutCompletedToday = done))
    }

    suspend fun toggleColdShower(done: Boolean) = withContext(Dispatchers.IO) {
        val current = sobrietyDao.getSobrietyStateDirect() ?: SobrietyLog()
        sobrietyDao.insertOrUpdate(current.copy(coldShowerToday = done))
    }

    suspend fun recordRelapse() = withContext(Dispatchers.IO) {
        val current = sobrietyDao.getSobrietyStateDirect() ?: SobrietyLog()
        val updated = current.copy(
            purityStartEpochMs = System.currentTimeMillis(),
            cleanDaysCount = 0,
            relapsesCount = current.relapsesCount + 1,
            workoutCompletedToday = false,
            coldShowerToday = false
        )
        sobrietyDao.insertOrUpdate(updated)
    }

    fun getMuhasabahHistory(): Flow<List<MuhasabahScore>> =
        muhasabahDao.getHistory()

    suspend fun ensureSeeded() = withContext(Dispatchers.IO) {
        if (dhikrDao.getCount() == 0) {
            dhikrDao.insertAll(AdhkarSeedData.getInitialAdhkar())
        }
        val currentSobriety = sobrietyDao.getSobrietyStateDirect()
        if (currentSobriety == null) {
            sobrietyDao.insertOrUpdate(SobrietyLog())
        }
        val today = LocalDate.now().toString()
        val currentPrayer = prayerDao.getPrayerLogDirect(today)
        if (currentPrayer == null) {
            prayerDao.insertOrUpdate(
                PrayerLog(
                    dateString = today,
                    fajrDone = true,
                    dhuhrDone = true,
                    asrDone = false,
                    maghribDone = false,
                    ishaDone = false
                )
            )
        }
        val currentBook = islamicBookDao.getBookByIdDirect("al_daa_wal_dawaa")
        if (currentBook == null) {
            islamicBookDao.insertAll(IslamicBooksSeedData.getInitialBooks())
            bookChunkDao.insertAll(IslamicBooksSeedData.getInitialChunks())
        }
        val currentTrack = recoveryTrackDao.getTrackDirect("SALAH")
        if (currentTrack == null) {
            recoveryTrackDao.insertAll(IslamicBooksSeedData.getInitialRecoveryTracks())
        }
    }

    fun isOnboardingCompleted(context: Context): Boolean =
        OnboardingPreferences.isOnboardingCompleted(context)

    fun setOnboardingCompleted(context: Context, completed: Boolean) {
        OnboardingPreferences.setOnboardingCompleted(context, completed)
    }
}
