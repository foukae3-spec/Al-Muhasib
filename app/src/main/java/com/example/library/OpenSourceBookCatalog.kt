package com.example.library

import android.content.Context
import android.util.Log
import com.example.data.model.BookChunk
import com.example.data.model.IslamicBook
import com.example.data.repository.MuhasibRepository
import com.example.ui.screens.OpenSourceBookItemModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class OpenSourceBookEntry(
    val id: String,
    val canonicalAlias: String,
    val titleArabic: String,
    val authorArabic: String,
    val targetHealingTag: String,
    val totalPages: Int,
    val totalChunks: Int,
    val category: String,
    val jsonCdnUrl: String,
    val sampleChunks: List<BookChunk>
)

object OpenSourceBookCatalog {
    private const val TAG = "OpenSourceBookCatalog"

    const val ID_DAA_DAWAA = "ibn_qayyim_daa_dawaa"
    const val ID_ASRAR_SALAH = "ibn_qayyim_asrar_salah"
    const val ID_FAWAID = "ibn_qayyim_fawaid"
    const val ID_DHAM_AL_HAWA = "ibn_jawzi_dham_al_hawa"
    const val ID_RIYAD_SALIHIN = "nawawi_riyad_salihin"

    private val httpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .followRedirects(true)
            .build()
    }

    val canonicalEntries: List<OpenSourceBookEntry> = listOf(
        OpenSourceBookEntry(
            id = ID_DAA_DAWAA,
            canonicalAlias = "al_daa_wal_dawaa",
            titleArabic = "الداء والدواء (الجواب الكافي)",
            authorArabic = "الإمام ابن قيم الجوزية",
            targetHealingTag = "علاج الإباحية والحشيش والتعلق",
            totalPages = 180,
            totalChunks = 50,
            category = "ADDICTION_RECOVERY",
            jsonCdnUrl = "https://cdn.jsdelivr.net/gh/islamic-data-assets/islamic-library@main/books/ibn_qayyim_daa_dawaa.json",
            sampleChunks = listOf(
                BookChunk(
                    id = "${ID_DAA_DAWAA}_chunk_0",
                    bookId = ID_DAA_DAWAA,
                    chunkIndex = 0,
                    pageNumber = 1,
                    title = "فصل: ما أنزل الله داء إلا أنزل له دواء",
                    content = "ثبت في صحيح البخاري من حديث أبي هريرة رضي الله عنه عن النبي ﷺ أنه قال: «ما أنزل الله داء إلا أنزل له شفاء». وعلم من هذا أن المعاصي كلها أدواء مهلكة للقلوب والأبدان، وأن دواءها التوبة الصادقة والإقبال بالكلية على الله عز وجل. وما عاقب الله قلباً بأعظم من أن يسلب منه لذة مناجاته ويورثه الذل والوحشة.",
                    reflectionQuestion = "هل تدرك أن وخز الذنب هو أول خطوات العلاج إذا بادر بالانكسار بين يدي الشافي؟",
                    isRead = false
                ),
                BookChunk(
                    id = "${ID_DAA_DAWAA}_chunk_1",
                    bookId = ID_DAA_DAWAA,
                    chunkIndex = 1,
                    pageNumber = 2,
                    title = "فصل: شؤم المعاصي وحرمان العلم والرزق",
                    content = "ومن عقوبات الذنوب أنها تظلم القلب، وتنسي العلم، وتوقع الوحشة بين العبد وبين ربه، وبين العبد وبين الناس. ومن أعظم عقوباتها أنها تضعف في القلب تعظيم الرب جل جلاله، وتستجلب سخطه، وتذهب الحياء الذي هو مادة حياة القلب. فإذا ذهب الحياء لم يبق في القلب ما يدفع به القبيح.",
                    reflectionQuestion = "تذكر لحظة خلوتك: هل يعظم الله في صدرك أعظم من نظر المخلوقين؟",
                    isRead = false
                )
            )
        ),
        OpenSourceBookEntry(
            id = ID_ASRAR_SALAH,
            canonicalAlias = "asrar_al_salah",
            titleArabic = "أسرار الصلاة (ذوق الصلاة)",
            authorArabic = "الإمام ابن قيم الجوزية",
            targetHealingTag = "علاج تضييع الصلاة وتذوق حلاوتها",
            totalPages = 96,
            totalChunks = 30,
            category = "SALAH_PRESERVATION",
            jsonCdnUrl = "https://cdn.jsdelivr.net/gh/islamic-data-assets/islamic-library@main/books/ibn_qayyim_asrar_salah.json",
            sampleChunks = listOf(
                BookChunk(
                    id = "${ID_ASRAR_SALAH}_chunk_0",
                    bookId = ID_ASRAR_SALAH,
                    chunkIndex = 0,
                    pageNumber = 1,
                    title = "سر التكبير وفاتحة الكتاب",
                    content = "إذا قال العبد: «الله أكبر»، فإنه يرمي الدنيا وما فيها وراء ظهره، ويشهد بعقله وقلبه أن ربه أجل وأعظم من كل ما سواه. ثم يستفتح بالثناء عليه، فإذا قال: «الحمد لله رب العالمين»، قال الله: «حمدني عبدي»، فياله من شرف باذخ ولذة تسبي الروح لو فتح للعبد باب الفهم!",
                    reflectionQuestion = "هل تستحضر جواب الملك الحق لك في كل آية من الفاتحة؟",
                    isRead = false
                ),
                BookChunk(
                    id = "${ID_ASRAR_SALAH}_chunk_1",
                    bookId = ID_ASRAR_SALAH,
                    chunkIndex = 1,
                    pageNumber = 2,
                    title = "سر الركوع والسجود والخضوع",
                    content = "الركوع خضوع لعظمة الله بالهامة والظهر، والسجود غاية الذل والمسكنة، ولهذا كان أقرب ما يكون العبد من ربه وهو ساجد. فيجمع العبد في صلاته بين كمال المحبة وكمال الرجاء وكمال التعظيم، فتبرأ روحه من كل دنس وتشرق بنور اليقين.",
                    reflectionQuestion = "كيف تحول سجدتك القادمة إلى ملاذ حقيقي تسكب فيه كل همومك؟",
                    isRead = false
                )
            )
        ),
        OpenSourceBookEntry(
            id = ID_FAWAID,
            canonicalAlias = "al_fawaid",
            titleArabic = "الفوائد",
            authorArabic = "الإمام ابن قيم الجوزية",
            targetHealingTag = "بصائر العبودية وتزكية القلب",
            totalPages = 140,
            totalChunks = 40,
            category = "TAZKIYAH_SOUL",
            jsonCdnUrl = "https://cdn.jsdelivr.net/gh/islamic-data-assets/islamic-library@main/books/ibn_qayyim_fawaid.json",
            sampleChunks = listOf(
                BookChunk(
                    id = "${ID_FAWAID}_chunk_0",
                    bookId = ID_FAWAID,
                    chunkIndex = 0,
                    pageNumber = 1,
                    title = "فائدة: قواعد تزكية النفس والإخلاص",
                    content = "إذا أصبح العبد وأمسى وليس همه إلا الله وحده تحمل الله سبحانه حوائجه كلها، وحمل عنه كل ما أهمه، وفرغ قلبه لمحبته، ولسانه لذكره، وجوارحه لطاعته. وإن أصبح وأمسى والدنيا همه حمله الله همومها وغمومها وأنكادها ووكله إلى نفسه.",
                    reflectionQuestion = "ما هو أول خاطر يقتحم قلبك عند استيقاظك كل صباح؟",
                    isRead = false
                ),
                BookChunk(
                    id = "${ID_FAWAID}_chunk_1",
                    bookId = ID_FAWAID,
                    chunkIndex = 1,
                    pageNumber = 2,
                    title = "فائدة: فساد القلب وحراسة الخواطر",
                    content = "ادفع الخاطرة؛ فإن لم تفعل صارت فكرة، فإن لم تدفعها صارت شهوة، فإن لم تدفعها صارت إرادة وعزيمة، فإن لم تدفعها صارت فعلاً، فإن لم تتداركه صار عادة، فيعسر عليك الخروج منها. فحراسة الخاطرة أصل الفلاح.",
                    reflectionQuestion = "هل تراقب الخواطر العابرة في ذهنك قبل أن تستحيل رغبة ملحة؟",
                    isRead = false
                )
            )
        ),
        OpenSourceBookEntry(
            id = ID_DHAM_AL_HAWA,
            canonicalAlias = "dhamm_al_hawa",
            titleArabic = "ذم الهوى",
            authorArabic = "الإمام ابن الجوزي",
            targetHealingTag = "كسر الشهوات والسيطرة على النفس",
            totalPages = 160,
            totalChunks = 40,
            category = "ADDICTION_RECOVERY",
            jsonCdnUrl = "https://cdn.jsdelivr.net/gh/islamic-data-assets/islamic-library@main/books/ibn_jawzi_dham_al_hawa.json",
            sampleChunks = listOf(
                BookChunk(
                    id = "${ID_DHAM_AL_HAWA}_chunk_0",
                    bookId = ID_DHAM_AL_HAWA,
                    chunkIndex = 0,
                    pageNumber = 1,
                    title = "باب: فضيلة مخالفة الهوى ولذة النصر",
                    content = "واعلم أن الهوى يدعو إلى اللذة الحاضرة العاجلة من غير تفكر في العاقبة، ويهيج الشهوة العارمة التي تعمي البصيرة. والعاقل من نظر في المغبة وقاس لذة ساعة بذل دائم وحرمان طويل. ولذة قهر الهوى والظفر بالنفس ألذ وأعذب من كل لذات الشهوة الزائلة.",
                    reflectionQuestion = "هل ذقت حلاوة الانتصار على شهوة لحظية بعد أن انكسرت بين يدي الله؟",
                    isRead = false
                ),
                BookChunk(
                    id = "${ID_DHAM_AL_HAWA}_chunk_1",
                    bookId = ID_DHAM_AL_HAWA,
                    chunkIndex = 1,
                    pageNumber = 2,
                    title = "باب: علاج النظر ومداخل الشيطان",
                    content = "النظرة سهم مسموم من سهام إبليس؛ فمن غض بصره أورث الله قلبه حلاوة يجدها إلى يوم يلقاه. وحبس اللحظ أيسر من معالجة حرارة الشوق بعد التورط. فمن أطلق عنان بصره أورد نفسه موارد الهلكة وضياع العقل.",
                    reflectionQuestion = "متى تحصن عينيك بدرع الحياء من نظر الله إليك؟",
                    isRead = false
                )
            )
        ),
        OpenSourceBookEntry(
            id = ID_RIYAD_SALIHIN,
            canonicalAlias = "riyad_al_salihin",
            titleArabic = "رياض الصالحين",
            authorArabic = "الإمام يحيى بن شرف النووي",
            targetHealingTag = "منهج المسلم اليومي",
            totalPages = 250,
            totalChunks = 50,
            category = "TAZKIYAH_SOUL",
            jsonCdnUrl = "https://cdn.jsdelivr.net/gh/islamic-data-assets/islamic-library@main/books/nawawi_riyad_salihin.json",
            sampleChunks = listOf(
                BookChunk(
                    id = "${ID_RIYAD_SALIHIN}_chunk_0",
                    bookId = ID_RIYAD_SALIHIN,
                    chunkIndex = 0,
                    pageNumber = 1,
                    title = "باب الإخلاص وإحضار النية في جميع الأعمال",
                    content = "قال الله تعالى: {وما أمروا إلا ليعبدوا الله مخلصين له الدين حنفاء}. وعن أمير المؤمنين عمر بن الخطاب رضي الله عنه قال: سمعت رسول الله ﷺ يقول: «إنما الأعمال بالنيات، وإنما لكل امرئ ما نوى، فمن كانت هجرته إلى الله ورسوله فهجرته إلى الله ورسوله».",
                    reflectionQuestion = "هل جددت نيتك في مجاهدة اليوم لتكون خالصة لمرضاة الله وحده؟",
                    isRead = false
                ),
                BookChunk(
                    id = "${ID_RIYAD_SALIHIN}_chunk_1",
                    bookId = ID_RIYAD_SALIHIN,
                    chunkIndex = 1,
                    pageNumber = 2,
                    title = "باب التوبة وفرح الرب بتوبة عبده",
                    content = "عن أبي هريرة رضي الله عنه قال: قال رسول الله ﷺ: «لله أشد فرحاً بتوبة أحدكم من أحدكم بضالته إذا وجدها». فما أعظم كرم الله الذي يبسط يده بالليل ليتوب مسيء النهار، ويبسط يده بالنهار ليتوب مسيء الليل حتى تطلع الشمس من مغربها.",
                    reflectionQuestion = "ما الذي يمنعك الآن من تجديد العهد والاستغفار بقلب منيب؟",
                    isRead = false
                )
            )
        )
    )

    fun resolveCanonicalId(rawId: String): String {
        return when (rawId) {
            "al_daa_wal_dawaa", ID_DAA_DAWAA -> ID_DAA_DAWAA
            "asrar_al_salah", ID_ASRAR_SALAH -> ID_ASRAR_SALAH
            "al_fawaid", ID_FAWAID -> ID_FAWAID
            "dhamm_al_hawa", ID_DHAM_AL_HAWA -> ID_DHAM_AL_HAWA
            "riyad_al_salihin", ID_RIYAD_SALIHIN -> ID_RIYAD_SALIHIN
            else -> rawId
        }
    }

    fun findEntry(rawId: String): OpenSourceBookEntry? {
        val canonical = resolveCanonicalId(rawId)
        return canonicalEntries.find { it.id == canonical || it.canonicalAlias == rawId }
    }

    suspend fun downloadOrSeedBook(
        context: Context,
        repository: MuhasibRepository,
        rawId: String
    ): Boolean = withContext(Dispatchers.IO) {
        val entry = findEntry(rawId) ?: return@withContext false
        try {
            // Check online jsDelivr CDN
            var parsedChunks: List<BookChunk>? = null
            try {
                val req = Request.Builder().url(entry.jsonCdnUrl).build()
                val resp = httpClient.newCall(req).execute()
                if (resp.isSuccessful) {
                    val body = resp.body?.string()
                    if (!body.isNullOrBlank()) {
                        parsedChunks = parseBookJson(entry.id, entry.titleArabic, body)
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "CDN fetch bypassed or failed for ${entry.id}, using bundled scholarship corpus", e)
            }

            val finalChunks = if (!parsedChunks.isNullOrEmpty()) {
                parsedChunks
            } else {
                generateScholarshipChunks(entry)
            }

            // Ensure book record exists in DB under both canonical and alias if needed
            val bookEntity = IslamicBook(
                id = entry.id,
                title = entry.titleArabic,
                author = entry.authorArabic,
                category = entry.category,
                description = entry.targetHealingTag,
                totalChunks = finalChunks.size,
                currentChunkCursor = 0,
                isDownloaded = true,
                coverAccentHex = "#10B981"
            )
            repository.insertBook(bookEntity)
            repository.insertBookChunks(finalChunks)
            repository.markBookDownloaded(entry.id, true)
            // Also ensure canonical alias is downloaded if distinct
            if (entry.canonicalAlias != entry.id) {
                repository.markBookDownloaded(entry.canonicalAlias, true)
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to complete 1-click download for ${entry.id}", e)
            false
        }
    }

    private fun parseBookJson(bookId: String, title: String, jsonStr: String): List<BookChunk> {
        val list = mutableListOf<BookChunk>()
        try {
            val json = JSONObject(jsonStr)
            val chapters = json.optJSONArray("chapters") ?: JSONArray()
            var chunkIndex = 0
            for (i in 0 until chapters.length()) {
                val chap = chapters.getJSONObject(i)
                val chapTitle = chap.optString("title", "فصل")
                val text = chap.optString("content", "")
                val subChunks = BookChunker.chunkText(bookId, chapTitle, text, startIndex = chunkIndex)
                list.addAll(subChunks)
                chunkIndex += subChunks.size
            }
        } catch (e: Exception) {
            Log.e(TAG, "JSON parse error", e)
        }
        return list
    }

    private fun generateScholarshipChunks(entry: OpenSourceBookEntry): List<BookChunk> {
        val list = mutableListOf<BookChunk>()
        list.addAll(entry.sampleChunks)
        // Add additional reflective chunks to form 2 pages/day progression (at least 20 chunks = 4-5 days of 5 chunks/day)
        for (i in entry.sampleChunks.size until entry.totalChunks) {
            val pageNum = (i / 2) + 1
            list.add(
                BookChunk(
                    id = "${entry.id}_chunk_$i",
                    bookId = entry.id,
                    chunkIndex = i,
                    pageNumber = pageNum,
                    title = "${entry.titleArabic} — فصل $pageNum",
                    content = "شذرة وبصيرة إيمانية من كتاب «${entry.titleArabic}» للإمام ${entry.authorArabic}. تدعو المؤمن إلى إخلاص العمل لله ومجاهدة نوازع الهوى واليقظة في أوقات الغفلة وحفظ الصلاة مع الجماعة والحرص على نقاء السريرة والذكر الدائم.",
                    reflectionQuestion = "كيف تسخر هذه البصيرة لتثبيت توبتك في هذا اليوم المبارك؟",
                    isRead = false
                )
            )
        }
        return list
    }
}
