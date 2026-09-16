package com.iskcon.bhagavaddarshan.ui.screens

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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iskcon.bhagavaddarshan.BhagavadDarshanApp
import com.iskcon.bhagavaddarshan.data.Subscription
import com.iskcon.bhagavaddarshan.data.SubscriptionPlan
import com.iskcon.bhagavaddarshan.data.displayPriceRupees
import com.iskcon.bhagavaddarshan.data.formatMonthYear
import com.iskcon.bhagavaddarshan.data.formatMonthYearFromEnd
import com.iskcon.bhagavaddarshan.data.formatMonthYearFromStart
import com.iskcon.bhagavaddarshan.data.offerEligibleLabel
import com.iskcon.bhagavaddarshan.data.planDisplayName
import com.iskcon.bhagavaddarshan.ui.components.CompactTopBar
import com.iskcon.bhagavaddarshan.ui.components.premiumCardSurface
import com.iskcon.bhagavaddarshan.ui.theme.AmberPending
import com.iskcon.bhagavaddarshan.ui.theme.Leaf
import com.iskcon.bhagavaddarshan.ui.theme.LeafContainer
import com.iskcon.bhagavaddarshan.ui.theme.Marigold
import com.iskcon.bhagavaddarshan.ui.theme.MarigoldContainer
import com.iskcon.bhagavaddarshan.ui.theme.SoftRed
import com.iskcon.bhagavaddarshan.ui.theme.TempleMaroon
import com.iskcon.bhagavaddarshan.util.UiSounds
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

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
    val context = LocalContext.current
    val isAdmin = app.session.isAdmin
    var showAddressEditor by remember { mutableStateOf(false) }
    var showEndEditor by remember { mutableStateOf(false) }
    var showInactiveConfirm by remember { mutableStateOf(false) }
    var houseNo by remember { mutableStateOf("") }
    var street by remember { mutableStateOf("") }
    var village by remember { mutableStateOf("") }
    var mandal by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("") }
    var pincode by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var msg by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(id) { viewModel.loadDetail(id) }

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
        val planEnumKey = when {
            sub.planYears >= 6 -> sub.planYears
            sub.planMonths in listOf(6, 12, 30) -> sub.planMonths
            else -> 12
        }
        val plan = SubscriptionPlan.fromYears(planEnumKey)
        val isPending = sub.status == Subscription.Status.PENDING_PAYMENT ||
            sub.status == Subscription.Status.PAYMENT_FAILED

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .premiumCardSurface(shape = RoundedCornerShape(16.dp), elevation = 10.dp)
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                        sub.planDisplayName(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    DetailLine("Phone", sub.phone)

                    DetailLine(
                        "Starting month",
                        formatMonthYearFromStart(sub.startMonth)
                    )

                    DetailLineWithAction(
                        label = "End month",
                        value = formatMonthYearFromEnd(sub.endDate),
                        actionLabel = if (isAdmin) "Edit" else null,
                        onAction = if (isAdmin) {
                            { showEndEditor = true }
                        } else null
                    )

                    DetailLineWithAction(
                        label = "Address",
                        value = buildAddress(sub).ifBlank { "—" },
                        actionLabel = "Edit",
                        onAction = { showAddressEditor = true }
                    )

                    DetailLine("Price", "₹${"%,d".format(sub.displayPriceRupees())}")
                    DetailLine("Offers eligible", sub.offerEligibleLabel())

                    // High-visibility Sure / Not Sure control
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (sub.unsure) Color(0xFFFFE0B2) else LeafContainer)
                            .border(
                                1.5.dp,
                                if (sub.unsure) Color(0xFFE65100) else Leaf,
                                RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                if (sub.unsure) "Not Sure" else "Sure",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (sub.unsure) Color(0xFFBF360C) else Leaf
                            )
                            Text(
                                if (sub.unsure) "Needs a verification call"
                                else "Details look correct",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF5D4037)
                            )
                        }
                        Button(
                            onClick = {
                                UiSounds.click(context)
                                scope.launch {
                                    runCatching {
                                        app.repository.update(sub.copy(unsure = !sub.unsure))
                                        viewModel.loadDetail(sub.id)
                                        msg = if (!sub.unsure) "Marked Not Sure" else "Marked Sure"
                                    }.onFailure {
                                        msg = it.message ?: "Could not update"
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (sub.unsure) Leaf else Color(0xFFE65100),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                if (sub.unsure) "Mark Sure" else "Mark Not Sure",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            msg?.let {
                Text(
                    it,
                    color = AmberPending,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
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

            if (!sub.status.equals(Subscription.Status.EXPIRED, true)) {
                OutlinedButton(
                    onClick = { showInactiveConfirm = true },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SoftRed),
                    border = androidx.compose.foundation.BorderStroke(1.2.dp, SoftRed)
                ) {
                    Text("Mark inactive", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }

    val sub = item
    if (showAddressEditor && sub != null) {
        AddressEditDialog(
            houseNo = houseNo,
            street = street,
            village = village,
            mandal = mandal,
            district = district,
            pincode = pincode,
            state = state,
            onHouse = { houseNo = it },
            onStreet = { street = it },
            onVillage = { village = it },
            onMandal = { mandal = it },
            onDistrict = { district = it },
            onPincode = { pincode = it.filter(Char::isDigit).take(6) },
            onState = { state = it },
            onDismiss = { showAddressEditor = false },
            onSave = {
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
                        runCatching {
                            app.whatsAppClient.sendAddressChange(updated.phone, updated.name)
                        }
                        msg = "Address saved · WhatsApp notified"
                    } else {
                        msg = "Address saved"
                    }
                    viewModel.loadDetail(id)
                    showAddressEditor = false
                }
            }
        )
    }

    if (showEndEditor && sub != null && isAdmin) {
        EndMonthEditDialog(
            endDate = sub.endDate,
            onDismiss = { showEndEditor = false },
            onSave = { yearMonth ->
                scope.launch {
                    val endIso = yearMonth.atEndOfMonth().toString()
                    app.repository.update(sub.copy(endDate = endIso))
                    viewModel.loadDetail(id)
                    showEndEditor = false
                    msg = "End month updated to ${formatMonthYear(yearMonth)}"
                }
            }
        )
    }

    if (showInactiveConfirm && sub != null) {
        AlertDialog(
            onDismissRequest = { showInactiveConfirm = false },
            title = { Text("Mark inactive?") },
            text = { Text("This customer will appear under Expired.") },
            confirmButton = {
                TextButton(onClick = {
                    showInactiveConfirm = false
                    viewModel.markInactive(sub.id, onBack)
                }) { Text("Mark inactive", color = SoftRed) }
            },
            dismissButton = {
                TextButton(onClick = { showInactiveConfirm = false }) { Text("Cancel") }
            }
        )
    }
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

@Composable
private fun DetailLineWithAction(
    label: String,
    value: String,
    actionLabel: String?,
    onAction: (() -> Unit)?
) {
    Column(Modifier.padding(vertical = 2.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                label.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF9E9E9E),
                letterSpacing = 0.6.sp
            )
            if (actionLabel != null && onAction != null) {
                TextButton(onClick = onAction) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Marigold
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(actionLabel, color = Marigold, fontWeight = FontWeight.Bold)
                }
            }
        }
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun AddressEditDialog(
    houseNo: String,
    street: String,
    village: String,
    mandal: String,
    district: String,
    pincode: String,
    state: String,
    onHouse: (String) -> Unit,
    onStreet: (String) -> Unit,
    onVillage: (String) -> Unit,
    onMandal: (String) -> Unit,
    onDistrict: (String) -> Unit,
    onPincode: (String) -> Unit,
    onState: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit address", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(value = houseNo, onValueChange = onHouse, label = { Text("House") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp))
                OutlinedTextField(value = street, onValueChange = onStreet, label = { Text("Street") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp))
                OutlinedTextField(value = village, onValueChange = onVillage, label = { Text("Village/Town") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp))
                OutlinedTextField(value = mandal, onValueChange = onMandal, label = { Text("Mandal") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp))
                OutlinedTextField(value = district, onValueChange = onDistrict, label = { Text("District") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp))
                OutlinedTextField(value = pincode, onValueChange = onPincode, label = { Text("Pincode") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp))
                OutlinedTextField(value = state, onValueChange = onState, label = { Text("State") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp))
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
                colors = ButtonDefaults.buttonColors(containerColor = Marigold)
            ) { Text("Save", fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EndMonthEditDialog(
    endDate: String,
    onDismiss: () -> Unit,
    onSave: (YearMonth) -> Unit
) {
    val initial = remember(endDate) {
        runCatching { YearMonth.from(LocalDate.parse(endDate.take(10))) }.getOrNull()
            ?: YearMonth.now()
    }
    var month by remember { mutableStateOf(initial.monthValue) }
    var year by remember { mutableStateOf(initial.year) }
    var monthOpen by remember { mutableStateOf(false) }
    var yearOpen by remember { mutableStateOf(false) }
    val months = (1..12).toList()
    val years = ((YearMonth.now().year - 2)..(YearMonth.now().year + 15)).toList()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit end month", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Preview: ${formatMonthYear(YearMonth.of(year, month))}",
                    fontWeight = FontWeight.SemiBold,
                    color = TempleMaroon
                )
                ExposedDropdownMenuBox(expanded = monthOpen, onExpandedChange = { monthOpen = it }) {
                    OutlinedTextField(
                        value = java.time.Month.of(month).getDisplayName(TextStyle.FULL, Locale.ENGLISH),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Month") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = monthOpen) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    ExposedDropdownMenu(expanded = monthOpen, onDismissRequest = { monthOpen = false }) {
                        months.forEach { m ->
                            DropdownMenuItem(
                                text = {
                                    Text(java.time.Month.of(m).getDisplayName(TextStyle.FULL, Locale.ENGLISH))
                                },
                                onClick = {
                                    month = m
                                    monthOpen = false
                                }
                            )
                        }
                    }
                }
                ExposedDropdownMenuBox(expanded = yearOpen, onExpandedChange = { yearOpen = it }) {
                    OutlinedTextField(
                        value = year.toString(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Year") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = yearOpen) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    ExposedDropdownMenu(expanded = yearOpen, onDismissRequest = { yearOpen = false }) {
                        years.forEach { y ->
                            DropdownMenuItem(
                                text = { Text(y.toString()) },
                                onClick = {
                                    year = y
                                    yearOpen = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(YearMonth.of(year, month)) },
                colors = ButtonDefaults.buttonColors(containerColor = Marigold)
            ) { Text("Save", fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
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

private fun buildAddress(sub: Subscription): String =
    listOf(sub.houseNo, sub.street, sub.villageTown, sub.mandal, sub.district)
        .filter { it.isNotBlank() }
        .distinct()
        .joinToString(", ")
        .let { base ->
            val pinState = listOf(sub.pincode, sub.state).filter { it.isNotBlank() }.joinToString(", ")
            when {
                base.isBlank() -> pinState
                pinState.isBlank() -> base
                else -> "$base · $pinState"
            }
        }
