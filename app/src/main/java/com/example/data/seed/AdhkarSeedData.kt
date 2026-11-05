package com.example.data.seed

import com.example.data.model.DhikrCategory
import com.example.data.model.DhikrItem

object AdhkarSeedData {
    fun getInitialAdhkar(): List<DhikrItem> = listOf(
        // Morning (Sabah)
        DhikrItem(
            category = DhikrCategory.MORNING.name,
            title = "Sayyid al-Istighfar (Chief of Repentance)",
            arabic = "اللَّهُمَّ أَنْتَ رَبِّي لَا إِلَهَ إِلَّا أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ، أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ، وَأَبُوءُ بِذَنْبِي فَاغْفِرْ لِي فَإِنَّهُ لَا يَغْفِرُ الذُّنُوبَ إِلَّا أَنْتَ",
            transliteration = "Allahumma anta Rabbi la ilaha illa anta, khalaqtani wa ana 'abduka, wa ana 'ala 'ahdika wa wa'dika mastata'tu, a'udhu bika min sharri ma sana'tu, abu'u laka bini'matika 'alayya, wa abu'u bidhanbi faghfir li fa'innahu la yaghfirudh-dhunuba illa anta.",
            translation = "O Allah, You are my Lord, there is no deity worthy of worship except You. You created me and I am Your servant, and I abide by Your covenant and promise as best I can. I seek refuge in You from the evil of what I have done. I acknowledge Your favors upon me, and I acknowledge my sin, so forgive me, for none can forgive sins except You.",
            benefit = "Whoever says this with certainty in the morning and dies before evening will enter Jannah (Sahih al-Bukhari).",
            targetCount = 1
        ),
        DhikrItem(
            category = DhikrCategory.MORNING.name,
            title = "Morning Protection (3x)",
            arabic = "بِسْمِ اللَّهِ الَّذِي لَا يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الْأَرْضِ وَلَا فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ",
            transliteration = "Bismillahil-ladhi la yadurru ma'as-mihi shay'un fil-ardi wa la fis-sama'i wa Huwas-Sami'ul-'Alim.",
            translation = "In the Name of Allah, with Whose Name nothing can cause harm in the earth or in the heavens, and He is the All-Hearing, the All-Knowing.",
            benefit = "Whoever recites it 3 times in the morning and evening, nothing will harm him (Sunan Abi Dawud).",
            targetCount = 3
        ),
        DhikrItem(
            category = DhikrCategory.MORNING.name,
            title = "Subhan Allahi wa bihamdihi (100x)",
            arabic = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ",
            transliteration = "Subhan Allahi wa bihamdihi.",
            translation = "Glory be to Allah and all praise is for Him.",
            benefit = "Whoever recites this 100 times in the morning and evening, his sins will be forgiven even if they are like the foam of the sea (Sahih Muslim).",
            targetCount = 100
        ),

        // Evening (Masaa)
        DhikrItem(
            category = DhikrCategory.EVENING.name,
            title = "Evening Affirmation",
            arabic = "أَمْسَيْنَا وَأَمْسَى الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ",
            transliteration = "Amsayna wa amsal-mulku lillah, wal-hamdu lillah, la ilaha illallahu wahdahu la sharika lah, lahul-mulku wa lahul-hamdu wa Huwa 'ala kulli shay'in Qadir.",
            translation = "We have entered the evening and to Allah belongs the kingdom. All praise is due to Allah. None has the right to be worshipped except Allah alone, without partner. To Him belongs sovereignty and praise, and He has power over all things.",
            benefit = "Invokes divine sovereignty and protection throughout the night (Sahih Muslim).",
            targetCount = 1
        ),
        DhikrItem(
            category = DhikrCategory.EVENING.name,
            title = "Refuge from Creation's Harm (3x)",
            arabic = "أَعُوذُ بِكَلِمَاتِ اللَّهِ التَّامَّاتِ مِنْ شَرِّ مَا خَلَقَ",
            transliteration = "A'udhu bikalimatil-lahit-tammati min sharri ma khalaq.",
            translation = "I seek refuge in the perfect words of Allah from the evil of what He has created.",
            benefit = "Shields against poisonous creatures, evil eyes, and nocturnal harms (Sahih Muslim).",
            targetCount = 3
        ),

        // Waking Up
        DhikrItem(
            category = DhikrCategory.WAKING.name,
            title = "Gratitude for Rebirth & Life",
            arabic = "الْحَمْدُ لِلَّهِ الَّذِي أَحْيَانَا بَعْدَ مَا أَمَاتَنَا وَإِلَيْهِ النُّشُورُ",
            transliteration = "Alhamdu lillahil-ladhi ahyana ba'da ma amatana wa ilayhin-nushur.",
            translation = "All praise is for Allah Who gave us life after having taken it from us, and unto Him is the resurrection.",
            benefit = "Instantly breaks the spiritual slumber knot and aligns consciousness toward Akhirah (Sahih al-Bukhari).",
            targetCount = 1
        ),

        // Sleep
        DhikrItem(
            category = DhikrCategory.SLEEP.name,
            title = "Ayat al-Kursi (The Throne Verse)",
            arabic = "اللَّهُ لَا إِلَهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ لَهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الْأَرْضِ مَنْ ذَا الَّذِي يَشْفَعُ عِنْدَهُ إِلَّا بِإِذْنِهِ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ وَلَا يُحِيطُونَ بِشَيْءٍ مِنْ عِلْمِهِ إِلَّا بِمَا شَاءَ وَسِعَ كُرْسِيُّهُ السَّمَاوَاتِ وَالْأَرْضَ وَلَا يَئُودُهُ حِفْظُهُمَا وَهُوَ الْعَلِيُّ الْعَظِيمُ",
            transliteration = "Allahu la ilaha illa Huwal-Hayyul-Qayyum. La ta'khudhuhu sinatuw-wa la nawm...",
            translation = "Allah! There is no deity except Him, the Ever-Living, the Sustainer of all existence. Neither drowsiness overtakes Him nor sleep...",
            benefit = "An angelic guardian guards you all night, and Shaytan cannot approach until dawn (Sahih al-Bukhari).",
            targetCount = 1
        ),
        DhikrItem(
            category = DhikrCategory.SLEEP.name,
            title = "Tasbih Fatimi before Sleep",
            arabic = "سُبْحَانَ اللَّهِ (٣٣) ، الْحَمْدُ لِلَّهِ (٣٣) ، اللَّهُ أَكْبَرُ (٣٤)",
            transliteration = "SubhanAllah (33x), Alhamdulillah (33x), Allahu Akbar (34x).",
            translation = "Glory be to Allah (33 times), Praise be to Allah (33 times), Allah is the Greatest (34 times).",
            benefit = "Better than a personal servant; bestows inner physical and spiritual vigor (Sahih al-Bukhari).",
            targetCount = 33
        ),

        // Home
        DhikrItem(
            category = DhikrCategory.HOME.name,
            title = "Leaving the Home (Tawakkul Shield)",
            arabic = "بِسْمِ اللَّهِ ، تَوَكَّلْتُ عَلَى اللَّهِ ، وَلَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ",
            transliteration = "Bismillahi tawakkaltu 'alallahi, wa la hawla wa la quwwata illa billah.",
            translation = "In the Name of Allah, I place my trust in Allah; there is no might nor power except with Allah.",
            benefit = "The angels say: 'You are guided, defended, and protected,' and Shaytan steps back (Abu Dawud).",
            targetCount = 1
        ),
        DhikrItem(
            category = DhikrCategory.HOME.name,
            title = "Entering the Home",
            arabic = "بِسْمِ اللَّهِ وَلَجْنَا، وَبِسْمِ اللَّهِ خَرَجْنَا، وَعَلَى رَبِّنَا تَوَكَّلْنَا",
            transliteration = "Bismillahi walajna, wa bismillahi kharajna, wa 'ala Rabbina tawakkalna.",
            translation = "In the Name of Allah we enter, in the Name of Allah we leave, and upon our Lord we rely.",
            benefit = "Shaytan proclaims: 'There is no dwelling for you tonight and no dinner' (Abu Dawud).",
            targetCount = 1
        ),

        // Mosque
        DhikrItem(
            category = DhikrCategory.MOSQUE.name,
            title = "Entering the Mosque",
            arabic = "بِسْمِ اللَّهِ وَالصَّلَاةُ وَالسَّلَامُ عَلَى رَسُولِ اللَّهِ، اللَّهُمَّ افْتَحْ لِي أَبْوَابَ رَحْمَتِكَ",
            transliteration = "Bismillah, was-salatu was-salamu 'ala Rasulillah, Allahummaftah li abwaba rahmatik.",
            translation = "In the Name of Allah, peace and blessings be upon the Messenger of Allah. O Allah, open the gates of Your mercy for me.",
            benefit = "Opens the gateways of divine mercy upon your arrival in Allah's house (Sahih Muslim).",
            targetCount = 1
        ),
        DhikrItem(
            category = DhikrCategory.MOSQUE.name,
            title = "Leaving the Mosque",
            arabic = "اللَّهُمَّ إِنِّي أَسْأَلُكَ مِنْ فَضْلِكَ، اللَّهُمَّ اعْصِمْنِي مِنَ الشَّيْطَانِ الرَّجِيمِ",
            transliteration = "Allahumma inni as'aluka min fadlik, Allahumma'simni minash-shaytanir-rajim.",
            translation = "O Allah, I ask You from Your bounty. O Allah, protect me from the accursed Satan.",
            benefit = "Shields you as you re-enter worldly distractions and trade (Sahih Muslim).",
            targetCount = 1
        ),

        // Restroom
        DhikrItem(
            category = DhikrCategory.RESTROOM.name,
            title = "Entering the Restroom",
            arabic = "اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنَ الْخُبُثِ وَالْخَبَائِثِ",
            transliteration = "Allahumma inni a'udhu bika minal-khubuthi wal-khaba'ith.",
            translation = "O Allah, I seek refuge with You from male and female foul devils.",
            benefit = "A veil is cast between the eyes of the jinn and the private parts of humanity (Sahih al-Bukhari).",
            targetCount = 1
        ),
        DhikrItem(
            category = DhikrCategory.RESTROOM.name,
            title = "Leaving the Restroom",
            arabic = "غُفْرَانَكَ",
            transliteration = "Ghufranak.",
            translation = "I ask Your forgiveness.",
            benefit = "Repentance for temporary pause from mentioning Allah while relieving oneself (Sunan al-Tirmidhi).",
            targetCount = 1
        ),

        // Post-Salah
        DhikrItem(
            category = DhikrCategory.POST_SALAH.name,
            title = "Istighfar & Salam (Post-Salah)",
            arabic = "أَسْتَغْفِرُ اللَّهَ (٣x) ، اللَّهُمَّ أَنْتَ السَّلَامُ وَمِنْكَ السَّلَامُ ، تَبَارَكْتَ يَا ذَا الْجَلَالِ وَالْإِكْرَامِ",
            transliteration = "Astaghfirullah (3x), Allahumma antas-Salamu wa minkas-salam, tabarakta ya dhal-Jalali wal-Ikram.",
            translation = "I seek Allah's forgiveness (3 times). O Allah, You are Peace, and from You comes peace. Blessed are You, O Possessor of majesty and honor.",
            benefit = "Sunnah immediately following the final Tasleem (Sahih Muslim).",
            targetCount = 3
        ),
        DhikrItem(
            category = DhikrCategory.POST_SALAH.name,
            title = "Tasbih, Tahmid, Takbir (33x each)",
            arabic = "سُبْحَانَ اللَّهِ (٣٣) ، الْحَمْدُ لِلَّهِ (٣٣) ، اللَّهُ أَكْبَرُ (٣٣) ، وَتَمَامُ الْمِائَةِ: لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ...",
            transliteration = "SubhanAllah (33x), Alhamdulillah (33x), Allahu Akbar (33x), then seal 100 with La ilaha illallah...",
            translation = "Glory be to Allah (33x), Praise be to Allah (33x), Allah is the Greatest (33x), completed with the Oneness of Allah.",
            benefit = "All minor sins forgiven even if they exceed the foam of the boundless oceans (Sahih Muslim).",
            targetCount = 33
        ),

        // Protection from Sins & Dopamine Triggers
        DhikrItem(
            category = DhikrCategory.PROTECTION.name,
            title = "Shattering Sexual Temptation & Shaytanic Whims",
            arabic = "أَعُوذُ بِاللَّهِ مِنَ الشَّيْطَانِ الرَّجِيمِ ، رَبِّ أَعُوذُ بِكَ مِنْ هَمَزَاتِ الشَّيَاطِينِ وَأَعُوذُ بِكَ رَبِّ أَنْ يَحْضُرُونِ",
            transliteration = "A'udhu billahi minash-shaytanir-rajim. Rabbi a'udhu bika min hamazatish-shayateen, wa a'udhu bika Rabbi an yahdurun.",
            translation = "I seek refuge in Allah from Satan the outcast. My Lord! I seek refuge with You from the whisperings of the devils, and I seek refuge with You lest they be present with me.",
            benefit = "Divine circuit-breaker against dopamine spikes, pornography urges, and illicit thoughts (Surah Al-Mu'minun).",
            targetCount = 3
        ),
        DhikrItem(
            category = DhikrCategory.PROTECTION.name,
            title = "Istighfar al-Kabir (Heart Purifier)",
            arabic = "أَسْتَغْفِرُ اللَّهَ الَّذِي لَا إِلَهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ وَأَتُوبُ إِلَيْهِ",
            transliteration = "Astaghfirullahal-'Azim alladhi la ilaha illa Huwal-Hayyul-Qayyum wa atubu ilayh.",
            translation = "I seek the forgiveness of Allah the Magnificent, whom there is no deity except Him, the Ever-Living, the Sustainer, and I turn to Him in repentance.",
            benefit = "Pardons grave offenses and burns toxic guilt into resolved steadfastness (Abu Dawud).",
            targetCount = 10
        )
    )
}
