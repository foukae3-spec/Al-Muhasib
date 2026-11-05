package com.example.quran.book

import androidx.compose.runtime.Immutable

/**
 * Written Quran Riwayah / Qira'ah standards (روايات وقراءات القرآن الكريم المتواترة المعتمدة).
 */
@Immutable
enum class WrittenQuranRiwayah(
    val id: String,
    val labelArabic: String,
    val shortName: String,
    val qariName: String,
    val rawiName: String,
    val description: String,
    val geographicalSpread: String = ""
) {
    HAFS(
        id = "hafs",
        labelArabic = "رواية حفص عن عاصم",
        shortName = "حفص",
        qariName = "عاصم بن أبي النَّجود الكوفي",
        rawiName = "حفص بن سليمان بن المغيرة الأسدي الكوفي",
        description = "المصحف الشريف برواية حفص عن عاصم الكوفي من طريق الشاطبية (المصحف المعتمد عالمياً والأوسع انتشاراً)",
        geographicalSpread = "المملكة العربية السعودية، مصر، بلاد الشام، الخليج العربي، والعالم الإسلامي أجمع"
    ),
    WARSH(
        id = "warsh",
        labelArabic = "رواية ورش عن نافع",
        shortName = "ورش",
        qariName = "نافع بن عبد الرحمن المدني",
        rawiName = "عثمان بن سعيد المصري الملقب بـ (ورش)",
        description = "المصحف الشريف برواية ورش عن نافع المدني من طريق الأزرق (الرسم والضبط والخط المغربي الأصيل)",
        geographicalSpread = "المغرب، الجزائر، موريتانيا، وأجزاء من السنغال وشمال غرب إفريقيا"
    ),
    QALOON(
        id = "qaloon",
        labelArabic = "رواية قالون عن نافع",
        shortName = "قالون",
        qariName = "نافع بن عبد الرحمن المدني",
        rawiName = "عيسى بن مينا المدني الملقب بـ (قالون)",
        description = "المصحف الشريف برواية قالون عن نافع المدني (تتميز بأوجه القصر والتوسط وصلة ميم الجمع وإسقاط الهمز)",
        geographicalSpread = "ليبيا، الجمهورية التونسية، ومناطق واسعة في تشاد والنيجر ونيجيريا"
    ),
    DOORI(
        id = "doori",
        labelArabic = "رواية الدوري عن أبي عمرو",
        shortName = "الدوري",
        qariName = "أبو عمرو بن العلاء البصري",
        rawiName = "حفص بن عمر بن عبد العزيز الدوري",
        description = "المصحف الشريف برواية الدوري عن أبي عمرو البصري من طريق الشاطبية (تتميز بالإدغام الكبير وأحكام الإمالات)",
        geographicalSpread = "السودان، الصومال، تشاد، إرتريا، وأجزاء من شرق إفريقيا"
    );

    companion object {
        val DEFAULT = HAFS

        fun fromId(id: String?): WrittenQuranRiwayah {
            return entries.find { 
                it.id.equals(id, ignoreCase = true) || it.name.equals(id, ignoreCase = true) 
            } ?: DEFAULT
        }
    }
}
