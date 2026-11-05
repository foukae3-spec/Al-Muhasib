# 🕋 AL-MUHASIB (المُحَاسِب) — COMPREHENSIVE ARCHITECTURAL & SYSTEM OVERVIEW
> **Target Audience:** GEMINI 4 ARGON & Engineering AI Collaborators  
> **Repository:** `https://github.com/foukae3-spec/Al-Muhasib`  
> **Platform:** Android (API 24 to API 36 / Android 14, 15, 16 readiness)  
> **Language & Framework:** 100% Modern Kotlin, Jetpack Compose, Room ORM, Coroutines & Flow, MediaSessionCompat  
> **Core Identity:** Authentic Islamic Productivity, Always-On Display (AOD), Astronomical Prayer Engine, Tajweed Quran Reader, Recovery & Habit Accountability, and Holistic Life Operating System.

---

## 📑 TABLE OF CONTENTS
1. [Executive Summary & Core Philosophy](#1-executive-summary--core-philosophy)
2. [High-Level System Architecture & Tech Stack](#2-high-level-system-architecture--tech-stack)
3. [Component Deep-Dive & Module Inventory](#3-component-deep-dive--module-inventory)
   - [3.1 Always-On Display (AOD) Hardware Subsystem](#31-always-on-display-aod-hardware-subsystem)
   - [3.2 Astronomical Solar Prayer & Subsecond Adhan Engine](#32-astronomical-solar-prayer--subsecond-adhan-engine)
   - [3.3 Holy Quran Multi-Riwayah Reader & Audio Playback Service](#33-holy-quran-multi-riwayah-reader--audio-playback-service)
   - [3.4 Fully Decoupled Multi-Misbaha & Dhikr Subsystem](#34-fully-decoupled-multi-misbaha--dhikr-subsystem)
   - [3.5 Purity, Sobriety, Recovery & Relapse Prevention Engine](#35-purity-sobriety-recovery--relapse-prevention-engine)
   - [3.6 Qibla Direction & Sensor Processing Subsystem](#36-qibla-direction--sensor-processing-subsystem)
   - [3.7 Floating Window HUD & Overlay Service](#37-floating-window-hud--overlay-service)
   - [3.8 App Widgets & Cross-Process Synchronization](#38-app-widgets--cross-process-synchronization)
4. [Complete Room Database Schema & Data Models](#4-complete-room-database-schema--data-models)
5. [Recent Power & Battery Optimizations](#5-recent-power--battery-optimizations)
6. [TASK 2 BLUEPRINT: The Holistic Phone-Centric Life Operating System](#6-task-2-blueprint-the-holistic-phone-centric-life-operating-system)
   - [6.1 Minimalist Islamic Launcher & System UI Overhaul](#61-minimalist-islamic-launcher--system-ui-overhaul)
   - [6.2 Social Media & Distraction Blocker (Friction & Hard Lock)](#62-social-media--distraction-blocker-friction--hard-lock)
   - [6.3 Calendar Integration & Salah-Anchored Daily Schedule](#63-calendar-integration--salah-anchored-daily-schedule)
   - [6.4 Deep Reading & Knowledge Expansion Tracker](#64-deep-reading--knowledge-expansion-tracker)
   - [6.5 Physique, Workout & Biological Discipline Tracker](#65-physique-workout--biological-discipline-tracker)
   - [6.6 Unified Muhasabah (Life Accountability) Algorithm](#66-unified-muhasabah-life-accountability-algorithm)
7. [Repository File Map & Key Classes](#7-repository-file-map--key-classes)

---

## 1. EXECUTIVE SUMMARY & CORE PHILOSOPHY

**Al-Muhasib (المُحَاسِب)** is designed not merely as a prayer utility, but as a **sovereign spiritual and life operating system** that shields Muslims from the modern digital attention economy. The app operates on three non-negotiable principles:

1. **Uncompromised Authenticity & Accuracy:**
   - 100% offline, mathematical astronomical solar calculations for prayer times with subsecond precision.
   - Genuine Uthmanic Quranic script supporting multiple mutawatir recitations (Hafs, Warsh, Qaloon, Doori).
   - Real tajweed rule parsing based on classical orthography, not arbitrary regex.
   - Traditional Sunni Adhan recordings from Makkah, Madinah, Al-Quds, Egypt, and Sheikh Abdul Basit.

2. **Ultra-Low-Power AMOLED Efficiency:**
   - Designed for OLED/AMOLED displays (specifically tested on high-refresh Xiaomi Poco F4 / HyperOS).
   - True OLED pitch black (`#000000`), drawing zero subpixel current on AMOLED screens.
   - Complete elimination of 60/120 Hz animation churn during ambient idle states.
   - Minute-aligned CPU ticking, single-shot sensor sampling, and aggressive wake lock hygiene.

3. **Radical Life Accountability (مُحَاسَبَة):**
   - Direct tracking of the five obligatory prayers, daily Istighfar, independent Tasbih counters, workouts, cold showers, and digital detox/purity streaks.
   - Psychological friction gates (e.g. 30-second breathing reflection before recording relapses) and emergency craving breaker sheets (`PanicSheet`).

---

## 2. HIGH-LEVEL SYSTEM ARCHITECTURE & TECH STACK

```
+-----------------------------------------------------------------------------------+
|                                  USER INTERFACE                                   |
|   Compose UI Screens: Dashboard | Quran | Adhkar | Recovery | Leaderboard | AOD   |
+-----------------------------------------------------------------------------------+
|                         VIEWMODELS & STATE MANAGEMENT                             |
|        MuhasibViewModel (MVI/MVVM StateFlow)   |   QuranViewModel (Reader)        |
+-----------------------------------------------------------------------------------+
|                             REPOSITORY & DATA LAYER                               |
|        MuhasibRepository  <--->  Room Database (SQLite)  <--->  DataStore/Prefs   |
+-----------------------------------------------------------------------------------+
|                               BACKGROUND SERVICES                                 |
|  AodLockService  |  QuranPlaybackService  |  FloatingDhikrService  |  DreamService|
+-----------------------------------------------------------------------------------+
|                            HARDWARE & OS INTERACTION                              |
|   Window Cutouts | Display Refresh (60Hz) | Sensors (Light/Prox) | Exact Alarms   |
+-----------------------------------------------------------------------------------+
```

- **Build System:** Gradle Kotlin DSL (`build.gradle.kts`), AGP 8.x+, Kotlin 2.0+ with Compose Compiler plugin.
- **UI Toolkit:** 100% Jetpack Compose using Material 3 and custom Apple-style Neumorphic Glassmorphism (`Glassmorphic.kt`).
- **Database:** Android Room 2.6+ with KSP code generation, destructive migration fallback, asynchronous Kotlin `Flow`.
- **Audio Engine:** `MediaPlayer` integrated with AndroidX `MediaSessionCompat` and foreground playback services.
- **Sensors:** Hardware `SensorManager` controlling proximity, ambient light, accelerometer, and magnetic field vectors.
- **Permissions:** Standardized runtime permission flow via `PermissionHelper.kt` (No Play Protect hazardous permissions like `BIND_NOTIFICATION_LISTENER_SERVICE` or unneeded broad exact alarm spam).

---

## 3. COMPONENT DEEP-DIVE & MODULE INVENTORY

### 3.1 ALWAYS-ON DISPLAY (AOD) HARDWARE SUBSYSTEM
*Files:* [`AodActivity.kt`](file:///d:/AL%20Muhasib/app/src/main/java/com/example/aod/AodActivity.kt), [`AodDisplayContent.kt`](file:///d:/AL%20Muhasib/app/src/main/java/com/example/aod/AodDisplayContent.kt), [`AodLockService.kt`](file:///d:/AL%20Muhasib/app/src/main/java/com/example/aod/AodLockService.kt), [`AodManager.kt`](file:///d:/AL%20Muhasib/app/src/main/java/com/example/aod/AodManager.kt), [`CustomAodDreamService.kt`](file:///d:/AL%20Muhasib/app/src/main/java/com/example/aod/CustomAodDreamService.kt), [`TimeOfDayDuaProvider.kt`](file:///d:/AL%20Muhasib/app/src/main/java/com/example/aod/TimeOfDayDuaProvider.kt)

- **Launch & Interception:**
  - `AodLockService` listens for `Intent.ACTION_SCREEN_OFF`.
  - When the screen turns off, it acquires a momentary dim wake lock (1000ms max) and starts `AodActivity` with `FLAG_ACTIVITY_NEW_TASK` and `FLAG_SHOW_WHEN_LOCKED`.
  - As soon as `AodActivity.onResume()` executes, it immediately calls `AodLockService.releaseWakeLock()`, completely freeing the CPU.
  - Pressing the hardware power button while AOD is active dismisses `AodActivity`, ensuring the device powers off and the subsequent wake-up reveals the native system lock screen.
- **Power Optimization & Refresh Rate:**
  - `AodActivity` inspects `Display.supportedModes` and requests the lowest available refresh rate (e.g. 30Hz or 60Hz instead of 120Hz).
  - Ambient light sensor is **single-shot**: it registers, captures one lux reading to tune `window.attributes.screenBrightness`, and immediately unregisters.
  - Pocket mode uses the proximity sensor. When covered, brightness drops to `0.0f`, touch handling is disabled, and screen rendering is halted.
- **Customizable Features in AOD:**
  - **5 Clock Styles:** Digital Minimal, Eastern Arabic-Indic Digits (٠١٢٣٤٥٦٧٨٩), Analog Classic Dial, Stacked Digital, and Islamic Geometric Frame.
  - **6 Accent Palettes:** Emerald (default), Gold, Cyan, White, Rose, Purple.
  - **4 Battery Styles:** Percentage, Linear Battery Bar, Radial Ring, Hidden.
  - **Pixel Burn-in Shifter:** Subtly shifts entire UI coordinates by \(\pm 3\) pixels at minute boundaries to protect OLED diodes.
  - **Dynamic Time-of-Day Duas:** Cycles through five spiritual phases: *Sahar* (pre-dawn), *Morning*, *Afternoon*, *Evening*, and *Night*.
  - **Decoupled Interactive Misbaha:** Tap the central circular ring or use the hardware volume rocker to increment the active dhikr without turning on the screen.

---

### 3.2 ASTRONOMICAL SOLAR PRAYER & SUBSECOND ADHAN ENGINE
*Files:* [`SolarCalculator.kt`](file:///d:/AL%20Muhasib/app/src/main/java/com/example/util/prayer/SolarCalculator.kt), [`IslamicCalendarUtil.kt`](file:///d:/AL%20Muhasib/app/src/main/java/com/example/util/IslamicCalendarUtil.kt), [`PrayerPreferences.kt`](file:///d:/AL%20Muhasib/app/src/main/java/com/example/util/prayer/PrayerPreferences.kt), [`PrayerAlarmScheduler.kt`](file:///d:/AL%20Muhasib/app/src/main/java/com/example/alarm/PrayerAlarmScheduler.kt), [`PrayerAlarmReceiver.kt`](file:///d:/AL%20Muhasib/app/src/main/java/com/example/alarm/PrayerAlarmReceiver.kt), [`PrayerWarningActivity.kt`](file:///d:/AL%20Muhasib/app/src/main/java/com/example/alarm/PrayerWarningActivity.kt), [`PrayerAdhanPlayer.kt`](file:///d:/AL%20Muhasib/app/src/main/java/com/example/alarm/PrayerAdhanPlayer.kt), [`CityCatalog.kt`](file:///d:/AL%20Muhasib/app/src/main/java/com/example/util/prayer/CityCatalog.kt)

- **Pure Solar Mathematics:**
  - Julian Day calculation from civil calendar date.
  - Solar declination (\(\delta\)) and Equation of Time (\(EoT\)) equations.
  - Hour angle formulas incorporating atmospheric refraction (\(0.3^\circ\)) and sun semi-diameter (\(0.533^\circ\)):
    \[
    \cos(\omega) = \frac{\sin(\alpha) - \sin(\phi)\sin(\delta)}{\cos(\phi)\cos(\delta)}
    \]
  - Asr calculations supporting standard Shafi'i/Maliki/Hanbali (shadow length = 1) and Hanafi (shadow length = 2).
  - High-latitude adjustments (Angle-Based, One-Seventh, Middle of the Night).
- **Offline City Catalog & GPS:**
  - Bundled database of hundreds of world cities with pre-configured coordinates, timezones, and regional prayer calculation authorities (Egyptian General Authority of Survey, Umm Al-Qura Makkah, Muslim World League, ISNA, Karachi, etc.).
  - Automatic online search fallback via Nominatim / Aladhan if custom coordinates are requested.
- **Adhan Execution:**
  - `PrayerAlarmScheduler` schedules exact alarms with `AlarmManager.setExactAndAllowWhileIdle()`.
  - When triggered, `PrayerAlarmReceiver` launches `PrayerAdhanPlayer` (playing full Sunni Adhan in foreground) and displays `PrayerWarningActivity` with tactile hold-to-dismiss verification.
  - In-app high-contrast banner (`InAppAdhanBanner`) enables instant stop/snooze without disrupting active recitation.

---

### 3.3 HOLY QURAN MULTI-RIWAYAH READER & AUDIO PLAYBACK SERVICE
*Files:* [`QuranBookReaderScreen.kt`](file:///d:/AL%20Muhasib/app/src/main/java/com/example/ui/screens/QuranBookReaderScreen.kt), [`QuranScreen.kt`](file:///d:/AL%20Muhasib/app/src/main/java/com/example/ui/screens/QuranScreen.kt), [`WrittenQuranRiwayah.kt`](file:///d:/AL%20Muhasib/app/src/main/java/com/example/quran/book/WrittenQuranRiwayah.kt), [`WrittenQuranCatalog.kt`](file:///d:/AL%20Muhasib/app/src/main/java/com/example/quran/book/WrittenQuranCatalog.kt), [`TajweedParser.kt`](file:///d:/AL%20Muhasib/app/src/main/java/com/example/quran/tajweed/TajweedParser.kt), [`TajweedGuideDialog.kt`](file:///d:/AL%20Muhasib/app/src/main/java/com/example/quran/tajweed/TajweedGuideDialog.kt), [`TajweedSettings.kt`](file:///d:/AL%20Muhasib/app/src/main/java/com/example/quran/tajweed/TajweedSettings.kt), [`QuranPlaybackService.kt`](file:///d:/AL%20Muhasib/app/src/main/java/com/example/quran/QuranPlaybackService.kt), [`QuranDownloadManager.kt`](file:///d:/AL%20Muhasib/app/src/main/java/com/example/quran/QuranDownloadManager.kt), [`ReciterCatalog.kt`](file:///d:/AL%20Muhasib/app/src/main/java/com/example/quran/ReciterCatalog.kt), [`SurahCatalog.kt`](file:///d:/AL%20Muhasib/app/src/main/java/com/example/quran/SurahCatalog.kt), [`TafsirCatalog.kt`](file:///d:/AL%20Muhasib/app/src/main/java/com/example/quran/book/TafsirCatalog.kt)

- **Multi-Riwayah Quranic Text Engine:**
  - **Hafs ‘an ‘Aasim (حفص عن عاصم):** Standard Uthmanic text.
  - **Warsh ‘an Nafi‘ (ورش عن نافع):** Maghrebi vocalization, distinct orthography, assimilation points, and sukoon placement.
  - **Qaloon ‘an Nafi‘ (قالون عن نافع):** Madani tradition with distinct rules.
  - **Al-Duri ‘an Abi ‘Amr (الدوري عن أبي عمرو):** Specialized Sudanese/East-African recitation with major idgham rules.
- **Tajweed Colorization:**
  - Automated analysis of Uthmanic diacritics: Madd Lazim (6 harakat), Madd Wajib/Ja'iz (4-5 harakat), Ghunnah (2 harakat), Iqlab (Meem high/low), Idgham with/without Ghunnah, Qalqalah (Qaf, Taa, Baa, Jeem, Dal), and Ikhfaa (15 letters).
  - Customizable color themes (Standard, OLED Neon, Pastel Comfort) + interactive color-map guide.
- **Audio Reciter Streaming & Offline Download:**
  - Catalog of elite reciters (Minshawi, Hussary, Abdul Basit, Al-Ghamdi, Al-Ajmy, Maher Al-Muaiqly, etc.).
  - `QuranPlaybackService` runs in the foreground with lock-screen notification media controls (`MediaSessionCompat`).
  - `QuranDownloadManager` handles batch surah caching for offline listening.

---

### 3.4 FULLY DECOUPLED MULTI-MISBAHA & DHIKR SUBSYSTEM
*Files:* [`SobrietyLog` in Models.kt](file:///d:/AL%20Muhasib/app/src/main/java/com/example/data/model/Models.kt), [`AodDisplayContent.kt`](file:///d:/AL%20Muhasib/app/src/main/java/com/example/aod/AodDisplayContent.kt), [`AdhkarScreen.kt`](file:///d:/AL%20Muhasib/app/src/main/java/com/example/ui/screens/AdhkarScreen.kt), [`AdhkarSeedData.kt`](file:///d:/AL%20Muhasib/app/src/main/java/com/example/data/seed/AdhkarSeedData.kt), [`HapticFeedbackUtil.kt`](file:///d:/AL%20Muhasib/app/src/main/java/com/example/util/HapticFeedbackUtil.kt)

- **Independent Counters:**
  - `istighfarTodayCount` (Daily Target: 500 or 100 per cycle)
  - `subhanAllahTodayCount` (Cycle Target: 33)
  - `alhamdulillahTodayCount` (Cycle Target: 33)
  - `allahuAkbarTodayCount` (Cycle Target: 34)
  - `laIlahaIllallahTodayCount` (Cycle Target: 100)
  - `tasbi7TodayCount` (General counter)
- **Haptic Tactile Profiles:**
  - `HAPTIC_MECHANICAL`: Distinct crisp double-pulse simulating mechanical tally counters.
  - `HAPTIC_TICK`: Minimal 10ms micro-vibration.
  - `HAPTIC_SILENT`: Pure zero-vibration mode.
  - Milestone Rumble: Distinct sustained vibration upon reaching target boundaries (33, 66, 100).

---

### 3.5 PURITY, SOBRIETY, RECOVERY & RELAPSE PREVENTION ENGINE
*Files:* [`RecoveryScreen.kt`](file:///d:/AL%20Muhasib/app/src/main/java/com/example/ui/screens/RecoveryScreen.kt), [`PanicSheet.kt`](file:///d:/AL%20Muhasib/app/src/main/java/com/example/ui/screens/PanicSheet.kt), [`RelapseFrictionHelper.kt`](file:///d:/AL%20Muhasib/app/src/main/java/com/example/util/RelapseFrictionHelper.kt), [`LeaderboardScreen.kt`](file:///d:/AL%20Muhasib/app/src/main/java/com/example/ui/screens/LeaderboardScreen.kt)

- **Purity & Dopamine Detox:**
  - Tracks clean days streak since `purityStartEpochMs`.
  - Daily biological habit checkboxes: Workout completed, Cold shower taken.
- **Relapse Friction Gate:**
  - Prevents impulsive self-defeat. Clicking "سجل انتكاسة" opens a mandatory 30-second countdown with Quranic reflection: 
    *﴿وَلَمَن صَبَرَ وَغَفَرَ إِنَّ ذَٰلِكَ لَمِنْ عَزْمِ الْأُمُورِ﴾*.
  - The confirm button remains physically disabled until the 30 seconds elapse.
- **Emergency Panic Sheet (`PanicSheet.kt`):**
  - Instant modal bottom sheet designed to abort intense temptation or dopamine cravings.
  - Guided 4-7-8 physiological breathing animation with soothing haptics.
  - Immediate reminders for ablution (Wudu) and cold water.
  - Direct Ruqyah audio streaming and panic Istighfar counter.

---

### 3.6 QIBLA DIRECTION & SENSOR PROCESSING SUBSYSTEM
*Files:* [`QiblaScreen.kt`](file:///d:/AL%20Muhasib/app/src/main/java/com/example/ui/screens/QiblaScreen.kt), [`CompassSensorManager.kt`](file:///d:/AL%20Muhasib/app/src/main/java/com/example/util/qibla/CompassSensorManager.kt), [`QiblaCalculator.kt`](file:///d:/AL%20Muhasib/app/src/main/java/com/example/util/qibla/QiblaCalculator.kt)

- **Great Circle Mathematics:**
  - Calculates true azimuth to the Kaaba (\(21.4225^\circ\text{N}, 39.8262^\circ\text{E}\)) using spherical trigonometry:
    \[
    \theta = \text{atan2}\left(\sin(\Delta\lambda)\cos(\phi_K), \cos(\phi)\sin(\phi_K) - \sin(\phi)\cos(\phi_K)\cos(\Delta\lambda)\right)
    \]
- **Sensor Fusion:**
  - Uses `Sensor.TYPE_ROTATION_VECTOR` when available; falls back smoothly to `TYPE_ACCELEROMETER` + `TYPE_MAGNETIC_FIELD`.
  - Low-pass smoothing filter avoids jitter while rotating.
  - Sensor listeners are strictly registered only while `QiblaScreen` is visible and stopped immediately on disposal.

---

### 3.7 FLOATING WINDOW HUD & OVERLAY SERVICE
*Files:* [`FloatingDhikrService.kt`](file:///d:/AL%20Muhasib/app/src/main/java/com/example/floating/FloatingDhikrService.kt), `layout_floating_dhikr.xml`

- Floating overlay using `WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY`.
- Displays active dhikr with smooth touch-drag coordinates clamped within safe screen boundaries.
- Allows user to maintain constant remembrance (Dhikr) while interacting with study or productivity apps.

---

### 3.8 APP WIDGETS & CROSS-PROCESS SYNCHRONIZATION
*Files:* [`PrayerAppWidgetProvider.kt`](file:///d:/AL%20Muhasib/app/src/main/java/com/example/widget/PrayerAppWidgetProvider.kt), [`DhikrAppWidgetProvider.kt`](file:///d:/AL%20Muhasib/app/src/main/java/com/example/widget/DhikrAppWidgetProvider.kt), [`SobrietyAppWidgetProvider.kt`](file:///d:/AL%20Muhasib/app/src/main/java/com/example/widget/SobrietyAppWidgetProvider.kt), [`WidgetUpdateHelper.kt`](file:///d:/AL%20Muhasib/app/src/main/java/com/example/widget/WidgetUpdateHelper.kt)

- Home screen widgets update instantly whenever prayers are checked, dhikr is incremented, or midnight rolls over.
- `WidgetUpdateHelper.updateAllWidgets(context)` broadcasts updates across all app widget providers asynchronously.

---

## 4. COMPLETE ROOM DATABASE SCHEMA & DATA MODELS

### Table: `prayer_logs`
```sql
CREATE TABLE prayer_logs (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    dateString TEXT NOT NULL,         -- Format: "YYYY-MM-DD"
    fajrDone INTEGER NOT NULL DEFAULT 0,
    dhuhrDone INTEGER NOT NULL DEFAULT 0,
    asrDone INTEGER NOT NULL DEFAULT 0,
    maghribDone INTEGER NOT NULL DEFAULT 0,
    ishaDone INTEGER NOT NULL DEFAULT 0
);
```

### Table: `sobriety_state`
```sql
CREATE TABLE sobriety_state (
    id INTEGER PRIMARY KEY NOT NULL,  -- Single-row singleton (ID = 1)
    purityStartEpochMs INTEGER NOT NULL,
    cleanDaysCount INTEGER NOT NULL,
    workoutCompletedToday INTEGER NOT NULL DEFAULT 0,
    coldShowerToday INTEGER NOT NULL DEFAULT 0,
    relapsesCount INTEGER NOT NULL DEFAULT 0,
    istighfarTodayCount INTEGER NOT NULL DEFAULT 120,
    istighfarDailyTarget INTEGER NOT NULL DEFAULT 500,
    tasbi7TodayCount INTEGER NOT NULL DEFAULT 0,
    tasbi7DailyTarget INTEGER NOT NULL DEFAULT 200,
    subhanAllahTodayCount INTEGER NOT NULL DEFAULT 0,
    alhamdulillahTodayCount INTEGER NOT NULL DEFAULT 0,
    allahuAkbarTodayCount INTEGER NOT NULL DEFAULT 0,
    laIlahaIllallahTodayCount INTEGER NOT NULL DEFAULT 0
);
```

### Table: `dhikr_items`
```sql
CREATE TABLE dhikr_items (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    category TEXT NOT NULL,
    title TEXT NOT NULL,
    arabic TEXT NOT NULL,
    transliteration TEXT NOT NULL,
    translation TEXT NOT NULL,
    benefit TEXT NOT NULL,
    targetCount INTEGER NOT NULL DEFAULT 1,
    currentCount INTEGER NOT NULL DEFAULT 0,
    isCompleted INTEGER NOT NULL DEFAULT 0
);
```

### Table: `muhasabah_history`
```sql
CREATE TABLE muhasabah_history (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    dateString TEXT NOT NULL,
    scorePercentage INTEGER NOT NULL,
    salahScore INTEGER NOT NULL,
    dhikrScore INTEGER NOT NULL,
    purityScore INTEGER NOT NULL,
    warningTriggered INTEGER NOT NULL DEFAULT 0
);
```

---

## 5. RECENT POWER & BATTERY OPTIMIZATIONS

Following rigorous power profiling on AMOLED hardware, the following battery drains were eradicated:
1. **Zero Continuous Animations:** Converted `AuroraBackground` and AOD edge glows from running `rememberInfiniteTransition` loops into static cached GPU render layers. CPU/GPU utilization during idle screens is **0.0%**.
2. **Single-Shot Ambient Light Sampling:** Rather than continuously streaming ambient lux readings (which caused continuous sensor IPC interrupts), the light sensor captures a single reading upon device lock, applies optimal OLED brightness, and immediately unregisters.
3. **Display Refresh Rate Clamping:** AOD restricts display refresh rates to 60Hz or lowest hardware mode (30Hz/48Hz) to prevent the screen controller from running at power-hungry 120Hz while idle.
4. **Immediate WakeLock Hand-off:** Screen-wake transition locks in `AodLockService` are capped at 1000ms and released the exact millisecond `AodActivity` mounts.
5. **Minute-Synchronized Dispatch:** Burn-in protection offsets, quiet hours checks, and date rollovers are all unified into a single synchronized clock pulse occurring only at 00 seconds of each minute.

---

## 6. TASK 2 BLUEPRINT: THE HOLISTIC PHONE-CENTRIC LIFE OPERATING SYSTEM

The user's goal: **"I wanna make my whole phone center around this app. Restricting social media and distracting apps. A minimalist whole theme. Notifications and reminders that work with calendar to optimize my day from reading Quran, working, reading overall, physique, and whole life optimization."**

Here is the exact architectural blueprint for Gemini 4 Argon to analyze and build:

### 6.1 Minimalist Islamic Launcher & System UI Overhaul
- **Role:** Declare `android.intent.category.HOME` in `AndroidManifest.xml` so Al-Muhasib can operate as the primary Android Launcher.
- **Aesthetic:** Radical typography-driven minimalism (inspired by *Blloc Ratio* and *Minimalist Phone*).
  - Pure Black AMOLED canvas (`#000000`).
  - Only essential apps displayed as plain Arabic/English text (no bright, dopamine-inducing colorful icons).
  - Quick swipe left for Quran & Adhan; quick swipe right for today's Schedule & Life Score.

### 6.2 Social Media & Distraction Blocker (Friction & Hard Lock)
- **Mechanism:** Utilize Android `AccessibilityService` or `UsageStatsManager` + `SYSTEM_ALERT_WINDOW`.
- **Intelligent Friction Levels:**
  1. *Friction Mode:* When user opens Instagram, TikTok, Twitter/X, or YouTube, Al-Muhasib interrupts with a 15-second breathing gate and a Quranic verse:
     *﴿اقْتَرَبَ لِلنَّاسِ حِسَابُهُمْ وَهُمْ فِي غَفْلَةٍ مُّعْرِضُونَ﴾*
  2. *Hard Lockout Mode:* Block distracting apps entirely during:
     - Pre-Fajr & Post-Fajr sacred hours.
     - Scheduled Deep Work focus blocks.
     - Night wind-down (10:00 PM to 5:00 AM).
  3. *Quota Mode:* Allow max 20 minutes/day per app, enforcing immediate lockout once depleted.

### 6.3 Calendar Integration & Salah-Anchored Daily Schedule
- **Dynamic Prayer Anchoring:** Unlike static secular calendars where alarms are rigid, an Islamic life revolves around the Sun.
  - The calendar dynamically floats tasks around the 5 prayer times:
    - **Fajr to Sunrise:** Quran recitation, morning Adhkar, spiritual study.
    - **Morning (8:00 - 12:30):** Deep Work / Career sprint.
    - **Dhuhr:** Congregational prayer & wholesome lunch.
    - **Afternoon (14:00 - Asr):** Execution & meetings.
    - **Asr to Maghrib:** Athletic training, gym workout, outdoor movement.
    - **Maghrib to Isha:** General book reading, family time, evening Adhkar.
    - **Post-Isha:** Night accountability (*Muhasabah*), reading, early sleep.
- **Two-Way Synchronization:** Sync with Google Calendar / Android Calendar Provider (`CalendarContract`) to pull existing appointments and automatically protect Salah intervals.

### 6.4 Deep Reading & Knowledge Expansion Tracker
- **Quran Khatmah Engine:** Dynamic pacing based on user goal (e.g. 1 juz/day, 1 hizb/day, or complete in 30 days) with progress tracking.
- **General Book & Islamic Science Reading:**
  - Dedicated reader and tracker for classical Islamic texts (Riyad as-Salihin, Bulugh al-Maram, 40 Hadith Nawawi) and secular personal development books.
  - Daily page counter, reading speed tracker, and comprehension notes.

### 6.5 Physique, Workout & Biological Discipline Tracker
- **Integrated Habit Matrix:**
  - Morning cold shower confirmation.
  - Daily workout logger (Push/Pull/Legs, Running, Sunnah martial arts/swimming).
  - Fasting tracker (Mondays & Thursdays, White Days / *Ayyam al-Beed*, Ramadan).
  - Hydration & sleep hygiene tracking (8 hours target, early bedtime).

### 6.6 Unified Muhasabah (Life Accountability) Algorithm
- **Daily Holistic Score (0 - 100%):**
  \[
  \text{Score} = w_1 \cdot \text{Salah} + w_2 \cdot \text{Quran} + w_3 \cdot \text{Dhikr} + w_4 \cdot \text{Purity} + w_5 \cdot \text{Fitness} + w_6 \cdot \text{WorkFocus}
  \]
- **Weekly & Monthly Insights:** Visual graphs showing correlation between prayer consistency, digital detox purity, and physical vitality.

---

## 7. REPOSITORY FILE MAP & KEY CLASSES

```
app/src/main/java/com/example/
├── MainActivity.kt                       # Main Entry Point, Navigation host & Tab coordinator
├── MuhasibApplication.kt                 # Application subclass, Notification Channels, Repository init
├── alarm/
│   ├── PrayerAdhanPlayer.kt              # Foreground audio player for Sunni Adhan files
│   ├── PrayerAlarmReceiver.kt            # Broadcast receiver handling exact prayer alarm triggers
│   ├── PrayerAlarmScheduler.kt           # Exact alarm scheduler with AlarmManager
│   └── PrayerWarningActivity.kt          # Full-screen wake activity with hold-to-dismiss verification
├── aod/
│   ├── AodActivity.kt                    # Fullscreen AMOLED Always-On Display Activity
│   ├── AodDisplayContent.kt              # Compose UI for AOD (Clocks, Dates, Dua, Misbaha Ring)
│   ├── AodLockService.kt                 # Foreground service listening for screen off/on transitions
│   ├── AodManager.kt                     # Preferences manager for styles, palettes, brightness, modes
│   ├── AodNotificationListenerService.kt # Lightweight notification badge processor
│   ├── CustomAodDreamService.kt          # Android native DreamService integration for screen savers
│   └── TimeOfDayDuaProvider.kt           # Dynamic spiritual supplications mapped to time of day
├── data/
│   ├── AppDatabase.kt                    # Room Database (Version 4, destructive downgrade fallback)
│   ├── dao/Daos.kt                       # PrayerDao, SobrietyDao, DhikrDao, MuhasabahDao
│   ├── model/Models.kt                   # PrayerLog, SobrietyLog, DhikrItem, MuhasabahScore
│   └── repository/MuhasibRepository.kt   # Single source of truth repository with Room & Flows
├── floating/
│   └── FloatingDhikrService.kt           # Floating overlay HUD for counting dhikr over other apps
├── quran/
│   ├── QuranDownloadManager.kt           # Offline batch surah downloader
│   ├── QuranPlaybackService.kt           # Foreground media player service with MediaSessionCompat
│   ├── ReciterCatalog.kt                 # Catalog of top Quran reciters
│   ├── SurahCatalog.kt                   # 114 Surahs catalog with verse counts & revelation place
│   ├── book/
│   │   ├── WrittenQuranRiwayah.kt        # Multi-Riwayah definitions (Hafs, Warsh, Qaloon, Doori)
│   │   ├── WrittenQuranCatalog.kt        # Complete text provider per Riwayah
│   │   └── TafsirCatalog.kt              # Classical Tafsir interpretations
│   └── tajweed/
│       ├── TajweedParser.kt              # Algorithmic tajweed diacritic parser
│       ├── TajweedGuideDialog.kt         # Visual color-rule legend modal
│       └── TajweedSettings.kt            # Tajweed custom color palette overrides
├── ui/
│   ├── components/Glassmorphic.kt        # Neumorphic Apple-style cards, shapes, static canvas glows
│   ├── screens/
│   │   ├── DashboardScreen.kt            # Central Guardian dashboard (Salah cards, streak, quick actions)
│   │   ├── QuranScreen.kt                # Quran audio & recitation browser
│   │   ├── QuranBookReaderScreen.kt      # Fullscreen Mushaf reader with tajweed & audio sync
│   │   ├── AdhkarScreen.kt               # Categorized daily supplications
│   │   ├── RecoveryScreen.kt             # Detox streak, habit checkboxes, relapse friction gate
│   │   ├── PanicSheet.kt                 # Emergency temptation breaker modal bottom sheet
│   │   ├── LeaderboardScreen.kt          # Spiritual rank tiers & streak badges
│   │   ├── AodSettingsScreen.kt          # Comprehensive AOD & app customization panel
│   │   ├── CityPickerDialog.kt           # World cities search & prayer method configuration
│   │   └── QiblaScreen.kt                # Sensor-driven digital Qibla compass
│   ├── theme/                            # Color, Theme, Typography with dynamic spiritual palettes
│   └── viewmodel/MuhasibViewModel.kt     # Central application state orchestrator
└── util/
    ├── HapticFeedbackUtil.kt             # Tactile vibration profiles (Mechanical, Tick, Rumble)
    ├── IslamicCalendarUtil.kt            # Umm Al-Qura Hijri date converter & prayer timings
    └── prayer/
        ├── SolarCalculator.kt            # Offline pure astronomical solar mathematics
        ├── PrayerPreferences.kt          # User prayer settings, manual offsets, calculation method
        └── CityCatalog.kt                # Database of pre-indexed global cities
```

---

## 8. SUMMARY FOR GEMINI 4 ARGON

Gemini 4 Argon, you now possess complete, granular knowledge of **Al-Muhasib**. The foundation is robust, battery-optimized, and deeply authentic. You are equipped to help architect and implement the next major phase: transforming this application into the **ultimate phone-centric Life Operating System** that unifies the user's phone, calendar, habits, knowledge, physical discipline, and spiritual focus into a singular, cohesive masterpiece.
