package com.example.data.model

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * DailyLifeMission — Represents one of the user's Daily Amal (High-Leverage Life Missions)
 * anchored to a specific Islamic Prayer / Barakah window on the Home Screen.
 */
@Entity(tableName = "daily_life_missions")
data class DailyLifeMission(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val dateString: String, // YYYY-MM-DD
    val title: String,
    val prayerAnchor: String, // "POST_FAJR", "DUHA_BLOCK", "ASR_BLOCK", "POST_MAGHRIB"
    val category: String, // "DEEN", "PROVISION", "HEALTH", "FAMILY", "KNOWLEDGE"
    val estimatedMinutes: Int = 25,
    val completedMinutes: Int = 0,
    val isCompleted: Boolean = false,
    val barakahPoints: Int = 15,
    val createdAtEpochMs: Long = System.currentTimeMillis()
)

/**
 * AppNiyyahSession — Logs every intentional app launch approved through the Niyyah Gatekeeper.
 * Used during the Evening Muhasabah Reckoning to audit whether phone usage aligned with stated intentions.
 */
@Entity(tableName = "app_niyyah_sessions")
data class AppNiyyahSession(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val dateString: String, // YYYY-MM-DD
    val packageName: String,
    val appLabel: String,
    val statedNiyyah: String,
    val allottedMinutes: Int,
    val startedAtEpochMs: Long = System.currentTimeMillis(),
    val endedAtEpochMs: Long? = null,
    val honoredTimeBox: Boolean = true
)

/**
 * AppGuardRule — Per-package customization for the Intentional App Drawer & Gatekeeper.
 */
@Entity(tableName = "app_guard_rules")
data class AppGuardRule(
    @PrimaryKey
    val packageName: String,
    val appLabel: String,
    val sanctuaryTierName: String, // "SANCTUARY", "ESSENTIAL_UTILITY", "WORK_AND_PROVISION", "GUARDED_DOPAMINE"
    val dailyLimitMinutes: Int = 20,
    val requireNiyyahPrompt: Boolean = true,
    val blockDuringSalahWindow: Boolean = true,
    val blockDuringNightGuard: Boolean = true,
    val isPinnedToHomeDock: Boolean = false
)

@Dao
interface LifeOsDao {

    // --- Daily Life Missions (Amal) ---
    @Query("SELECT * FROM daily_life_missions WHERE dateString = :dateString ORDER BY isCompleted ASC, id ASC")
    fun observeMissionsForDate(dateString: String): Flow<List<DailyLifeMission>>

    @Query("SELECT * FROM daily_life_missions WHERE dateString = :dateString ORDER BY isCompleted ASC, id ASC")
    suspend fun getMissionsForDate(dateString: String): List<DailyLifeMission>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMission(mission: DailyLifeMission): Long

    @Update
    suspend fun updateMission(mission: DailyLifeMission)

    @Query("UPDATE daily_life_missions SET isCompleted = :completed, completedMinutes = estimatedMinutes WHERE id = :missionId")
    suspend fun setMissionCompleted(missionId: Int, completed: Boolean)

    @Query("DELETE FROM daily_life_missions WHERE id = :missionId")
    suspend fun deleteMission(missionId: Int)

    // --- App Niyyah Sessions (Intentionality Audit) ---
    @Query("SELECT * FROM app_niyyah_sessions WHERE dateString = :dateString ORDER BY startedAtEpochMs DESC")
    fun observeNiyyahSessionsForDate(dateString: String): Flow<List<AppNiyyahSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNiyyahSession(session: AppNiyyahSession): Long

    @Query("UPDATE app_niyyah_sessions SET endedAtEpochMs = :endedAtMs, honoredTimeBox = :honored WHERE id = :sessionId")
    suspend fun closeNiyyahSession(sessionId: Int, endedAtMs: Long, honored: Boolean)

    // --- App Guard Rules ---
    @Query("SELECT * FROM app_guard_rules")
    fun observeAllAppGuardRules(): Flow<List<AppGuardRule>>

    @Query("SELECT * FROM app_guard_rules")
    suspend fun getAllAppGuardRules(): List<AppGuardRule>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAppGuardRule(rule: AppGuardRule)
}
