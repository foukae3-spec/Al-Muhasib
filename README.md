# 🕋 Al-Muhasib (المُحَاسِب)
> **The Sovereign Islamic Life Operating System & Ambient AMOLED Companion**

[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0+-purple.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-blue.svg)](https://developer.android.com/jetpack/compose)
[![OLED Friendly](https://img.shields.io/badge/OLED-True%20Black%20%23000000-black.svg)]()
[![License](https://img.shields.io/badge/License-Proprietary-red.svg)]()

---

## 📖 Overview

**Al-Muhasib (المُحَاسِب)** is a comprehensive, privacy-first Android application designed to elevate spiritual discipline, life accountability, and digital well-being. Built entirely from scratch with **Modern Kotlin and Jetpack Compose**, it unites high-precision Islamic sciences with modern mobile engineering.

> 🤖 **Collaborator Deep-Dive:** If you are **Gemini 4 Argon** or an AI collaborator reviewing this project, please consult the complete architectural manual:  
> 👉 **[ARGON_SYSTEM_OVERVIEW.md](ARGON_SYSTEM_OVERVIEW.md)**

---

## 🌟 Key Features

### 1. 🌙 Ultra-Low-Power AMOLED Always-On Display (AOD)
- **Authentic OLED True Black (`#000000`):** Pixels are physically powered down on AMOLED displays.
- **Hardware-Level Refresh Rate Clamping:** Constrained to 60Hz/30Hz to prevent battery drain on 120Hz panels.
- **5 Selectable Clock Styles:** Minimal Digital, Eastern Arabic-Indic Digits (٠١٢٣٤٥٦٧٨٩), Classical Analog Dial, Stacked Digital, and Islamic Geometric.
- **6 Spiritual Color Palettes:** Emerald, Gold, Cyan, White, Rose, Purple.
- **Interactive Multi-Misbaha Ring:** Count Istighfar, SubhanAllah, Alhamdulillah, AllahuAkbar, and La Ilaha Illallah directly from the AOD without waking the device.
- **Dynamic Duas:** Supplications automatically rotate based on the 5 spiritual phases of the day (*Sahar*, *Morning*, *Afternoon*, *Evening*, *Night*).
- **Burn-in Protection:** Sub-pixel shifting routine keeps OLED diodes pristine.
- **Pocket Mode:** Proximity detection blacks out screen completely and stops touch dispatching when in pocket or face-down.

### 2. 🕌 Astronomical Subsecond Prayer & Adhan Engine
- **Pure Solar Mathematics:** 100% offline, zero-network calculations based on Julian Day geometry, solar declination, and equation of time.
- **Subsecond Accuracy:** Accurate to the exact second for any global coordinate.
- **Sunni Adhan Recitations:** High-fidelity recordings from Makkah, Madinah, Al-Aqsa, Egypt, and Sheikh Abdul Basit.
- **Interactive Hold-to-Dismiss:** Tactile pressure verification on prayer warning wake screens prevents accidental dismissal.

### 3. 📖 Multi-Riwayah Quran Reader & Media Center
- **Mutawatir Recitations:** Hafs ‘an ‘Aasim, Warsh ‘an Nafi‘ (Maghrebi script), Qaloon ‘an Nafi‘, and Al-Duri ‘an Abi ‘Amr.
- **Algorithmic Tajweed Colorization:** Real-time parsing of Uthmanic diacritics (Madd Lazim, Ghunnah, Iqlab, Idgham, Qalqalah, Ikhfaa) with customizable themes.
- **Reciter Streaming & Offline Caching:** Background playback service with full MediaSession lock-screen controls.

### 4. 🛡️ Purity, Recovery & Dopamine Detox Engine
- **Clean Days Streak Tracker:** Visual accountability score for digital detox and purity.
- **30-Second Relapse Reflection Gate:** Psychological friction countdown featuring Quranic contemplation before confirming any relapse.
- **Emergency Craving Breaker (`PanicSheet`):** Guided 4-7-8 breathing exercises, direct Ruqyah streaming, Wudu reminders, and panic Istighfar counter.

### 5. 🧭 Qibla Compass & Floating HUD
- **Real-Time Compass:** Low-latency sensor fusion (Rotation Vector / Accelerometer + Magnetometer).
- **Floating Dhikr HUD:** System overlay window (`TYPE_APPLICATION_OVERLAY`) to maintain dhikr while studying or reading.

---

## 🛠️ Tech Stack & Architecture

- **Language:** Kotlin 2.0+
- **Architecture:** Clean Architecture + MVI/MVVM pattern with reactive Kotlin `Flow` & `StateFlow`
- **UI:** Jetpack Compose + Material 3 + Custom Neumorphic Glassmorphism
- **Database:** AndroidX Room ORM with KSP
- **Sensors:** Android SensorManager (Proximity, Light, Accelerometer, Magnetometer)
- **Audio:** AndroidX Media & MediaSessionCompat

---

## 📱 Build & Installation

```bash
# Clone the repository
git clone https://github.com/foukae3-spec/Al-Muhasib.git

# Navigate to project directory
cd Al-Muhasib

# Run unit tests
./gradlew testDebugUnitTest

# Assemble debug APK
./gradlew assembleDebug
```

---

## 📜 Documentation

For the complete technical specifications, database schema, and future Life OS roadmap, refer to:
- **[ARGON_SYSTEM_OVERVIEW.md](ARGON_SYSTEM_OVERVIEW.md)**
