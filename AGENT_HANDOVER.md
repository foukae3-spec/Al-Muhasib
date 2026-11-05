# Al-Muhasib — Agent Handover & Current Project State

> **Notice for the Next AI Assistant**: Read this file first. It contains the exact state of the codebase, completed features, resolved architecture bugs, environment setup, details on what the background subagent was doing when interrupted, and immediate next steps as of September 24, 2026.

---

## 1. Project Overview
- **App Name**: Al-Muhasib (المُحاسِب) — Islamic Spiritual Guardian, Dopamine Recovery, Hisn al-Muslim Adhkar Engine & Custom OLED AOD.
- **Package**: `com.aistudio.almuhasib.spgdaod` (Namespace: `com.example`)
- **Tech Stack**: Kotlin 2.2.10, Jetpack Compose (BOM 2024.09.00), Material 3, Room 2.7.0, KSP 2.3.5, Coroutines, Gradle 9.3.1, AGP 9.1.1.
- **Target OS / Hardware**: Android 14+ (Target SDK 36, Min SDK 24). Optimized for **Poco F4 (Xiaomi HyperOS / MIUI)** with AMOLED display.

---

## 2. What the Subagent Was Working On When Halted

The previous agent invoked `DeepCoderImprovementWorker` (session `efb578b7-9ee7-4513-98ed-4cf74816186c`) to implement and verify 4 major user requests:
1. **Fix AOD returning to lockscreen instead of showing custom AOD**:
   - Diagnosed root cause: `FLAG_ALLOW_LOCK_WHILE_SCREEN_ON` was previously active in `AodActivity`, which instructed Android/MIUI to draw the keyguard on top of the AOD.
   - Removed that flag, added high-priority Full-Screen Intent notification launcher (`CHANNEL_ID_AOD_LAUNCHER`), transparent system bars (`FLAG_LAYOUT_NO_LIMITS`), and enforced `setShowWhenLocked(true)` and `setTurnScreenOn(true)` before `super.onCreate()`.
2. **Context-aware dynamic Adhkar home screen widget**:
   - Implemented `IslamicCalendarUtil.getCurrentDhikrPeriod()` mapping time of day to Morning (Sabah), Evening (Masaa), Post-Salah, or Sleep.
   - Updated `widget_dhikr.xml` and `WidgetUpdateHelper.kt` with a 1-tap "تم ✓" button (`ACTION_MARK_DONE_AND_NEXT`) that increments count and immediately advances to the next Dhikr on the home screen.
3. **Floating Adhkar HUD over other apps**:
   - Built `FloatingDhikrService.kt` using `SYSTEM_ALERT_WINDOW` (`TYPE_APPLICATION_OVERLAY`).
   - Created `layout_floating_dhikr.xml` with dark green neumorphic styling, live counter, next dhikr button, and minimize/dismiss actions.
   - Added toggle switch in `AodSettingsScreen.kt`.
4. **Exact prayer time notifications with Adhan and 10-minute snooze**:
   - Shifted notification from 30 minutes early to **exact scheduled prayer time** (`setExactAndAllowWhileIdle`).
   - Added Adhan audio playback via `PrayerAdhanPlayer.kt`.
   - Added notification action button "تذكير بعد 10 دقائق" (`ACTION_SNOOZE_10_MIN`).

### At the Exact Moment of Interruption:
- The subagent was inspecting `d:/al-muhasib/app/src/main/java/com/example/alarm/PrayerWarningActivity.kt` (lines 240–450).
- **Goal**: Checking how `PrayerWarningActivity` presents the full-screen emergency prayer warning and snooze button when the prayer deadline is imminent, ensuring the snooze action in the activity harmonizes with the 10-minute snooze action in `PrayerAlarmReceiver.kt`.
- The code in `PrayerWarningActivity.kt` is compiling and valid.
- All 33 unit tests pass cleanly (`.\gradlew.bat testDebugUnitTest`).
- The debug APK (`al-muhasib-debug.apk`) has been compiled and is bundled in the root.

---

## 3. Completed Features & Code References

### A. Dark Green Neumorphic UI Design
- **Color Palette** (`app/src/main/java/com/example/ui/theme/Color.kt`):
  - Base: `DarkBackground = Color(0xFF0A150F)` (Pitch Forest Green, OLED-friendly).
  - Surface: `DarkSurface = Color(0xFF0E1E16)`, `DarkCard = Color(0xFF13281E)`.
  - Ambient Highlights: `DarkHighlight = Color(0x1F34D399)`, `DarkShadow = Color(0xCC030805)`.
  - Text: `DarkTextPrimary = Color(0xFFEDF8F2)` (Radiant Mint), `DarkTextSecondary = Color(0xFF8FAFA0)` (Sage).
- **3 Distinct Spiritual Ring Colors**:
  - **Outer Ring (5 Daily Prayers)**: `RingPrayerColor = Color(0xFF10B981)` (Luminous Emerald)
  - **Middle Ring (Dhikr & Istighfar)**: `RingDhikrColor = Color(0xFF06B6D4)` (Celestial Cyan / Azure)
  - **Inner Ring (Purity & Sobriety Streak)**: `RingDetoxColor = Color(0xFFF59E0B)` (Warm Amber Gold)

### B. True Custom OLED Always-On Display (AOD)
- **Automatic Screen-Off Trigger** (`app/src/main/java/com/example/aod/AodLockService.kt`):
  - Foreground service listening for `Intent.ACTION_SCREEN_OFF`. Wakes `AodActivity` automatically when the phone is locked.
  - Automatically dismisses on `Intent.ACTION_USER_PRESENT` (fingerprint/PIN unlock).
- **Lockscreen Overlap Fix** (`app/src/main/java/com/example/aod/AodActivity.kt`):
  - Removed `FLAG_ALLOW_LOCK_WHILE_SCREEN_ON`.
  - Full-Screen Intent notification fallback (`CHANNEL_ID_AOD_LAUNCHER`), `setShowWhenLocked(true)`, `setTurnScreenOn(true)` before `onCreate`/window attachment, `FLAG_LAYOUT_NO_LIMITS`, transparent status/navigation bars.
- **AOD Interactivity & Protection**:
  - Digital Misbaha directly on AOD screen with haptic feedback.
  - Double-tap to wake/exit to lockscreen.
  - Pocket mode via proximity sensor (`Sensor.TYPE_PROXIMITY`).
  - Burn-in pixel shifting (shifts elements 2–3 pixels every 60s).

### C. Context-Aware Dynamic Adhkar Home Screen Widget
- **Automatic Period Determination** (`app/src/main/java/com/example/util/IslamicCalendarUtil.kt`):
  - Maps hour to `MORNING`, `EVENING`, `SLEEP`, or `POST_SALAH`.
- **Interactive Progression** (`app/src/main/java/com/example/widget/DhikrWidgetActionReceiver.kt`, `WidgetUpdateHelper.kt`):
  - Shows current Adhkar text in Arabic with count.
  - "+1" button increments counter.
  - "تم ✓" button (`ACTION_MARK_DONE_AND_NEXT`) marks as done and transitions to next Dhikr directly on the home screen.

### D. Floating Overlay Adhkar HUD (On Top of Other Apps)
- **Implementation** (`app/src/main/java/com/example/floating/FloatingDhikrService.kt`):
  - Uses `SYSTEM_ALERT_WINDOW` (`TYPE_APPLICATION_OVERLAY`).
  - Draggable dark green neumorphic floating pill displaying Arabic text, counter, tap-to-advance, and minimize/dismiss.
  - Can be toggled on/off in **الإعدادات -> شاشة AOD -> نافذة الأذكار العائمة**.

### E. Exact Prayer Notification, Adhan Audio & 10-Minute Snooze
- **Exact Timing** (`app/src/main/java/com/example/alarm/PrayerAlarmScheduler.kt`):
  - Fires at exact scheduled prayer minute using `AlarmManager.setExactAndAllowWhileIdle`.
- **Adhan Audio** (`app/src/main/java/com/example/alarm/PrayerAdhanPlayer.kt`):
  - Plays Adhan on `AudioAttributes.USAGE_ALARM`.
- **10-Minute Snooze Action** (`app/src/main/java/com/example/alarm/PrayerAlarmReceiver.kt`):
  - Action button: **"تذكير بعد 10 دقائق"** (`ACTION_SNOOZE_10_MIN`). Schedules snooze for `now + 10m` and clears current notification.

---

## 4. Environment & Build Requirements

1. **Java JDK 21**:
   - Must use full JDK 21 containing `bin/jlink.exe` (AGP 9.1.1 + compileSdk 36 requires jlink).
   - Set in PowerShell:
     ```powershell
     $env:JAVA_HOME = "C:\Users\<user>\.jdks\jdk-21.0.12.1+1" # (or your local JDK 21 path)
     $env:ANDROID_HOME = "C:\Users\<user>\AppData\Local\Android\Sdk"
     ```
2. **Gradle Commands**:
   - Run unit tests: `.\gradlew.bat testDebugUnitTest` (33 tests currently passing)
   - Build debug APK: `.\gradlew.bat assembleDebug`
   - Output APK path: `app/build/outputs/apk/debug/app-debug.apk` (and `al-muhasib-debug.apk` in project root)

---

## 5. Immediate Next Steps / Roadmap for Next Agent

1. **Install and Test APK on Target Device (Poco F4 / Xiaomi HyperOS)**:
   - Install: `adb install -r al-muhasib-debug.apk`
   - Enable required MIUI/HyperOS permissions in **Settings -> Apps -> Manage apps -> Al-Muhasib -> Other permissions**:
     - Enable **"Display pop-up windows while running in the background"** (required for AOD & Floating HUD)
     - Enable **"Show on Lock screen"** (required for AOD over keyguard)
     - Set Battery Saver to **"No restrictions"**
2. **Device Verification Checklist**:
   - Lock phone -> Verify `AodActivity` displays on OLED without showing lockscreen or status bar heading.
   - Tap digital misbaha on AOD -> Check haptic feedback & counter increment.
   - Add home screen Adhkar widget -> Verify current period dhikr displays and "تم ✓" advances to next.
   - Enable Floating HUD in AOD Settings -> Verify floating pill appears over other apps.
   - Test prayer alarm -> Verify exact firing, Adhan playback, and 10-minute snooze notification button.
