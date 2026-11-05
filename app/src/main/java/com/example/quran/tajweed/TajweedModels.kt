package com.example.quran.tajweed

import androidx.compose.ui.graphics.Color

/**
 * Universal Tajweed Rules according to classic Qira'at standards (Madinah / Dar al-Ma'rifah).
 */
enum class TajweedRule(
    val id: String,
    val arabicName: String,
    val description: String,
    val harakatDuration: String,
    val exampleText: String,
    val defaultColor: Color,
    val oledNeonColor: Color,
    val pastelColor: Color
) {
    MADD_LAZIM(
        id = "madd_lazim",
        arabicName = "المد اللازم",
        description = "يمد بمقدار ست حركات لزوماً لوقوع ساكن أصلي أو حرف مشدد بعد حرف المد",
        harakatDuration = "٦ حركات (طويل)",
        exampleText = "الضَّآلِّينَ • الحَآقَّةُ • طٰسٓمٓ",
        defaultColor = Color(0xFFDC2626),      // Deep Crimson Red
        oledNeonColor = Color(0xFFFF3366),     // Neon Red/Crimson
        pastelColor = Color(0xFFF87171)        // Soft Coral Red
    ),

    MADD_WAJIB_JAIZ(
        id = "madd_wajib_jaiz",
        arabicName = "المد المتصل والمنفصل",
        description = "يمد بمقدار ٤ إلى ٥ حركات عند مجيء همزة بعد حرف المد متصلة في الكلمة أو منفصلة",
        harakatDuration = "٤ - ٥ حركات (متوسط)",
        exampleText = "جَآءَ • سُوٓءَ • بِمَآ أُنزِلَ",
        defaultColor = Color(0xFF9333EA),      // Purple
        oledNeonColor = Color(0xFFA855F7),     // Neon Violet
        pastelColor = Color(0xFFC084FC)        // Lavender
    ),

    QALQALAH(
        id = "qalqalah",
        arabicName = "القلقلة",
        description = "اضطراب الصوت عند النطق بأحد حروف (ق، ط، ب، ج، د) إذا كانت ساكنة",
        harakatDuration = "نبرة واضحة في المخرج",
        exampleText = "قُلْ هُوَ اللَّهُ أَحَدٌ • الْفَلَقِ • مُحِيطٌ",
        defaultColor = Color(0xFF2563EB),      // Royal Blue
        oledNeonColor = Color(0xFF38BDF8),     // Electric Cyan/Blue
        pastelColor = Color(0xFF60A5FA)        // Sky Blue
    ),

    GHUNNAH_MUSHADDAD(
        id = "ghunnah_mushaddad",
        arabicName = "الغنة المشددة",
        description = "غنة أكمل ما تكون بمقدار حركتين في النون والميم المشددتين (نّ، مّ)",
        harakatDuration = "حركتان",
        exampleText = "إِنَّ • عَمَّ يَتَسَآءَلُونَ • النَّاسِ",
        defaultColor = Color(0xFFEA580C),      // Deep Orange
        oledNeonColor = Color(0xFFFB923C),     // Glowing Amber
        pastelColor = Color(0xFFFDBA74)        // Warm Apricot
    ),

    IKHFAA(
        id = "ikhfaa",
        arabicName = "الإخفاء الحقيقي والشفوي",
        description = "نطق النون أو التنوين بصفة بين الإظهار والإدغام مع بقاء الغنة عند الـ ١٥ حرفاً",
        harakatDuration = "حركتان مع الغنة",
        exampleText = "مَن ذَا • كُنتُمْ • صِرَاطًا سَوِيًّا",
        defaultColor = Color(0xFF16A34A),      // Emerald Green
        oledNeonColor = Color(0xFF4ADE80),     // Neon Green
        pastelColor = Color(0xFF86EFAC)        // Soft Mint
    ),

    IDGHAM_GHUNNAH(
        id = "idgham_ghunnah",
        arabicName = "الإدغام بغنة",
        description = "إدخال النون الساكنة أو التنوين في حروف (ي، ن، م، و) مع الغنة",
        harakatDuration = "حركتان مع الغنة",
        exampleText = "مَن يَقُولُ • مِّن مَّالٍ • خَيْرًا يَرَهُ",
        defaultColor = Color(0xFFD97706),      // Amber / Dark Yellow
        oledNeonColor = Color(0xFFFACC15),     // Glowing Yellow
        pastelColor = Color(0xFFFDE047)        // Pastel Butter
    ),

    IDGHAM_NO_GHUNNAH(
        id = "idgham_no_ghunnah",
        arabicName = "الإدغام بغير غنة",
        description = "إدخال النون الساكنة أو التنوين في اللام والراء إدخالاً تاماً دون غنة",
        harakatDuration = "إدغام كامل بدون مد",
        exampleText = "مِّن رَّبِّهِمْ • غَفُورٌ رَّحِيمٌ • هُدًى لِّلْمُتَّقِينَ",
        defaultColor = Color(0xFF64748B),      // Slate Gray
        oledNeonColor = Color(0xFF94A3B8),     // Light Slate
        pastelColor = Color(0xFFCBD5E1)        // Silver Gray
    ),

    IQLAB(
        id = "iqlab",
        arabicName = "الإقلاب",
        description = "قلب النون الساكنة أو التنوين ميماً مخفاة بغنة عند ملاقاة حرف الباء",
        harakatDuration = "حركتان مع الغنة",
        exampleText = "مِنۢ بَعْدِ • أَنۢبِئْهُم • سَمِيعٌۢ بَصِيرٌ",
        defaultColor = Color(0xFF0D9488),      // Teal
        oledNeonColor = Color(0xFF2DD4BF),     // Glowing Cyan/Turquoise
        pastelColor = Color(0xFF5EEAD4)        // Pastel Teal
    )
}

/**
 * Visual styling theme for Tajweed rules.
 */
enum class TajweedTheme(val labelArabic: String, val description: String) {
    STANDARD("المصحف القياسي (دار المعرفة)", "الألوان المعتمدة رسمياً في مصاحف التجويد المطبوعة"),
    OLED_NEON("تباين عالي (OLED مضيء)", "ألوان نيونية ساطعة على الخلفيات الداكنة وشاشات AMOLED"),
    PASTEL_COMFORT("هادئ لراحة العينين", "درجات باستيل هادئة مريحة للقراءة المطولة وكبار السن")
}
