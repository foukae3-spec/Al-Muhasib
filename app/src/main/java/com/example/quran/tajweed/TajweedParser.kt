package com.example.quran.tajweed

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit

object TajweedParser {

    private val QALQALAH_LETTERS = setOf('ق', 'ط', 'ب', 'ج', 'د')
    private val IKHFAA_LETTERS = setOf(
        'ص', 'ذ', 'ث', 'ك', 'ج', 'ش', 'ق', 'س', 'د', 'ط', 'ز', 'ف', 'ت', 'ض', 'ظ'
    )
    private val IDGHAM_GHUNNAH_LETTERS = setOf('ي', 'ن', 'م', 'و')
    private val IDGHAM_NO_GHUNNAH_LETTERS = setOf('ل', 'ر')
    private val HAMZAH_LETTERS = setOf('ء', 'أ', 'إ', 'ؤ', 'ئ', 'آ')

    const val SUKOON_STD = '\u0652'
    const val SUKOON_QURANIC = '\u06E1' // Small high head of Khah
    const val SHADDAH = '\u0651'
    const val MADDAH = '\u0653'
    const val MADDAH_QURANIC = '\u06E4' // Quranic small high madda
    const val SMALL_HIGH_MEEM = '\u06E8' // Iqlab symbol ۢ
    const val SMALL_LOW_MEEM = '\u06ED'  // ۭ
    const val FATHATAN = '\u064B'
    const val DAMMATAN = '\u064C'
    const val KASRATAN = '\u064D'

    fun getRuleColor(rule: TajweedRule, theme: TajweedTheme, customOverrides: Map<String, Color> = emptyMap()): Color {
        customOverrides[rule.id]?.let { return it }
        return when (theme) {
            TajweedTheme.STANDARD -> rule.defaultColor
            TajweedTheme.OLED_NEON -> rule.oledNeonColor
            TajweedTheme.PASTEL_COMFORT -> rule.pastelColor
        }
    }

    /**
     * Parse Arabic Uthmanic text and produce an AnnotatedString with precise Tajweed coloring.
     */
    fun parseTajweed(
        text: String,
        theme: TajweedTheme = TajweedTheme.STANDARD,
        baseColor: Color,
        fontSize: TextUnit,
        fontFamily: FontFamily = FontFamily.Serif,
        fontWeight: FontWeight = FontWeight.Normal,
        customColors: Map<String, Color> = emptyMap()
    ): AnnotatedString {
        if (text.isBlank()) return AnnotatedString("")

        val rulesAtIndices = mutableMapOf<Int, TajweedRule>()
        val len = text.length

        var i = 0
        while (i < len) {
            val c = text[i]

            // 1. Maddah (ـٓ / ۤ / آ) Detection
            if (c == MADDAH || c == MADDAH_QURANIC || c == 'آ') {
                // Check following characters for Shaddah or Hamzah
                var isShaddah = false
                var isHamzah = false
                for (j in (i + 1)..minOf(i + 5, len - 1)) {
                    val nextChar = text[j]
                    if (nextChar == SHADDAH) {
                        isShaddah = true
                        break
                    }
                    if (nextChar in HAMZAH_LETTERS) {
                        isHamzah = true
                        break
                    }
                    if (nextChar == ' ' || nextChar == '\n') break
                }

                val rule = if (isShaddah) TajweedRule.MADD_LAZIM else TajweedRule.MADD_WAJIB_JAIZ
                // Color the preceding letter if it's an alif/waw/yaa and the maddah itself
                if (i > 0 && text[i - 1] in setOf('ا', 'و', 'ي', 'ى', 'ٱ', 'ٰ')) {
                    rulesAtIndices[i - 1] = rule
                }
                rulesAtIndices[i] = rule
            }

            // 2. Ghunnah on Mushaddad (نّ, مّ)
            if (c == SHADDAH && i > 0) {
                val prev = text[i - 1]
                if (prev == 'ن' || prev == 'م') {
                    rulesAtIndices[i - 1] = TajweedRule.GHUNNAH_MUSHADDAD
                    rulesAtIndices[i] = TajweedRule.GHUNNAH_MUSHADDAD
                }
            }

            // 3. Qalqalah (حروف قطب جد ساكنة)
            if (c in QALQALAH_LETTERS) {
                var isSakin = false
                if (i + 1 < len) {
                    val next = text[i + 1]
                    if (next == SUKOON_STD || next == SUKOON_QURANIC) {
                        isSakin = true
                        rulesAtIndices[i + 1] = TajweedRule.QALQALAH
                    } else if (next == ' ' || next == '\n' || next in setOf('۝', 'ۛ', 'ۚ', 'ۖ', 'ۗ', 'ۘ', 'ۙ')) {
                        isSakin = true
                    }
                } else {
                    isSakin = true
                }
                if (isSakin) {
                    rulesAtIndices[i] = TajweedRule.QALQALAH
                }
            }

            // 4. Iqlab (علامة الميم الصغيرة ۢ فوق النون أو التنوين)
            if (c == SMALL_HIGH_MEEM || c == SMALL_LOW_MEEM) {
                rulesAtIndices[i] = TajweedRule.IQLAB
                if (i > 0) rulesAtIndices[i - 1] = TajweedRule.IQLAB
            }

            // 5. Noon Sakinah & Tanween rules (Idgham / Ikhfaa)
            val isTanween = c == FATHATAN || c == DAMMATAN || c == KASRATAN
            val isNoonSakin = c == 'ن' && (i + 1 < len && (text[i + 1] == SUKOON_STD || text[i + 1] == SUKOON_QURANIC || text[i + 1] == ' ' || text[i + 1] !in setOf('\u064E', '\u064F', '\u0650', '\u0651')))

            if (isTanween || isNoonSakin) {
                // Find next non-space letter
                var nextLetterIndex = -1
                for (k in (i + 1) until len) {
                    val ch = text[k]
                    if (ch != ' ' && ch != '\n' && ch != SUKOON_STD && ch != SUKOON_QURANIC) {
                        nextLetterIndex = k
                        break
                    }
                }

                if (nextLetterIndex != -1) {
                    val nextLetter = text[nextLetterIndex]
                    when {
                        nextLetter in IDGHAM_GHUNNAH_LETTERS -> {
                            rulesAtIndices[i] = TajweedRule.IDGHAM_GHUNNAH
                            rulesAtIndices[nextLetterIndex] = TajweedRule.IDGHAM_GHUNNAH
                        }
                        nextLetter in IDGHAM_NO_GHUNNAH_LETTERS -> {
                            rulesAtIndices[i] = TajweedRule.IDGHAM_NO_GHUNNAH
                            rulesAtIndices[nextLetterIndex] = TajweedRule.IDGHAM_NO_GHUNNAH
                        }
                        nextLetter in IKHFAA_LETTERS -> {
                            rulesAtIndices[i] = TajweedRule.IKHFAA
                            rulesAtIndices[nextLetterIndex] = TajweedRule.IKHFAA
                        }
                        nextLetter == 'ب' -> {
                            rulesAtIndices[i] = TajweedRule.IQLAB
                            rulesAtIndices[nextLetterIndex] = TajweedRule.IQLAB
                        }
                    }
                }
            }

            i++
        }

        return buildAnnotatedString {
            var currRule: TajweedRule? = null
            var blockStart = 0

            for (idx in text.indices) {
                val rule = rulesAtIndices[idx]
                if (rule != currRule) {
                    if (idx > blockStart) {
                        val chunk = text.substring(blockStart, idx)
                        val color = if (currRule != null) {
                            getRuleColor(currRule, theme, customColors)
                        } else {
                            baseColor
                        }
                        val weight = if (currRule != null) FontWeight.SemiBold else fontWeight
                        pushStyle(
                            SpanStyle(
                                color = color,
                                fontSize = fontSize,
                                fontFamily = fontFamily,
                                fontWeight = weight
                            )
                        )
                        append(chunk)
                        pop()
                    }
                    currRule = rule
                    blockStart = idx
                }
            }

            if (blockStart < text.length) {
                val chunk = text.substring(blockStart)
                val color = if (currRule != null) {
                    getRuleColor(currRule, theme, customColors)
                } else {
                    baseColor
                }
                val weight = if (currRule != null) FontWeight.SemiBold else fontWeight
                pushStyle(
                    SpanStyle(
                        color = color,
                        fontSize = fontSize,
                        fontFamily = fontFamily,
                        fontWeight = weight
                    )
                )
                append(chunk)
                pop()
            }
        }
    }
}
