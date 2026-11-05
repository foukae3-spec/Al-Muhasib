package com.example.aod

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalTime

class TimeOfDayDuaProviderTest {

    @Test
    fun testPhaseDetectionAcrossDay() {
        // Sahar: 03:00 - 04:59
        assertEquals(DaySpiritualPhase.SAHAR, TimeOfDayDuaProvider.getCurrentPhase(LocalTime.of(3, 0)))
        assertEquals(DaySpiritualPhase.SAHAR, TimeOfDayDuaProvider.getCurrentPhase(LocalTime.of(3, 45)))
        assertEquals(DaySpiritualPhase.SAHAR, TimeOfDayDuaProvider.getCurrentPhase(LocalTime.of(4, 59)))

        // Morning: 05:00 - 10:59
        assertEquals(DaySpiritualPhase.MORNING, TimeOfDayDuaProvider.getCurrentPhase(LocalTime.of(5, 0)))
        assertEquals(DaySpiritualPhase.MORNING, TimeOfDayDuaProvider.getCurrentPhase(LocalTime.of(7, 30)))
        assertEquals(DaySpiritualPhase.MORNING, TimeOfDayDuaProvider.getCurrentPhase(LocalTime.of(10, 59)))

        // Afternoon: 11:00 - 16:59
        assertEquals(DaySpiritualPhase.AFTERNOON, TimeOfDayDuaProvider.getCurrentPhase(LocalTime.of(11, 0)))
        assertEquals(DaySpiritualPhase.AFTERNOON, TimeOfDayDuaProvider.getCurrentPhase(LocalTime.of(13, 15)))
        assertEquals(DaySpiritualPhase.AFTERNOON, TimeOfDayDuaProvider.getCurrentPhase(LocalTime.of(16, 59)))

        // Evening: 17:00 - 20:59
        assertEquals(DaySpiritualPhase.EVENING, TimeOfDayDuaProvider.getCurrentPhase(LocalTime.of(17, 0)))
        assertEquals(DaySpiritualPhase.EVENING, TimeOfDayDuaProvider.getCurrentPhase(LocalTime.of(19, 20)))
        assertEquals(DaySpiritualPhase.EVENING, TimeOfDayDuaProvider.getCurrentPhase(LocalTime.of(20, 59)))

        // Night: 21:00 - 02:59
        assertEquals(DaySpiritualPhase.NIGHT, TimeOfDayDuaProvider.getCurrentPhase(LocalTime.of(21, 0)))
        assertEquals(DaySpiritualPhase.NIGHT, TimeOfDayDuaProvider.getCurrentPhase(LocalTime.of(23, 45)))
        assertEquals(DaySpiritualPhase.NIGHT, TimeOfDayDuaProvider.getCurrentPhase(LocalTime.of(0, 30)))
        assertEquals(DaySpiritualPhase.NIGHT, TimeOfDayDuaProvider.getCurrentPhase(LocalTime.of(2, 59)))
    }

    @Test
    fun testDuasForCurrentPhaseCompleteness() {
        val phases = listOf(
            LocalTime.of(3, 30),
            LocalTime.of(8, 0),
            LocalTime.of(14, 0),
            LocalTime.of(18, 30),
            LocalTime.of(22, 0)
        )

        for (time in phases) {
            val duas = TimeOfDayDuaProvider.getDuasForCurrentPhase(time)
            assertNotNull(duas)
            assertTrue("Duas list should not be empty for $time", duas.isNotEmpty())
            for (dua in duas) {
                assertTrue("Arabic text must not be blank", dua.arabic.isNotBlank())
                assertTrue("Source reference must not be blank", dua.source.isNotBlank())
            }
        }
    }

    @Test
    fun testEasternArabicDigits() {
        assertEquals("١٢:٣٤", toEasternArabicDigits("12:34"))
        assertEquals("٠٠:٠٠", toEasternArabicDigits("00:00"))
        assertEquals("٠٥:٥٩", toEasternArabicDigits("05:59"))
        assertEquals("٢٣:٤٥", toEasternArabicDigits("23:45"))
    }
}
