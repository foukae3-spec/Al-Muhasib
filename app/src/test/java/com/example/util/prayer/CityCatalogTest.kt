package com.example.util.prayer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId

class CityCatalogTest {

    @Test
    fun testAllCitiesHaveValidTimezonesAndCoordinates() {
        assertTrue(CityCatalog.CITIES.isNotEmpty())

        for (city in CityCatalog.CITIES) {
            assertNotNull(city.id)
            assertTrue(city.arabicName.isNotBlank())
            assertTrue(city.englishName.isNotBlank())
            assertTrue(city.countryArabic.isNotBlank())
            assertTrue(city.timezoneId.isNotBlank())

            // Ensure ZoneId is parseable
            val zone = ZoneId.of(city.timezoneId)
            assertNotNull(zone)

            // Ensure coordinates are within valid world bounds
            assertTrue("Latitude out of range for ${city.id}", city.latitude in -90.0..90.0)
            assertTrue("Longitude out of range for ${city.id}", city.longitude in -180.0..180.0)
        }
    }

    @Test
    fun testFindCityById() {
        val makkah = CityCatalog.findCityById("SA_MAK")
        assertEquals("SA_MAK", makkah.id)
        assertEquals("مكة المكرمة", makkah.arabicName)

        val algiers = CityCatalog.findCityById("DZ_16")
        assertEquals("DZ_16", algiers.id)
        assertEquals("الجزائر العاصمة", algiers.arabicName)
        assertEquals("Africa/Algiers", algiers.timezoneId)

        val cairo = CityCatalog.findCityById("EG_CAI")
        assertEquals("EG_CAI", cairo.id)
        assertEquals("القاهرة", cairo.arabicName)
        assertEquals("Africa/Cairo", cairo.timezoneId)
    }

    @Test
    fun testSearchCities_arabicAndEnglish() {
        val searchArabic = CityCatalog.searchCities("الجزائر")
        assertTrue(searchArabic.any { it.id == "DZ_16" })

        val searchEnglish = CityCatalog.searchCities("Cairo")
        assertTrue(searchEnglish.any { it.id == "EG_CAI" })

        val searchRiyadh = CityCatalog.searchCities("الرياض")
        assertTrue(searchRiyadh.any { it.id == "SA_RUH" })
    }

    @Test
    fun testFindNearestCity() {
        // Near Algiers center coordinates (36.75, 3.05)
        val nearestAlgiers = CityCatalog.findNearestCity(36.7538, 3.0588)
        assertEquals("DZ_16", nearestAlgiers.id)

        // Near Makkah Haram (21.42, 39.82)
        val nearestMakkah = CityCatalog.findNearestCity(21.4225, 39.8262)
        assertEquals("SA_MAK", nearestMakkah.id)
    }
}
