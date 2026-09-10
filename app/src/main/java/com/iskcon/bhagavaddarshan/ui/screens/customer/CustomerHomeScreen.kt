package com.iskcon.bhagavaddarshan.ui.screens.customer

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.iskcon.bhagavaddarshan.BhagavadDarshanApp
import com.iskcon.bhagavaddarshan.R
import com.iskcon.bhagavaddarshan.data.planDisplayLabel
import com.iskcon.bhagavaddarshan.data.planDurationMonths
import com.iskcon.bhagavaddarshan.network.BdApi
import com.iskcon.bhagavaddarshan.ui.components.EmergedButton
import com.iskcon.bhagavaddarshan.ui.components.customerFieldColors
import com.iskcon.bhagavaddarshan.ui.theme.EditCard
import com.iskcon.bhagavaddarshan.ui.theme.DevoteeTeal
import com.iskcon.bhagavaddarshan.ui.theme.EditChocolate
import com.iskcon.bhagavaddarshan.ui.theme.EditCream
import com.iskcon.bhagavaddarshan.ui.theme.EditMint
import com.iskcon.bhagavaddarshan.ui.theme.EditMintText
import com.iskcon.bhagavaddarshan.ui.theme.EditMuted
import com.iskcon.bhagavaddarshan.ui.theme.EditPeach
import com.iskcon.bhagavaddarshan.ui.theme.EditTerracotta
import com.iskcon.bhagavaddarshan.ui.theme.Inter
import com.iskcon.bhagavaddarshan.ui.theme.PlayfairDisplay
import kotlinx.coroutines.delay
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

private data class PlanSegmentUi(
    val id: Long,
    val planYears: Int,
    val planMonths: Int,
    val endDate: LocalDate?,
    val status: String
)

private data class DonationPack(
    val label: String,
    val count: Int,
    val amountRupees: Int
)

private val donationPacks = listOf(
    DonationPack("10 gitas", 10, 4000),
    DonationPack("20 gitas", 20, 8000),
    DonationPack("50 gitas", 50, 2000),
    DonationPack("100 gitas", 100, 40000)
)

@Composable
fun CustomerHomeScreen(
    app: BhagavadDarshanApp,
    language: CustomerLanguage = CustomerLanguage.ENGLISH,
    @Suppress("UNUSED_PARAMETER") onLogout: () -> Unit,
    onRenew: () -> Unit,
    onBookPlan: () -> Unit,
    @Suppress("UNUSED_PARAMETER") onProfile: () -> Unit = {},
    @Suppress("UNUSED_PARAMETER") onArchive: () -> Unit = {},
    onDonate: (amountPaise: Int, title: String) -> Unit = { _, _ -> },
    onBuyBook: (SacredBook) -> Unit = {}
) {
    val api = remember { BdApi() }
    var planSegments by remember { mutableStateOf<List<JSONObject>>(emptyList()) }
    var planCardIndex by remember { mutableIntStateOf(0) }
    var showDonationDialog by remember { mutableStateOf(false) }
    var selectedDonationLabel by remember { mutableStateOf(donationPacks.first().label) }
    var selectedDonationAmount by remember { mutableIntStateOf(donationPacks.first().amountRupees) }
    var customGitaCount by remember { mutableStateOf("") }
    var useCustomDonation by remember { mutableStateOf(false) }
    val bookListState = rememberLazyListState()

    LaunchedEffect(app.session.authToken) {
        val token = app.session.authToken
        if (token.isBlank()) return@LaunchedEffect
        api.getMe(token).onSuccess { json ->
            val arr = json.optJSONArray("subscriptions")
            val list = buildList {
                if (arr != null) {
                    for (i in 0 until arr.length()) add(arr.getJSONObject(i))
                } else {
                    json.optJSONObject("subscription")?.let { add(it) }
                }
            }
            planSegments = list
            planCardIndex = 0
            val latest = list.firstOrNull()
            if (latest != null) {
                app.session.bindSubscription(
                    latest.optLong("id"),
                    json.optJSONObject("customer")?.optString("name")
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        val count = CustomerCatalog.books.size
        if (count <= 1) return@LaunchedEffect
        var idx = 0
        while (true) {
            delay(2000)
            idx = (idx + 1) % count
            bookListState.animateScrollToItem(idx)
        }
    }

    val today = remember { LocalDate.now() }
    val parsedSegments = remember(planSegments) {
        planSegments.mapNotNull { s ->
            val end = s.optString("end_date").takeIf { it.length >= 10 }?.let { raw ->
                runCatching { LocalDate.parse(raw.take(10)) }.getOrNull()
            }
            PlanSegmentUi(
                id = s.optLong("id"),
                planYears = s.optInt("plan_years").takeIf { it > 0 } ?: s.optInt("planYears"),
                planMonths = s.optInt("plan_months").takeIf { it > 0 } ?: s.optInt("planMonths"),
                endDate = end,
                status = s.optString("status")
            )
        }
    }
    val mainSegment = parsedSegments.firstOrNull()
    val coverageEnd = parsedSegments.mapNotNull { it.endDate }.maxOrNull()
    val visibleSegment = parsedSegments.getOrNull(planCardIndex.coerceIn(0, (parsedSegments.size - 1).coerceAtLeast(0)))
    val isMainCard = planCardIndex == 0
    val displayEnd = if (isMainCard) coverageEnd else visibleSegment?.endDate
    val monthsLeft = displayEnd?.let {
        ChronoUnit.MONTHS.between(today.withDayOfMonth(1), it.withDayOfMonth(1)).coerceAtLeast(0)
    }
    val status = visibleSegment?.status.orEmpty()
    val active = status.equals("active", true) || (monthsLeft != null && monthsLeft > 0) ||
        (displayEnd != null && !displayEnd.isBefore(today))
    val name = app.session.agentName.ifBlank { "Devotee" }
    val planYears = visibleSegment?.planYears ?: 0
    val planMonthsSpan = planDurationMonths(planYears, visibleSegment?.planMonths ?: 0)
    val endDateLabel = remember(displayEnd) {
        displayEnd?.format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH)).orEmpty()
    }
    val planLine = planDisplayLabel(planYears, visibleSegment?.planMonths ?: 0)
    val carryOverMonths = remember(parsedSegments, today, coverageEnd, mainSegment) {
        if (parsedSegments.size <= 1 || coverageEnd == null || mainSegment?.endDate == null) 0L
        else {
            val othersMax = parsedSegments.drop(1).mapNotNull { it.endDate }.maxOrNull()
            if (othersMax == null) 0L
            else ChronoUnit.MONTHS.between(today.withDayOfMonth(1), othersMax.withDayOfMonth(1)).coerceAtLeast(0)
        }
    }
    val elapsedFraction = remember(displayEnd, monthsLeft, planMonthsSpan, carryOverMonths) {
        val left = monthsLeft ?: return@remember 0f
        val span = (planMonthsSpan + carryOverMonths.toInt()).coerceAtLeast(left.toInt()).toFloat().coerceAtLeast(1f)
        (1f - (left.toFloat() / span)).coerceIn(0f, 1f)
    }

    if (showDonationDialog) {
        val customCount = customGitaCount.toIntOrNull()
        val customValid = customCount != null && customCount > 0
        val customError = useCustomDonation && !customValid
        AlertDialog(
            onDismissRequest = { showDonationDialog = false },
            containerColor = Color.White,
            title = {
                Text(
                    tr(language, "Gita Daan Options", "గీత దాన ఎంపికలు"),
                    fontFamily = PlayfairDisplay,
                    fontWeight = FontWeight.Bold,
                    color = EditChocolate
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    donationPacks.forEach { pack ->
                        val selected = selectedDonationLabel == pack.label
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (selected) EditPeach else Color(0xFFFDFBF7))
                                .border(
                                    1.dp,
                                    if (selected) EditTerracotta else Color(0xFFE8DDD0),
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable {
                                    useCustomDonation = false
                                    selectedDonationLabel = pack.label
                                    selectedDonationAmount = pack.amountRupees
                                }
                                .padding(14.dp)
                        ) {
                            Text(pack.label, fontFamily = Inter, fontWeight = FontWeight.Bold, color = EditChocolate)
                            Text("₹${pack.amountRupees}", fontFamily = Inter, color = EditMuted)
                        }
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (useCustomDonation) EditPeach else Color(0xFFFDFBF7))
                            .border(
                                1.dp,
                                if (useCustomDonation) EditTerracotta else Color(0xFFE8DDD0),
                                RoundedCornerShape(16.dp)
                            )
                            .clickable {
                                useCustomDonation = true
                                if (customGitaCount.isBlank()) customGitaCount = "1"
                            }
                            .padding(12.dp)
                    ) {
                        Text(tr(language, "Custom count", "కస్టమ్ సంఖ్య"), fontFamily = Inter, fontWeight = FontWeight.Bold, color = EditChocolate)
                        OutlinedTextField(
                            value = customGitaCount,
                            onValueChange = {
                                useCustomDonation = true
                                customGitaCount = it.filter(Char::isDigit)
                            },
                            label = { Text("Number of Gitas") },
                            supportingText = {
                                Text(
                                    if (customError) "Enter 1 or more" else "Minimum 1. Each Gita costs 400"
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            isError = customError,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = customerFieldColors()
                        )
                    }
                    if (useCustomDonation && customValid) {
                        Text(
                            "Custom total: ₹${customCount * 400}",
                            fontFamily = Inter,
                            fontWeight = FontWeight.Bold,
                            color = EditTerracotta
                        )
                    }
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDonationDialog = false },
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(tr(language, "Cancel", "రద్దు"), color = EditMuted, fontWeight = FontWeight.Bold)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val finalCount = customCount.takeIf { useCustomDonation && customValid }
                        val amount = if (finalCount != null) finalCount * 400 else selectedDonationAmount
                        val title = if (finalCount != null) {
                            "Gita Daan - $finalCount Gitas"
                        } else {
                            "Gita Daan - $selectedDonationLabel"
                        }
                        showDonationDialog = false
                        onDonate(amount * 100, title)
                    },
                    enabled = !useCustomDonation || customValid,
                    colors = ButtonDefaults.buttonColors(containerColor = EditTerracotta),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(tr(language, "Proceed to pay", "చెల్లింపుకు కొనసాగండి"), color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(EditCream)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(start = 22.dp, end = 22.dp, top = 8.dp, bottom = 28.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(DevoteeTeal)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = CustomerCatalog.HOME_EMBLEM,
                        contentDescription = "Tilak",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .width(28.dp)
                            .height(52.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            tr(language, "Hare Krishna,", "హరే కృష్ణ,"),
                            fontFamily = Inter,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            letterSpacing = 1.2.sp,
                            color = Color.White.copy(alpha = 0.92f)
                        )
                        Text(
                            name,
                            fontFamily = PlayfairDisplay,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = Color.White
                        )
                    }
                }
            }

            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (parsedSegments.size > 1) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable(enabled = planCardIndex > 0) {
                                        if (planCardIndex > 0) planCardIndex -= 1
                                    }
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                    contentDescription = tr(language, "Previous plan", "మునుపటి ప్లాన్"),
                                    tint = if (planCardIndex > 0) EditTerracotta else EditMuted.copy(alpha = 0.35f)
                                )
                                Text(
                                    tr(language, "Prev", "మునుపటి"),
                                    fontFamily = Inter,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (planCardIndex > 0) EditTerracotta else EditMuted.copy(alpha = 0.35f)
                                )
                            }
                            Text(
                                if (isMainCard)
                                    tr(language, "Current plan", "ప్రస్తుత ప్లాన్")
                                else
                                    tr(language, "Earlier plan", "మునుపటి ప్లాన్"),
                                fontFamily = Inter,
                                fontSize = 15.sp,
                                color = EditMuted
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable(enabled = planCardIndex < parsedSegments.lastIndex) {
                                        if (planCardIndex < parsedSegments.lastIndex) planCardIndex += 1
                                    }
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    tr(language, "Next", "తదుపరి"),
                                    fontFamily = Inter,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (planCardIndex < parsedSegments.lastIndex) EditTerracotta else EditMuted.copy(alpha = 0.35f)
                                )
                                Icon(
                                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = tr(language, "Next plan", "తదుపరి ప్లాన్"),
                                    tint = if (planCardIndex < parsedSegments.lastIndex) EditTerracotta else EditMuted.copy(alpha = 0.35f)
                                )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(4.dp, RoundedCornerShape(22.dp), spotColor = Color(0x18000000))
                            .clip(RoundedCornerShape(22.dp))
                            .border(1.dp, Color(0xFFE8DDD0), RoundedCornerShape(22.dp))
                            .background(EditCard)
                            .padding(18.dp)
                    ) {
                        if (mainSegment == null) {
                            Text(
                                tr(language, "Spiritual Wisdom at Your Doorstep", "ఆధ్యాత్మిక జ్ఞానం మీ ఇంటి ముందుకు"),
                                fontFamily = PlayfairDisplay,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = EditChocolate
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                tr(language, "Subscribe to Bhagavad Darshan and receive the magazine at home every month.", "భగవద్ దర్శన్ సభ్యత్వం తీసుకుని ప్రతి నెల పత్రికను ఇంటికి పొందండి."),
                                fontFamily = Inter,
                                fontSize = 16.sp,
                                color = EditMuted
                            )
                            Spacer(Modifier.height(16.dp))
                            EditorialButton(tr(language, "Choose a plan", "ప్లాన్ ఎంచుకోండి"), onClick = onBookPlan)
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                PlanLineText(planLine = planLine)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isMainCard) Color(0xFFDDF8E4) else EditPeach)
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        when {
                                            isMainCard && active -> tr(language, "CURRENT", "ప్రస్తుతం")
                                            isMainCard -> tr(language, "PENDING", "పెండింగ్")
                                            else -> tr(language, "CARRY-OVER", "మిగిలినది")
                                        },
                                        fontFamily = Inter,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isMainCard) Color(0xFF198C37) else EditTerracotta
                                    )
                                }
                            }
                            Spacer(Modifier.height(14.dp))
                            Text(
                                "${monthsLeft ?: 0} ${tr(language, "Months", "నెలలు")}",
                                fontFamily = PlayfairDisplay,
                                fontWeight = FontWeight.Bold,
                                fontSize = 30.sp,
                                color = EditChocolate
                            )
                            Text(
                                if (isMainCard)
                                    tr(language, "remaining in your subscription", "మీ సభ్యత్వంలో మిగిలినవి")
                                else
                                    tr(language, "left on this earlier plan", "ఈ మునుపటి ప్లాన్‌లో మిగిలినవి"),
                                fontFamily = Inter,
                                fontSize = 15.sp,
                                color = EditMuted
                            )
                            if (isMainCard && carryOverMonths > 0) {
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    tr(
                                        language,
                                        "Includes $carryOverMonths carry-over months from earlier plans · coverage through $endDateLabel",
                                        "మునుపటి ప్లాన్‌ల నుండి $carryOverMonths నెలలు కలిపి · $endDateLabel వరకు కవరేజ్"
                                    ),
                                    fontFamily = Inter,
                                    fontSize = 15.sp,
                                    color = EditTerracotta
                                )
                            }
                            Spacer(Modifier.height(14.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    tr(language, "Expires on", "ముగిసే తేదీ"),
                                    fontFamily = Inter,
                                    fontSize = 15.sp,
                                    color = EditMuted
                                )
                                Text(
                                    endDateLabel.ifBlank { "--" },
                                    fontFamily = Inter,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EditChocolate
                                )
                            }
                            Spacer(Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(EditPeach)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(elapsedFraction)
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(999.dp))
                                        .background(EditTerracotta)
                                )
                            }
                            if (isMainCard) {
                                Spacer(Modifier.height(16.dp))
                                EditorialButton(tr(language, "Renew my plan", "నా ప్లాన్ రిన్యూ చేయండి"), onClick = onRenew)
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    tr(language, "Latest Magazine", "తాజా మ్యాగజైన్"),
                    fontFamily = PlayfairDisplay,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = EditChocolate
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                        .shadow(6.dp, RoundedCornerShape(24.dp), spotColor = Color(0x18000000))
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White)
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    AsyncImage(
                        model = CustomerCatalog.LATEST_COVER,
                        contentDescription = "Latest magazine",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .width(106.dp)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(12.dp))
                    )
                    Column(modifier = Modifier.align(Alignment.CenterVertically)) {
                        Text(
                            CustomerCatalog.LATEST_ISSUE_LABEL.uppercase(),
                            fontFamily = Inter,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            color = EditTerracotta
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            CustomerCatalog.LATEST_ISSUE_TITLE,
                            fontFamily = PlayfairDisplay,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = EditChocolate
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            CustomerCatalog.LATEST_ISSUE_BLURB,
                            fontFamily = Inter,
                            fontSize = 16.sp,
                            color = EditMuted,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            tr(language, "SHIPPED TO YOUR HOME", "మీ ఇంటికి పంపబడుతుంది"),
                            fontFamily = Inter,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = EditMintText
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        tr(language, "Vedic Literature", "వేద సాహిత్యం"),
                        fontFamily = PlayfairDisplay,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = EditChocolate
                    )
                    Text(
                        tr(language, "Swipe", "స్వైప్"),
                        fontFamily = Inter,
                        fontSize = 15.sp,
                        color = EditMuted
                    )
                }
                Spacer(Modifier.height(12.dp))
                LazyRow(
                    state = bookListState,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(end = 48.dp),
                    modifier = Modifier.height(310.dp)
                ) {
                    items(CustomerCatalog.books, key = { it.title }) { book ->
                        EditorialBookCard(
                            book = book,
                            onBuy = { onBuyBook(book) }
                        )
                    }
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(EditPeach)
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                tr(language, "SEVA OPPORTUNITY", "సేవా అవకాశం"),
                                fontFamily = Inter,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp,
                                color = EditTerracotta
                            )
                            Text(
                                "Gita Daan (Donate Gita)",
                                fontFamily = PlayfairDisplay,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = EditChocolate
                            )
                        }
                        Icon(Icons.Default.Favorite, contentDescription = null, tint = EditTerracotta)
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "Sponsor Bhagavad Gitas for students and seekers. Your donation helps spread divine wisdom to everyone.",
                        fontFamily = Inter,
                        fontSize = 16.sp,
                        color = EditMuted
                    )
                    Spacer(Modifier.height(18.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = CustomerCatalog.LORD_KRISHNA,
                            contentDescription = "Lord Krishna",
                            modifier = Modifier
                                .width(168.dp)
                                .height(280.dp),
                            contentScale = ContentScale.Fit
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "\u201CFor one who explains this supreme secret to the devotees, pure devotional service is guaranteed, and at the end he will come back to Me.\u201D",
                                fontFamily = PlayfairDisplay,
                                fontStyle = FontStyle.Italic,
                                fontSize = 15.sp,
                                color = EditChocolate,
                                lineHeight = 22.sp
                            )
                            Text(
                                "\u2014 Bhagavad Gita 18.68",
                                fontFamily = Inter,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = EditTerracotta
                            )
                        }
                    }
                    Spacer(Modifier.height(18.dp))
                    EditorialButton(tr(language, "Donate Now", "ఇప్పుడే దానం చేయండి"), onClick = { showDonationDialog = true })
                }
            }
        }
    }
}

@Composable
private fun EditorialButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(EditTerracotta)
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            fontFamily = Inter,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Color.White
        )
    }
}

@Composable
private fun EditorialBookCard(book: SacredBook, onBuy: () -> Unit) {
    Column(
        modifier = Modifier
            .width(165.dp)
            .height(300.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .padding(10.dp)
    ) {
        AsyncImage(
            model = book.imageModel,
            contentDescription = book.title,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF8F4EC))
        )
        Spacer(Modifier.height(8.dp))
        Text(
            book.title,
            fontFamily = PlayfairDisplay,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = EditChocolate,
            minLines = 2,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            book.subtitle,
            fontFamily = Inter,
            fontSize = 13.sp,
            color = EditMuted,
            maxLines = 1
        )
        Spacer(Modifier.weight(1f))
        Text(
            "₹${book.priceRupees}",
            fontFamily = Inter,
            fontWeight = FontWeight.Bold,
            color = EditTerracotta,
            fontSize = 16.sp
        )
        Spacer(Modifier.height(6.dp))
        EmergedButton(
            text = "Buy now",
            onClick = onBuy,
            modifier = Modifier.fillMaxWidth(),
            verticalPadding = 9.dp,
            horizontalPadding = 10.dp,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun PlanLineText(planLine: String) {
    val firstSpace = planLine.indexOf(' ')
    val leading = if (firstSpace > 0) planLine.substring(0, firstSpace) else planLine
    val trailing = if (firstSpace > 0) planLine.substring(firstSpace) else ""
    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold)) {
                append(leading)
            }
            withStyle(SpanStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold)) {
                append(trailing)
            }
        },
        fontFamily = PlayfairDisplay,
        color = EditChocolate
    )
}
