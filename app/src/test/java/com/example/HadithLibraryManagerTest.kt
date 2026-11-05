package com.example

import com.example.hadith.HadithLibraryManager
import com.example.hadith.HeritageBook
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HadithLibraryManagerTest {

    @Test
    fun verifyHeritageBooksDefinitions() {
        val books = HeritageBook.entries
        assertEquals("Should define 3 core authentic heritage works", 3, books.size)

        val bukhari = HeritageBook.SAHIH_BUKHARI
        assertEquals("bukhari", bukhari.id)
        assertEquals("صحيح البخاري", bukhari.titleArabic)
        assertTrue(bukhari.authorArabic.contains("البخاري"))
        assertEquals(7563, bukhari.approximateCount)

        val muslim = HeritageBook.SAHIH_MUSLIM
        assertEquals("muslim", muslim.id)
        assertEquals("صحيح مسلم", muslim.titleArabic)
        assertTrue(muslim.authorArabic.contains("مسلم"))
        assertEquals(7500, muslim.approximateCount)

        val seerah = HeritageBook.SEERAH_NABAWYYAH
        assertEquals("seerah", seerah.id)
        assertEquals("السيرة النبوية (الرحيق المختوم)", seerah.titleArabic)
        assertTrue(seerah.authorArabic.contains("المباركفوري"))
    }

    @Test
    fun verifySplitSanadAndMatn() {
        val narration1 = "حَدَّثَنَا الْحُمَيْدِيُّ عَنْ سُفْيَانَ قَالَ سَمِعْتُ رَسُولَ اللَّهِ يَقُولُ إِنَّمَا الأَعْمَالُ بِالنِّيَّاتِ"
        val (sanad1, matn1) = HadithLibraryManager.splitSanadAndMatn(narration1)

        assertEquals("حَدَّثَنَا الْحُمَيْدِيُّ عَنْ سُفْيَانَ", sanad1)
        assertTrue("Matn should start with noble quotation", matn1.startsWith("قَالَ سَمِعْتُ رَسُولَ اللَّهِ"))

        val narration2 = "عَنْ أَبِي هُرَيْرَةَ رَضِيَ اللَّهُ عَنْهُ، عَنِ النَّبِيِّ صَلَّى اللَّهُ عَلَيْهِ وَسَلَّمَ قَالَ: الإِيمَانُ بِضْعٌ وَسَبْعُونَ شُعْبَةً"
        val (sanad2, matn2) = HadithLibraryManager.splitSanadAndMatn(narration2)
        assertEquals("عَنْ أَبِي هُرَيْرَةَ رَضِيَ اللَّهُ عَنْهُ،", sanad2)
        assertTrue(matn2.contains("عَنِ النَّبِيِّ"))

        // Pure text without standard narrator markers
        val plainText = "هذا نص عام في التاريخ والسير دون إسناد صريح"
        val (emptySanad, unchangedMatn) = HadithLibraryManager.splitSanadAndMatn(plainText)
        assertEquals("", emptySanad)
        assertEquals(plainText, unchangedMatn)
    }
}
