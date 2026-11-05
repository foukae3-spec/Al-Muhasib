package com.example.ui.screens


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DhikrItem
import com.example.data.model.PrayerLog
import com.example.data.model.SobrietyLog
import com.example.ui.components.AppleGlassCard
import com.example.ui.components.SquircleShapeLarge
import com.example.ui.components.SquircleShapeMedium
import com.example.ui.theme.LocalAppColors
import com.example.ui.theme.SpiritualEmeraldBright
import kotlin.math.roundToInt

// ─── Ranking Tiers ──────────────────────────────────────────────────────────

private enum class RankTier(
    val label: String,
    val arabicLabel: String,
    val color: Color,
    val glowColor: Color,
    val emoji: String
) {
    PLATINUM("Platinum", "بلاتيني", Color(0xFFE5E4E2), Color(0xAAE5E4E2), "🏅"),
    GOLD("Gold", "ذهبي", Color(0xFFFFD700), Color(0xAAFFD700), "🥇"),
    SILVER("Silver", "فضي", Color(0xFFC0C0C0), Color(0xAAC0C0C0), "🥈"),
    BRONZE("Bronze", "برونزي", Color(0xFFCD7F32), Color(0xAACD7F32), "🥉"),
    DEFAULT("Competitor", "متسابق", Color(0xFF10B981), Color(0x8810B981), "🌱")
}

private fun calculateTier(score: Int, maxBenchmark: Int): RankTier {
    val pct = (score.toFloat() / maxBenchmark.toFloat()).coerceIn(0f, 1f)
    return when {
        pct >= 0.90f -> RankTier.PLATINUM
        pct >= 0.75f -> RankTier.GOLD
        pct >= 0.50f -> RankTier.SILVER
        pct >= 0.25f -> RankTier.BRONZE
        else -> RankTier.DEFAULT
    }
}

private fun topPercentLabel(score: Int, maxBenchmark: Int): String {
    val pct = (score.toFloat() / maxBenchmark.toFloat()).coerceIn(0f, 1f)
    return when {
        pct >= 0.99f -> "أعلى 1%"
        pct >= 0.95f -> "أعلى 5%"
        pct >= 0.90f -> "أعلى 10%"
        pct >= 0.75f -> "أعلى 25%"
        pct >= 0.50f -> "أعلى 50%"
        else -> "في المنافسة"
    }
}

// ─── Ghost Competitors (seeded, local-only, privacy-safe) ────────────────────

private data class GhostCompetitor(val name: String, val score: Int)

private fun generateGhosts(userScore: Int, spread: Int): List<GhostCompetitor> {
    val names = listOf(
        "عبدالله الشمري", "محمد العتيبي", "أحمد القحطاني", "سعد الغامدي",
        "عمر الزهراني", "خالد الدوسري", "يوسف الرشيدي", "إبراهيم الحربي",
        "ناصر الأحمدي", "فيصل المطيري", "عبدالرحمن السلمي", "بلال الشهراني"
    )
    val shuffled = names.shuffled()
    return shuffled.take(10).mapIndexed { i, name ->
        val offset = (spread * (i - 5)).coerceAtLeast(-userScore + 1)
        GhostCompetitor(name, (userScore + offset).coerceAtLeast(1))
    }.sortedByDescending { it.score }
}

// ─── Motivational Hadiths ────────────────────────────────────────────────────

private val hadiths = listOf(
    "«سَبَقَ الْمُفَرِّدُونَ» قَالُوا: وَمَا الْمُفَرِّدُونَ يَا رَسُولَ اللَّهِ؟ قَالَ: «الذَّاكِرُونَ اللَّهَ كَثِيرًا وَالذَّاكِرَاتُ»",
    "«مَثَلُ الَّذِي يَذْكُرُ رَبَّهُ وَالَّذِي لا يَذْكُرُ مَثَلُ الْحَيِّ وَالْمَيِّتِ»",
    "«خَيْرُ الذِّكْرِ الْخَفِيُّ، وَخَيْرُ الرِّزْقِ مَا يَكْفِي»",
    "«أَلَا أُنَبِّئُكُمْ بِخَيْرِ أَعْمَالِكُمْ وَأَزْكَاهَا عِنْدَ مَلِيكِكُمْ، وَأَرْفَعِهَا فِي دَرَجَاتِكُمْ؟» قَالُوا: بَلَى. قَالَ: «ذِكْرُ اللَّهِ تَعَالَى»",
    "«وَمَنْ أَبْطَأَ بِهِ عَمَلُهُ لَمْ يُسْرِعْ بِهِ نَسَبُهُ»"
)

// ─── Main Screen ──────────────────────────────────────────────────────────────

@Composable
fun LeaderboardScreen(
    sobrietyLog: SobrietyLog,
    todayPrayerLog: PrayerLog,
    allDhikr: List<DhikrItem>,
    modifier: Modifier = Modifier
) {
    val appColors = LocalAppColors.current

    // ── Compute user scores ──────────────────────────────────────────────────
    val adhkarCompleted = remember(allDhikr) { allDhikr.count { it.isCompleted } }
    val adhkarTotal = remember(allDhikr) { allDhikr.size.coerceAtLeast(1) }
    val adhkarScore = remember(adhkarCompleted) { adhkarCompleted * 10 } // scale to 0–1000
    val adhkarMax = 1000

    val tasbi7Score = sobrietyLog.tasbi7TodayCount
    val tasbi7Max = sobrietyLog.tasbi7DailyTarget.coerceAtLeast(1)

    val istighfarScore = sobrietyLog.istighfarTodayCount
    val istighfarMax = sobrietyLog.istighfarDailyTarget.coerceAtLeast(1)

    val prayersDone = remember(todayPrayerLog) {
        listOf(
            todayPrayerLog.fajrDone, todayPrayerLog.dhuhrDone, todayPrayerLog.asrDone,
            todayPrayerLog.maghribDone, todayPrayerLog.ishaDone
        ).count { it }
    }
    val streakDays = sobrietyLog.cleanDaysCount

    // Composite score for overall tier (weighted sum)
    val overallScore = adhkarScore + tasbi7Score + istighfarScore + (prayersDone * 20) + (streakDays * 5)
    val overallMax = adhkarMax + tasbi7Max + istighfarMax + 100 + 500

    val overallTier = remember(overallScore) { calculateTier(overallScore, overallMax) }
    val topPctLabel = remember(overallScore) { topPercentLabel(overallScore, overallMax) }

    // Ghost competitors around user's composite score
    val ghosts = remember(overallScore) { generateGhosts(overallScore, spread = overallMax / 20) }
    val userRank = remember(ghosts, overallScore) { ghosts.count { it.score > overallScore } + 1 }

    // Hadith (deterministic by day-of-year so it doesn't flicker)
    val hadith = remember { hadiths[(System.currentTimeMillis() / 86_400_000L % hadiths.size).toInt()] }

    // Build combined list: ghosts + user, sorted by score
    val allEntries = remember(ghosts, overallScore) {
        (ghosts.map { Pair(it.name, it.score) } + Pair("أنت 🌟", overallScore))
            .sortedByDescending { it.second }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ── Title ────────────────────────────────────────────────────────────
        item {
            Spacer(Modifier.height(12.dp))
            Text(
                text = "لوحة المتسابقين",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = appColors.textPrimary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Text(
                text = "تنافس على ذكر الله اليوم",
                fontSize = 13.sp,
                color = appColors.textSecondary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))
        }

        // ── Hero Tier Card ───────────────────────────────────────────────────
        item {
            TierHeroCard(
                tier = overallTier,
                topPctLabel = topPctLabel,
                rank = userRank,
                totalCompetitors = ghosts.size + 1
            )
        }

        // ── Today's Breakdown ────────────────────────────────────────────────
        item {
            AppleGlassCard(modifier = Modifier.fillMaxWidth(), shape = SquircleShapeLarge) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "إنجازات اليوم",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = appColors.textPrimary
                    )
                    StatRow("الأذكار المكتملة", adhkarCompleted, adhkarTotal, SpiritualEmeraldBright)
                    StatRow("التسبيح", tasbi7Score, tasbi7Max, Color(0xFF6EE7B7))
                    StatRow("الاستغفار", istighfarScore, istighfarMax, Color(0xFF34D399))
                    StatRow("الصلوات", prayersDone, 5, Color(0xFFA7F3D0))
                    Spacer(Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Star, null, tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "سلسلة التقوى: $streakDays يوم",
                            fontSize = 13.sp,
                            color = appColors.textSecondary
                        )
                    }
                }
            }
        }

        // ── Leaderboard List ─────────────────────────────────────────────────
        item {
            Text(
                "المتسابقون اليوم",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = appColors.textPrimary,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        itemsIndexed(allEntries, key = { _, item -> item.first }) { index, (name, score) ->
            val isUser = name.startsWith("أنت")
            LeaderboardRow(
                rank = index + 1,
                name = name,
                score = score,
                maxScore = overallMax,
                isUser = isUser
            )
        }

        // ── Hadith Footer ────────────────────────────────────────────────────
        item {
            Spacer(Modifier.height(4.dp))
            AppleGlassCard(modifier = Modifier.fillMaxWidth(), shape = SquircleShapeMedium) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("📿", fontSize = 22.sp)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        hadith,
                        fontSize = 12.sp,
                        color = appColors.textSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }
            }
            Spacer(Modifier.height(80.dp)) // nav bar padding
        }
    }
}

// ─── Tier Hero Card ───────────────────────────────────────────────────────────

@Composable
private fun TierHeroCard(
    tier: RankTier,
    topPctLabel: String,
    rank: Int,
    totalCompetitors: Int
) {
    val appColors = LocalAppColors.current
    val glowAlpha = 0.75f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(SquircleShapeLarge)
            .background(
                Brush.verticalGradient(
                    listOf(
                        tier.glowColor.copy(alpha = glowAlpha * 0.3f),
                        appColors.glassCard
                    )
                )
            )
            .border(1.5.dp, tier.color.copy(alpha = glowAlpha * 0.7f), SquircleShapeLarge)
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(tier.emoji, fontSize = 42.sp)
            Text(
                tier.arabicLabel,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = tier.color
            )
            Text(
                "أنت في $topPctLabel من المتسابقين!",
                fontSize = 14.sp,
                color = appColors.textPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(2.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.EmojiEvents, null, tint = tier.color, modifier = Modifier.size(16.dp))
                Text(
                    "المركز $rank من ${totalCompetitors + 1}",
                    fontSize = 13.sp,
                    color = appColors.textSecondary
                )
            }
        }
    }
}

// ─── Stat Row with progress bar ───────────────────────────────────────────────

@Composable
private fun StatRow(label: String, current: Int, target: Int, color: Color) {
    val appColors = LocalAppColors.current
    val progress = (current.toFloat() / target.toFloat()).coerceIn(0f, 1f)
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 13.sp, color = appColors.textSecondary)
            Text(
                "$current / $target",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp),
            color = color,
            trackColor = color.copy(alpha = 0.15f),
            strokeCap = StrokeCap.Round
        )
    }
}

// ─── Leaderboard Row ──────────────────────────────────────────────────────────

@Composable
private fun LeaderboardRow(rank: Int, name: String, score: Int, maxScore: Int, isUser: Boolean) {
    val appColors = LocalAppColors.current
    val bgColor = if (isUser) SpiritualEmeraldBright.copy(alpha = 0.12f) else appColors.glassCard
    val borderColor = if (isUser) SpiritualEmeraldBright.copy(alpha = 0.6f) else appColors.glassBorder
    val rankColor = when (rank) {
        1 -> Color(0xFFFFD700)
        2 -> Color(0xFFC0C0C0)
        3 -> Color(0xFFCD7F32)
        else -> appColors.textSecondary
    }
    val rankEmoji = when (rank) { 1 -> "🥇"; 2 -> "🥈"; 3 -> "🥉"; else -> "#$rank" }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(SquircleShapeMedium)
            .background(bgColor)
            .border(1.dp, borderColor, SquircleShapeMedium)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Rank badge
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(rankColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(rankEmoji, fontSize = if (rank <= 3) 16.sp else 12.sp, color = rankColor, fontWeight = FontWeight.Bold)
        }

        // Name
        Column(modifier = Modifier.weight(1f)) {
            Text(
                name,
                fontSize = 14.sp,
                fontWeight = if (isUser) FontWeight.Bold else FontWeight.Normal,
                color = if (isUser) SpiritualEmeraldBright else appColors.textPrimary
            )
            val pct = ((score.toFloat() / maxScore.toFloat()) * 100).roundToInt().coerceIn(0, 100)
            Text("$pct% من الهدف", fontSize = 11.sp, color = appColors.textSecondary)
        }

        // Score
        Text(
            score.toString(),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = if (isUser) SpiritualEmeraldBright else appColors.textPrimary
        )
    }
}
