package com.example.aod

import java.time.LocalTime

data class SpiritualDua(
    val arabic: String,
    val source: String,
    val phase: DaySpiritualPhase
)

enum class DaySpiritualPhase(val arabicTitle: String) {
    SAHAR("الثلث الأخير من الليل • وَبِالْأَسْحَارِ هُمْ يَسْتَغْفِرُونَ"),
    MORNING("أذكار الصباح والإشراق"),
    AFTERNOON("أذكار العمل والسعي في الأرض"),
    EVENING("أذكار الغروب والمساء"),
    NIGHT("أذكار الليل وسكون النفس")
}

object TimeOfDayDuaProvider {

    fun getCurrentPhase(now: LocalTime = LocalTime.now()): DaySpiritualPhase {
        val hour = now.hour
        return when {
            hour in 3..4 -> DaySpiritualPhase.SAHAR
            hour in 5..10 -> DaySpiritualPhase.MORNING
            hour in 11..16 -> DaySpiritualPhase.AFTERNOON
            hour in 17..20 -> DaySpiritualPhase.EVENING
            else -> DaySpiritualPhase.NIGHT
        }
    }

    private val saharDuas = listOf(
        SpiritualDua(
            arabic = "أَسْتَغْفِرُ اللَّهَ الْعَظِيمَ الَّذِي لَا إِلَهَ إِلَّا هُوَ الْحَيَّ الْقَيُّومَ وَأَتُوبُ إِلَيْهِ",
            source = "سُنن الترمذي • كفارة الذنوب وتفريج الكروب",
            phase = DaySpiritualPhase.SAHAR
        ),
        SpiritualDua(
            arabic = "اللَّهُمَّ إِنَّكَ عَفُوٌّ كَرِيمٌ تُحِبُّ الْعَفْوَ فَاعْفُ عَنِّي",
            source = "جامع الترمذي • دعاء السحر والخلوة مع الله",
            phase = DaySpiritualPhase.SAHAR
        ),
        SpiritualDua(
            arabic = "رَبَّنَا إِنَّنَا آمَنَّا فَاغْفِرْ لَنَا ذُنُوبَنَا وَقِنَا عَذَابَ النَّارِ",
            source = "سورة آل عمران (١٦) • دعاء المستغفرين بالأسحار",
            phase = DaySpiritualPhase.SAHAR
        ),
        SpiritualDua(
            arabic = "يَا حَيُّ يَا قَيُّومُ بِرَحْمَتِكَ أَسْتَغِيثُ، أَصْلِحْ لِي شَأْنِي كُلَّهُ وَلَا تَكِلْنِي إِلَى نَفْسِي طَرْفَةَ عَيْنٍ",
            source = "المستدرك على الصحيحين • دعاء الاستغاثة برحمة الله",
            phase = DaySpiritualPhase.SAHAR
        )
    )

    private val morningDuas = listOf(
        SpiritualDua(
            arabic = "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ",
            source = "صحيح مسلم • أذكار الصباح المأثورة",
            phase = DaySpiritualPhase.MORNING
        ),
        SpiritualDua(
            arabic = "اللَّهُمَّ أَنْتَ رَبِّي لَا إِلَهَ إِلَّا أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ",
            source = "صحيح البخاري • سيّد الاستغفار",
            phase = DaySpiritualPhase.MORNING
        ),
        SpiritualDua(
            arabic = "اللَّهُمَّ إِنِّي أَسْأَلُكَ عِلْمًا نَافِعًا، وَرِزْقًا طَيِّبًا، وَعَمَلًا مُتَقَبَّلًا",
            source = "سنن ابن ماجه • دعاء الرزق والبركة",
            phase = DaySpiritualPhase.MORNING
        ),
        SpiritualDua(
            arabic = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ: عَدَدَ خَلْقِهِ، وَرِضَا نَفْسِهِ، وَزِنَةَ عَرْشِهِ، وَمِدَادَ كَلِمَاتِهِ",
            source = "صحيح مسلم • الذكر المضاعف",
            phase = DaySpiritualPhase.MORNING
        ),
        SpiritualDua(
            arabic = "بِسْمِ اللَّهِ الَّذِي لَا يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الْأَرْضِ وَلَا فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ",
            source = "سنن أبي داود • حرز وأمان اليوم",
            phase = DaySpiritualPhase.MORNING
        )
    )

    private val afternoonDuas = listOf(
        SpiritualDua(
            arabic = "رَبِّ اشْرَحْ لِي صَدْرِي • وَيَسِّرْ لِي أَمْرِي",
            source = "سورة طه (٢٥-٢٦) • دعاء التيسير والسكينة",
            phase = DaySpiritualPhase.AFTERNOON
        ),
        SpiritualDua(
            arabic = "حَسْبِيَ اللَّهُ لَا إِلَهَ إِلَّا هُوَ عَلَيْهِ تَوَكَّلْتُ وَهُوَ رَبُّ الْعَرْشِ الْعَظِيمِ",
            source = "سنن أبي داود • كفاية هموم الدنيا والآخرة",
            phase = DaySpiritualPhase.AFTERNOON
        ),
        SpiritualDua(
            arabic = "اللَّهُمَّ لَا سَهْلَ إِلَّا مَا جَعَلْتَهُ سَهْلًا، وَأَنْتَ تَجْعَلُ الْحَزْنَ إِذَا شِئْتَ سَهْلًا",
            source = "صحيح ابن حبان • تيسير الصعاب والأمور",
            phase = DaySpiritualPhase.AFTERNOON
        ),
        SpiritualDua(
            arabic = "لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ الْعَلِيِّ الْعَظِيمِ",
            source = "متفق عليه • كنز من كنوز الجنة والمدد الإلهي",
            phase = DaySpiritualPhase.AFTERNOON
        )
    )

    private val eveningDuas = listOf(
        SpiritualDua(
            arabic = "أَمْسَيْنَا وَأَمْسَى الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ",
            source = "صحيح مسلم • أذكار المساء وحفظ العبد",
            phase = DaySpiritualPhase.EVENING
        ),
        SpiritualDua(
            arabic = "اللَّهُمَّ بِكَ أَمْسَيْنَا، وَبِكَ أَصْبَحْنَا، وَبِكَ نَحْيَا، وَبِكَ نَمُوتُ، وَإِلَيْكَ الْمَصِيرُ",
            source = "جامع الترمذي • أذكار حفظ المساء",
            phase = DaySpiritualPhase.EVENING
        ),
        SpiritualDua(
            arabic = "أَعُوذُ بِكَلِمَاتِ اللَّهِ التَّامَّاتِ مِنْ شَرِّ مَا خَلَقَ",
            source = "صحيح مسلم • حفظ تام من كل مكروه وضر",
            phase = DaySpiritualPhase.EVENING
        ),
        SpiritualDua(
            arabic = "اللَّهُمَّ مَا أَمْسَى بِي مِنْ نِعْمَةٍ أَوْ بِأَحَدٍ مِنْ خَلْقِكَ فَمِنْكَ وَحْدَكَ لَا شَرِيكَ لَكَ، فَلَكَ الْحَمْدُ وَلَكَ الشُّكْرُ",
            source = "سنن أبي داود • أداء شكر ليلة العبد",
            phase = DaySpiritualPhase.EVENING
        )
    )

    private val nightDuas = listOf(
        SpiritualDua(
            arabic = "بِاسْمِكَ رَبِّي وَضَعْتُ جَنْبِي، وَبِكَ أَرْفَعُهُ، فَإِنْ أَمْسَكْتَ نَفْسِي فَارْحَمْهَا، وَإِنْ أَرْسَلْتَهَا فَاحْفَظْهَا",
            source = "متفق عليه • أذكار النوم وحفظ الروح",
            phase = DaySpiritualPhase.NIGHT
        ),
        SpiritualDua(
            arabic = "اللَّهُمَّ قِنِي عَذَابَكَ يَوْمَ تَبْعَثُ عِبَادَكَ",
            source = "سنن أبي داود • دعاء الاستعاذة عند الاضطجاع",
            phase = DaySpiritualPhase.NIGHT
        ),
        SpiritualDua(
            arabic = "سُبْحَانَ الْمَلِكِ الْقُدُّوسِ • رَبِّ الْمَلَائِكَةِ وَالرُّوحِ",
            source = "سنن النسائي • تسبيح السكون وصفاء السريرة",
            phase = DaySpiritualPhase.NIGHT
        ),
        SpiritualDua(
            arabic = "لَا إِلَهَ إِلَّا أَنْتَ سُبْحَانَكَ إِنِّي كُنْتُ مِنَ الظَّالِمِينَ",
            source = "سورة الأنبياء (٨٧) • دعاء ذي النون وتفريج كل كرب",
            phase = DaySpiritualPhase.NIGHT
        )
    )

    fun getDuasForCurrentPhase(now: LocalTime = LocalTime.now()): List<SpiritualDua> {
        return when (getCurrentPhase(now)) {
            DaySpiritualPhase.SAHAR -> saharDuas
            DaySpiritualPhase.MORNING -> morningDuas
            DaySpiritualPhase.AFTERNOON -> afternoonDuas
            DaySpiritualPhase.EVENING -> eveningDuas
            DaySpiritualPhase.NIGHT -> nightDuas
        }
    }
}
