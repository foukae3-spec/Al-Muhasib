package com.example.aod

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalTime

class AodLockServiceLogicTest {

    @Test
    fun testQuietHoursOvernight() {
        val start = LocalTime.of(23, 30)
        val end = LocalTime.of(5, 0)

        // At midnight (00:00) -> inside quiet hours
        assertTrue(AodLockService.isWithinScheduledQuietHours(LocalTime.of(0, 0), start, end))

        // At 03:15 -> inside quiet hours
        assertTrue(AodLockService.isWithinScheduledQuietHours(LocalTime.of(3, 15), start, end))

        // At 23:45 -> inside quiet hours
        assertTrue(AodLockService.isWithinScheduledQuietHours(LocalTime.of(23, 45), start, end))

        // At 05:00 exact -> outside quiet hours
        assertFalse(AodLockService.isWithinScheduledQuietHours(LocalTime.of(5, 0), start, end))

        // At midday (12:00) -> outside quiet hours
        assertFalse(AodLockService.isWithinScheduledQuietHours(LocalTime.of(12, 0), start, end))
    }

    @Test
    fun testBatterySaverBlockRules() {
        // Battery saver disabled -> never blocks
        assertFalse(AodLockService.shouldBlockAodDueToBattery(false, isPowerSaveMode = true, batteryPct = 10f))
        assertFalse(AodLockService.shouldBlockAodDueToBattery(false, isPowerSaveMode = false, batteryPct = 5f))

        // Battery saver enabled + power save on -> blocks
        assertTrue(AodLockService.shouldBlockAodDueToBattery(true, isPowerSaveMode = true, batteryPct = 80f))

        // Battery saver enabled + low battery (< 15%) -> blocks
        assertTrue(AodLockService.shouldBlockAodDueToBattery(true, isPowerSaveMode = false, batteryPct = 14f))

        // Battery saver enabled + healthy battery (>= 15%) + no power save -> does not block
        assertFalse(AodLockService.shouldBlockAodDueToBattery(true, isPowerSaveMode = false, batteryPct = 15f))
        assertFalse(AodLockService.shouldBlockAodDueToBattery(true, isPowerSaveMode = false, batteryPct = 85f))
    }

    @Test
    fun testAutoBrightnessCurve() {
        // Lux < 5 -> 3% minimum safe visible floor
        assertEquals(0.03f, AodActivity.calculateAutoBrightness(2f), 0.001f)

        // Lux 50 -> 5%
        assertEquals(0.05f, AodActivity.calculateAutoBrightness(50f), 0.001f)

        // Lux 200 -> 9%
        assertEquals(0.09f, AodActivity.calculateAutoBrightness(200f), 0.001f)

        // Lux 800 -> 14%
        assertEquals(0.14f, AodActivity.calculateAutoBrightness(800f), 0.001f)

        // Lux 1500 -> 22%
        assertEquals(0.22f, AodActivity.calculateAutoBrightness(1500f), 0.001f)
    }

    @Test
    fun testProximityPocketDetection() {
        val maxRange = 5f

        // Near zero distance -> covered (in pocket)
        assertTrue(AodActivity.calculateIsCovered(0.0f, maxRange))
        assertTrue(AodActivity.calculateIsCovered(0.5f, maxRange))

        // Max range -> clear (out of pocket)
        assertFalse(AodActivity.calculateIsCovered(5.0f, maxRange))
        assertFalse(AodActivity.calculateIsCovered(10.0f, maxRange))
    }
}
