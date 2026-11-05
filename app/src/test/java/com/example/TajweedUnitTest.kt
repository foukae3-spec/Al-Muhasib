package com.example

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import androidx.test.core.app.ApplicationProvider
import com.example.quran.tajweed.TajweedParser
import com.example.quran.tajweed.TajweedRule
import com.example.quran.tajweed.TajweedSettings
import com.example.quran.tajweed.TajweedTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TajweedUnitTest {

    @Test
    fun verifyTajweedRulesEnumAndProperties() {
        val rules = TajweedRule.entries
        assertEquals("Must support 8 canonical Tajweed rules", 8, rules.size)

        // Madd Lazim: 6 harakat
        val lazim = TajweedRule.MADD_LAZIM
        assertEquals("madd_lazim", lazim.id)
        assertEquals("المد اللازم", lazim.arabicName)
        assertTrue(lazim.harakatDuration.contains("٦"))
        assertEquals(Color(0xFFDC2626), lazim.defaultColor)

        // Madd Wajib / Ja'iz: 4-5 harakat
        val wajib = TajweedRule.MADD_WAJIB_JAIZ
        assertEquals("المد المتصل والمنفصل", wajib.arabicName)
        assertTrue(wajib.harakatDuration.contains("٤"))

        // Qalqalah: 5 letters (قطب جد)
        val qalqalah = TajweedRule.QALQALAH
        assertEquals("القلقلة", qalqalah.arabicName)
        assertTrue(qalqalah.description.contains("ق، ط، ب، ج، د"))

        // Ghunnah Mushaddadah
        val ghunnah = TajweedRule.GHUNNAH_MUSHADDAD
        assertEquals("الغنة المشددة", ghunnah.arabicName)
        assertTrue(ghunnah.harakatDuration.contains("حركتان"))

        // Ikhfaa
        val ikhfaa = TajweedRule.IKHFAA
        assertEquals("الإخفاء الحقيقي والشفوي", ikhfaa.arabicName)

        // Idgham
        val idghamGhunnah = TajweedRule.IDGHAM_GHUNNAH
        assertEquals("الإدغام بغنة", idghamGhunnah.arabicName)

        val idghamNoGhunnah = TajweedRule.IDGHAM_NO_GHUNNAH
        assertEquals("الإدغام بغير غنة", idghamNoGhunnah.arabicName)

        // Iqlab
        val iqlab = TajweedRule.IQLAB
        assertEquals("الإقلاب", iqlab.arabicName)
    }

    @Test
    fun verifyTajweedThemes() {
        val themes = TajweedTheme.entries
        assertEquals(3, themes.size)

        val standard = TajweedTheme.STANDARD
        assertTrue(standard.labelArabic.contains("دار المعرفة"))

        val oled = TajweedTheme.OLED_NEON
        assertTrue(oled.labelArabic.contains("OLED"))

        val pastel = TajweedTheme.PASTEL_COMFORT
        assertTrue(pastel.labelArabic.contains("راحة العينين"))
    }

    @Test
    fun verifyTajweedParserAnnotatedStringGeneration() {
        val testAyah = "قُلْ أَعُوذُ بِرَبِّ النَّاسِ"
        val annotated = TajweedParser.parseTajweed(
            text = testAyah,
            theme = TajweedTheme.STANDARD,
            baseColor = Color.Black,
            fontSize = 24.sp,
            fontFamily = FontFamily.Serif
        )

        assertNotNull(annotated)
        assertEquals(testAyah, annotated.text)
        assertTrue("Annotated string should have span styles applied", annotated.spanStyles.isNotEmpty())
    }

    @Test
    fun verifyTajweedParserDetectsMaddAndGhunnah() {
        // Al-Fatihah:7 has Madd Lazim in "الضَّآلِّينَ"
        val verseWithMadd = "غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّآلِّينَ"
        val annotated = TajweedParser.parseTajweed(
            text = verseWithMadd,
            theme = TajweedTheme.STANDARD,
            baseColor = Color.Black,
            fontSize = 24.sp
        )

        val hasMaddColor = annotated.spanStyles.any { span ->
            span.item.color == TajweedRule.MADD_LAZIM.defaultColor
        }
        assertTrue("Madd Lazim in الضالين should receive crimson color", hasMaddColor)

        // Verse with Noon Mushaddadah: "إِنَّ"
        val verseWithGhunnah = "إِنَّ الْإِنسَانَ لَفِي خُسْرٍ"
        val annotatedGhunnah = TajweedParser.parseTajweed(
            text = verseWithGhunnah,
            theme = TajweedTheme.STANDARD,
            baseColor = Color.Black,
            fontSize = 24.sp
        )

        val hasGhunnahColor = annotatedGhunnah.spanStyles.any { span ->
            span.item.color == TajweedRule.GHUNNAH_MUSHADDAD.defaultColor
        }
        assertTrue("Noon Mushaddadah in إن should receive Ghunnah amber/orange color", hasGhunnahColor)
    }

    @Test
    fun verifyTajweedSettingsPersistence() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()

        // Default state
        assertTrue("Tajweed should be enabled by default", TajweedSettings.isTajweedEnabled(context))
        assertEquals(TajweedTheme.STANDARD, TajweedSettings.getTajweedTheme(context))

        // Toggle enabled
        TajweedSettings.setTajweedEnabled(context, false)
        assertFalse(TajweedSettings.isTajweedEnabled(context))
        TajweedSettings.setTajweedEnabled(context, true)
        assertTrue(TajweedSettings.isTajweedEnabled(context))

        // Change Theme
        TajweedSettings.setTajweedTheme(context, TajweedTheme.OLED_NEON)
        assertEquals(TajweedTheme.OLED_NEON, TajweedSettings.getTajweedTheme(context))
        TajweedSettings.setTajweedTheme(context, TajweedTheme.STANDARD)
        assertEquals(TajweedTheme.STANDARD, TajweedSettings.getTajweedTheme(context))

        // Custom Color Overrides
        TajweedSettings.setCustomColor(context, TajweedRule.QALQALAH.id, Color(0xFF00E5FF))
        assertEquals(Color(0xFF00E5FF), TajweedSettings.getCustomColor(context, TajweedRule.QALQALAH.id))
        assertTrue(TajweedSettings.getAllCustomColors(context).containsKey(TajweedRule.QALQALAH.id))

        // Reset Custom Colors
        TajweedSettings.resetCustomColors(context)
        assertNull(TajweedSettings.getCustomColor(context, TajweedRule.QALQALAH.id))
        assertTrue(TajweedSettings.getAllCustomColors(context).isEmpty())
    }
}
