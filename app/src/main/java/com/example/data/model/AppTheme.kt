package com.example.data.model

import androidx.compose.ui.graphics.Color

/**
 * Spiritually inspired multi-theme customization palettes for Al-Muhasib.
 * Each palette is precision-engineered for OLED battery preservation and optimal spiritual focus.
 */
enum class AppTheme(
    val id: String,
    val arabicTitle: String,
    val englishTitle: String,
    val subtitle: String,
    val backgroundHex: Long,
    val primaryHex: Long,
    val surfaceHex: Long,
    val cardHex: Long,
    val cardElevatedHex: Long,
    val borderHex: Long,
    val accentHex: Long,
    val textPrimaryHex: Long,
    val textSecondaryHex: Long,
    val textTertiaryHex: Long
) {
    /**
     * الزمردي الليلي (Emerald Night) - Default
     * Pure OLED pitch black (#000000) with Spiritual Emerald (#10B981).
     */
    EMERALD_NIGHT(
        id = "emerald_night",
        arabicTitle = "الزمردي الليلي",
        englishTitle = "Emerald Night",
        subtitle = "أسود نقي OLED مع زمردي روحاني",
        backgroundHex = 0xFF000000,
        primaryHex = 0xFF10B981,
        surfaceHex = 0xFF08140E,
        cardHex = 0xFF0E2218,
        cardElevatedHex = 0xFF142E21,
        borderHex = 0x3310B981,
        accentHex = 0xFF34D399,
        textPrimaryHex = 0xFFEDF8F2,
        textSecondaryHex = 0xFF8FAFA0,
        textTertiaryHex = 0xFF567264
    ),

    /**
     * الكحلي الملكي (Royal Midnight Navy)
     * Deep navy black (#070B14) with Celestial Sapphire (#38BDF8).
     */
    ROYAL_NAVY(
        id = "royal_navy",
        arabicTitle = "الكحلي الملكي",
        englishTitle = "Royal Midnight Navy",
        subtitle = "كحلي ليلي عميق مع ياقوتي سماوي",
        backgroundHex = 0xFF070B14,
        primaryHex = 0xFF38BDF8,
        surfaceHex = 0xFF0C1322,
        cardHex = 0xFF121B30,
        cardElevatedHex = 0xFF18243F,
        borderHex = 0x3338BDF8,
        accentHex = 0xFF60A5FA,
        textPrimaryHex = 0xFFF0F6FC,
        textSecondaryHex = 0xFF94A3B8,
        textTertiaryHex = 0xFF64748B
    ),

    /**
     * الورق العتيق (Warm Sepia Paper)
     * Calming eye-care sepia parchment (#1A1612) with Amber gold (#F59E0B).
     */
    WARM_SEPIA(
        id = "warm_sepia",
        arabicTitle = "الورق العتيق",
        englishTitle = "Warm Sepia Paper",
        subtitle = "ورق عتيق مريح للعين مع ذهب كهرماني",
        backgroundHex = 0xFF1A1612,
        primaryHex = 0xFFF59E0B,
        surfaceHex = 0xFF231E18,
        cardHex = 0xFF2C261F,
        cardElevatedHex = 0xFF372F27,
        borderHex = 0x33F59E0B,
        accentHex = 0xFFFBBF24,
        textPrimaryHex = 0xFFFAF5EF,
        textSecondaryHex = 0xFFC4B5A5,
        textTertiaryHex = 0xFF8C7E72
    ),

    /**
     * الذهب الكعبوي (Kaaba Black & Gold)
     * Deep obsidian (#0A0A0A) with rich Makkah Gold (#EAB308).
     */
    KAABA_GOLD(
        id = "kaaba_gold",
        arabicTitle = "الذهب الكعبوي",
        englishTitle = "Kaaba Black & Gold",
        subtitle = "أسود كسوة الكعبة مع ذهب مكي خالص",
        backgroundHex = 0xFF0A0A0A,
        primaryHex = 0xFFEAB308,
        surfaceHex = 0xFF141414,
        cardHex = 0xFF1C1C1C,
        cardElevatedHex = 0xFF242424,
        borderHex = 0x3BEAB308,
        accentHex = 0xFFFDE047,
        textPrimaryHex = 0xFFFCFBF7,
        textSecondaryHex = 0xFFB8B2A2,
        textTertiaryHex = 0xFF7A7568
    ),

    /**
     * الأحادي المعتم (Monochrome Titanium)
     * OLED black (#000000) with minimalist Silver/White (#F3F4F6).
     */
    MONOCHROME(
        id = "monochrome",
        arabicTitle = "الأحادي المعتم",
        englishTitle = "Monochrome Titanium",
        subtitle = "أسود معتم أنيق مع فضي تيتانيوم هادئ",
        backgroundHex = 0xFF000000,
        primaryHex = 0xFFF3F4F6,
        surfaceHex = 0xFF121212,
        cardHex = 0xFF1A1A1A,
        cardElevatedHex = 0xFF242424,
        borderHex = 0x30F3F4F6,
        accentHex = 0xFFE5E7EB,
        textPrimaryHex = 0xFFFFFFFF,
        textSecondaryHex = 0xFFA1A1AA,
        textTertiaryHex = 0xFF71717A
    ),

    /**
     * سَكِينَةُ اللَّيْل (Mihrab AMOLED Dark)
     * #000000 pitch black canvas, #0D0F0E glass surface, #F4F1EA ivory ink, #10B981 / #D4AF37 spiritual accents.
     */
    MIHRAB_AMOLED_DARK(
        id = "mihrab_dark",
        arabicTitle = "سَكِينَةُ اللَّيْل (المحراب)",
        englishTitle = "Mihrab AMOLED Dark",
        subtitle = "أسود نقي #000000 مع حبر عاجي وزبرجد محرابي لتوفير الطاقة 100%",
        backgroundHex = 0xFF000000,
        primaryHex = 0xFF10B981,
        surfaceHex = 0xFF0D0F0E,
        cardHex = 0xFF0D0F0E,
        cardElevatedHex = 0xFF141916,
        borderHex = 0xFF1F2421,
        accentHex = 0xFFD4AF37,
        textPrimaryHex = 0xFFF4F1EA,
        textSecondaryHex = 0xFF8A8E8B,
        textTertiaryHex = 0xFF5C605D
    ),

    /**
     * وَرَقُ المَصَاحِف (Mihrab Parchment Light)
     * #FAF6EE warm ivory canvas, #F2ECE1 warm sand surface, #141413 deep calligraphy ink, #0B6E4F deep green accent.
     */
    MIHRAB_PARCHMENT_LIGHT(
        id = "mihrab_light",
        arabicTitle = "وَرَقُ المَصَاحِف (المحراب)",
        englishTitle = "Mihrab Parchment Light",
        subtitle = "عاجي دافئ #FAF6EE كلون ورق المصحف الشريف مع حبر خطاطة أسود عميق",
        backgroundHex = 0xFFFAF6EE,
        primaryHex = 0xFF0B6E4F,
        surfaceHex = 0xFFF2ECE1,
        cardHex = 0xFFF2ECE1,
        cardElevatedHex = 0xFFE9E2D5,
        borderHex = 0xFFE2D9C8,
        accentHex = 0xFF9E7B22,
        textPrimaryHex = 0xFF141413,
        textSecondaryHex = 0xFF5C5852,
        textTertiaryHex = 0xFF8C8479
    );

    val backgroundColor: Color get() = Color(backgroundHex)
    val primaryColor: Color get() = Color(primaryHex)
    val surfaceColor: Color get() = Color(surfaceHex)
    val cardColor: Color get() = Color(cardHex)
    val cardElevatedColor: Color get() = Color(cardElevatedHex)
    val borderColor: Color get() = Color(borderHex)
    val accentColor: Color get() = Color(accentHex)
    val textPrimaryColor: Color get() = Color(textPrimaryHex)
    val textSecondaryColor: Color get() = Color(textSecondaryHex)
    val textTertiaryColor: Color get() = Color(textTertiaryHex)

    companion object {
        val DEFAULT = EMERALD_NIGHT

        fun fromId(id: String?): AppTheme {
            if (id == null) return DEFAULT
            return entries.firstOrNull {
                it.id.equals(id, ignoreCase = true) || it.name.equals(id, ignoreCase = true)
            } ?: DEFAULT
        }
    }
}
