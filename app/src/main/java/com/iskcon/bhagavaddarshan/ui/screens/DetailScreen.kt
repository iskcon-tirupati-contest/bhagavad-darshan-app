package com.iskcon.bhagavaddarshan.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import com.iskcon.bhagavaddarshan.data.Subscription
import com.iskcon.bhagavaddarshan.data.SubscriptionPlan
import com.iskcon.bhagavaddarshan.data.planDurationMonths
import com.iskcon.bhagavaddarshan.ui.components.ActiveGreenChip
import com.iskcon.bhagavaddarshan.ui.components.CompactTopBar
import com.iskcon.bhagavaddarshan.ui.components.MetaChip
import com.iskcon.bhagavaddarshan.ui.components.StatusChip
import com.iskcon.bhagavaddarshan.ui.components.statusContainerColor
import com.iskcon.bhagavaddarshan.ui.components.statusContentColor
import com.iskcon.bhagavaddarshan.ui.theme.AmberPending
import com.iskcon.bhagavaddarshan.ui.theme.AmberPendingContainer
import com.iskcon.bhagavaddarshan.ui.theme.Leaf
import com.iskcon.bhagavaddarshan.ui.theme.Marigold
import com.iskcon.bhagavaddarshan.ui.theme.MarigoldContainer
import com.iskcon.bhagavaddarshan.ui.theme.SoftRed
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    id: Long,
    viewModel: SubscriptionViewModel,
    app: BhagavadDarshanApp,
    onBack: () -> Unit,
    razorpayReady: Boolean = false,
    onPayWithRazorpay: (Long, SubscriptionPlan, String, String) -> Unit = { _, _, _, _ -> }
) {
    val item by viewModel.selected.collectAsState()
    var editingAddress by remember { mutableStateOf(false) }
    var houseNo by remember { mutableStateOf("") }
    var street by remember { mutableStateOf("") }
    var village by remember { mutableStateOf("") }
    var mandal by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("") }
    var pincode by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var msg by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(id) {
        viewModel.loadDetail(id)
    }

    LaunchedEffect(item?.id) {
        val s = item ?: return@LaunchedEffect
        houseNo = s.houseNo
        street = s.street
        village = s.villageTown
        mandal = s.mandal
        district = s.district
        pincode = s.pincode
        state = s.state
    }

    Scaffold_(
        title = item?.name ?: "Subscriber Details",
        onBack = onBack
    ) { padding ->
        val sub = item
        if (sub == null) {
            Column(Modifier.padding(padding).padding(16.dp)) {
                Text("Loading…", style = MaterialTheme.typography.bodyLarge)
            }
            return@Scaffold_
        }
        val plan = SubscriptionPlan.fromYears(sub.planYears)
        val progress = remember(sub.startMonth, sub.endDate, sub.planYears, sub.planMonths) {
            subscriptionProgress(sub)
        }
        val isPending = sub.status == Subscription.Status.PENDING_PAYMENT ||
            sub.status == Subscription.Status.PAYMENT_FAILED
        val isActive = sub.status.equals(Subscription.Status.ACTIVE, ignoreCase = true)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Receipt banner
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    Modifier
                        .border(1.dp, Color(0xFFE8E0D6), RoundedCornerShape(12.dp))
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Receipt #${sub.receiptNo}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            sub.name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF6D6D6D)
                        )
                    }
                    when {
                        isActive -> ActiveGreenChip("Active")
                        isPending -> MetaChip(
                            text = "Pending",
                            container = AmberPendingContainer,
                            content = AmberPending
                        )
                        else -> StatusChip(
                            text = sub.status.replace('_', ' '),
                            containerColor = statusContainerColor(sub.status),
                            contentColor = statusContentColor(sub.status)
                        )
                    }
                }
            }

            // Current plan + details
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    Modifier
                        .border(1.dp, Color(0xFFE8E0D6), RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MarigoldContainer)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            "CURRENT PLAN",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Marigold,
                            letterSpacing = 0.8.sp
                        )
                    }
                    Text(
                        "${planDurationMonths(sub.planYears, sub.planMonths)} month(s) · ₹${sub.totalAmount}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    DetailLine("Phone", sub.phone)
                    DetailLine("Registered by", sub.registeredBy.ifBlank { "self" })
                    DetailLine("Address", buildAddress(sub))
                    DetailLine(
                        "Plan",
                        "${planDurationMonths(sub.planYears, sub.planMonths)} month(s) · ₹${sub.totalAmount}"
                    )
                    DetailLine("Gift books", sub.giftBooks.toString())
                    DetailLine("Start month", sub.startMonth)
                    DetailLine("Ends", sub.endDate)
                    DetailLine("Payment ref", sub.paymentRef.ifBlank { "—" })
                    DetailLine("Collector", sub.collectorName.ifBlank { "—" })
                    if (sub.notes.isNotBlank()) DetailLine("Notes", sub.notes)

                    progress?.let { (elapsed, total, fraction) ->
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "SUBSCRIPTION PROGRESS",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF9E9E9E),
                            letterSpacing = 0.6.sp
                        )
                        Spacer(Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { fraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = Marigold,
                            trackColor = Color(0xFFF0EBE4)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "$elapsed of $total months",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF6D6D6D)
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = msg != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = AmberPendingContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Text(
                        msg.orEmpty(),
                        modifier = Modifier.padding(12.dp),
                        color = AmberPending,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (!editingAddress) {
                OutlinedButton(
                    onClick = { editingAddress = true },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Marigold),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Marigold)
                ) { Text("Edit Address", fontWeight = FontWeight.SemiBold) }
            } else {
                Card(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        Modifier
                            .border(1.dp, Color(0xFFE8E0D6), RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Edit address", fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = houseNo,
                            onValueChange = { houseNo = it },
                            label = { Text("House") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = street,
                            onValueChange = { street = it },
                            label = { Text("Street") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = village,
                            onValueChange = { village = it },
                            label = { Text("Village/Town") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = mandal,
                            onValueChange = { mandal = it },
                            label = { Text("Mandal") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = district,
                            onValueChange = { district = it },
                            label = { Text("District") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = pincode,
                            onValueChange = { pincode = it.filter(Char::isDigit).take(6) },
                            label = { Text("Pincode") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = state,
                            onValueChange = { state = it },
                            label = { Text("State") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        Button(
                            onClick = {
                                scope.launch {
                                    val updated = sub.copy(
                                        houseNo = houseNo.trim(),
                                        street = street.trim(),
                                        villageTown = village.trim(),
                                        mandal = mandal.trim(),
                                        district = district.trim(),
                                        pincode = pincode.trim(),
                                        state = state.trim()
                                    )
                                    app.repository.update(updated)
                                    if (app.whatsAppClient.isConfigured()) {
                                        app.whatsAppClient.sendAddressChange(updated.phone, updated.name)
                                        msg = "Address saved · WhatsApp notified"
                                    } else {
                                        msg = "Address saved"
                                    }
                                    viewModel.loadDetail(id)
                                    editingAddress = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Marigold)
                        ) { Text("Save address", fontWeight = FontWeight.Bold) }
                    }
                }
            }

            if (sub.status == Subscription.Status.PENDING_PAYMENT && razorpayReady) {
                Button(
                    onClick = { onPayWithRazorpay(sub.id, plan, sub.name, sub.phone) },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Marigold)
                ) {
                    Icon(Icons.Default.QrCode2, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Show payment QR · ₹${plan.totalRupees}", fontWeight = FontWeight.Bold)
                }
            }

            if (isPending) {
                Button(
                    onClick = { viewModel.markActive(sub.id) },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Leaf)
                ) { Text("Mark Paid", fontWeight = FontWeight.Bold) }
            }

            TextButton(
                onClick = { viewModel.delete(sub.id, onBack) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Delete", color = SoftRed, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Scaffold_(
    title: String,
    onBack: () -> Unit,
    content: @Composable (androidx.compose.foundation.layout.PaddingValues) -> Unit
) {
    androidx.compose.material3.Scaffold(
        topBar = {
            CompactTopBar(
                title = {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        content = content
    )
}

@Composable
private fun DetailLine(label: String, value: String) {
    Column(Modifier.padding(vertical = 2.dp)) {
        Text(
            label.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = Color(0xFF9E9E9E),
            letterSpacing = 0.6.sp
        )
        Spacer(Modifier.height(2.dp))
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
    }
}

private fun buildAddress(sub: Subscription): String =
    listOf(sub.houseNo, sub.street, sub.villageTown, sub.mandal, sub.district, sub.pincode, sub.state)
        .filter { it.isNotBlank() }
        .joinToString(", ")

/** Returns (elapsedMonths, totalMonths, fraction) when computable. */
private fun subscriptionProgress(sub: Subscription): Triple<Int, Int, Float>? {
    val startYm = runCatching { YearMonth.parse(sub.startMonth) }.getOrNull() ?: return null
    val endDate = runCatching { LocalDate.parse(sub.endDate) }.getOrNull()
    val totalFromDates = endDate?.let {
        ChronoUnit.MONTHS.between(startYm, YearMonth.from(it)).toInt() + 1
    }
    val totalFromPlan = when {
        sub.planYears >= 6 -> sub.planYears
        sub.planMonths > 0 -> sub.planMonths
        sub.planYears > 0 -> sub.planYears * 12
        else -> null
    }
    val total = (totalFromDates ?: totalFromPlan)?.coerceAtLeast(1) ?: return null
    val now = YearMonth.now()
    val elapsed = ChronoUnit.MONTHS.between(startYm, now).toInt().coerceIn(0, total)
    val fraction = (elapsed.toFloat() / total.toFloat()).coerceIn(0f, 1f)
    return Triple(elapsed, total, fraction)
}
