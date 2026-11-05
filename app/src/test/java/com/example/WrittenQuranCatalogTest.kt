package com.example

import com.example.quran.book.WrittenQuranCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class WrittenQuranCatalogTest {

    @Test
    fun verifyMadaniMushafSurahPageIndices() {
        val surahs = WrittenQuranCatalog.SURAH_PAGE_INDICES
        assertEquals("Must contain 114 Surahs", 114, surahs.size)

        // Surah 1: Al-Fatihah
        val fatihah = surahs.first { it.number == 1 }
        assertEquals("الفاتحة", fatihah.nameArabic)
        assertEquals(1, fatihah.pageNumber)
        assertEquals(7, fatihah.ayahCount)
        assertTrue(fatihah.isMeccan)

        // Surah 2: Al-Baqarah starts on page 2
        val baqarah = surahs.first { it.number == 2 }
        assertEquals("البقرة", baqarah.nameArabic)
        assertEquals(2, baqarah.pageNumber)
        assertEquals(286, baqarah.ayahCount)
        assertFalse(baqarah.isMeccan)

        // Surah 18: Al-Kahf starts on page 293
        val kahf = surahs.first { it.number == 18 }
        assertEquals("الكهف", kahf.nameArabic)
        assertEquals(293, kahf.pageNumber)

        // Surah 36: Yasin starts on page 440
        val yasin = surahs.first { it.number == 36 }
        assertEquals("يس", yasin.nameArabic)
        assertEquals(440, yasin.pageNumber)

        // Surah 67: Al-Mulk starts on page 562
        val mulk = surahs.first { it.number == 67 }
        assertEquals("الملك", mulk.nameArabic)
        assertEquals(562, mulk.pageNumber)

        // Surah 114: An-Nas starts on page 604
        val nas = surahs.first { it.number == 114 }
        assertEquals("الناس", nas.nameArabic)
        assertEquals(604, nas.pageNumber)

        // Check strictly monotonic start pages
        for (i in 0 until surahs.size - 1) {
            assertTrue(
                "Surah ${surahs[i+1].nameArabic} start page (${surahs[i+1].pageNumber}) must be >= ${surahs[i].nameArabic} start page (${surahs[i].pageNumber})",
                surahs[i+1].pageNumber >= surahs[i].pageNumber
            )
        }
    }

    @Test
    fun verify30AjzaCoverage() {
        val ajza = WrittenQuranCatalog.JUZ_INDICES
        assertEquals("Quran has 30 Ajza", 30, ajza.size)

        assertEquals(1, ajza.first().juzNumber)
        assertEquals(1, ajza.first().startPageNumber)
        assertEquals(30, ajza.last().juzNumber)
        assertTrue(ajza.last().startPageNumber > 500)

        // Verify strictly increasing start page numbers across all 30 Ajza
        for (i in 0 until ajza.size - 1) {
            assertTrue(
                "Juz ${i + 2} start page must be > Juz ${i + 1} start page",
                ajza[i + 1].startPageNumber > ajza[i].startPageNumber
            )
        }
    }

    @Test
    fun verifyArabicDiacriticNormalization() {
        val tashkeelText = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ"
        val normalized = WrittenQuranCatalog.normalizeArabic(tashkeelText)
        assertEquals("بسم الله الرحمن الرحيم", normalized)

        val alefVariations = "أإآءإبراهيم"
        val normalizedAlef = WrittenQuranCatalog.normalizeArabic(alefVariations)
        assertEquals("اااءابراهيم", normalizedAlef)
    }

    @Test
    fun verifyBuiltInSeedPages() {
        // Page 1: Al-Fatihah
        val page1 = WrittenQuranCatalog.getPage(1)
        assertNotNull(page1)
        assertEquals(1, page1.pageNumber)
        assertEquals("الفاتحة", page1.surahNameArabic)
        assertTrue(page1.ayahs.first().textUthmani.contains("بِسْمِ اللَّهِ"))
        assertTrue(WrittenQuranCatalog.normalizeArabic(page1.ayahs.first().textUthmani).contains("بسم الله الرحمن الرحيم"))

        // Page 293: Al-Kahf
        val page293 = WrittenQuranCatalog.getPage(293)
        assertNotNull(page293)
        assertEquals("الكهف", page293.surahNameArabic)
        assertTrue(page293.ayahs.first().textUthmani.contains("الْحَمْدُ لِلَّهِ الَّذِي أَنزَلَ عَلَىٰ عَبْدِهِ الْكِتَابَ"))

        // Page 562: Al-Mulk
        val page562 = WrittenQuranCatalog.getPage(562)
        assertNotNull(page562)
        assertEquals("الملك", page562.surahNameArabic)
        assertTrue(page562.ayahs.first().textUthmani.contains("تَبَارَكَ الَّذِي بِيَدِهِ الْمُلْكُ"))
    }

    @Test
    fun verifySearchInSeedPages() {
        val results = WrittenQuranCatalog.searchVerses("الحمد لله")
        assertTrue("Search for 'الحمد لله' must yield results in seed pages", results.isNotEmpty())
        assertTrue(results.any { it.surahNumber == 1 && it.ayahNumber == 2 })
    }

    @Test
    fun verifyTotalPages() {
        assertEquals(604, WrittenQuranCatalog.getTotalPages())
    }

    @Test
    fun verifyRiwayahDefinitionsAndFromId() {
        val riwayahs = com.example.quran.book.WrittenQuranRiwayah.entries
        assertEquals("Must contain 4 authentic Riwayahs / Qira'at", 4, riwayahs.size)

        // Hafs
        val hafs = com.example.quran.book.WrittenQuranRiwayah.HAFS
        assertEquals("hafs", hafs.id)
        assertEquals("حفص", hafs.shortName)
        assertTrue(hafs.labelArabic.contains("حفص"))
        assertTrue(hafs.qariName.contains("عاصم"))
        assertTrue(hafs.rawiName.contains("حفص"))
        assertTrue(hafs.description.isNotBlank())
        assertTrue(hafs.geographicalSpread.isNotBlank())

        // Warsh
        val warsh = com.example.quran.book.WrittenQuranRiwayah.WARSH
        assertEquals("warsh", warsh.id)
        assertEquals("ورش", warsh.shortName)
        assertTrue(warsh.labelArabic.contains("ورش"))
        assertTrue(warsh.qariName.contains("نافع"))
        assertTrue(warsh.rawiName.contains("ورش"))
        assertTrue(warsh.geographicalSpread.contains("المغرب"))

        // Qaloon
        val qaloon = com.example.quran.book.WrittenQuranRiwayah.QALOON
        assertEquals("qaloon", qaloon.id)
        assertEquals("قالون", qaloon.shortName)
        assertTrue(qaloon.labelArabic.contains("قالون"))
        assertTrue(qaloon.qariName.contains("نافع"))
        assertTrue(qaloon.rawiName.contains("قالون"))
        assertTrue(qaloon.geographicalSpread.contains("ليبيا"))

        // Doori
        val doori = com.example.quran.book.WrittenQuranRiwayah.DOORI
        assertEquals("doori", doori.id)
        assertEquals("الدوري", doori.shortName)
        assertTrue(doori.labelArabic.contains("الدوري"))
        assertTrue(doori.qariName.contains("أبو عمرو"))
        assertTrue(doori.rawiName.contains("الدوري"))
        assertTrue(doori.geographicalSpread.contains("السودان"))

        // Companion fromId checks
        assertEquals(warsh, com.example.quran.book.WrittenQuranRiwayah.fromId("warsh"))
        assertEquals(warsh, com.example.quran.book.WrittenQuranRiwayah.fromId("WARSH"))
        assertEquals(qaloon, com.example.quran.book.WrittenQuranRiwayah.fromId("qaloon"))
        assertEquals(doori, com.example.quran.book.WrittenQuranRiwayah.fromId("doori"))
        assertEquals(hafs, com.example.quran.book.WrittenQuranRiwayah.fromId("hafs"))
        assertEquals(com.example.quran.book.WrittenQuranRiwayah.DEFAULT, com.example.quran.book.WrittenQuranRiwayah.fromId(null))
        assertEquals(com.example.quran.book.WrittenQuranRiwayah.DEFAULT, com.example.quran.book.WrittenQuranRiwayah.fromId("unknown_riwayah"))
    }

    @Test
    fun verifyAuthenticPageImageUrlsAcrossRiwayahs() {
        // Warsh endpoints
        val warshPage1 = WrittenQuranCatalog.getAuthenticPageImageUrl(1, com.example.quran.book.WrittenQuranRiwayah.WARSH)
        assertEquals("https://files.quran.app/warsh/original/width_1260/page001.png", warshPage1)

        val warshPage604 = WrittenQuranCatalog.getAuthenticPageImageUrl(604, com.example.quran.book.WrittenQuranRiwayah.WARSH)
        assertEquals("https://files.quran.app/warsh/original/width_1260/page604.png", warshPage604)

        // Hafs endpoints
        val hafsPage1 = WrittenQuranCatalog.getAuthenticPageImageUrl(1, com.example.quran.book.WrittenQuranRiwayah.HAFS)
        assertEquals("https://files.quran.app/hafs/madani/width_1260/page001.png", hafsPage1)

        val hafsPage293 = WrittenQuranCatalog.getAuthenticPageImageUrl(293, com.example.quran.book.WrittenQuranRiwayah.HAFS)
        assertEquals("https://files.quran.app/hafs/madani/width_1260/page293.png", hafsPage293)

        // Clamping edge cases
        val clampedUnder = WrittenQuranCatalog.getAuthenticPageImageUrl(0, com.example.quran.book.WrittenQuranRiwayah.WARSH)
        assertEquals("https://files.quran.app/warsh/original/width_1260/page001.png", clampedUnder)

        val clampedOver = WrittenQuranCatalog.getAuthenticPageImageUrl(700, com.example.quran.book.WrittenQuranRiwayah.WARSH)
        assertEquals("https://files.quran.app/warsh/original/width_1260/page604.png", clampedOver)
    }

    @Test
    fun verifyTafsirCatalogEditionsAndSeedAyahs() = kotlinx.coroutines.runBlocking {
        val editions = com.example.quran.book.TafsirEdition.entries
        assertEquals("Must support 3 authentic Tafsir editions", 3, editions.size)

        val muyassar = com.example.quran.book.TafsirEdition.MUYASSAR
        assertEquals("ar.muyassar", muyassar.id)
        assertEquals("التفسير الميسر", muyassar.nameArabic)

        val jalalayn = com.example.quran.book.TafsirEdition.JALALAYN
        assertEquals("ar.jalalayn", jalalayn.id)
        assertEquals("تفسير الجلالين", jalalayn.nameArabic)

        val saadi = com.example.quran.book.TafsirEdition.SAADI
        assertEquals("ar.saadi", saadi.id)
        assertEquals("تيسير الكريم الرحمن (السعدي)", saadi.nameArabic)

        assertEquals(muyassar, com.example.quran.book.TafsirEdition.fromId("ar.muyassar"))
        assertEquals(jalalayn, com.example.quran.book.TafsirEdition.fromId("ar.jalalayn"))
        assertEquals(saadi, com.example.quran.book.TafsirEdition.fromId("ar.saadi"))
        assertEquals(com.example.quran.book.TafsirEdition.DEFAULT, com.example.quran.book.TafsirEdition.fromId("invalid"))

        // Surah 1:1 Al-Fatihah across all 3 editions
        val fatihahMuyassar = com.example.quran.book.TafsirCatalog.getTafsir(1, 1, muyassar)
        assertTrue(fatihahMuyassar.tafsirText.contains("أبدأ قراءتي مستعيناً باسم الله"))
        assertTrue(fatihahMuyassar.isOfflineAvailable)

        val fatihahJalalayn = com.example.quran.book.TafsirCatalog.getTafsir(1, 1, jalalayn)
        assertTrue(fatihahJalalayn.tafsirText.contains("أبتدئ مستعيناً باسم الله"))
        assertTrue(fatihahJalalayn.isOfflineAvailable)

        val fatihahSaadi = com.example.quran.book.TafsirCatalog.getTafsir(1, 1, saadi)
        assertTrue(fatihahSaadi.tafsirText.contains("استعانة بالله وتبركاً باسمه"))
        assertTrue(fatihahSaadi.isOfflineAvailable)

        // Ayat Al-Kursi (2:255)
        val kursi = com.example.quran.book.TafsirCatalog.getTafsir(2, 255, muyassar)
        assertTrue(kursi.tafsirText.contains("الله الذي لا إله بحق إلا هو"))
        assertTrue(kursi.isOfflineAvailable)

        // Surah 18:1 (Al-Kahf)
        val kahf = com.example.quran.book.TafsirCatalog.getTafsir(18, 1, muyassar)
        assertTrue(kahf.tafsirText.contains("الحمد والشكر لله وحده الذي أنزل على عبده"))

        // Surah 114:1 (An-Nas)
        val nas = com.example.quran.book.TafsirCatalog.getTafsir(114, 1, muyassar)
        assertTrue(nas.tafsirText.contains("قل: أعتصم وأتحصن برب الناس"))

        // Unseeded fallback (robust both online and offline)
        val unseeded = com.example.quran.book.TafsirCatalog.getTafsir(55, 1, muyassar)
        assertNotNull(unseeded)
        assertTrue(unseeded.tafsirText.contains("الرحمن"))
    }

    @Test
    fun verifyReaderTypographyAndColorPersistence() {
        val context = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>()

        // Line Spacing
        assertEquals(1.9f, WrittenQuranCatalog.getSavedLineSpacing(context), 0.01f)
        WrittenQuranCatalog.saveLineSpacing(context, 2.3f)
        assertEquals(2.3f, WrittenQuranCatalog.getSavedLineSpacing(context), 0.01f)
        WrittenQuranCatalog.saveLineSpacing(context, 1.6f)
        assertEquals(1.6f, WrittenQuranCatalog.getSavedLineSpacing(context), 0.01f)

        // Tashkeel Contrast
        assertFalse(WrittenQuranCatalog.getSavedTashkeelContrast(context))
        WrittenQuranCatalog.saveTashkeelContrast(context, true)
        assertTrue(WrittenQuranCatalog.getSavedTashkeelContrast(context))
        WrittenQuranCatalog.saveTashkeelContrast(context, false)
        assertFalse(WrittenQuranCatalog.getSavedTashkeelContrast(context))

        // Font Size
        assertEquals(24f, WrittenQuranCatalog.getSavedFontSize(context), 0.01f)
        WrittenQuranCatalog.saveFontSize(context, 32f)
        assertEquals(32f, WrittenQuranCatalog.getSavedFontSize(context), 0.01f)

        // Riwayah
        assertEquals(com.example.quran.book.WrittenQuranRiwayah.HAFS, WrittenQuranCatalog.getSavedRiwayah(context))
        WrittenQuranCatalog.saveRiwayah(context, com.example.quran.book.WrittenQuranRiwayah.WARSH)
        assertEquals(com.example.quran.book.WrittenQuranRiwayah.WARSH, WrittenQuranCatalog.getSavedRiwayah(context))
        WrittenQuranCatalog.saveRiwayah(context, com.example.quran.book.WrittenQuranRiwayah.QALOON)
        assertEquals(com.example.quran.book.WrittenQuranRiwayah.QALOON, WrittenQuranCatalog.getSavedRiwayah(context))
        WrittenQuranCatalog.saveRiwayah(context, com.example.quran.book.WrittenQuranRiwayah.DOORI)
        assertEquals(com.example.quran.book.WrittenQuranRiwayah.DOORI, WrittenQuranCatalog.getSavedRiwayah(context))

        // Tafsir Preferred Edition
        assertEquals(com.example.quran.book.TafsirEdition.MUYASSAR, com.example.quran.book.TafsirCatalog.getPreferredEdition(context))
        com.example.quran.book.TafsirCatalog.savePreferredEdition(context, com.example.quran.book.TafsirEdition.SAADI)
        assertEquals(com.example.quran.book.TafsirEdition.SAADI, com.example.quran.book.TafsirCatalog.getPreferredEdition(context))
    }
}
