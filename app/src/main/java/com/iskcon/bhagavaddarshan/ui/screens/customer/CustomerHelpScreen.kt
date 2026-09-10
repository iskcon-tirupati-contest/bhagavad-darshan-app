package com.iskcon.bhagavaddarshan.ui.screens.customer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenu
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iskcon.bhagavaddarshan.BhagavadDarshanApp
import com.iskcon.bhagavaddarshan.network.BdApi
import com.iskcon.bhagavaddarshan.ui.components.BdCard
import com.iskcon.bhagavaddarshan.ui.components.RadiantGoldButton
import com.iskcon.bhagavaddarshan.ui.components.customerFieldColors
import com.iskcon.bhagavaddarshan.ui.screens.friendlyError
import com.iskcon.bhagavaddarshan.ui.theme.BdMotion
import com.iskcon.bhagavaddarshan.ui.theme.BdShape
import com.iskcon.bhagavaddarshan.ui.theme.BdSpace
import com.iskcon.bhagavaddarshan.ui.theme.BdType
import com.iskcon.bhagavaddarshan.ui.theme.EditChocolate
import com.iskcon.bhagavaddarshan.ui.theme.EditCream
import com.iskcon.bhagavaddarshan.ui.theme.EditMuted
import com.iskcon.bhagavaddarshan.ui.theme.EditPeach
import com.iskcon.bhagavaddarshan.ui.theme.EditTerracotta
import com.iskcon.bhagavaddarshan.ui.theme.Inter
import com.iskcon.bhagavaddarshan.ui.theme.Montserrat
import com.iskcon.bhagavaddarshan.ui.theme.PlayfairDisplay
import com.iskcon.bhagavaddarshan.ui.theme.TextSecondaryDark
import com.iskcon.bhagavaddarshan.ui.theme.UxGold200
import com.iskcon.bhagavaddarshan.ui.theme.UxInk
import com.iskcon.bhagavaddarshan.ui.theme.UxSaffron
import com.iskcon.bhagavaddarshan.ui.theme.customerContentWidth
import kotlinx.coroutines.launch
import org.json.JSONObject

private val subjects = listOf(
    "Magazine not received",
    "Wrong address in system",
    "Payment deducted but no plan",
    "Quality of paper/printing",
    "Other issues"
)

private val faqs = listOf(
    "How do I renew my plan?" to
        "Go to the Plans tab, select your preferred year, and complete the payment via UPI. Your current subscription will be automatically extended.",
    "When will I receive my magazine?" to
        "Magazines are dispatched via Indian Post between the 1st and 5th of every month. It typically reaches your home within 7–10 working days.",
    "Cancellation Policy" to
        "Subscriptions once paid are treated as donations to ISKCON Tirupati and are generally non-refundable. If you made a duplicate payment, please register a complaint.",
    "Terms & Conditions" to
        "By subscribing, you agree to receive physical copies of Bhagavad Darshan magazine at your registered address. Address changes must be updated 15 days prior to the next dispatch cycle."
)

@Composable
fun CustomerHelpScreen(
    app: BhagavadDarshanApp,
    language: CustomerLanguage = CustomerLanguage.ENGLISH
) {
    val api = remember { BdApi() }
    val token = app.session.authToken
    val scope = rememberCoroutineScope()

    var subject by remember { mutableStateOf("") }
    var subjectOpen by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var status by remember { mutableStateOf<String?>(null) }
    var showPast by remember { mutableStateOf(false) }
    var complaints by remember { mutableStateOf<List<JSONObject>>(emptyList()) }
    var openFaq by remember { mutableStateOf<String?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var openComplaintId by remember { mutableStateOf<Long?>(null) }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = {
                Text(
                    tr(language, "Request submitted", "అభ్యర్థన సమర్పించబడింది"),
                    fontFamily = PlayfairDisplay,
                    fontWeight = FontWeight.Bold,
                    color = UxInk
                )
            },
            text = {
                Text(
                    tr(
                        language,
                        "Soon our team will resolve it.\nThank you for contacting us! Hare Krishna!",
                        "త్వరలో మా బృందం పరిష్కరిస్తుంది.\nమమ్మల్ని సంప్రదించినందుకు ధన్యవాదాలు! హరే కృష్ణ!"
                    ),
                    fontFamily = Inter
                )
            },
            confirmButton = {
                Button(
                    onClick = { showSuccessDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = UxSaffron)
                ) {
                    Text("OK")
                }
            }
        )
    }

    fun loadComplaints() {
        if (token.isBlank()) return
        scope.launch {
            api.listComplaints(token).onSuccess { json ->
                val arr = json.optJSONArray("complaints")
                complaints = buildList {
                    if (arr != null) for (i in 0 until arr.length()) add(arr.getJSONObject(i))
                }
            }
        }
    }

    LaunchedEffect(token) { loadComplaints() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(EditCream)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .customerContentWidth()
                .padding(horizontal = BdSpace.ScreenHorizontal, vertical = BdSpace.ScreenTop)
                .padding(bottom = BdSpace.BottomNavClearance)
        ) {
            Text(
                tr(
                    language,
                    "Choose a subject and tell us how we can help.",
                    "విషయం ఎంచుకుని మీ సమస్య చెప్పండి."
                ),
                style = BdType.PageSubtitle
            )

            Spacer(Modifier.height(20.dp))

            Text("SUBJECT", style = BdType.Label)
            Spacer(Modifier.height(8.dp))
            Box {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, UxGold200, RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .clickable { subjectOpen = true }
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            if (subject.isBlank())
                                tr(language, "Select a subject", "విషయం ఎంచుకోండి")
                            else subject,
                            style = BdType.Body,
                            color = if (subject.isBlank()) EditMuted else EditChocolate
                        )
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = EditTerracotta)
                    }
                }
                DropdownMenu(
                    expanded = subjectOpen,
                    onDismissRequest = { subjectOpen = false }
                ) {
                    subjects.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option, style = BdType.Body) },
                            onClick = {
                                subject = option
                                subjectOpen = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            BdCard(modifier = Modifier.fillMaxWidth()) {
                Text("MESSAGE", style = BdType.Label)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    placeholder = {
                        Text(
                            tr(language, "Describe your request...", "మీ అభ్యర్థనను వివరించండి..."),
                            fontFamily = Montserrat
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = customerFieldColors(),
                    isError = status == "Message is compulsory"
                )
                status?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, style = BdType.Caption.copy(color = EditTerracotta))
                }
                Spacer(Modifier.height(12.dp))
                RadiantGoldButton(
                    text = tr(language, "Submit Request", "అభ్యర్థన సమర్పించండి"),
                    onClick = {
                        if (subject.isBlank()) {
                            status = "Please select a subject"
                            return@RadiantGoldButton
                        }
                        if (message.trim().isBlank()) {
                            status = "Message is compulsory"
                            return@RadiantGoldButton
                        }
                        scope.launch {
                            api.createComplaint(
                                token = token,
                                message = "$subject: ${message.trim()}",
                                category = "support",
                                transactionRef = ""
                            ).onSuccess {
                                status = null
                                message = ""
                                subject = ""
                                loadComplaints()
                                showSuccessDialog = true
                            }.onFailure {
                                status = friendlyError(it)
                            }
                        }
                    }
                )
            }

            Spacer(Modifier.height(28.dp))
            Text(
                tr(language, "Frequently Asked Questions", "తరచుగా అడిగే ప్రశ్నలు"),
                style = BdType.SectionTitle
            )
            Spacer(Modifier.height(12.dp))
            faqs.forEach { (q, a) ->
                val expanded = openFaq == q
                val chevron by animateFloatAsState(
                    targetValue = if (expanded) 180f else 0f,
                    animationSpec = BdMotion.FaqTween,
                    label = "faqChevron"
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .clip(BdShape.Soft)
                        .border(1.dp, UxGold200, BdShape.Soft)
                        .background(Color.White)
                        .clickable { openFaq = if (expanded) null else q }
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            q,
                            modifier = Modifier.weight(1f),
                            style = BdType.BodyStrong
                        )
                        Icon(
                            Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = EditTerracotta.copy(alpha = 0.7f),
                            modifier = Modifier.rotate(chevron)
                        )
                    }
                    AnimatedVisibility(
                        visible = expanded,
                        enter = fadeIn(tween(BdMotion.FaqMs)) + expandVertically(tween(BdMotion.FaqMs)),
                        exit = fadeOut(tween(BdMotion.FaqMs)) + shrinkVertically(tween(BdMotion.FaqMs))
                    ) {
                        Text(
                            a,
                            modifier = Modifier.padding(top = 10.dp),
                            style = BdType.Body
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(BdShape.Button)
                    .border(1.4.dp, UxGold200, BdShape.Button)
                    .background(EditPeach)
                    .clickable {
                        showPast = !showPast
                        if (showPast) loadComplaints()
                    }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (showPast)
                        tr(language, "Hide Past Requests", "గత అభ్యర్థనలను దాచు")
                    else
                        tr(language, "View My Past Requests", "నా గత అభ్యర్థనలు చూడండి"),
                    style = BdType.BodyStrong.copy(color = EditTerracotta)
                )
            }

            if (showPast) {
                Spacer(Modifier.height(12.dp))
                if (complaints.isEmpty()) {
                    Text(
                        tr(language, "No requests yet.", "ఇంకా అభ్యర్థనలు లేవు."),
                        style = BdType.Body
                    )
                } else {
                    complaints.forEach { c ->
                        val complaintId = c.optLong("id")
                        val rawMessage = c.optString("message")
                        val subjectLabel = rawMessage.substringBefore(
                            ": ",
                            missingDelimiterValue = c.optString("category").ifBlank { "Request" }
                        )
                        val body = rawMessage.substringAfter(": ", missingDelimiterValue = rawMessage)
                        val timeLabel = c.optString("created_at").ifBlank { c.optString("createdAt") }
                            .ifBlank { c.optString("status") }
                        val open = openComplaintId == complaintId
                        val chevron by animateFloatAsState(
                            targetValue = if (open) 180f else 0f,
                            animationSpec = BdMotion.FaqTween,
                            label = "reqChevron"
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                                .clip(BdShape.Soft)
                                .border(1.dp, UxGold200, BdShape.Soft)
                                .background(Color.White)
                                .clickable {
                                    openComplaintId = if (open) null else complaintId
                                }
                                .padding(14.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(subjectLabel, style = BdType.BodyStrong)
                                    Text(timeLabel, style = BdType.Caption)
                                }
                                Icon(
                                    Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    tint = EditTerracotta.copy(alpha = 0.7f),
                                    modifier = Modifier.rotate(chevron)
                                )
                            }
                            AnimatedVisibility(
                                visible = open,
                                enter = fadeIn(tween(BdMotion.FaqMs)) + expandVertically(tween(BdMotion.FaqMs)),
                                exit = fadeOut(tween(BdMotion.FaqMs)) + shrinkVertically(tween(BdMotion.FaqMs))
                            ) {
                                Text(body, modifier = Modifier.padding(top = 10.dp), style = BdType.Body)
                            }
                        }
                    }
                }
            }
        }
    }
}
