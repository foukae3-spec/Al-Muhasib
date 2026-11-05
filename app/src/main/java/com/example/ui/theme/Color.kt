package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// =========================================================================
// ISLAMIC DARK GREEN NEUMORPHISM PALETTE (Rich Forest & Emerald Depth)
// =========================================================================

// Dark Mode - Deep Forest Neumorphism Base
val DarkBackground = Color(0xFF0A150F)       // Deepest Pitch Forest Green (OLED-friendly)
val DarkSurface = Color(0xFF0E1E16)          // Standard Neumorphic Surface
val DarkCard = Color(0xFF13281E)             // Elevated Neumorphic Card Body
val DarkCardElevated = Color(0xFF162F23)     // Highly Elevated Surface
val DarkBorder = Color(0x2E10B981)           // Subtle Emerald Edge Highlight
val DarkHighlight = Color(0x1F34D399)        // Ambient Top-Left Light Specular
val DarkShadow = Color(0xCC030805)           // Deep Ambient Bottom-Right Shadow
val DarkInset = Color(0xFF07100B)            // Debossed / Inset Sunken Surface

// Mint & Sage Typography
val DarkTextPrimary = Color(0xFFEDF8F2)      // Crisp Radiant Mint White
val DarkTextSecondary = Color(0xFF8FAFA0)    // Muted Sage Green
val DarkTextTertiary = Color(0xFF567264)     // Deep Muted Forest Gray

// Light Mode - Pristine Sage & Alabaster
val LightBackground = Color(0xFFF1F7F4)
val LightSurface = Color(0xFFFFFFFF)
val LightCard = Color(0xFFFFFFFF)
val LightCardElevated = Color(0xFFFFFFFF)
val LightBorder = Color(0x1410B981)
val LightHighlight = Color(0x40FFFFFF)
val LightShadow = Color(0x14000000)
val LightInset = Color(0xFFE8EFEA)

val LightTextPrimary = Color(0xFF0D2117)
val LightTextSecondary = Color(0xFF385547)
val LightTextTertiary = Color(0xFF6B8A7A)

// =========================================================================
// 3 HIGH-CONTRAST DISTINCT SPIRITUAL CONCENTRIC RING COLORS
// =========================================================================
val RingPrayerColor = Color(0xFF10B981)      // 1. Luminous Emerald (Salah / 5 Daily Prayers)
val RingDhikrColor = Color(0xFF06B6D4)        // 2. Celestial Azure/Cyan (Dhikr / Istighfar)
val RingDetoxColor = Color(0xFFF59E0B)        // 3. Warm Amber Gold (Purity / Sobriety Streak)

// Spiritual Color Tokens
val SpiritualEmerald = Color(0xFF10B981)
val SpiritualEmeraldBright = Color(0xFF34D399)
val SpiritualEmeraldSoft = Color(0x2610B981)

val SpiritualCyan = Color(0xFF06B6D4)
val SpiritualCyanBright = Color(0xFF22D3EE)
val SpiritualCyanSoft = Color(0x2606B6D4)

val SpiritualGold = Color(0xFFF59E0B)
val SpiritualGoldBright = Color(0xFFFBBF24)
val SpiritualGoldSoft = Color(0x26F59E0B)

val SpiritualAmber = Color(0xFFF59E0B)
val SpiritualAmberSoft = Color(0x26F59E0B)

val SpiritualPlatinum = Color(0xFFCBD5E1)
val SpiritualPlatinumSoft = Color(0x24CBD5E1)

// Emergency / Alarm Crimson (Strictly reserved for critical alerts & panic)
val AppleCrimson = Color(0xFFEF4444)
val AppleCrimsonSoft = Color(0x28EF4444)

// Backward-compatible aliases for existing components
val AppleGold = SpiritualGold
val AppleGoldBright = SpiritualGoldBright
val AppleGoldSoft = SpiritualGoldSoft

val AppleAmber = SpiritualAmber
val AppleAmberSoft = SpiritualAmberSoft

val ApplePlatinum = SpiritualPlatinum
val ApplePlatinumSoft = SpiritualPlatinumSoft

val AppleEmerald = SpiritualEmerald
val AppleEmeraldSoft = SpiritualEmeraldSoft

val AppleBlue = SpiritualCyan
val AppleBlueSoft = SpiritualCyanSoft

val AppleIndigo = SpiritualCyan
val ApplePurple = SpiritualEmerald

// True Pitch-Black for OLED AOD
val TrueOledBlack = Color(0xFF000000)
val AodDimText = Color(0xFF7E978B)
val AodGlowEmerald = Color(0xFF10B981)
val AodGlowGold = Color(0xFFF59E0B)
val AodGlowCyan = Color(0xFF06B6D4)

// =========================================================================
// 5 SPIRITUALLY INSPIRED THEME PALETTES
// =========================================================================

// 1. الزمردي الليلي (Emerald Night) - Default OLED pitch black + Spiritual Emerald
val EmeraldNightBackground = Color(0xFF000000)
val EmeraldNightPrimary = Color(0xFF10B981)
val EmeraldNightAccent = Color(0xFF34D399)
val EmeraldNightSurface = Color(0xFF08140E)
val EmeraldNightCard = Color(0xFF0E2218)
val EmeraldNightCardElevated = Color(0xFF142E21)
val EmeraldNightBorder = Color(0x3310B981)
val EmeraldNightHighlight = Color(0x2434D399)

// 2. الكحلي الملكي (Royal Midnight Navy) - Deep navy black + Celestial Sapphire
val RoyalNavyBackground = Color(0xFF070B14)
val RoyalNavyPrimary = Color(0xFF38BDF8)
val RoyalNavyAccent = Color(0xFF60A5FA)
val RoyalNavySurface = Color(0xFF0C1322)
val RoyalNavyCard = Color(0xFF121B30)
val RoyalNavyCardElevated = Color(0xFF18243F)
val RoyalNavyBorder = Color(0x3338BDF8)
val RoyalNavyHighlight = Color(0x2038BDF8)

// 3. الورق العتيق (Warm Sepia Paper) - Calming eye-care sepia parchment + Amber gold
val WarmSepiaBackground = Color(0xFF1A1612)
val WarmSepiaPrimary = Color(0xFFF59E0B)
val WarmSepiaAccent = Color(0xFFFBBF24)
val WarmSepiaSurface = Color(0xFF231E18)
val WarmSepiaCard = Color(0xFF2C261F)
val WarmSepiaCardElevated = Color(0xFF372F27)
val WarmSepiaBorder = Color(0x33F59E0B)
val WarmSepiaHighlight = Color(0x20FBBF24)

// 4. الذهب الكعبوي (Kaaba Black & Gold) - Deep obsidian + Rich Makkah Gold
val KaabaGoldBackground = Color(0xFF0A0A0A)
val KaabaGoldPrimary = Color(0xFFEAB308)
val KaabaGoldAccent = Color(0xFFFDE047)
val KaabaGoldSurface = Color(0xFF141414)
val KaabaGoldCard = Color(0xFF1C1C1C)
val KaabaGoldCardElevated = Color(0xFF242424)
val KaabaGoldBorder = Color(0x3BEAB308)
val KaabaGoldHighlight = Color(0x24FDE047)

// 5. الأحادي المعتم (Monochrome Titanium) - OLED black + Minimalist Silver/White
val MonochromeBackground = Color(0xFF000000)
val MonochromePrimary = Color(0xFFF3F4F6)
val MonochromeAccent = Color(0xFFE5E7EB)
val MonochromeSurface = Color(0xFF121212)
val MonochromeCard = Color(0xFF1A1A1A)
val MonochromeCardElevated = Color(0xFF242424)
val MonochromeBorder = Color(0x30F3F4F6)
val MonochromeHighlight = Color(0x20FFFFFF)

