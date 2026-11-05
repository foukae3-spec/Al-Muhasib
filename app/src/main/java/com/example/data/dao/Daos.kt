package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.DhikrItem
import com.example.data.model.MuhasabahScore
import com.example.data.model.PrayerLog
import com.example.data.model.SobrietyLog
import kotlinx.coroutines.flow.Flow

@Dao
interface DhikrDao {
    @Query("SELECT * FROM dhikr_items ORDER BY id ASC")
    fun getAllDhikr(): Flow<List<DhikrItem>>

    @Query("SELECT * FROM dhikr_items WHERE category = :category ORDER BY id ASC")
    fun getDhikrByCategory(category: String): Flow<List<DhikrItem>>

    @Query("SELECT * FROM dhikr_items WHERE category = :category ORDER BY id ASC")
    suspend fun getDhikrByCategoryDirect(category: String): List<DhikrItem>

    @Query("SELECT * FROM dhikr_items WHERE id = :id LIMIT 1")
    suspend fun getDhikrById(id: Int): DhikrItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<DhikrItem>)

    @Update
    suspend fun update(item: DhikrItem)

    @Query("UPDATE dhikr_items SET currentCount = :count, isCompleted = :completed WHERE id = :id")
    suspend fun updateCount(id: Int, count: Int, completed: Boolean)

    @Query("UPDATE dhikr_items SET currentCount = 0, isCompleted = 0 WHERE category = :category")
    suspend fun resetCategoryCounts(category: String)

    @Query("UPDATE dhikr_items SET currentCount = 0, isCompleted = 0")
    suspend fun resetAllCounts()

    @Query("SELECT COUNT(*) FROM dhikr_items")
    suspend fun getCount(): Int
}

@Dao
interface PrayerDao {
    @Query("SELECT * FROM prayer_logs WHERE dateString = :date LIMIT 1")
    fun getPrayerLogForDate(date: String): Flow<PrayerLog?>

    @Query("SELECT * FROM prayer_logs WHERE dateString = :date LIMIT 1")
    suspend fun getPrayerLogDirect(date: String): PrayerLog?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(prayerLog: PrayerLog)

    @Query("SELECT * FROM prayer_logs ORDER BY id DESC LIMIT 7")
    fun getPastWeekLogs(): Flow<List<PrayerLog>>
}

@Dao
interface SobrietyDao {
    @Query("SELECT * FROM sobriety_state WHERE id = 1 LIMIT 1")
    fun getSobrietyState(): Flow<SobrietyLog?>

    @Query("SELECT * FROM sobriety_state WHERE id = 1 LIMIT 1")
    suspend fun getSobrietyStateDirect(): SobrietyLog?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(sobrietyLog: SobrietyLog)

    @Query("UPDATE sobriety_state SET istighfarTodayCount = istighfarTodayCount + 1 WHERE id = 1")
    suspend fun incrementIstighfar()

    @Query("UPDATE sobriety_state SET istighfarTodayCount = :count WHERE id = 1")
    suspend fun setIstighfarCount(count: Int)

    // ponytail: tasbih is fully separate from istighfar
    @Query("UPDATE sobriety_state SET tasbi7TodayCount = tasbi7TodayCount + 1 WHERE id = 1")
    suspend fun incrementTasbi7()

    @Query("UPDATE sobriety_state SET subhanAllahTodayCount = subhanAllahTodayCount + 1 WHERE id = 1")
    suspend fun incrementSubhanAllah()

    @Query("UPDATE sobriety_state SET alhamdulillahTodayCount = alhamdulillahTodayCount + 1 WHERE id = 1")
    suspend fun incrementAlhamdulillah()

    @Query("UPDATE sobriety_state SET allahuAkbarTodayCount = allahuAkbarTodayCount + 1 WHERE id = 1")
    suspend fun incrementAllahuAkbar()

    @Query("UPDATE sobriety_state SET laIlahaIllallahTodayCount = laIlahaIllallahTodayCount + 1 WHERE id = 1")
    suspend fun incrementLaIlahaIllallah()
}

@Dao
interface MuhasabahDao {
    @Query("SELECT * FROM muhasabah_history ORDER BY id DESC LIMIT 14")
    fun getHistory(): Flow<List<MuhasabahScore>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(score: MuhasabahScore)
}
