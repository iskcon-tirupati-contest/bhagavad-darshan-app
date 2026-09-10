package com.iskcon.bhagavaddarshan.ui.screens.customer

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.iskcon.bhagavaddarshan.ui.theme.BdType
import com.iskcon.bhagavaddarshan.ui.theme.CustomTypography
import com.iskcon.bhagavaddarshan.ui.theme.EditChocolate
import com.iskcon.bhagavaddarshan.ui.theme.EditMuted
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

private enum class SuccessKind { DONATION, BOOK, SUBSCRIPTION }

@Composable
fun PaymentSuccessScreen(
    language: CustomerLanguage = CustomerLanguage.ENGLISH,
    title: String = "Hare Krishna!",
    detail: String,
    confirmLabel: String = "Return to Home",
    onContinue: () -> Unit
) {
    val info = remember(detail) { parsePaymentSuccess(detail) }

    val headline = when (info.kind) {
        SuccessKind.DONATION ->
            tr(language, "Your seva has been received", "మీ సేవ స్వీకరించబడింది")
        SuccessKind.BOOK ->
            tr(language, "Your book order is confirmed", "మీ పుస్తక ఆర్డర్ నిర్ధారించబడింది")
        SuccessKind.SUBSCRIPTION ->
            tr(language, "Your subscription is active", "మీ సభ్యత్వం యాక్టివ్‌లో ఉంది")
    }
    val body = when (info.kind) {
        SuccessKind.DONATION ->
            tr(
                language,
                "Thank you for supporting Bhagavad Darshan. Your contribution helps spread sacred literature.",
                "భగవద్ దర్శన్‌కు మీ మద్దతుకు ధన్యవాదాలు. మీ సహాయం పవిత్ర సాహిత్య ప్రచారానికి దోహదం చేస్తుంది."
            )
        SuccessKind.BOOK ->
            tr(
                language,
                "Your books will be delivered to your address within 5 days.",
                "మీ పుస్తకాలు 5 రోజుల్లోపు మీ చిరునామాకు చేరుతాయి."
            )
        SuccessKind.SUBSCRIPTION ->
            tr(
                language,
                "Thank you for supporting Bhagavad Darshan. Your magazine will continue to reach your delivery address.",
                "భగవద్ దర్శన్‌కు మీ మద్దతుకు ధన్యవాదాలు. మీ పత్రిక మీ చిరునామాకు కొనసాగుతూ చేరుతుంది."
            )
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
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SuccessCheckmark(animated = animated)
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
            when (info.kind) {
                SuccessKind.DONATION -> DonationDetails(language, info)
                SuccessKind.BOOK -> BookPurchaseDetails(language, info)
                SuccessKind.SUBSCRIPTION -> SubscriptionDetails(language, info)
            }
        }

        Spacer(Modifier.height(28.dp))
        RadiantGoldButton(
            text = tr(language, confirmLabel, "హోమ్‌కు వెళ్లండి"),
            onClick = onContinue
        )
    }
}

@Composable
private fun DonationDetails(language: CustomerLanguage, info: PaymentSuccessInfo) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(tr(language, "Payment reference", "చెల్లింపు రిఫరెన్స్"), style = CustomTypography.labelLarge, color = UxGold600)
        Text(info.reference.ifBlank { "BD-PAID" }, style = CustomTypography.titleLarge, color = TextPrimaryDark)
        if (info.itemTitle.isNotBlank()) {
            Text(
                "${info.itemTitle}${if (info.amountRupees > 0) " · ₹${info.amountRupees}" else ""}",
                style = CustomTypography.bodyLarge,
                color = TextSecondaryDark
            )
        }
    }
}

@Composable
private fun BookPurchaseDetails(language: CustomerLanguage, info: PaymentSuccessInfo) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        DetailLabel(tr(language, "Purchased books", "కొనుగోలు చేసిన పుస్తకాలు"))
        Text(
            info.itemTitle.ifBlank { tr(language, "Sacred book", "పవిత్ర పుస్తకం") },
            style = BdType.BodyStrong,
            color = EditChocolate
        )
        Spacer(Modifier.height(2.dp))
        AmountRow(tr(language, "Subtotal", "సబ్‌టోటల్"), "₹${info.amountRupees}")
        AmountRow(tr(language, "Total", "మొత్తం"), "₹${info.amountRupees}", strong = true)
        Spacer(Modifier.height(4.dp))
        DetailLabel(tr(language, "Delivery address", "డెలివరీ చిరునామా"))
        Text(
            info.address.ifBlank { tr(language, "Address on your profile", "మీ ప్రొఫైల్‌లోని చిరునామా") },
            style = CustomTypography.bodyLarge,
            color = TextSecondaryDark
        )
        Text(
            tr(language, "Delivery within 5 days", "5 రోజుల్లోపు డెలివరీ"),
            style = BdType.Caption.copy(color = EditTerracotta)
        )
        if (info.reference.isNotBlank()) {
            Spacer(Modifier.height(2.dp))
            Text(
                "${tr(language, "Payment reference", "చెల్లింపు రిఫరెన్స్")}: ${info.reference}",
                style = BdType.Caption
            )
        }
    }
}

@Composable
private fun SubscriptionDetails(language: CustomerLanguage, info: PaymentSuccessInfo) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        DetailLabel(tr(language, "Newly purchased", "కొత్తగా కొనుగోలు"))
        Text(
            info.planLabel.ifBlank { tr(language, "Magazine plan", "మ్యాగజైన్ ప్లాన్") },
            style = CustomTypography.titleLarge,
            color = EditChocolate
        )
        if (info.endPretty.isNotBlank()) {
            Text(
                tr(
                    language,
                    "Coverage through ${info.endPretty}",
                    "కవరేజ్ ${info.endPretty} వరకు"
                ),
                style = CustomTypography.bodyLarge,
                color = TextSecondaryDark
            )
        }
        if (info.carryOverDays > 0 && info.previousEndPretty.isNotBlank()) {
            Text(
                tr(
                    language,
                    "Includes ${info.carryOverDays} days carried over from your earlier plan (was valid until ${info.previousEndPretty}).",
                    "మీ మునుపటి ప్లాన్ నుండి ${info.carryOverDays} రోజులు కూడా ఉన్నాయి (గతంలో ${info.previousEndPretty} వరకు)."
                ),
                style = BdType.Caption
            )
        } else if (info.startPretty.isNotBlank() && info.endPretty.isNotBlank()) {
            Text(
                tr(
                    language,
                    "Your plan period now runs through ${info.endPretty}.",
                    "మీ ప్లాన్ కాలం ఇప్పుడు ${info.endPretty} వరకు ఉంటుంది."
                ),
                style = BdType.Caption
            )
        }
        if (info.amountRupees > 0) {
            AmountRow(tr(language, "Amount paid", "చెల్లించిన మొత్తం"), "₹${info.amountRupees}", strong = true)
        }
        Spacer(Modifier.height(2.dp))
        DetailLabel(tr(language, "Customer", "గ్రాహకుడు"))
        Text(
            info.customerName.ifBlank { "Devotee" },
            style = BdType.BodyStrong,
            color = EditChocolate
        )
        DetailLabel(tr(language, "Delivery address", "డెలివరీ చిరునామా"))
        Text(
            info.address.ifBlank { tr(language, "Address on your profile", "మీ ప్రొఫైల్‌లోని చిరునామా") },
            style = CustomTypography.bodyLarge,
            color = TextSecondaryDark
        )
        if (info.reference.isNotBlank()) {
            Spacer(Modifier.height(2.dp))
            Text(
                "${tr(language, "Payment reference", "చెల్లింపు రిఫరెన్స్")}: ${info.reference}",
                style = BdType.Caption
            )
        }
    }
}

@Composable
private fun DetailLabel(text: String) {
    Text(text, style = CustomTypography.labelLarge, color = UxGold600)
}

@Composable
private fun AmountRow(label: String, value: String, strong: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = if (strong) BdType.BodyStrong else BdType.Body, color = if (strong) EditChocolate else EditMuted)
        Text(value, style = if (strong) BdType.BodyStrong else BdType.Body, color = EditChocolate)
    }
}

@Composable
private fun SuccessCheckmark(animated: Float) {
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
}

private data class PaymentSuccessInfo(
    val kind: SuccessKind,
    val reference: String,
    val planLabel: String,
    val itemTitle: String,
    val amountRupees: Int,
    val customerName: String,
    val address: String,
    val startPretty: String,
    val endPretty: String,
    val previousEndPretty: String = "",
    val carryOverDays: Int = 0
)

private fun parsePaymentSuccess(detail: String): PaymentSuccessInfo {
    val json = runCatching { JSONObject(detail) }.getOrNull()
    if (json != null && json.has("kind")) {
        val rawKind = json.optString("kind").lowercase()
        val kind = when (rawKind) {
            "book" -> SuccessKind.BOOK
            "donation", "seva" -> SuccessKind.DONATION
            else -> SuccessKind.SUBSCRIPTION
        }
        val planLabel = json.optString("planLabel")
        val itemTitle = json.optString("itemTitle").ifBlank {
            planLabel.substringBefore(" · ").trim()
        }
        val amount = json.optInt("amountRupees").takeIf { it > 0 }
            ?: Regex("""₹\s*(\d+)""").find(planLabel)?.groupValues?.getOrNull(1)?.toIntOrNull()
            ?: 0
        return PaymentSuccessInfo(
            kind = kind,
            reference = json.optString("reference"),
            planLabel = planLabel,
            itemTitle = itemTitle,
            amountRupees = amount,
            customerName = json.optString("customerName"),
            address = json.optString("address"),
            startPretty = prettyEndDate(json.optString("startDate")),
            endPretty = prettyEndDate(json.optString("endDate")),
            previousEndPretty = prettyEndDate(json.optString("previousEndDate")),
            carryOverDays = json.optInt("carryOverDays", 0)
        )
    }
    return PaymentSuccessInfo(
        kind = SuccessKind.SUBSCRIPTION,
        reference = detail.substringBefore(" · ").ifBlank { detail },
        planLabel = detail.substringAfter(" · ", ""),
        itemTitle = "",
        amountRupees = 0,
        customerName = "",
        address = "",
        startPretty = "",
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
