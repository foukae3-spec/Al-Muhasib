package com.example.util.prayer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class SolarCalculatorTest {

    @Test
    fun testAlgiersPrayerTimes() {
        // Algiers: 36.7538° N, 3.0588° E, UTC+1
        val date = LocalDate.of(2026, 10, 2)
        val calc = SolarCalculator.calculate(
            latitude = 36.7538,
            longitude = 3.0588,
            timezoneOffsetHours = 1.0,
            date = date,
            method = CalculationMethod.ALGERIA
        )
        println("Algiers on 2026-10-02: Fajr=${calc.fajr}, Sunrise=${calc.sunrise}, Dhuhr=${calc.dhuhr}, Asr=${calc.asr}, Maghrib=${calc.maghrib}, Isha=${calc.isha}")

        // In Algiers on Oct 2:
        // Dhuhr solar noon should be around 12:35 to 12:40 (since lon is 3° E, 15° - 3° = 12° = 48 min after UTC solar noon, eq of time ~ +10 min)
        // 12:00 + 1 - (3.0588/15) - (eqTime/60) = 13:00 - 0.2039 - ~0.18 = ~12:37
        assertEquals(12, calc.dhuhr.hour)
        assertTrue("Dhuhr minute should be between 30 and 45 but was ${calc.dhuhr.minute}", calc.dhuhr.minute in 30..45)

        // Sunrise should be around 06:30 - 06:45
        assertEquals(6, calc.sunrise.hour)

        // Fajr (18°) should be around 05:00 - 05:25
        assertEquals(5, calc.fajr.hour)

        // Maghrib should be around 18:20 - 18:35
        assertEquals(18, calc.maghrib.hour)

        // Isha should be around 19:40 - 20:00
        assertEquals(19, calc.isha.hour)
    }

    @Test
    fun testMakkahPrayerTimes() {
        // Makkah: 21.4225° N, 39.8262° E, UTC+3
        val date = LocalDate.of(2026, 10, 2)
        val calc = SolarCalculator.calculate(
            latitude = 21.4225,
            longitude = 39.8262,
            timezoneOffsetHours = 3.0,
            date = date,
            method = CalculationMethod.UMM_AL_QURA
        )
        println("Makkah on 2026-10-02: Fajr=${calc.fajr}, Sunrise=${calc.sunrise}, Dhuhr=${calc.dhuhr}, Asr=${calc.asr}, Maghrib=${calc.maghrib}, Isha=${calc.isha}")
        assertEquals(12, calc.dhuhr.hour)
        assertEquals(6, calc.sunrise.hour)
        assertEquals(18, calc.maghrib.hour)
    }
}
