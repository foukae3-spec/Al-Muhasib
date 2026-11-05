package com.example.alarm

import com.example.util.IslamicCalendarUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class PrayerAlarmSchedulerTest {

    private fun createPrayer(key: String, arabicName: String, time: LocalTime): IslamicCalendarUtil.PrayerSchedule {
        return IslamicCalendarUtil.PrayerSchedule(
            key = key,
            arabicName = arabicName,
            time = time,
            timeFormatted = time.toString(),
            isPassed = false,
            isNext = false,
            timeRemainingFormatted = ""
        )
    }

    @Test
    fun testMidnightRolloverCalculation() {
        val zone = ZoneId.of("UTC")

        // 1. Before 00:00:05 (e.g. 00:00:01)
        val before = LocalDateTime.of(2026, 10, 2, 0, 0, 1)
        val targetBefore = PrayerAlarmScheduler.calculateNextMidnightEpochMs(before, zone)
        val expectedBefore = LocalDateTime.of(2026, 10, 2, 0, 0, 5).atZone(zone).toInstant().toEpochMilli()
        assertEquals(expectedBefore, targetBefore)

        // 2. After 00:00:05 (e.g. 14:30:00)
        val midday = LocalDateTime.of(2026, 10, 2, 14, 30, 0)
        val targetMidday = PrayerAlarmScheduler.calculateNextMidnightEpochMs(midday, zone)
        val expectedNextDay = LocalDateTime.of(2026, 10, 3, 0, 0, 5).atZone(zone).toInstant().toEpochMilli()
        assertEquals(expectedNextDay, targetMidday)

        // Verifying trigger is in the future
        assertTrue(targetMidday > midday.atZone(zone).toInstant().toEpochMilli())
    }

    @Test
    fun testCalculateNextPrayerTarget_daytimeSelection() {
        val today = LocalDate.of(2026, 10, 2)
        val schedule = listOf(
            createPrayer("fajr", "الفجر", LocalTime.of(5, 0)),
            createPrayer("dhuhr", "الظهر", LocalTime.of(12, 30)),
            createPrayer("asr", "العصر", LocalTime.of(15, 45)),
            createPrayer("maghrib", "المغرب", LocalTime.of(18, 15)),
            createPrayer("isha", "العشاء", LocalTime.of(19, 45))
        )

        // At 13:00, next prayer should be Asr at 15:45 today
        val now = LocalTime.of(13, 0)
        val (targetDateTime, prayerName) = PrayerAlarmScheduler.calculateNextPrayerTarget(schedule, now, today)

        assertEquals("العصر", prayerName)
        assertEquals(LocalDateTime.of(today, LocalTime.of(15, 45)), targetDateTime)
    }

    @Test
    fun testCalculateNextPrayerTarget_nighttimeRollsOverToTomorrowFajr() {
        val today = LocalDate.of(2026, 10, 2)
        val schedule = listOf(
            createPrayer("fajr", "الفجر", LocalTime.of(5, 0)),
            createPrayer("dhuhr", "الظهر", LocalTime.of(12, 30)),
            createPrayer("asr", "العصر", LocalTime.of(15, 45)),
            createPrayer("maghrib", "المغرب", LocalTime.of(18, 15)),
            createPrayer("isha", "العشاء", LocalTime.of(19, 45))
        )

        // At 21:00 (after Isha), next prayer must roll over to tomorrow's Fajr
        val now = LocalTime.of(21, 0)
        val (targetDateTime, prayerName) = PrayerAlarmScheduler.calculateNextPrayerTarget(schedule, now, today)

        assertEquals("الفجر", prayerName)
        assertEquals(LocalDateTime.of(today.plusDays(1), LocalTime.of(5, 0)), targetDateTime)
    }

    @Test
    fun testCalculateNextPrayerTarget_thirtySecondSafetyWindow() {
        val today = LocalDate.of(2026, 10, 2)
        val schedule = listOf(
            createPrayer("dhuhr", "الظهر", LocalTime.of(12, 30)),
            createPrayer("asr", "العصر", LocalTime.of(15, 45))
        )

        // At 12:29:45, Dhuhr is only 15 seconds away (< 30s threshold).
        // It must NOT pick Dhuhr to prevent immediately re-triggering an alarm in progress, but pick Asr instead.
        val now = LocalTime.of(12, 29, 45)
        val (targetDateTime, prayerName) = PrayerAlarmScheduler.calculateNextPrayerTarget(schedule, now, today)

        assertEquals("العصر", prayerName)
        assertEquals(LocalDateTime.of(today, LocalTime.of(15, 45)), targetDateTime)
    }

    @Test
    fun testCalculateTriggerEpochMs_respectsCityTimezone() {
        val target = LocalDateTime.of(2026, 10, 2, 12, 0, 0)
        val algiersZone = ZoneId.of("Africa/Algiers") // UTC+1
        val riyadhZone = ZoneId.of("Asia/Riyadh")     // UTC+3

        val algiersEpochMs = PrayerAlarmScheduler.calculateTriggerEpochMs(target, algiersZone)
        val riyadhEpochMs = PrayerAlarmScheduler.calculateTriggerEpochMs(target, riyadhZone)

        // Riyadh is 2 hours ahead of Algiers, so 12:00 in Riyadh happens 2 hours (7,200,000 ms) before 12:00 in Algiers!
        val diffMs = algiersEpochMs - riyadhEpochMs
        assertEquals(2 * 3600 * 1000L, diffMs)
    }

    @Test
    fun testPreAdhanTimingCalculation() {
        val target = LocalDateTime.of(2026, 10, 2, 12, 30, 0)
        val zone = ZoneId.of("Africa/Algiers")
        val preMinutes = 15

        val targetEpochMs = PrayerAlarmScheduler.calculateTriggerEpochMs(target, zone)
        val preTarget = target.minusMinutes(preMinutes.toLong())
        val preEpochMs = PrayerAlarmScheduler.calculateTriggerEpochMs(preTarget, zone)

        assertEquals(15 * 60 * 1000L, targetEpochMs - preEpochMs)
    }
}
