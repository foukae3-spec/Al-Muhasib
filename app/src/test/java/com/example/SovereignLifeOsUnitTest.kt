package com.example

import com.example.data.model.AppGuardRule
import com.example.data.model.AppNiyyahSession
import com.example.data.model.DailyLifeMission
import com.example.floating.FloatingDhikrService
import com.example.ui.theme.HeavenlyLiquidGlassTokens
import com.example.ui.theme.SacredAccentPalette
import com.example.util.AppLaunchDecision
import com.example.util.AppLauncherManager
import com.example.util.AppSanctuaryTier
import com.example.util.DirectiveActionType
import com.example.util.InstalledLauncherApp
import com.example.util.SacredPhonePhase
import com.example.util.SacredPhonePhaseEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SovereignLifeOsUnitTest {

    @Test
    fun verifySacredPhonePhaseDiurnalResolution() {
        // Test manual overrides
        for (phase in SacredPhonePhase.entries) {
            val resolved = SacredPhonePhaseEngine.resolveCurrentPhase(
                overridePhase = phase,
                minutesToNextPrayer = 45,
                isCurrentPrayerLogged = true,
                nightGuardEnabled = true
            )
            assertEquals("Manual override should return exact phase", phase, resolved)
        }

        // Test Salah Convergence priority when next prayer <= 15 minutes and prayer not yet logged
        val convergencePhase = SacredPhonePhaseEngine.resolveCurrentPhase(
            overridePhase = null,
            minutesToNextPrayer = 10,
            isCurrentPrayerLogged = false,
            nightGuardEnabled = true
        )
        assertEquals(SacredPhonePhase.SALAH_CONVERGENCE, convergencePhase)
        assertTrue(convergencePhase.locksDopamineAppsByDefault)
        assertTrue(convergencePhase.requiresNiyyahForAllNonEssential)
    }

    @Test
    fun verifyLifeDirectiveSynthesisForPhases() {
        // Verify Fajr directive
        val fajrDirective = SacredPhonePhaseEngine.generateLifeDirective(
            phase = SacredPhonePhase.FAJR_DAWN,
            nextPrayerName = "Dhuhr",
            minutesToNextPrayer = 300,
            completedPrayersCount = 0,
            pendingMissionsCount = 3,
            activeMissionTitle = "Morning Adhkar",
            screenTimeMinutesToday = 5,
            dailyScreenBudgetMinutes = 60,
            activeBookTitle = "Al-Daa wa Al-Dawaa",
            currentBookChunk = 4,
            muhasabahCompletedToday = false
        )
        assertEquals(DirectiveActionType.OPEN_ADHKAR, fajrDirective.ctaActionType)
        assertNotNull(fajrDirective.arabicBadge)
        assertNotNull(fajrDirective.headline)

        // Verify Salah Convergence directive
        val salahDirective = SacredPhonePhaseEngine.generateLifeDirective(
            phase = SacredPhonePhase.SALAH_CONVERGENCE,
            nextPrayerName = "Asr",
            minutesToNextPrayer = 5,
            completedPrayersCount = 2,
            pendingMissionsCount = 2,
            activeMissionTitle = "Deep Craft",
            screenTimeMinutesToday = 20,
            dailyScreenBudgetMinutes = 60,
            activeBookTitle = "Al-Daa wa Al-Dawaa",
            currentBookChunk = 4,
            muhasabahCompletedToday = false
        )
        assertEquals(DirectiveActionType.LOG_PRAYER, salahDirective.ctaActionType)

        // Verify Duha focus directive
        val duhaDirective = SacredPhonePhaseEngine.generateLifeDirective(
            phase = SacredPhonePhase.DUHA_DEEP_WORK,
            nextPrayerName = "Dhuhr",
            minutesToNextPrayer = 120,
            completedPrayersCount = 1,
            pendingMissionsCount = 2,
            activeMissionTitle = "Deep Craft Deliverable",
            screenTimeMinutesToday = 25,
            dailyScreenBudgetMinutes = 60,
            activeBookTitle = "Al-Daa wa Al-Dawaa",
            currentBookChunk = 4,
            muhasabahCompletedToday = false
        )
        assertEquals(DirectiveActionType.START_FOCUS_MISSION, duhaDirective.ctaActionType)
    }

    @Test
    fun verifyAppTierClassificationAndLaunchEvaluation() {
        // Essential Tools: Allowed immediately
        val dialerApp = InstalledLauncherApp(
            packageName = "com.android.dialer",
            label = "Phone",
            tier = AppSanctuaryTier.ESSENTIAL_UTILITY,
            usageMinutesToday = 5,
            dailyLimitMinutes = 120
        )
        val dialerDecision = AppLauncherManager.evaluateLaunchAttempt(
            app = dialerApp,
            currentPhase = SacredPhonePhase.DUHA_DEEP_WORK,
            salahLockActive = false,
            nightGuardActive = false
        )
        assertTrue(dialerDecision is AppLaunchDecision.AllowedImmediately)

        // Work Apps: Require Niyyah gate during Duha
        val notionApp = InstalledLauncherApp(
            packageName = "notion.id",
            label = "Notion",
            tier = AppSanctuaryTier.WORK_AND_PROVISION,
            usageMinutesToday = 15,
            dailyLimitMinutes = 90
        )
        val notionDecision = AppLauncherManager.evaluateLaunchAttempt(
            app = notionApp,
            currentPhase = SacredPhonePhase.DUHA_DEEP_WORK,
            salahLockActive = false,
            nightGuardActive = false
        )
        assertTrue(notionDecision is AppLaunchDecision.RequiresNiyyahGate)

        // Dopamine Apps: Hard-blocked during Salah Convergence
        val youtubeApp = InstalledLauncherApp(
            packageName = "com.google.android.youtube",
            label = "YouTube",
            tier = AppSanctuaryTier.GUARDED_DOPAMINE,
            usageMinutesToday = 10,
            dailyLimitMinutes = 20
        )
        val ytDecisionDuringSalah = AppLauncherManager.evaluateLaunchAttempt(
            app = youtubeApp,
            currentPhase = SacredPhonePhase.SALAH_CONVERGENCE,
            salahLockActive = true,
            nightGuardActive = false
        )
        assertTrue("YouTube must be hard blocked during Salah convergence", ytDecisionDuringSalah is AppLaunchDecision.HardBlocked)

        // Dopamine Apps: Hard-blocked during Night Guard
        val ytDecisionNight = AppLauncherManager.evaluateLaunchAttempt(
            app = youtubeApp,
            currentPhase = SacredPhonePhase.NIGHT_GUARD_LOCKDOWN,
            salahLockActive = false,
            nightGuardActive = true
        )
        assertTrue("YouTube must be hard blocked during night lockdown", ytDecisionNight is AppLaunchDecision.HardBlocked)

        // Over-budget Dopamine App: Hard-blocked regardless of phase
        val overBudgetYt = youtubeApp.copy(usageMinutesToday = 25, dailyLimitMinutes = 20)
        val overBudgetDecision = AppLauncherManager.evaluateLaunchAttempt(
            app = overBudgetYt,
            currentPhase = SacredPhonePhase.DUHA_DEEP_WORK,
            salahLockActive = false,
            nightGuardActive = false
        )
        assertTrue("Over-budget app must be hard blocked", overBudgetDecision is AppLaunchDecision.HardBlocked)
    }

    @Test
    fun verifyFloatingDhikrBoundsClampingEdgeCases() {
        val screenW = 1080
        val screenH = 2400
        val viewW = 400
        val viewH = 100
        val margin = 50

        // Case 1: Negative coordinates (dragged past top-left)
        val (negX, negY) = FloatingDhikrService.clampToSafeBounds(
            targetX = -200,
            targetY = -150,
            viewWidth = viewW,
            viewHeight = viewH,
            screenWidth = screenW,
            screenHeight = screenH,
            marginPx = margin
        )
        assertEquals(margin, negX)
        assertEquals(margin, negY)

        // Case 2: Excessive coordinates (dragged past bottom-right)
        val (hugeX, hugeY) = FloatingDhikrService.clampToSafeBounds(
            targetX = 2000,
            targetY = 4000,
            viewWidth = viewW,
            viewHeight = viewH,
            screenWidth = screenW,
            screenHeight = screenH,
            marginPx = margin
        )
        assertEquals(screenW - viewW - margin, hugeX)
        assertEquals(screenH - viewH - margin, hugeY)

        // Case 3: Valid middle coordinates
        val (midX, midY) = FloatingDhikrService.clampToSafeBounds(
            targetX = 350,
            targetY = 500,
            viewWidth = viewW,
            viewHeight = viewH,
            screenWidth = screenW,
            screenHeight = screenH,
            marginPx = margin
        )
        assertEquals(350, midX)
        assertEquals(500, midY)
    }

    @Test
    fun verifyHeavenlyDesignSystemTokensAndPalettes() {
        // Verify all 6 SacredAccentPalettes exist with non-null colors and brushes
        for (palette in SacredAccentPalette.entries) {
            assertNotNull(palette.displayName)
            assertNotNull(palette.primaryTint)
            assertNotNull(palette.secondaryTint)
            assertNotNull(palette.canvasTop)
            assertNotNull(palette.canvasMid)
            assertNotNull(palette.canvasBottom)
            assertNotNull(palette.specularHighlight)
            assertNotNull(palette.ambientGlow)
        }

        // Verify fluid spring physics specification
        val springSpec = HeavenlyLiquidGlassTokens.fluidSpring<Float>()
        assertEquals(0.76f, springSpec.dampingRatio)
    }

    @Test
    fun verifyLifeOsDomainModels() {
        val mission = DailyLifeMission(
            id = 10,
            dateString = "2026-11-06",
            title = "Test Amal Mission",
            prayerAnchor = "DUHA_BLOCK",
            category = "DEEN",
            estimatedMinutes = 30,
            completedMinutes = 30,
            isCompleted = true,
            barakahPoints = 20
        )
        assertEquals(10, mission.id)
        assertTrue(mission.isCompleted)

        val session = AppNiyyahSession(
            id = 1,
            dateString = "2026-11-06",
            packageName = "com.test.app",
            appLabel = "Test App",
            statedNiyyah = "Checking study note",
            allottedMinutes = 10,
            honoredTimeBox = true
        )
        assertTrue(session.honoredTimeBox)
        assertEquals(10, session.allottedMinutes)

        val rule = AppGuardRule(
            packageName = "com.social.app",
            appLabel = "Social",
            sanctuaryTierName = "GUARDED_DOPAMINE",
            dailyLimitMinutes = 15,
            requireNiyyahPrompt = true,
            blockDuringSalahWindow = true,
            blockDuringNightGuard = true,
            isPinnedToHomeDock = false
        )
        assertTrue(rule.blockDuringSalahWindow)
        assertTrue(rule.blockDuringNightGuard)
        assertEquals(15, rule.dailyLimitMinutes)
    }
}
