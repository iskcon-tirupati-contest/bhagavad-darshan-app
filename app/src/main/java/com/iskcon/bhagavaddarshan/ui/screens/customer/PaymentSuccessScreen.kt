package com.iskcon.bhagavaddarshan.ui.screens.customer

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.iskcon.bhagavaddarshan.ui.components.AnimatedCard
import com.iskcon.bhagavaddarshan.ui.components.RadiantGoldButton
import com.iskcon.bhagavaddarshan.ui.theme.CustomTypography
import com.iskcon.bhagavaddarshan.ui.theme.EditTerracotta
import com.iskcon.bhagavaddarshan.ui.theme.TextPrimaryDark
import com.iskcon.bhagavaddarshan.ui.theme.TextSecondaryDark
import com.iskcon.bhagavaddarshan.ui.theme.TulsiGreen
import com.iskcon.bhagavaddarshan.ui.theme.UxCream
import com.iskcon.bhagavaddarshan.ui.theme.UxGold600
import kotlinx.coroutines.delay
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun PaymentSuccessScreen(
    language: CustomerLanguage = CustomerLanguage.ENGLISH,
    title: String = "Hare Krishna!",
    detail: String,
    confirmLabel: String = "Return to Home",
    onContinue: () -> Unit
) {
    val info = remember(detail) { parsePaymentSuccess(detail) }
    val headline = if (info.isSeva) {
        tr(language, "Your seva has been received", "మీ సేవ స్వీకరించబడింది")
    } else {
        tr(language, "Your subscription is active", "మీ సభ్యత్వం యాక్టివ్‌లో ఉంది")
    }
    val body = if (info.isSeva) {
        tr(language, "Thank you for supporting Bhagavad Darshan. Your contribution helps spread sacred literature.", "భగవద్ దర్శన్‌కు మీ మద్దతుకు ధన్యవాదాలు. మీ సహాయం పవిత్ర సాహిత్య ప్రచారానికి దోహదం చేస్తుంది.")
    } else {
        tr(language, "Thank you for supporting Bhagavad Darshan. Your magazine will continue to reach your delivery address.", "భగవద్ దర్శన్‌కు మీ మద్దతుకు ధన్యవాదాలు. మీ పత్రిక మీ చిరునామాకు కొనసాగుతూ చేరుతుంది.")
    }
    val planLine = buildString {
        append(info.planLabel)
        if (info.endPretty.isNotBlank()) {
            append(tr(language, " · Active until ", " · చెల్లుబాటు అయ్యేది "))
            append(info.endPretty)
        }
    }

    var progress by remember { mutableFloatStateOf(0f) }
    val animated by animateFloatAsState(targetValue = progress, animationSpec = tween(700), label = "tick")

    LaunchedEffect(Unit) {
        delay(80)
        progress = 1f
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(UxCream)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Canvas(modifier = Modifier.size(120.dp)) {
            val stroke = 10.dp.toPx()
            drawCircle(
                color = TulsiGreen.copy(alpha = 0.18f),
                radius = size.minDimension / 2f
            )
            drawCircle(
                color = TulsiGreen,
                radius = size.minDimension / 2f - stroke,
                style = Stroke(width = stroke)
            )
            val p = animated
            if (p > 0f) {
                val start = Offset(size.width * 0.28f, size.height * 0.52f)
                val mid = Offset(size.width * 0.44f, size.height * 0.68f)
                val end = Offset(size.width * 0.74f, size.height * 0.36f)
                val firstLen = 1f
                val secondLen = 1f
                val total = firstLen + secondLen
                val drawn = total * p
                if (drawn > 0f) {
                    val t1 = (drawn / firstLen).coerceAtMost(1f)
                    drawLine(
                        color = TulsiGreen,
                        start = start,
                        end = Offset(
                            start.x + (mid.x - start.x) * t1,
                            start.y + (mid.y - start.y) * t1
                        ),
                        strokeWidth = stroke,
                        cap = StrokeCap.Round
                    )
                }
                if (drawn > firstLen) {
                    val t2 = ((drawn - firstLen) / secondLen).coerceAtMost(1f)
                    drawLine(
                        color = TulsiGreen,
                        start = mid,
                        end = Offset(
                            mid.x + (end.x - mid.x) * t2,
                            mid.y + (end.y - mid.y) * t2
                        ),
                        strokeWidth = stroke,
                        cap = StrokeCap.Round
                    )
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        Text(
            title,
            style = CustomTypography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = EditTerracotta,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            headline,
            style = CustomTypography.titleLarge,
            color = TextPrimaryDark,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            body,
            style = CustomTypography.bodyLarge,
            color = TextSecondaryDark,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        AnimatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(tr(language, "Payment reference", "చెల్లింపు రిఫరెన్స్"), style = CustomTypography.labelLarge, color = UxGold600)
                Text(
                    info.reference.ifBlank { "BD-PAID" },
                    style = CustomTypography.titleLarge,
                    color = TextPrimaryDark
                )
                if (planLine.isNotBlank()) {
                    Text(planLine, style = CustomTypography.bodyLarge, color = TextSecondaryDark)
                }
            }
        }
        Spacer(Modifier.height(28.dp))
        RadiantGoldButton(text = tr(language, confirmLabel, "హోమ్‌కు వెళ్లండి"), onClick = onContinue)
    }
}

private data class PaymentSuccessInfo(
    val isSeva: Boolean,
    val reference: String,
    val planLabel: String,
    val endPretty: String
)

private fun parsePaymentSuccess(detail: String): PaymentSuccessInfo {
    val json = runCatching { JSONObject(detail) }.getOrNull()
    if (json != null && json.has("kind")) {
        val kind = json.optString("kind")
        return PaymentSuccessInfo(
            isSeva = kind == "seva" || kind == "donation" || kind == "book",
            reference = json.optString("reference"),
            planLabel = json.optString("planLabel"),
            endPretty = prettyEndDate(json.optString("endDate"))
        )
    }
    return PaymentSuccessInfo(
        isSeva = false,
        reference = detail.substringBefore(" · ").ifBlank { detail },
        planLabel = detail.substringAfter(" · ", ""),
        endPretty = ""
    )
}

private fun prettyEndDate(raw: String): String {
    val day = raw.take(10)
    if (day.length < 10) return ""
    return runCatching {
        LocalDate.parse(day).format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH))
    }.getOrDefault("")
}
