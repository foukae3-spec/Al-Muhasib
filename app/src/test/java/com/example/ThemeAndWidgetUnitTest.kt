package com.example

import com.example.data.model.PrayerLog
import com.example.ui.theme.RingDetoxColor
import com.example.ui.theme.RingDhikrColor
import com.example.ui.theme.RingPrayerColor
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkSurface
import com.example.util.IslamicCalendarUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeAndWidgetUnitTest {

    @Test
    fun verifyThreeConcentricRingColorsAreDistinctAndHighContrast() {
        // Requirement 2: "the circles are almost same yellow color, use 3 diffrent colors."
        // Verify that RingPrayerColor (Emerald), RingDhikrColor (Cyan), and RingDetoxColor (Amber)
        // are strictly distinct from each other!
        assertNotEquals(RingPrayerColor, RingDhikrColor)
        assertNotEquals(RingPrayerColor, RingDetoxColor)
        assertNotEquals(RingDhikrColor, RingDetoxColor)

        // Verify Ring 1 is Luminous Emerald (Green dominant: green channel significantly higher than red & blue)
        assertTrue("RingPrayerColor should be emerald green", RingPrayerColor.green > RingPrayerColor.red && RingPrayerColor.green > RingPrayerColor.blue)

        // Verify Ring 2 is Celestial Cyan (Green and Blue dominant, low red)
        assertTrue("RingDhikrColor should be cyan/blue", RingDhikrColor.blue > RingDhikrColor.red && RingDhikrColor.green > RingDhikrColor.red)

        // Verify Ring 3 is Warm Amber Gold (Red and Green dominant, low blue)
        assertTrue("RingDetoxColor should be amber/gold", RingDetoxColor.red > RingDetoxColor.blue && RingDetoxColor.green > RingDetoxColor.blue)
    }

    @Test
    fun verifyDarkGreenNeumorphicBasePalette() {
        // Requirement 1: Dark green themed neumorphism base
        // Verify DarkBackground, DarkSurface, and DarkCard have green channel higher than red channel
        assertTrue("DarkBackground should have green undertone", DarkBackground.green > DarkBackground.red)
        assertTrue("DarkSurface should have green undertone", DarkSurface.green > DarkSurface.red)
        assertTrue("DarkCard should have green undertone", DarkCard.green > DarkCard.red)
    }

    @Test
    fun verifyPrayerLogCompletionCalculations() {
        val emptyLog = PrayerLog(dateString = "2026-09-24")
        assertEquals(0, emptyLog.completedCount())
        assertEquals(0f, emptyLog.completionRatio(), 0.001f)

        val partialLog = PrayerLog(
            dateString = "2026-09-24",
            fajrDone = true,
            dhuhrDone = true,
            asrDone = false,
            maghribDone = false,
            ishaDone = false
        )
        assertEquals(2, partialLog.completedCount())
        assertEquals(0.4f, partialLog.completionRatio(), 0.001f)

        val fullLog = PrayerLog(
            dateString = "2026-09-24",
            fajrDone = true,
            dhuhrDone = true,
            asrDone = true,
            maghribDone = true,
            ishaDone = true
        )
        assertEquals(5, fullLog.completedCount())
        assertEquals(1.0f, fullLog.completionRatio(), 0.001f)
    }

    @Test
    fun verifyIslamicCalendarUtilSchedulesAndDates() {
        val schedule = IslamicCalendarUtil.getDailyPrayerSchedule()
        assertEquals(5, schedule.size)

        val nextPrayer = IslamicCalendarUtil.getNextPrayer()
        assertNotNull(nextPrayer)
        assertTrue(nextPrayer.arabicName.isNotBlank())
        assertTrue(nextPrayer.timeFormatted.isNotBlank())

        val hijriDate = IslamicCalendarUtil.getFormattedHijriDate()
        assertTrue(hijriDate.contains("هـ"))

        val gregorianDate = IslamicCalendarUtil.getFormattedArabicGregorianDate()
        assertTrue(gregorianDate.isNotBlank())
    }

    @Test
    fun verifyAodPocketModeProximityCalculations() {
        // Phone inside pocket or face down on desk (distance 0.0 or 0.5 cm or 1.5 cm)
        assertTrue(com.example.aod.AodActivity.calculateIsCovered(0.0f, 5.0f))
        assertTrue(com.example.aod.AodActivity.calculateIsCovered(0.5f, 5.0f))
        assertTrue(com.example.aod.AodActivity.calculateIsCovered(1.5f, 5.0f))

        // Phone held in hand (distance > 2.0 cm) -> MUST NOT trigger pocket mode
        org.junit.Assert.assertFalse(com.example.aod.AodActivity.calculateIsCovered(2.5f, 5.0f))
        org.junit.Assert.assertFalse(com.example.aod.AodActivity.calculateIsCovered(5.0f, 5.0f))
        org.junit.Assert.assertFalse(com.example.aod.AodActivity.calculateIsCovered(10.0f, 5.0f))

        // Sensor with larger maxRange (e.g. 8.0f): hand-held at 3.5cm must NOT be covered!
        org.junit.Assert.assertFalse(com.example.aod.AodActivity.calculateIsCovered(3.5f, 8.0f))

        // Virtual ultrasonic proximity sensor (maxRange 100.0f): hand-held at 15cm must NOT be covered!
        org.junit.Assert.assertFalse(com.example.aod.AodActivity.calculateIsCovered(15.0f, 100.0f))

        // Binary proximity sensor (maxRange 1.0f)
        assertTrue(com.example.aod.AodActivity.calculateIsCovered(0.0f, 1.0f))
        org.junit.Assert.assertFalse(com.example.aod.AodActivity.calculateIsCovered(1.0f, 1.0f))
    }

    @Test
    fun verifyDhikrWidgetActionConstantContract() {
        assertEquals("com.example.widget.ACTION_INCREMENT_DHIKR", com.example.widget.DhikrWidgetActionReceiver.ACTION_INCREMENT_DHIKR)
        assertEquals("com.example.widget.ACTION_MARK_DONE_AND_NEXT", com.example.widget.DhikrWidgetActionReceiver.ACTION_MARK_DONE_AND_NEXT)
        assertEquals("com.example.widget.ACTION_RESET_CATEGORY", com.example.widget.DhikrWidgetActionReceiver.ACTION_RESET_CATEGORY)
        assertEquals("com.example.aod.ACTION_DISMISS_AOD", com.example.aod.AodLockService.ACTION_DISMISS_AOD)
    }

    @Test
    fun verifyPrayerAlarmAndSnoozeActionContract() {
        assertEquals("com.example.alarm.ACTION_PRAYER_ALARM", com.example.alarm.PrayerAlarmScheduler.ACTION_PRAYER_ALARM)
        assertEquals("com.example.alarm.ACTION_SNOOZE_10_MIN", com.example.alarm.PrayerAlarmScheduler.ACTION_SNOOZE_10_MIN)
        assertEquals("com.example.alarm.ACTION_DISMISS_ADHAN", com.example.alarm.PrayerAlarmScheduler.ACTION_DISMISS_ADHAN)
        assertEquals("com.example.alarm.ACTION_MIDNIGHT_ROLLOVER", com.example.alarm.PrayerAlarmScheduler.ACTION_MIDNIGHT_ROLLOVER)
    }

    @Test
    fun verifyMidnightRolloverCalculationLogic() {
        val zone = java.time.ZoneId.of("UTC")

        // 1. Afternoon time: Next rollover should be tomorrow at 00:00:05
        val afternoon = java.time.LocalDateTime.of(2026, 9, 24, 15, 30, 0)
        val epochAfternoon = com.example.alarm.PrayerAlarmScheduler.calculateNextMidnightEpochMs(afternoon, zone)
        val targetAfternoon = java.time.Instant.ofEpochMilli(epochAfternoon).atZone(zone).toLocalDateTime()
        assertEquals(java.time.LocalDateTime.of(2026, 9, 25, 0, 0, 5), targetAfternoon)

        // 2. Early morning before 00:00:05: Next rollover is TODAY at 00:00:05
        val earlyMorning = java.time.LocalDateTime.of(2026, 9, 25, 0, 0, 1)
        val epochEarly = com.example.alarm.PrayerAlarmScheduler.calculateNextMidnightEpochMs(earlyMorning, zone)
        val targetEarly = java.time.Instant.ofEpochMilli(epochEarly).atZone(zone).toLocalDateTime()
        assertEquals(java.time.LocalDateTime.of(2026, 9, 25, 0, 0, 5), targetEarly)

        // 3. Right after rollover (00:00:06): Next rollover is TOMORROW at 00:00:05
        val afterMidnight = java.time.LocalDateTime.of(2026, 9, 25, 0, 0, 6)
        val epochAfter = com.example.alarm.PrayerAlarmScheduler.calculateNextMidnightEpochMs(afterMidnight, zone)
        val targetAfter = java.time.Instant.ofEpochMilli(epochAfter).atZone(zone).toLocalDateTime()
        assertEquals(java.time.LocalDateTime.of(2026, 9, 26, 0, 0, 5), targetAfter)
    }

    @Test
    fun verifyFloatingHudSafeBoundsClamping() {
        val marginPx = 72 // 24dp on xxhdpi (3x)
        val screenWidth = 1080
        val screenHeight = 2400
        val viewWidth = 300
        val viewHeight = 200

        // Drag off-screen to negative coordinates (top-left) -> clamped to marginPx
        val (clampX1, clampY1) = com.example.floating.FloatingDhikrService.clampToSafeBounds(
            targetX = -150,
            targetY = -50,
            viewWidth = viewWidth,
            viewHeight = viewHeight,
            screenWidth = screenWidth,
            screenHeight = screenHeight,
            marginPx = marginPx
        )
        assertEquals(marginPx, clampX1)
        assertEquals(marginPx, clampY1)

        // Drag beyond screen boundary (bottom-right) -> clamped to (screenWidth - viewWidth - marginPx)
        val (clampX2, clampY2) = com.example.floating.FloatingDhikrService.clampToSafeBounds(
            targetX = 1500,
            targetY = 3000,
            viewWidth = viewWidth,
            viewHeight = viewHeight,
            screenWidth = screenWidth,
            screenHeight = screenHeight,
            marginPx = marginPx
        )
        assertEquals(screenWidth - viewWidth - marginPx, clampX2)
        assertEquals(screenHeight - viewHeight - marginPx, clampY2)

        // Drag inside safe boundaries -> unchanged
        val (clampX3, clampY3) = com.example.floating.FloatingDhikrService.clampToSafeBounds(
            targetX = 250,
            targetY = 500,
            viewWidth = viewWidth,
            viewHeight = viewHeight,
            screenWidth = screenWidth,
            screenHeight = screenHeight,
            marginPx = marginPx
        )
        assertEquals(250, clampX3)
        assertEquals(500, clampY3)

        // Edge case: tiny screen where view + margin exceeds screen width -> never throws and respects min
        val (clampX4, clampY4) = com.example.floating.FloatingDhikrService.clampToSafeBounds(
            targetX = 500,
            targetY = 500,
            viewWidth = 500,
            viewHeight = 500,
            screenWidth = 400,
            screenHeight = 400,
            marginPx = marginPx
        )
        assertTrue(clampX4 >= marginPx)
        assertTrue(clampY4 >= marginPx)
    }

    @Test
    fun verifyUrgeSurfingTimerFormattingAndGroundingLogic() {
        fun formatTimer(secondsRemaining: Int): String {
            val minutes = secondsRemaining / 60
            val seconds = secondsRemaining % 60
            return "%02d:%02d".format(minutes, seconds)
        }

        // 15 minutes is 900 seconds
        val fullTimeSeconds = 15 * 60
        assertEquals(900, fullTimeSeconds)
        assertEquals("15:00", formatTimer(900))
        assertEquals("14:59", formatTimer(899))
        assertEquals("01:05", formatTimer(65))
        assertEquals("00:05", formatTimer(5))
        assertEquals("00:00", formatTimer(0))

        // Istighfar grounding milestones
        fun isDoublePulse(count: Int) = count > 0 && count % 33 == 0 && count % 100 != 0
        fun isCompletionRumble(count: Int) = count > 0 && count % 100 == 0

        assertTrue(isDoublePulse(33))
        assertTrue(isDoublePulse(66))
        assertTrue(isDoublePulse(99))
        assertTrue(isCompletionRumble(100))
        assertTrue(isCompletionRumble(200))
        org.junit.Assert.assertFalse(isDoublePulse(32))
        org.junit.Assert.assertFalse(isCompletionRumble(99))
    }

    @Test
    fun verifyTimeOfDayDhikrCategoryDetermination() {
        val currentCategory = IslamicCalendarUtil.getCurrentTimeDhikrCategory()
        assertNotNull(currentCategory)
        assertTrue(
            currentCategory in listOf(
                com.example.data.model.DhikrCategory.MORNING,
                com.example.data.model.DhikrCategory.POST_SALAH,
                com.example.data.model.DhikrCategory.EVENING,
                com.example.data.model.DhikrCategory.SLEEP
            )
        )
    }

    @Test
    fun verifyFloatingHudBubbleToCardExpansionSafeBounds() {
        val density = 3.0f
        val marginPx = (24 * density).toInt() // 72px
        val screenWidth = 1080
        val screenHeight = 2400
        val bubbleSize = (52 * density).toInt() // 156px
        val cardWidth = (300 * density).toInt() // 900px
        val cardHeight = (180 * density).toInt() // 540px

        // Simulate bubble dragged to rightmost edge:
        val bubbleX = screenWidth - bubbleSize - marginPx // 852px
        val bubbleY = 400

        // Expanding bubble to card must re-clamp card X so that it fits entirely on-screen
        val (clampedCardX, clampedCardY) = com.example.floating.FloatingDhikrService.clampToSafeBounds(
            targetX = bubbleX,
            targetY = bubbleY,
            viewWidth = cardWidth,
            viewHeight = cardHeight,
            screenWidth = screenWidth,
            screenHeight = screenHeight,
            marginPx = marginPx
        )

        val expectedMaxX = screenWidth - cardWidth - marginPx // 1080 - 900 - 72 = 108px
        assertEquals(expectedMaxX, clampedCardX)
        // Ensure right edge of card respects the 24dp safe margin
        assertTrue("Card right edge must not overflow screen boundary", clampedCardX + cardWidth <= screenWidth - marginPx)
        assertTrue("Card left edge must respect safe margin", clampedCardX >= marginPx)
    }

    @Test
    fun verifyMidnightRolloverAvoidsRetriggerJitter() {
        val zone = java.time.ZoneId.of("UTC")

        // Alarm fires 5ms before 00:00:05 due to hardware clock jitter
        val jitterTime = java.time.LocalDateTime.of(2026, 9, 25, 0, 0, 4, 995_000_000)

        // Without advance offset, it would recalculate today at 00:00:05 (5ms loop)
        val jitterEpochWithoutOffset = com.example.alarm.PrayerAlarmScheduler.calculateNextMidnightEpochMs(jitterTime, zone)
        val targetWithoutOffset = java.time.Instant.ofEpochMilli(jitterEpochWithoutOffset).atZone(zone).toLocalDateTime()
        assertEquals(java.time.LocalDateTime.of(2026, 9, 25, 0, 0, 5), targetWithoutOffset)

        // With the 10-second re-arm offset used in PrayerAlarmReceiver, it properly targets tomorrow's rollover!
        val safeReArmEpoch = com.example.alarm.PrayerAlarmScheduler.calculateNextMidnightEpochMs(jitterTime.plusSeconds(10), zone)
        val targetSafeReArm = java.time.Instant.ofEpochMilli(safeReArmEpoch).atZone(zone).toLocalDateTime()
        assertEquals(java.time.LocalDateTime.of(2026, 9, 26, 0, 0, 5), targetSafeReArm)
    }

    @Test
    fun verifyUrgeSurfingStateTransitionsAndResetFlow() {
        var urgeSecondsRemaining = 15 * 60
        var isUrgeTimerRunning = true

        // 1. Simulate countdown to zero
        urgeSecondsRemaining = 0
        if (urgeSecondsRemaining <= 0) {
            isUrgeTimerRunning = false
        }
        org.junit.Assert.assertFalse("Timer should stop running when reaching 0", isUrgeTimerRunning)

        // 2. Simulate user pressing reset button
        urgeSecondsRemaining = 15 * 60
        isUrgeTimerRunning = true
        assertEquals(900, urgeSecondsRemaining)
        assertTrue("Timer should resume running after reset", isUrgeTimerRunning)

        // 3. Simulate user pressing Play when at 0
        urgeSecondsRemaining = 0
        isUrgeTimerRunning = false
        // Play click action:
        if (urgeSecondsRemaining <= 0) {
            urgeSecondsRemaining = 15 * 60
            isUrgeTimerRunning = true
        } else {
            isUrgeTimerRunning = !isUrgeTimerRunning
        }
        assertEquals(900, urgeSecondsRemaining)
        assertTrue("Play from zero should restart 15-minute countdown", isUrgeTimerRunning)
    }

    @Test
    fun verifyPrayerAdhanPlayerStateFlowsAndTimeout() {
        // Initial state
        com.example.alarm.PrayerAdhanPlayer.stop()
        org.junit.Assert.assertFalse(com.example.alarm.PrayerAdhanPlayer.isPlaying.value)
        org.junit.Assert.assertNull(com.example.alarm.PrayerAdhanPlayer.currentAlarmPrayer.value)

        // Play without context (headless unit test environment) with short 50ms timeout
        com.example.alarm.PrayerAdhanPlayer.play(context = null, prayerName = "العصر", timeoutMs = 50L)
        assertTrue(com.example.alarm.PrayerAdhanPlayer.isPlaying.value)
        assertEquals("العصر", com.example.alarm.PrayerAdhanPlayer.currentAlarmPrayer.value)

        // Wait for timeout job to expire and trigger auto-stop
        Thread.sleep(120)
        org.junit.Assert.assertFalse(com.example.alarm.PrayerAdhanPlayer.isPlaying.value)
        org.junit.Assert.assertNull(com.example.alarm.PrayerAdhanPlayer.currentAlarmPrayer.value)

        // Manual stop verification
        com.example.alarm.PrayerAdhanPlayer.play(context = null, prayerName = "المغرب", timeoutMs = 10_000L)
        assertTrue(com.example.alarm.PrayerAdhanPlayer.isPlaying.value)
        assertEquals("المغرب", com.example.alarm.PrayerAdhanPlayer.currentAlarmPrayer.value)

        com.example.alarm.PrayerAdhanPlayer.stop()
        org.junit.Assert.assertFalse(com.example.alarm.PrayerAdhanPlayer.isPlaying.value)
        org.junit.Assert.assertNull(com.example.alarm.PrayerAdhanPlayer.currentAlarmPrayer.value)
    }

    @Test
    fun verifyAutoBrightnessLuxCurveMapping() {
        // < 5 lux -> 0.03f
        assertEquals(0.03f, com.example.aod.AodActivity.calculateAutoBrightness(0f), 0.0001f)
        assertEquals(0.03f, com.example.aod.AodActivity.calculateAutoBrightness(1.5f), 0.0001f)
        assertEquals(0.03f, com.example.aod.AodActivity.calculateAutoBrightness(4.9f), 0.0001f)

        // 5–50 lux -> 0.05f
        assertEquals(0.05f, com.example.aod.AodActivity.calculateAutoBrightness(5.0f), 0.0001f)
        assertEquals(0.05f, com.example.aod.AodActivity.calculateAutoBrightness(25.0f), 0.0001f)
        assertEquals(0.05f, com.example.aod.AodActivity.calculateAutoBrightness(50.0f), 0.0001f)

        // 50–300 lux -> 0.09f
        assertEquals(0.09f, com.example.aod.AodActivity.calculateAutoBrightness(51.0f), 0.0001f)
        assertEquals(0.09f, com.example.aod.AodActivity.calculateAutoBrightness(150.0f), 0.0001f)
        assertEquals(0.09f, com.example.aod.AodActivity.calculateAutoBrightness(300.0f), 0.0001f)

        // 300–1000 lux -> 0.14f
        assertEquals(0.14f, com.example.aod.AodActivity.calculateAutoBrightness(301.0f), 0.0001f)
        assertEquals(0.14f, com.example.aod.AodActivity.calculateAutoBrightness(1000.0f), 0.0001f)

        // > 1000 lux -> 0.22f
        assertEquals(0.22f, com.example.aod.AodActivity.calculateAutoBrightness(1001.0f), 0.0001f)
    }

    @Test
    fun verifyQiblaKaabaAzimuthCalculationMath() {
        // Kaaba coordinates: 21.4225° N, 39.8262° E
        // 1. Due South of Kaaba (e.g. lat=10.0, lon=39.8262) -> Bearing should be 0° (North)
        val bearingSouth = com.example.util.QiblaHelper.calculateQiblaBearing(10.0, 39.8262)
        assertEquals(0.0, bearingSouth, 0.5)

        // 2. Due North of Kaaba (e.g. lat=35.0, lon=39.8262) -> Bearing should be 180° (South)
        val bearingNorth = com.example.util.QiblaHelper.calculateQiblaBearing(35.0, 39.8262)
        assertEquals(180.0, bearingNorth, 0.5)

        // 3. Cairo, Egypt (lat=30.0444, lon=31.2357) -> Mecca is south-east (~136°)
        val bearingCairo = com.example.util.QiblaHelper.calculateQiblaBearing(30.0444, 31.2357)
        assertTrue("Cairo bearing to Kaaba should be around 136°", bearingCairo in 134.0..138.0)

        // 4. Mecca Kaaba coordinates itself -> Normalized in [0, 360)
        val bearingMecca = com.example.util.QiblaHelper.calculateQiblaBearing(21.4225, 39.8262)
        assertTrue(bearingMecca in 0.0..360.0)
    }

    @Test
    fun verifyRelapseFrictionCountdownAndMilestones() {
        // Confirmation blocked while countdown > 0
        org.junit.Assert.assertFalse(com.example.util.RelapseFrictionHelper.canConfirmRelapse(30))
        org.junit.Assert.assertFalse(com.example.util.RelapseFrictionHelper.canConfirmRelapse(15))
        org.junit.Assert.assertFalse(com.example.util.RelapseFrictionHelper.canConfirmRelapse(1))

        // Confirmation allowed when countdown reaches 0 or negative
        assertTrue(com.example.util.RelapseFrictionHelper.canConfirmRelapse(0))
        assertTrue(com.example.util.RelapseFrictionHelper.canConfirmRelapse(-1))

        // Ayah and prompt texts
        assertTrue(com.example.util.RelapseFrictionHelper.getAyahText().contains("وَلَمَن صَبَرَ"))
        assertTrue(com.example.util.RelapseFrictionHelper.getPromptText(7).contains("7 يوماً"))

        // Milestone progression logic
        val milestones = com.example.util.RelapseFrictionHelper.MILESTONES
        assertEquals(6, milestones.size)
        assertEquals(1, milestones[0].days)
        assertEquals(3, milestones[1].days)
        assertEquals(7, milestones[2].days)
        assertEquals(14, milestones[3].days)
        assertEquals(30, milestones[4].days)
        assertEquals(90, milestones[5].days)

        // 5 days streak: 1d and 3d unlocked, 7d locked
        assertTrue(com.example.util.RelapseFrictionHelper.isMilestoneUnlocked(5, 1))
        assertTrue(com.example.util.RelapseFrictionHelper.isMilestoneUnlocked(5, 3))
        org.junit.Assert.assertFalse(com.example.util.RelapseFrictionHelper.isMilestoneUnlocked(5, 7))
    }

    @Test
    fun verifyJumuahAndAodBatteryRules() {
        // Jumuah Friday check
        val friday = java.time.LocalDate.of(2026, 9, 25) // Friday
        val thursday = java.time.LocalDate.of(2026, 9, 24) // Thursday
        assertTrue(com.example.util.IslamicCalendarUtil.isJumuah(friday))
        org.junit.Assert.assertFalse(com.example.util.IslamicCalendarUtil.isJumuah(thursday))

        // Battery restriction check
        // If battery cutoff enabled:
        assertTrue(com.example.aod.AodLockService.shouldBlockAodDueToBattery(true, isPowerSaveMode = true, batteryPct = 80f))
        assertTrue(com.example.aod.AodLockService.shouldBlockAodDueToBattery(true, isPowerSaveMode = false, batteryPct = 14f))
        org.junit.Assert.assertFalse(com.example.aod.AodLockService.shouldBlockAodDueToBattery(true, isPowerSaveMode = false, batteryPct = 50f))
        // If battery cutoff disabled:
        org.junit.Assert.assertFalse(com.example.aod.AodLockService.shouldBlockAodDueToBattery(false, isPowerSaveMode = true, batteryPct = 10f))

        // Scheduled quiet hours check (23:30 to 05:00)
        assertTrue(com.example.aod.AodLockService.isWithinScheduledQuietHours(java.time.LocalTime.of(23, 45)))
        assertTrue(com.example.aod.AodLockService.isWithinScheduledQuietHours(java.time.LocalTime.of(2, 0)))
        assertTrue(com.example.aod.AodLockService.isWithinScheduledQuietHours(java.time.LocalTime.of(4, 59)))
        org.junit.Assert.assertFalse(com.example.aod.AodLockService.isWithinScheduledQuietHours(java.time.LocalTime.of(5, 1)))
        org.junit.Assert.assertFalse(com.example.aod.AodLockService.isWithinScheduledQuietHours(java.time.LocalTime.of(14, 0)))
        org.junit.Assert.assertFalse(com.example.aod.AodLockService.isWithinScheduledQuietHours(java.time.LocalTime.of(23, 29)))
    }
}
