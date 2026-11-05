package com.example.aod

import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * مكوّن الاقتباس والشذرة القرائية على شاشة التوقف الدائمة (Always-On Display)
 * - يستهلك 0.0% من المعالج (بدون أي حلقات تحريك)
 * - نقرة مزدوجة (Double Tap) تُعلّم الفقرة كمقروءة وتنتقل للفقرة التالية في الكتاب!
 */
@Composable
fun AodBookQuotePlaque(
    bookTitle: String,
    authorName: String,
    quoteText: String,
    pageNumber: Int,
    dailyProgressLabel: String, // مثلاً: "وِرد اليوم: 2/5"
    accentColor: Color,
    onDoubleTapAdvanceChunk: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .border(
                width = 0.75.dp,
                color = accentColor.copy(alpha = 0.22f),
                shape = RoundedCornerShape(20.dp)
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { onDoubleTapAdvanceChunk() }
                )
            }
            .padding(horizontal = 18.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // نص الاقتباس أو الشذرة القرائية بإضاءة خافتة مريحة في الظلام
        Text(
            text = "« $quoteText »",
            color = Color(0xFFD8D5CE),
            fontFamily = FontFamily.Serif,
            fontSize = 14.sp,
            lineHeight = 24.sp,
            textAlign = TextAlign.Center,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(8.dp))

        // المصدر ورقم الصفحة ومؤشر العلامة المرجعية
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$bookTitle ($authorName) • ص $pageNumber",
                color = accentColor.copy(alpha = 0.80f),
                fontSize = 11.sp
            )
            Text(
                text = "• $dailyProgressLabel (انقر مرتين للإتمام)",
                color = Color(0xFF6E7370),
                fontSize = 10.sp
            )
        }
    }
}
