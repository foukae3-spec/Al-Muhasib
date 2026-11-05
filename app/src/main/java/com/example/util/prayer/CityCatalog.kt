package com.example.util.prayer

data class CityLocation(
    val id: String,
    val arabicName: String,
    val englishName: String,
    val countryArabic: String,
    val countryEnglish: String,
    val latitude: Double,
    val longitude: Double,
    val timezoneId: String,
    val defaultMethod: CalculationMethod
)

object CityCatalog {

    val CITIES: List<CityLocation> = listOf(
        // Saudi Arabia
        CityLocation("SA_MAK", "مكة المكرمة", "Makkah", "المملكة العربية السعودية", "Saudi Arabia", 21.4225, 39.8262, "Asia/Riyadh", CalculationMethod.UMM_AL_QURA),
        CityLocation("SA_MED", "المدينة المنورة", "Madinah", "المملكة العربية السعودية", "Saudi Arabia", 24.4672, 39.6111, "Asia/Riyadh", CalculationMethod.UMM_AL_QURA),
        CityLocation("SA_RUH", "الرياض", "Riyadh", "المملكة العربية السعودية", "Saudi Arabia", 24.7136, 46.6753, "Asia/Riyadh", CalculationMethod.UMM_AL_QURA),
        CityLocation("SA_JED", "جدة", "Jeddah", "المملكة العربية السعودية", "Saudi Arabia", 21.5433, 39.1728, "Asia/Riyadh", CalculationMethod.UMM_AL_QURA),
        CityLocation("SA_DMM", "الدمام", "Dammam", "المملكة العربية السعودية", "Saudi Arabia", 26.4207, 50.0888, "Asia/Riyadh", CalculationMethod.UMM_AL_QURA),

        // Egypt
        CityLocation("EG_CAI", "القاهرة", "Cairo", "جمهورية مصر العربية", "Egypt", 30.0444, 31.2357, "Africa/Cairo", CalculationMethod.EGYPTIAN),
        CityLocation("EG_ALX", "الإسكندرية", "Alexandria", "جمهورية مصر العربية", "Egypt", 31.2001, 29.9187, "Africa/Cairo", CalculationMethod.EGYPTIAN),
        CityLocation("EG_GIZ", "الجيزة", "Giza", "جمهورية مصر العربية", "Egypt", 30.0131, 31.2089, "Africa/Cairo", CalculationMethod.EGYPTIAN),
        CityLocation("EG_PTS", "بورسعيد", "Port Said", "جمهورية مصر العربية", "Egypt", 31.2653, 32.3019, "Africa/Cairo", CalculationMethod.EGYPTIAN),
        CityLocation("EG_ASW", "أسوان", "Aswan", "جمهورية مصر العربية", "Egypt", 24.0889, 32.8998, "Africa/Cairo", CalculationMethod.EGYPTIAN),

        // UAE
        CityLocation("AE_DXB", "دبي", "Dubai", "الإمارات العربية المتحدة", "United Arab Emirates", 25.2048, 55.2708, "Asia/Dubai", CalculationMethod.GULF),
        CityLocation("AE_AUH", "أبوظبي", "Abu Dhabi", "الإمارات العربية المتحدة", "United Arab Emirates", 24.4539, 54.3773, "Asia/Dubai", CalculationMethod.GULF),
        CityLocation("AE_SHJ", "الشارقة", "Sharjah", "الإمارات العربية المتحدة", "United Arab Emirates", 25.3463, 55.4209, "Asia/Dubai", CalculationMethod.GULF),

        // Qatar, Kuwait, Bahrain, Oman
        CityLocation("QA_DOH", "الدوحة", "Doha", "قطر", "Qatar", 25.2854, 51.5310, "Asia/Qatar", CalculationMethod.GULF),
        CityLocation("KW_KWI", "الكويت", "Kuwait City", "الكويت", "Kuwait", 29.3759, 47.9774, "Asia/Kuwait", CalculationMethod.GULF),
        CityLocation("BH_BAH", "المنامة", "Manama", "البحرين", "Bahrain", 26.2285, 50.5860, "Asia/Bahrain", CalculationMethod.GULF),
        CityLocation("OM_MCT", "مسقط", "Muscat", "سلطنة عمان", "Oman", 23.5880, 58.3829, "Asia/Muscat", CalculationMethod.GULF),

        // Levant & Iraq
        CityLocation("PS_JER", "القدس الشريف", "Jerusalem (Al-Quds)", "فلسطين", "Palestine", 31.7683, 35.2137, "Asia/Jerusalem", CalculationMethod.MUSLIM_WORLD_LEAGUE),
        CityLocation("PS_GAZ", "غزة", "Gaza", "فلسطين", "Palestine", 31.5017, 34.4668, "Asia/Gaza", CalculationMethod.MUSLIM_WORLD_LEAGUE),
        CityLocation("JO_AMM", "عمان", "Amman", "الأردن", "Jordan", 31.9454, 35.9284, "Asia/Amman", CalculationMethod.MUSLIM_WORLD_LEAGUE),
        CityLocation("LB_BEY", "بيروت", "Beirut", "لبنان", "Lebanon", 33.8938, 35.5018, "Asia/Beirut", CalculationMethod.MUSLIM_WORLD_LEAGUE),
        CityLocation("SY_DAM", "دمشق", "Damascus", "سوريا", "Syria", 33.5138, 36.2765, "Asia/Damascus", CalculationMethod.MUSLIM_WORLD_LEAGUE),
        CityLocation("IQ_BGW", "بغداد", "Baghdad", "العراق", "Iraq", 33.3152, 44.3661, "Asia/Baghdad", CalculationMethod.MUSLIM_WORLD_LEAGUE),

        // North Africa (Algeria - All 58 Wilayas)
        CityLocation("DZ_01", "أدرار", "Adrar", "الجزائر", "Algeria", 27.8743, -0.2939, "Africa/Algiers", CalculationMethod.ALGERIA),
        CityLocation("DZ_02", "الشلف", "Chlef", "الجزائر", "Algeria", 36.1652, 1.3345, "Africa/Algiers", CalculationMethod.ALGERIA),
        CityLocation("DZ_03", "الأغواط", "Laghouat", "الجزائر", "Algeria", 33.8000, 2.8651, "Africa/Algiers", CalculationMethod.ALGERIA),
        CityLocation("DZ_04", "أم البواقي", "Oum El Bouaghi", "الجزائر", "Algeria", 35.8755, 7.1135, "Africa/Algiers", CalculationMethod.ALGERIA),
        CityLocation("DZ_05", "باتنة", "Batna", "الجزائر", "Algeria", 35.5558, 6.1741, "Africa/Algiers", CalculationMethod.ALGERIA),
        CityLocation("DZ_06", "بجاية", "Bejaia", "الجزائر", "Algeria", 36.7511, 5.0642, "Africa/Algiers", CalculationMethod.ALGERIA),
        CityLocation("DZ_07", "بسكرة", "Biskra", "الجزائر", "Algeria", 34.8504, 5.7280, "Africa/Algiers", CalculationMethod.ALGERIA),
        CityLocation("DZ_08", "بشار", "Bechar", "الجزائر", "Algeria", 31.6217, -2.2272, "Africa/Algiers", CalculationMethod.ALGERIA),
        CityLocation("DZ_09", "البليدة", "Blida", "الجزائر", "Algeria", 36.4700, 2.8277, "Africa/Algiers", CalculationMethod.ALGERIA),
        CityLocation("DZ_10", "البويرة", "Bouira", "الجزائر", "Algeria", 36.3749, 3.9020, "Africa/Algiers", CalculationMethod.ALGERIA),
        CityLocation("DZ_11", "تمنراست", "Tamanrasset", "الجزائر", "Algeria", 22.7850, 5.5228, "Africa/Algiers", CalculationMethod.ALGERIA),
        CityLocation("DZ_12", "تبسة", "Tebessa", "الجزائر", "Algeria", 35.4042, 8.1242, "Africa/Algiers", CalculationMethod.ALGERIA),
        CityLocation("DZ_13", "تلمسان", "Tlemcen", "الجزائر", "Algeria", 34.8783, -1.3150, "Africa/Algiers", CalculationMethod.ALGERIA),
        CityLocation("DZ_14", "تيارت", "Tiaret", "الجزائر", "Algeria", 35.3710, 1.3170, "Africa/Algiers", CalculationMethod.ALGERIA),
        CityLocation("DZ_15", "تيزي وزو", "Tizi Ouzou", "الجزائر", "Algeria", 36.7118, 4.0459, "Africa/Algiers", CalculationMethod.ALGERIA),
        CityLocation("DZ_16", "الجزائر العاصمة", "Algiers", "الجزائر", "Algeria", 36.7538, 3.0588, "Africa/Algiers", CalculationMethod.ALGERIA),
        CityLocation("DZ_17", "الجلفة", "Djelfa", "الجزائر", "Algeria", 34.6728, 3.2630, "Africa/Algiers", CalculationMethod.ALGERIA),
        CityLocation("DZ_18", "جيجل", "Jijel", "الجزائر", "Algeria", 36.8206, 5.7667, "Africa/Algiers", CalculationMethod.ALGERIA),
        CityLocation("DZ_19", "سطيف", "Setif", "الجزائر", "Algeria", 36.1898, 5.4108, "Africa/Algiers", CalculationMethod.ALGERIA),
        CityLocation("DZ_20", "سعيدة", "Saida", "الجزائر", "Algeria", 34.8303, 0.1517, "Africa/Algiers", CalculationMethod.ALGERIA),
        CityLocation("DZ_21", "سكيكدة", "Skikda", "الجزائر", "Algeria", 36.8762, 6.9092, "Africa/Algiers", CalculationMethod.ALGERIA),
        CityLocation("DZ_22", "سيدي بلعباس", "Sidi Bel Abbes", "الجزائر", "Algeria", 35.1899, -0.6309, "Africa/Algiers", CalculationMethod.ALGERIA),
        CityLocation("DZ_23", "عنابة", "Annaba", "الجزائر", "Algeria", 36.9000, 7.7667, "Africa/Algiers", CalculationMethod.ALGERIA),
        CityLocation("DZ_24", "قالمة", "Guelma", "الجزائر", "Algeria", 36.4621, 7.4261, "Africa/Algiers", CalculationMethod.ALGERIA),
        CityLocation("DZ_25", "قسنطينة", "Constantine", "الجزائر", "Algeria", 36.3650, 6.6147, "Africa/Algiers", CalculationMethod.ALGERIA),
        CityLocation("DZ_26", "المدية", "Medea", "الجزائر", "Algeria", 36.2642, 2.7539, "Africa/Algiers", CalculationMethod.ALGERIA),
        CityLocation("DZ_27", "مستغانم", "Mostaganem", "الجزائر", "Algeria", 35.9312, 0.0892, "Africa/Algiers", CalculationMethod.ALGERIA),
        CityLocation("DZ_28", "المسيلة", "M'Sila", "الجزائر", "Algeria", 35.7058, 4.5419, "Africa/Algiers", CalculationMethod.ALGERIA),
        CityLocation("DZ_29", "معسكر", "Mascara", "الجزائر", "Algeria", 35.3966, 0.1403, "Africa/Algiers", CalculationMethod.ALGERIA),
        CityLocation("DZ_30", "ورقلة", "Ouargla", "الجزائر", "Algeria", 31.9493, 5.3250, "Africa/Algiers", CalculationMethod.ALGERIA),
        CityLocation("DZ_31", "وهران", "Oran", "الجزائر", "Algeria", 35.6987, -0.6349, "Africa/Algiers", CalculationMethod.ALGERIA),
        CityLocation("DZ_32", "البيض", "El Bayadh", "الجزائر", "Algeria", 33.6832, 1.0193, "Africa/Algiers", CalculationMethod.ALGERIA),
        CityLocation("DZ_33", "إليزي", "Illizi", "الجزائر", "Algeria", 26.4833, 8.4667, "Africa/Algiers", CalculationMethod.ALGERIA),
        CityLocation("DZ_34", "برج بوعريريج", "Bordj Bou Arreridj", "الجزائر", "Algeria", 36.0732, 4.7611, "Africa/Algiers", CalculationMethod.ALGERIA),
        CityLocation("DZ_35", "بومرداس", "Boumerdes", "الجزائر", "Algeria", 36.7564, 3.4772, "Africa/Algiers", CalculationMethod.ALGERIA),
        CityLocation("DZ_36", "الطارف", "El Tarf", "الجزائر", "Algeria", 36.7672, 8.3138, "Africa/Algiers", CalculationMethod.ALGERIA),
        CityLocation("DZ_37", "تندوف", "Tindouf", "الجزائر", "Algeria", 27.6744, -8.1478, "Africa/Algiers", CalculationMethod.ALGERIA),
        CityLocation("DZ_38", "تيسمسيلت", "Tissemsilt", "الجزائر", "Algeria", 35.6072, 1.8108, "Africa/Algiers", CalculationMethod.ALGERIA),
        CityLocation("DZ_39", "الوادي", "El Oued", "الجزائر", "Algeria", 33.3683, 6.8674, "Africa/Algiers", CalculationMethod.ALGERIA),
        CityLocation("DZ_40", "خنشلة", "Khenchela", "الجزائر", "Algeria", 35.4358, 7.1433, "Africa/Algiers", CalculationMethod.ALGERIA),
        CityLocation("DZ_41", "سوق أهراس", "Souk Ahras", "الجزائر", "Algeria", 36.2864, 7.9511, "Africa/Algiers", CalculationMethod.ALGERIA),
        CityLocation("DZ_42", "تيبازة", "Tipaza", "الجزائر", "Algeria", 36.5897, 2.4475, "Africa/Algiers", CalculationMethod.ALGERIA),
        CityLocation("DZ_43", "ميلة", "Mila", "الجزائر", "Algeria", 36.4503, 6.2644, "Africa/Algiers", CalculationMethod.ALGERIA),
        CityLocation("DZ_44", "عين الدفلى", "Ain Defla", "الجزائر", "Algeria", 36.2647, 1.9679, "Africa/Algiers", CalculationMethod.ALGERIA),
        CityLocation("DZ_45", "النعامة", "Naama", "الجزائر", "Algeria", 33.2667, -0.3167, "Africa/Algiers", CalculationMethod.ALGERIA),
        CityLocation("DZ_46", "عين تموشنت", "Ain Temouchent", "الجزائر", "Algeria", 35.2975, -1.1404, "Africa/Algiers", CalculationMethod.ALGERIA),
        CityLocation("DZ_47", "غرداية", "Ghardaia", "الجزائر", "Algeria", 32.4909, 3.6735, "Africa/Algiers", CalculationMethod.ALGERIA),
        CityLocation("DZ_48", "غليزان", "Relizane", "الجزائر", "Algeria", 35.7373, 0.5558, "Africa/Algiers", CalculationMethod.ALGERIA),
        CityLocation("DZ_49", "تيميمون", "Timimoun", "الجزائر", "Algeria", 29.2639, 0.2310, "Africa/Algiers", CalculationMethod.ALGERIA),
        CityLocation("DZ_50", "برج باجي مختار", "Bordj Badji Mokhtar", "الجزائر", "Algeria", 21.3283, 0.9542, "Africa/Algiers", CalculationMethod.ALGERIA),
        CityLocation("DZ_51", "أولاد جلال", "Ouled Djellal", "الجزائر", "Algeria", 34.4333, 5.0667, "Africa/Algiers", CalculationMethod.ALGERIA),
        CityLocation("DZ_52", "بني عباس", "Beni Abbes", "الجزائر", "Algeria", 30.1333, -2.1667, "Africa/Algiers", CalculationMethod.ALGERIA),
        CityLocation("DZ_53", "عين صالح", "In Salah", "الجزائر", "Algeria", 27.1936, 2.4608, "Africa/Algiers", CalculationMethod.ALGERIA),
        CityLocation("DZ_54", "عين قزام", "In Guezzam", "الجزائر", "Algeria", 19.5722, 5.7694, "Africa/Algiers", CalculationMethod.ALGERIA),
        CityLocation("DZ_55", "تقرت", "Touggourt", "الجزائر", "Algeria", 33.1053, 6.0578, "Africa/Algiers", CalculationMethod.ALGERIA),
        CityLocation("DZ_56", "جانت", "Djanet", "الجزائر", "Algeria", 24.5550, 9.4858, "Africa/Algiers", CalculationMethod.ALGERIA),
        CityLocation("DZ_57", "المغير", "El M'Ghair", "الجزائر", "Algeria", 33.9500, 5.9167, "Africa/Algiers", CalculationMethod.ALGERIA),
        CityLocation("DZ_58", "المنيعة", "El Meniaa", "الجزائر", "Algeria", 30.5833, 2.8833, "Africa/Algiers", CalculationMethod.ALGERIA),

        // Other Maghreb & Arab countries
        CityLocation("MA_CAS", "الدار البيضاء", "Casablanca", "المغرب", "Morocco", 33.5731, -7.5898, "Africa/Casablanca", CalculationMethod.ALGERIA),
        CityLocation("MA_RAB", "الرباط", "Rabat", "المغرب", "Morocco", 34.0209, -6.8416, "Africa/Casablanca", CalculationMethod.ALGERIA),
        CityLocation("MA_MRK", "مراكش", "Marrakech", "المغرب", "Morocco", 31.6295, -7.9811, "Africa/Casablanca", CalculationMethod.ALGERIA),
        CityLocation("TN_TUN", "تونس", "Tunis", "تونس", "Tunisia", 36.8065, 10.1815, "Africa/Tunis", CalculationMethod.ALGERIA),
        CityLocation("LY_TIP", "طرابلس", "Tripoli", "ليبيا", "Libya", 32.8872, 13.1913, "Africa/Tripoli", CalculationMethod.ALGERIA),
        CityLocation("SD_KRT", "الخرطوم", "Khartoum", "السودان", "Sudan", 15.5007, 32.5599, "Africa/Khartoum", CalculationMethod.EGYPTIAN),

        // Turkey, Pakistan, Asia
        CityLocation("TR_IST", "إسطنبول", "Istanbul", "تركيا", "Turkey", 41.0082, 28.9784, "Europe/Istanbul", CalculationMethod.DIYANET),
        CityLocation("TR_ANK", "أنقرة", "Ankara", "تركيا", "Turkey", 39.9334, 32.8597, "Europe/Istanbul", CalculationMethod.DIYANET),
        CityLocation("PK_KHI", "كراتشي", "Karachi", "باكستان", "Pakistan", 24.8607, 67.0011, "Asia/Karachi", CalculationMethod.KARACHI),
        CityLocation("PK_LHE", "لاهور", "Lahore", "باكستان", "Pakistan", 31.5204, 74.3587, "Asia/Karachi", CalculationMethod.KARACHI),
        CityLocation("ID_JKT", "جاكرتا", "Jakarta", "إندونيسيا", "Indonesia", -6.2088, 106.8456, "Asia/Jakarta", CalculationMethod.MUSLIM_WORLD_LEAGUE),
        CityLocation("MY_KUL", "كوالالمبور", "Kuala Lumpur", "ماليزيا", "Malaysia", 3.1390, 101.6869, "Asia/Kuala_Lumpur", CalculationMethod.MUSLIM_WORLD_LEAGUE),

        // Europe
        CityLocation("GB_LON", "لندن", "London", "المملكة المتحدة", "United Kingdom", 51.5074, -0.1278, "Europe/London", CalculationMethod.MUSLIM_WORLD_LEAGUE),
        CityLocation("GB_BIR", "برمنغهام", "Birmingham", "المملكة المتحدة", "United Kingdom", 52.4862, -1.8904, "Europe/London", CalculationMethod.MUSLIM_WORLD_LEAGUE),
        CityLocation("FR_PAR", "باريس", "Paris", "فرنسا", "France", 48.8566, 2.3522, "Europe/Paris", CalculationMethod.MUSLIM_WORLD_LEAGUE),
        CityLocation("DE_BER", "برلين", "Berlin", "ألمانيا", "Germany", 52.5200, 13.4050, "Europe/Berlin", CalculationMethod.MUSLIM_WORLD_LEAGUE),

        // North America
        CityLocation("US_NYC", "نيويورك", "New York", "الولايات المتحدة", "United States", 40.7128, -74.0060, "America/New_York", CalculationMethod.ISNA),
        CityLocation("US_CHI", "شيكاغو", "Chicago", "الولايات المتحدة", "United States", 41.8781, -87.6298, "America/Chicago", CalculationMethod.ISNA),
        CityLocation("US_LAX", "لوس أنجلوس", "Los Angeles", "الولايات المتحدة", "United States", 34.0522, -118.2437, "America/Los_Angeles", CalculationMethod.ISNA),
        CityLocation("US_HOU", "هيوستن", "Houston", "الولايات المتحدة", "United States", 29.7604, -95.3698, "America/Chicago", CalculationMethod.ISNA),
        CityLocation("CA_TOR", "تورونتو", "Toronto", "كندا", "Canada", 43.6532, -79.3832, "America/Toronto", CalculationMethod.ISNA)
    )

    fun getDefaultCity(): CityLocation {
        return try {
            val systemTz = java.time.ZoneId.systemDefault().id
            val matchedByTz = CITIES.firstOrNull { it.timezoneId.equals(systemTz, ignoreCase = true) }
            if (matchedByTz != null) return matchedByTz

            if (systemTz.contains("Algiers", ignoreCase = true)) {
                return CITIES.firstOrNull { it.id == "DZ_16" } ?: CITIES[0]
            }
            if (systemTz.contains("Cairo", ignoreCase = true)) {
                return CITIES.firstOrNull { it.id == "EG_CAI" } ?: CITIES[0]
            }
            CITIES[0]
        } catch (_: Exception) {
            CITIES[0]
        }
    }

    fun findCityById(id: String): CityLocation {
        return CITIES.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: getDefaultCity()
    }

    private fun normalizeArabic(text: String): String {
        return text.lowercase()
            .replace("[أإآ]".toRegex(), "ا")
            .replace("ة", "ه")
            .replace("ى", "ي")
            .replace("[\u064B-\u065F]".toRegex(), "")
            .trim()
    }

    fun searchCities(query: String): List<CityLocation> {
        val q = normalizeArabic(query)
        if (q.isEmpty()) return CITIES
        val cleanQ = if (q.startsWith("ال") && q.length > 3) q.substring(2) else q
        return CITIES.filter {
            val normAr = normalizeArabic(it.arabicName)
            val normEn = it.englishName.lowercase()
            val normCountryAr = normalizeArabic(it.countryArabic)
            val normCountryEn = it.countryEnglish.lowercase()
            normAr.contains(q) || normAr.contains(cleanQ) ||
            normEn.contains(q) ||
            normCountryAr.contains(q) ||
            normCountryEn.contains(q)
        }
    }

    fun findNearestCity(latitude: Double, longitude: Double): CityLocation {
        return CITIES.minByOrNull {
            val dLat = it.latitude - latitude
            val dLon = it.longitude - longitude
            dLat * dLat + dLon * dLon
        } ?: getDefaultCity()
    }
}
