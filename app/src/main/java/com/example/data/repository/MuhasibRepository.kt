package com.example.data.repository

import android.content.Context
import com.example.data.AppDatabase
import com.example.data.model.DhikrItem
import com.example.data.model.MuhasabahScore
import com.example.data.model.PrayerLog
import com.example.data.model.SobrietyLog
import com.example.data.seed.AdhkarSeedData
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
    }

    fun isOnboardingCompleted(context: Context): Boolean =
        OnboardingPreferences.isOnboardingCompleted(context)

    fun setOnboardingCompleted(context: Context, completed: Boolean) {
        OnboardingPreferences.setOnboardingCompleted(context, completed)
    }
}
