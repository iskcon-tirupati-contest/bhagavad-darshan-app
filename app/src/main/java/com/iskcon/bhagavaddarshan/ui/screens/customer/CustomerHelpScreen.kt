package com.iskcon.bhagavaddarshan.ui.screens.customer

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iskcon.bhagavaddarshan.BhagavadDarshanApp
import com.iskcon.bhagavaddarshan.network.BdApi
import com.iskcon.bhagavaddarshan.ui.components.AnimatedCard
import com.iskcon.bhagavaddarshan.ui.components.RadiantGoldButton
import com.iskcon.bhagavaddarshan.ui.components.customerFieldColors
import com.iskcon.bhagavaddarshan.ui.screens.friendlyError
import com.iskcon.bhagavaddarshan.ui.theme.Inter
import com.iskcon.bhagavaddarshan.ui.theme.PlayfairDisplay
import com.iskcon.bhagavaddarshan.ui.theme.TextSecondaryDark
import com.iskcon.bhagavaddarshan.ui.theme.UxCream
import com.iskcon.bhagavaddarshan.ui.theme.UxGold100
import com.iskcon.bhagavaddarshan.ui.theme.UxGold200
import com.iskcon.bhagavaddarshan.ui.theme.UxGold400
import com.iskcon.bhagavaddarshan.ui.theme.UxGold600
import com.iskcon.bhagavaddarshan.ui.theme.UxInk
import com.iskcon.bhagavaddarshan.ui.theme.UxSaffron
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
                    "Your complaint registered successfully",
                    fontFamily = PlayfairDisplay,
                    fontWeight = FontWeight.Bold,
                    color = UxInk
                )
            },
            text = {
                Text(
                    "Soon our team will resolve it.\nThank you for contacting us! Hare Krishna!",
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
            .background(UxCream)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .padding(bottom = 28.dp)
    ) {
        Column {
            Text(
                "SUPPORT",
                fontFamily = Inter,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.4.sp,
                color = UxSaffron
            )
            Spacer(Modifier.height(8.dp))
            Text(
                tr(language, "Help & support", "సహాయం మరియు మద్దతు"),
                fontFamily = PlayfairDisplay,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = UxInk
            )
        }

        Spacer(Modifier.height(24.dp))
        Text(
            "Register a Complaint",
            fontFamily = PlayfairDisplay,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = UxInk
        )
        Spacer(Modifier.height(12.dp))
        AnimatedCard(modifier = Modifier.fillMaxWidth(), corner = 28.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "SUBJECT",
                    fontFamily = Inter,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondaryDark,
                    letterSpacing = 1.sp
                )
                Box {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(2.dp, UxGold200, RoundedCornerShape(12.dp))
                            .background(UxGold100.copy(alpha = 0.35f))
                            .clickable { subjectOpen = true }
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                if (subject.isBlank()) "Select category" else subject,
                                fontFamily = Inter,
                                fontSize = 14.sp,
                                color = if (subject.isBlank()) TextSecondaryDark else UxInk
                            )
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = UxGold400)
                        }
                    }
                    DropdownMenu(expanded = subjectOpen, onDismissRequest = { subjectOpen = false }) {
                        subjects.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    subject = option
                                    subjectOpen = false
                                }
                            )
                        }
                    }
                }
                Text(
                    "MESSAGE",
                    fontFamily = Inter,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondaryDark,
                    letterSpacing = 1.sp
                )
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    placeholder = { Text("Describe your problem...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = customerFieldColors(),
                    isError = status == "Message is compulsory"
                )
                status?.let {
                    Text(it, fontFamily = Inter, fontSize = 13.sp, color = UxSaffron)
                }
                RadiantGoldButton(
                    text = "Submit Complaint",
                    onClick = {
                        if (subject.isBlank()) {
                            status = "Please select a category"
                            return@RadiantGoldButton
                        }
                        if (message.trim().isBlank()) {
                            status = "Message is compulsory"
                            return@RadiantGoldButton
                        }
                        scope.launch {
                            api.createComplaint(
                                token = token,
                                message = "${subject}: ${message.trim()}",
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
        }

        Spacer(Modifier.height(28.dp))
        Text(
            "Frequently Asked Questions",
            fontFamily = PlayfairDisplay,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = UxInk
        )
        Spacer(Modifier.height(12.dp))
        faqs.forEach { (q, a) ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, UxGold200, RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .clickable { openFaq = if (openFaq == q) null else q }
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        q,
                        modifier = Modifier.weight(1f),
                        fontFamily = Inter,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = UxInk
                    )
                    Icon(Icons.Default.ExpandMore, contentDescription = null, tint = UxGold400)
                }
                AnimatedVisibility(visible = openFaq == q) {
                    Text(
                        a,
                        modifier = Modifier.padding(top = 10.dp),
                        fontFamily = Inter,
                        fontSize = 14.sp,
                        color = TextSecondaryDark
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(2.dp, UxGold200, RoundedCornerShape(16.dp))
                .background(UxGold100)
                .clickable {
                    showPast = !showPast
                    if (showPast) loadComplaints()
                }
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                if (showPast) "Hide Past Complaints" else "View My Past Complaints",
                fontFamily = Inter,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = UxGold600
            )
        }

        if (showPast) {
            Spacer(Modifier.height(12.dp))
            if (complaints.isEmpty()) {
                Text("No complaints yet.", fontFamily = Inter, color = TextSecondaryDark)
            } else {
                complaints.forEach { c ->
                    val complaintId = c.optLong("id")
                    val rawMessage = c.optString("message")
                    val subjectLabel = rawMessage.substringBefore(": ", missingDelimiterValue = c.optString("category").ifBlank { "Complaint" })
                    val body = rawMessage.substringAfter(": ", missingDelimiterValue = rawMessage)
                    val timeLabel = c.optString("created_at").ifBlank { c.optString("createdAt") }.ifBlank { c.optString("status") }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.dp, UxGold200, RoundedCornerShape(14.dp))
                            .background(Color.White)
                            .clickable {
                                openComplaintId = if (openComplaintId == complaintId) null else complaintId
                            }
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(subjectLabel, fontFamily = Inter, fontWeight = FontWeight.Bold, color = UxInk)
                                Text(timeLabel, fontFamily = Inter, fontSize = 12.sp, color = TextSecondaryDark)
                            }
                            Icon(Icons.Default.ExpandMore, contentDescription = null, tint = UxGold400)
                        }
                        AnimatedVisibility(visible = openComplaintId == complaintId) {
                            Text(
                                body,
                                modifier = Modifier.padding(top = 10.dp),
                                fontFamily = Inter,
                                fontSize = 13.sp,
                                color = TextSecondaryDark
                            )
                        }
                    }
                }
            }
        }
    }
}
