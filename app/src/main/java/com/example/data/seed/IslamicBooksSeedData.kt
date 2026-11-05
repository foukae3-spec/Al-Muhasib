package com.example.data.seed

import com.example.data.model.BookChunk
import com.example.data.model.IslamicBook
import com.example.data.model.RecoveryTrack

object IslamicBooksSeedData {

    const val CAT_ADDICTION = "ADDICTION_RECOVERY"
    const val CAT_SALAH = "SALAH_PRESERVATION"
    const val CAT_TAZKIYAH = "TAZKIYAH_SOUL"

    fun getInitialBooks(): List<IslamicBook> {
        return listOf(
            // القسم الأول: كسر الإدمان ومجاهدة الشهوات
            IslamicBook(
                id = "al_daa_wal_dawaa",
                title = "الداء والدواء (الجواب الكافي)",
                author = "الإمام ابن قيم الجوزية",
                category = CAT_ADDICTION,
                description = "أعظم كتاب حُللت فيه سيكولوجية التعلق والشهوة، وعواقب المعاصي، وكيفية اقتلاع الإدمان من جذوره بنور الوحي واليقين.",
                totalChunks = 50,
                currentChunkCursor = 0,
                isDownloaded = true,
                coverAccentHex = "#EF4444"
            ),
            IslamicBook(
                id = "dhamm_al_hawa",
                title = "ذم الهوى",
                author = "الإمام ابن الجوزي",
                category = CAT_ADDICTION,
                description = "منهج عملي صارم في كسر النزوات اللحظية، والسيطرة على اندفاع الدوبامين، ومجابهة وساوس الانتكاسة بحكمة بالغة.",
                totalChunks = 40,
                currentChunkCursor = 0,
                isDownloaded = true,
                coverAccentHex = "#F97316"
            ),
            IslamicBook(
                id = "talbees_iblees",
                title = "تلبيس إبليس",
                author = "الإمام ابن الجوزي",
                category = CAT_ADDICTION,
                description = "يكشف الحيل النفسية والتبريرات الكاذبة التي يخدعك بها عقلك عند الرغبة في العودة للإدمان والشهوات المحرمة.",
                totalChunks = 35,
                currentChunkCursor = 0,
                isDownloaded = true,
                coverAccentHex = "#DC2626"
            ),
            IslamicBook(
                id = "rawdat_al_muhibbeen",
                title = "روضة المحبين ونزهة المشتاقين",
                author = "الإمام ابن قيم الجوزية",
                category = CAT_ADDICTION,
                description = "تهذيب الغريزة الإنسانية وصرف التعلق المرضي بالصور والفانيات إلى لذة الأنس بمحبة الله والنعيم الدائم.",
                totalChunks = 45,
                currentChunkCursor = 0,
                isDownloaded = true,
                coverAccentHex = "#E11D48"
            ),

            // القسم الثاني: إقامة الصلاة وتذوق حلاوتها
            IslamicBook(
                id = "asrar_al_salah",
                title = "أسرار الصلاة (ذوق الصلاة)",
                author = "الإمام ابن قيم الجوزية",
                category = CAT_SALAH,
                description = "يشرح كيف تتحول الصلاة من واجب ثقيل إلى قرة عين وراحة قلب تغنيك عن كل ملذات الدنيا الزائفة.",
                totalChunks = 30,
                currentChunkCursor = 0,
                isDownloaded = true,
                coverAccentHex = "#10B981"
            ),
            IslamicBook(
                id = "tazeem_qadr_salah",
                title = "تعظيم قدر الصلاة",
                author = "الإمام محمد بن نصر المروزي",
                category = CAT_SALAH,
                description = "يرسخ في القلب هيبة الوقوف بين يدي الله عز وجل، وخطورة التفريط في المواقيت وأجر الخشوع التام.",
                totalChunks = 35,
                currentChunkCursor = 0,
                isDownloaded = true,
                coverAccentHex = "#059669"
            ),

            // القسم الثالث: صناعة العابد لله وتزكية الروح
            IslamicBook(
                id = "al_fawaid",
                title = "الفوائد",
                author = "الإمام ابن قيم الجوزية",
                category = CAT_TAZKIYAH,
                description = "شذرات وبصائر إيمانية وفكرية مكثفة، تشحذ البصيرة وتوقظ القلب الغافل وتصلح مسار الفكر والنية.",
                totalChunks = 40,
                currentChunkCursor = 0,
                isDownloaded = true,
                coverAccentHex = "#D4AF37"
            ),
            IslamicBook(
                id = "al_ubudiyyah",
                title = "العبودية",
                author = "شيخ الإسلام ابن تيمية",
                category = CAT_TAZKIYAH,
                description = "الدليل التأصيلي المحكم لمعنى أن تكون عبداً خالصاً لله في كل سكنة وحركة، والتحرر التام من أسر المخلوقين.",
                totalChunks = 25,
                currentChunkCursor = 0,
                isDownloaded = true,
                coverAccentHex = "#3B82F6"
            ),
            IslamicBook(
                id = "madarij_al_salikeen",
                title = "مدارج السالكين (تهذيب)",
                author = "الإمام ابن قيم الجوزية",
                category = CAT_TAZKIYAH,
                description = "منازل السير إلى الله تعالى: التوبة، والإنابة، والمحاسبة، والمراقبة، والصبر، واليقين في رحلة الروح.",
                totalChunks = 60,
                currentChunkCursor = 0,
                isDownloaded = true,
                coverAccentHex = "#8B5CF6"
            ),
            IslamicBook(
                id = "riyad_al_saliheen",
                title = "رياض الصالحين",
                author = "الإمام محيي الدين النووي",
                category = CAT_TAZKIYAH,
                description = "جامع السنن النبوية الشريفة لتهذيب الأخلاق وبناء السلوك المستقيم وتنظيم حياة المسلم اليومية.",
                totalChunks = 50,
                currentChunkCursor = 0,
                isDownloaded = true,
                coverAccentHex = "#14B8A6"
            ),
            IslamicBook(
                id = "al_arbaoon_al_nawawiyyah",
                title = "الأربعون النووية",
                author = "الإمام محيي الدين النووي",
                category = CAT_TAZKIYAH,
                description = "جوامع الكلم وقواعد الدين الكلية التي يدور عليها فلك الإسلام وصلاح باطن العبد وظاهره.",
                totalChunks = 20,
                currentChunkCursor = 0,
                isDownloaded = true,
                coverAccentHex = "#06B6D4"
            ),
            IslamicBook(
                id = "al_wabil_al_sayyib",
                title = "الوابل الصيب من الكلم الطيب",
                author = "الإمام ابن قيم الجوزية",
                category = CAT_TAZKIYAH,
                description = "بيان أثر ذكر الله في جلاء صدأ القلوب وطرد الوحشة، وفضائل الأذكار اليومية وثمراتها العاجلة والآجلة.",
                totalChunks = 35,
                currentChunkCursor = 0,
                isDownloaded = true,
                coverAccentHex = "#F59E0B"
            )
        )
    }

    fun getInitialChunks(): List<BookChunk> {
        val list = mutableListOf<BookChunk>()

        // 1. الداء والدواء (الجواب الكافي)
        list.add(
            BookChunk(
                id = "al_daa_wal_dawaa_chunk_0",
                bookId = "al_daa_wal_dawaa",
                chunkIndex = 0,
                pageNumber = 1,
                title = "حقيقة الدعاء والدواء الناجع",
                content = "قد أجمع العارفون بالله على أن التوفيق ألا يكلك الله إلى نفسك، والخذلان أن يخلي بينك وبين نفسك. وما ابتلي العبد بذنب إلا لعجبه بنفسه وغفلته عن الافتقار إلى ربه. والدعاء من أقوى الأسباب في دفع المكروه وحصول المطلوب، وهو عدو البلاء يدافعه ويعالجه ويمنع نزوله ويرفعه أو يخففه إذا نزل.",
                reflectionQuestion = "هل سألت الله بصدق وافتقار اليوم أن يعيذك من شهوتك ونفسك الأمارة بالسوء؟"
            )
        )
        list.add(
            BookChunk(
                id = "al_daa_wal_dawaa_chunk_1",
                bookId = "al_daa_wal_dawaa",
                chunkIndex = 1,
                pageNumber = 2,
                title = "شؤم المعاصي وحرمان العلم والرزق",
                content = "للمعاصي من الآثار القبيحة المذمومة المضرة بالقلب والبدن في الدنيا والآخرة ما لا يعلمه إلا الله. فمنها: حرمان العلم، فإن العلم نور يقذفه الله في القلب والمعصية تطفئه. ومنها: حرمان الرزق، فإن العبد ليحرم الرزق بالذنب يصيبه. ومنها: وحشة يجدها العاصي في قلبه بينه وبين الله لا توازيها لذة أصلاً.",
                reflectionQuestion = "تذكر لحظة الوحشة والندم بعد السقوط، هل كانت اللذة العابرة تستحق سلب نور بصيرتك؟"
            )
        )
        list.add(
            BookChunk(
                id = "al_daa_wal_dawaa_chunk_2",
                bookId = "al_daa_wal_dawaa",
                chunkIndex = 2,
                pageNumber = 3,
                title = "تدرج المعصية في استعباد العقل",
                content = "مبدأ كل فتنة إنما هو من قِبل الخطرات؛ فالخطرة تصير فكرة، ثم الفكرة تصير شهوة، ثم الشهوة تصير إرادة، ثم تقوى فتصير عزيمة جازمة، ثم يقع الفعل ولابد ما لم يمنع منه مانع. فدفع الخاطرة في أول ورودها أسهل بكثير من قطع المعصية بعد استحكامها واستيلائها على قوى النفس.",
                reflectionQuestion = "حين تهجم عليك الخاطرة الشيطانية الآن، هل تطردها في ثوانيها الأولى أم تدعها تستفحل؟"
            )
        )
        list.add(
            BookChunk(
                id = "al_daa_wal_dawaa_chunk_3",
                bookId = "al_daa_wal_dawaa",
                chunkIndex = 3,
                pageNumber = 4,
                title = "علاج التعلق بالصور والشهوات الزائلة",
                content = "القلب لا يفرغ قط من حب وإرادة؛ فإن لم يملأه العبد بمحبة خالقه ومولاه وابتغاء وجهه، امتلأ بمحبة الصور الفانية والأماني الكاذبة. وعشق الصور إنما يتمكن من قلب فارغ من محبة الله، معرض عنه، متعوض بغيره عنه؛ فإذا امتلأ القلب بحب الله ورجائه وخشيته دفع كل محبة دونه.",
                reflectionQuestion = "املأ فراغ قلبك بذكر الله وتلاوة آياته لتبدد كل صورة باطلة تراود مخيلتك."
            )
        )
        list.add(
            BookChunk(
                id = "al_daa_wal_dawaa_chunk_4",
                bookId = "al_daa_wal_dawaa",
                chunkIndex = 4,
                pageNumber = 5,
                title = "الجزاء من جنس العمل وعزة الطاعة",
                content = "من ترك لله شيئاً عوضه الله خيراً منه، والعوض أنواع شتى؛ وأعظم ما يعوض به الأنس بالله، وقوة القلب، وانشراحه، وسكونه، ورضاه عن ربه. وإن في معالجة فطام النفس عن محبوباتها من المشقة ما يثيب الله عليه بأعظم مما يحتسب العبد، حتى يجد حلاوة الإيمان في قلبه ألذ من كل شهوة تركها.",
                reflectionQuestion = "استحضر عزة العبودية لله ولذة الانتصار على هواك حين تضع هاتفك جانباً وتستقيم."
            )
        )

        // 2. أسرار الصلاة
        list.add(
            BookChunk(
                id = "asrar_al_salah_chunk_0",
                bookId = "asrar_al_salah",
                chunkIndex = 0,
                pageNumber = 1,
                title = "قرة العين ومستراح الأرواح",
                content = "كان النبي ﷺ إذا حزبه أمر فزع إلى الصلاة، وقال: «يا بلال أرحنا بها». فجعل راحته فيها لما فيها من مناجاة محبوبه، وتفريج هموم الدنيا، والاشتغال بعظمة الله عن كل شاغل. وفرق بين من يصلي ليستريح من الصلاة كأنها عبء يلقيه عن عاتقه، وبين من يصلي ليستريح بالصلاة من هموم الدنيا وشواغلها.",
                reflectionQuestion = "هل تدخل صلاتك طلباً للراحة بها، أم تؤديها استعجالاً للتخلص من ثقلها؟"
            )
        )
        list.add(
            BookChunk(
                id = "asrar_al_salah_chunk_1",
                bookId = "asrar_al_salah",
                chunkIndex = 1,
                pageNumber = 2,
                title = "سر التكبير والافتتاح",
                content = "افتتاح الصلاة بـ«الله أكبر» تذكير للعبد بأنه لا شيء في الوجود أكبر ولا أعظم من ربه الذي يقف بين يديه. فإذا كبر بلسانه وقلبه ملتفت إلى دنياه أو تجارته أو شهوته، كذّب قوله بفعله! فليكن التكبير مخلصاً لقلبك من كل قيود الأرض ومصائد الغفلة لتتصل بالسماء.",
                reflectionQuestion = "عندما ترفع يديك مكبراً، ألقِ كل شؤون الدنيا وراء ظهرك متأملاً عظمة رب العالمين."
            )
        )
        list.add(
            BookChunk(
                id = "asrar_al_salah_chunk_2",
                bookId = "asrar_al_salah",
                chunkIndex = 2,
                pageNumber = 3,
                title = "خضوع الركوع وقرب السجود",
                content = "الركوع خضوع لعظمة الجبار وتذلل لجلاله، ولذلك شرع فيه تعظيم الرب: «سبحان ربي العظيم». أما السجود فهو غاية التذلل والعبودية، وفيه يضع العبد أشرف ما فيه وهو وجهه على الأرض بين يدي ربه؛ فلذلك كان أقرب ما يكون العبد من ربه وهو ساجد، فاجتمع له قرب الجلال ولذة الوصال.",
                reflectionQuestion = "أطل سجودك في الفريضة القادمة، واطرح بين يدي ربك كل ثقل تحمله روحك."
            )
        )

        // 3. الفوائد
        list.add(
            BookChunk(
                id = "al_fawaid_chunk_0",
                bookId = "al_fawaid",
                chunkIndex = 0,
                pageNumber = 1,
                title = "قاعدة في حفظ الخواطر والأفكار",
                content = "دافع الخطرة، فإن لم تفعل صارت فكرة، فدافع الفكرة، فإن لم تفعل صارت شهوة، فحاربها، فإن لم تفعل صارت عزيمة وهمة، فإن لم تدافعها صارت فعلاً، فإن لم تتداركه بضده صار عادة، فيعسر عليك الخروج منها! واعلم أن أنفع الأفكار ما كان في محاسبة النفس على ما فرطت في جنب الله ومراقبته في الخلوات.",
                reflectionQuestion = "راقب فكرك اليوم، واقطع شريط الأفكار السلبية أو المحرمة في مهدها بذكر الله."
            )
        )
        list.add(
            BookChunk(
                id = "al_fawaid_chunk_1",
                bookId = "al_fawaid",
                chunkIndex = 1,
                pageNumber = 2,
                title = "إضاعة الوقت وفساد القلب",
                content = "إضاعة الوقت أشد من الموت، لأن إضاعة الوقت تقطعك عن الله والدار الآخرة، والموت يقطعك عن الدنيا وأهلها. وإنما يفسد القلب بأمور خمسة: كثرة الخلطة، والتمني، والتعلق بغير الله، والشبع، وكثرة المنام. فاحفظ قلبك من هذه السموم تستقم لك حياتك وتصلح سريرتك.",
                reflectionQuestion = "كم ساعة ضاعت اليوم أمام الشاشات دون فائدة؟ اجعل صلاتك ووردك وقود إنجازك."
            )
        )

        // 4. ذم الهوى
        list.add(
            BookChunk(
                id = "dhamm_al_hawa_chunk_0",
                bookId = "dhamm_al_hawa",
                chunkIndex = 0,
                pageNumber = 1,
                title = "حقيقة مجاهدة الهوى ومرارة الصبر",
                content = "اعلم أن الصبر عن الشهوة أيسر بكثير من الصبر على ما توجبه الشهوة؛ فإنها إما أن توجب ألماً وعقوبة، وإما أن تقطع لذة أكمل منها، وإما أن تضيع وقتاً إضاعته حسرة وندامة، وإما أن تثلم عرضاً توفيره أنفع، وإما أن تذهب مالاً بقاؤه خير، وإما أن تسلب نعمة زوالها نقمة. فالعاقل من قارن بين عاجل اللذة وآجل العواقب.",
                reflectionQuestion = "إذا هممت بمعصية، فكر في ألم ما بعد ارتكابها بعشر دقائق: ضياع الشرف والوقار والسكينة."
            )
        )

        // 5. رياض الصالحين
        list.add(
            BookChunk(
                id = "riyad_al_saliheen_chunk_0",
                bookId = "riyad_al_saliheen",
                chunkIndex = 0,
                pageNumber = 1,
                title = "باب الإخلاص وإحضار النية",
                content = "عن أمير المؤمنين أبي حفص عمر بن الخطاب رضي الله عنه قال: سمعت رسول الله ﷺ يقول: «إنما الأعمال بالنيات، وإنما لكل امرئ ما نوى؛ فمن كانت هجرته إلى الله ورسوله فهجرته إلى الله ورسوله، ومن كانت هجرته لدنيا يصيبها أو امرأة ينكحها فهجرته إلى ما هاجر إليه» متفق عليه.",
                reflectionQuestion = "جدد نيتك في سعيك وعملك ونومك واستيقاظك ليكون خالصاً لوجه الله تعالى."
            )
        )

        // 6. الأربعون النووية
        list.add(
            BookChunk(
                id = "al_arbaoon_al_nawawiyyah_chunk_0",
                bookId = "al_arbaoon_al_nawawiyyah",
                chunkIndex = 0,
                pageNumber = 1,
                title = "الحديث الثاني: مراتب الدين والإحسان",
                content = "قال جبريل عليه السلام: «فأخبرني عن الإحسان؟ قال: أن تعبد الله كأنك تراه، فإن لم تكن تراه فإنه يراك». وهذا المقام هو أصل المراقبة وعماد التقوى؛ فمن استشعر نظر الله إليه في حركاته وسكناته وخلواته استحيا أن يراه ربه على معصية أو حيث نهاه.",
                reflectionQuestion = "تذكر في خلوتك وأنت وحدك مع الهاتف: الله مطلع عليك، فاجعل استشعار نظره حاجزك."
            )
        )

        // 7. الوابل الصيب
        list.add(
            BookChunk(
                id = "al_wabil_al_sayyib_chunk_0",
                bookId = "al_wabil_al_sayyib",
                chunkIndex = 0,
                pageNumber = 1,
                title = "مثل القلب الذاكر والقلب الغافل",
                content = "الذكر للقلب مثل الماء للسمك، فكيف يكون حال السمك إذا فارق الماء؟ والذكر يطرد الشيطان ويقمعه، ويرضي الرحمن، ويزيل الهم والغم عن القلب، ويجلب للقلب الفرح والسرور، وينور الوجه والقلب، ويجلب الرزق، ويكسو الذاكر المهابة والحلاوة والنضرة.",
                reflectionQuestion = "بلل لسانك الآن بـ«سبحان الله وبحمده سبحان الله العظيم» واطرد وحشة الغفلة."
            )
        )

        return list
    }

    fun getInitialRecoveryTracks(): List<RecoveryTrack> {
        val now = System.currentTimeMillis()
        return listOf(
            RecoveryTrack(
                trackType = "SALAH",
                title = "حصن الصلاة وتذوق حلاوتها",
                startDateTimestamp = now,
                cleanStreakSeconds = 3600L * 24 * 7, // 7 days baseline
                relapseCount = 0,
                moneySavedAmount = 0.0,
                cigarettesAvoided = 0,
                highestStreakSeconds = 3600L * 24 * 14,
                nightGuardEnabled = true,
                salahLockEnabled = true,
                lastCheckInTimestamp = now
            ),
            RecoveryTrack(
                trackType = "CANNABIS",
                title = "التعافي من الحشيش وتطهير العقل",
                startDateTimestamp = now - (3600L * 24 * 10 * 1000), // 10 days clean
                cleanStreakSeconds = 3600L * 24 * 10,
                relapseCount = 0,
                moneySavedAmount = 15000.0, // 15,000 DZD saved
                cigarettesAvoided = 0,
                highestStreakSeconds = 3600L * 24 * 10,
                nightGuardEnabled = true,
                salahLockEnabled = false,
                lastCheckInTimestamp = now
            ),
            RecoveryTrack(
                trackType = "PORNOGRAPHY",
                title = "التعافي من الإباحية وغض البصر",
                startDateTimestamp = now - (3600L * 24 * 14 * 1000), // 14 days clean
                cleanStreakSeconds = 3600L * 24 * 14,
                relapseCount = 0,
                moneySavedAmount = 0.0,
                cigarettesAvoided = 0,
                highestStreakSeconds = 3600L * 24 * 21,
                nightGuardEnabled = true,
                salahLockEnabled = false,
                lastCheckInTimestamp = now
            ),
            RecoveryTrack(
                trackType = "SMOKING",
                title = "الإقلاع عن التدخين واستعادة الرئة",
                startDateTimestamp = now - (3600L * 24 * 5 * 1000), // 5 days clean
                cleanStreakSeconds = 3600L * 24 * 5,
                relapseCount = 0,
                moneySavedAmount = 2500.0, // 2,500 DZD saved (500 DZD/pack)
                cigarettesAvoided = 100, // 20 cigs/day * 5
                highestStreakSeconds = 3600L * 24 * 5,
                nightGuardEnabled = false,
                salahLockEnabled = false,
                lastCheckInTimestamp = now
            )
        )
    }
}
