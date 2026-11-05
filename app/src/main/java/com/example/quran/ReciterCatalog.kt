package com.example.quran

/**
 * Catalog of Holy Quran reciters from mp3quran.net repository.
 * Features high-definition CDN streaming & direct download links.
 * Default reciter: Yasser Al-Dossari (ياسر الدوسري).
 */
object ReciterCatalog {

    val RECITERS: List<Reciter> = listOf(
        Reciter(
            id = 1,
            nameArabic = "ياسر الدوسري",
            nameEnglish = "Yasser Al-Dossari",
            serverUrl = "https://server11.mp3quran.net/yasser/",
            rewayaArabic = "حفص عن عاصم",
            description = "إمام وخطيب المسجد الحرام بمكة المكرمة - تلاوة خاشعة مميزة"
        ),
        Reciter(
            id = 2,
            nameArabic = "مشاري راشد العفاسي",
            nameEnglish = "Mishary Rashid Alafasy",
            serverUrl = "https://server8.mp3quran.net/afs/",
            rewayaArabic = "حفص عن عاصم",
            description = "تلاوة ندية عذبة ومتقنة بأعلى جودة صوتية"
        ),
        Reciter(
            id = 3,
            nameArabic = "عبد الباسط عبد الصمد",
            nameEnglish = "Abdulbasit Abdulsamad",
            serverUrl = "https://server7.mp3quran.net/basit/",
            rewayaArabic = "حفص عن عاصم",
            description = "صوت مكة الخالد - المصحف المرتل برواية حفص"
        ),
        Reciter(
            id = 4,
            nameArabic = "ماهر المعيقلي",
            nameEnglish = "Maher Al-Muaiqly",
            serverUrl = "https://server12.mp3quran.net/maher/",
            rewayaArabic = "حفص عن عاصم",
            description = "إمام المسجد الحرام بمكة المكرمة - قراءة شجية وواضحة"
        ),
        Reciter(
            id = 5,
            nameArabic = "محمد صديق المنشاوي",
            nameEnglish = "Mohamed Siddiq Al-Minshawi",
            serverUrl = "https://server10.mp3quran.net/minsh/",
            rewayaArabic = "حفص عن عاصم",
            description = "صاحب الحنجرة الباكية - المصحف المرتل"
        ),
        Reciter(
            id = 6,
            nameArabic = "سعد الغامدي",
            nameEnglish = "Saad Al-Ghamdi",
            serverUrl = "https://server7.mp3quran.net/s_gmd/",
            rewayaArabic = "حفص عن عاصم",
            description = "تلاوة هادئة ووقار تأسر القلوب"
        ),
        Reciter(
            id = 7,
            nameArabic = "محمود خليل الحصري",
            nameEnglish = "Mahmoud Khalil Al-Husary",
            serverUrl = "https://server13.mp3quran.net/husr/",
            rewayaArabic = "حفص عن عاصم",
            description = "شيخ عموم المقارئ المصرية - المرجع الأول في أحكام التجويد"
        ),
        Reciter(
            id = 8,
            nameArabic = "عبد الرحمن السديس",
            nameEnglish = "Abdul Rahman Al-Sudais",
            serverUrl = "https://server11.mp3quran.net/sds/",
            rewayaArabic = "حفص عن عاصم",
            description = "رئيس الشؤون الدينية وإمام الحرم المكي الشريف"
        ),
        Reciter(
            id = 9,
            nameArabic = "سعود الشريم",
            nameEnglish = "Saud Al-Shuraim",
            serverUrl = "https://server7.mp3quran.net/shur/",
            rewayaArabic = "حفص عن عاصم",
            description = "إمام وخطيب المسجد الحرام سابقاً"
        ),
        Reciter(
            id = 10,
            nameArabic = "أحمد بن علي العجمي",
            nameEnglish = "Ahmed Al-Ajmy",
            serverUrl = "https://server10.mp3quran.net/ajm/",
            rewayaArabic = "حفص عن عاصم",
            description = "تلاوة شجية مؤثرة ذات صدى واسع"
        ),
        Reciter(
            id = 11,
            nameArabic = "ناصر القطامي",
            nameEnglish = "Nasser Al-Qatami",
            serverUrl = "https://server6.mp3quran.net/qtm/",
            rewayaArabic = "حفص عن عاصم",
            description = "إمام جامع الملك عبد الله بالرياض - تلاوة خاشعة محبوبة"
        ),
        Reciter(
            id = 12,
            nameArabic = "فارس عباد",
            nameEnglish = "Fares Abbad",
            serverUrl = "https://server8.mp3quran.net/frs_a/",
            rewayaArabic = "حفص عن عاصم",
            description = "نبرة يمنية أصيلة شجية وعذبة"
        ),

        // ── ورش عن نافع (Warsh 'an Nafi') ─────────────────────────────────────
        Reciter(
            id = 20,
            nameArabic = "عمر القزابري",
            nameEnglish = "Omar Al-Qazabri",
            serverUrl = "https://server9.mp3quran.net/omar_warsh/",
            rewayaArabic = "ورش عن نافع",
            description = "إمام مسجد الحسن الثاني بالدار البيضاء - مصحف مرتل برواية ورش عن نافع"
        ),
        Reciter(
            id = 21,
            nameArabic = "العيون الكوشي",
            nameEnglish = "El Ayoun El Kouchi",
            serverUrl = "https://server11.mp3quran.net/koshi/",
            rewayaArabic = "ورش عن نافع",
            description = "قراءة مغربية شجية خاشعة ومحبوبة برواية ورش عن نافع"
        ),
        Reciter(
            id = 22,
            nameArabic = "محمود خليل الحصري (ورش)",
            nameEnglish = "Mahmoud Khalil Al-Husary (Warsh)",
            serverUrl = "https://server13.mp3quran.net/husr/Rewayat-Warsh-A-n-Nafi/",
            rewayaArabic = "ورش عن نافع",
            description = "قمة الإتقان والضبط لأحكام رواية ورش عن نافع من طريق الأزرق"
        ),
        Reciter(
            id = 23,
            nameArabic = "القارئ ياسين الجزائري",
            nameEnglish = "Yassin Al-Jazaery",
            serverUrl = "https://server11.mp3quran.net/qari/",
            rewayaArabic = "ورش عن نافع",
            description = "مصحف ورش برواية الإمام نافع بصوت ندِيّ وعذب من الجزائر"
        ),
        Reciter(
            id = 24,
            nameArabic = "عبد الباسط عبد الصمد (ورش)",
            nameEnglish = "Abdulbasit Abdulsamad (Warsh)",
            serverUrl = "https://server7.mp3quran.net/basit/Rewayat-Warsh-A-n-Nafi/",
            rewayaArabic = "ورش عن نافع",
            description = "التلاوة النادرة لصوت مكة الخالد برواية ورش عن نافع"
        ),

        // ── قالون عن نافع (Qalun 'an Nafi') ───────────────────────────────────
        Reciter(
            id = 30,
            nameArabic = "علي بن عبد الرحمن الحذيفي (قالون)",
            nameEnglish = "Ali Al-Huthaify (Qalun)",
            serverUrl = "https://server9.mp3quran.net/huthifi_qalon/",
            rewayaArabic = "قالون عن نافع",
            description = "إمام المسجد النبوي الشريف بالمدينة المنورة - برواية قالون عن نافع"
        ),
        Reciter(
            id = 31,
            nameArabic = "الدوكالي محمد العالم",
            nameEnglish = "Al-Dokali Mohamed Al-Alem",
            serverUrl = "https://server7.mp3quran.net/dokali/",
            rewayaArabic = "قالون عن نافع",
            description = "مصحف قالون المرتل الشهير بنبرة شجية وأصيلة"
        ),
        Reciter(
            id = 32,
            nameArabic = "أحمد الطرابلسي (قالون)",
            nameEnglish = "Ahmed Al-Trablsi (Qalun)",
            serverUrl = "https://server10.mp3quran.net/trablsi/",
            rewayaArabic = "قالون عن نافع",
            description = "قراءة هادئة متقنة برواية قالون عن نافع المدني"
        ),

        // ── الدوري عن أبي عمرو (Al-Duri 'an Abi 'Amr) ──────────────────────────
        Reciter(
            id = 40,
            nameArabic = "نورين محمد صديق",
            nameEnglish = "Nourin Mohamed Siddiq",
            serverUrl = "https://server16.mp3quran.net/nourin_siddig/Rewayat-Aldori-A-n-Abi-Amr/",
            rewayaArabic = "الدوري عن أبي عمرو",
            description = "التلاوة السودانية الخاشعة الآسرة التي تخترق القلوب برواية الدوري"
        ),
        Reciter(
            id = 41,
            nameArabic = "محمود خليل الحصري (الدوري)",
            nameEnglish = "Mahmoud Khalil Al-Husary (Al-Duri)",
            serverUrl = "https://server13.mp3quran.net/husr/Rewayat-Aldori-A-n-Abi-Amr/",
            rewayaArabic = "الدوري عن أبي عمرو",
            description = "تحقيق وضبط أحكام رواية الدوري عن أبي عمرو البصري"
        ),
        Reciter(
            id = 42,
            nameArabic = "مفتاح السلطني (الدوري)",
            nameEnglish = "Muftah Al-Sultani (Al-Duri)",
            serverUrl = "https://server14.mp3quran.net/muftah_sultany/Rewayat-Aldori-A-n-Abi-Amr/",
            rewayaArabic = "الدوري عن أبي عمرو",
            description = "تلاوة ندية محكمة برواية الدوري عن أبي عمرو"
        )
    )

    /**
     * Default reciter as requested: Yasser Al-Dossari (ياسر الدوسري).
     */
    val DEFAULT_RECITER: Reciter = RECITERS[0]

    fun getReciterById(id: Int): Reciter {
        return RECITERS.find { it.id == id } ?: DEFAULT_RECITER
    }

    /**
     * Filter reciters by authentic Islamic Riwayah / Qira'ah.
     */
    fun getRecitersByRewaya(rewaya: String): List<Reciter> {
        if (rewaya == "الكل" || rewaya.isBlank()) return RECITERS
        return RECITERS.filter { it.rewayaArabic == rewaya }
    }

    /**
     * Returns list of all available distinct Riwayat in catalog.
     */
    fun getAllRewayat(): List<String> {
        return listOf("الكل", "حفص عن عاصم", "ورش عن نافع", "قالون عن نافع", "الدوري عن أبي عمرو")
    }

    /**
     * Constructs direct CDN download/stream MP3 URL for a given reciter and surah number.
     * e.g. "https://server11.mp3quran.net/yasser/001.mp3"
     */
    fun getSurahAudioUrl(reciter: Reciter, surahNumber: Int): String {
        val paddedNumber = surahNumber.toString().padStart(3, '0')
        val baseUrl = if (reciter.serverUrl.endsWith("/")) reciter.serverUrl else "${reciter.serverUrl}/"
        return "$baseUrl$paddedNumber.mp3"
    }
}
