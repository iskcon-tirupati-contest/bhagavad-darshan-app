package com.iskcon.bhagavaddarshan.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iskcon.bhagavaddarshan.data.IndianStates
import com.iskcon.bhagavaddarshan.data.SubscriptionPlan
import com.iskcon.bhagavaddarshan.payment.AgentPaymentMethod
import com.iskcon.bhagavaddarshan.payment.BookRedeemCalculator
import com.iskcon.bhagavaddarshan.ui.components.AccentSectionTitle
import com.iskcon.bhagavaddarshan.ui.components.CompactTopBar
import com.iskcon.bhagavaddarshan.ui.components.FigmaRegistrationStepper
import com.iskcon.bhagavaddarshan.ui.components.MetaChip
import com.iskcon.bhagavaddarshan.ui.theme.Marigold
import com.iskcon.bhagavaddarshan.ui.theme.MarigoldContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: SubscriptionViewModel,
    agentName: String,
    onDone: () -> Unit,
    onBack: () -> Unit,
    onCollectPayment: (AgentPaymentMethod) -> Unit = {}
) {
    val form by viewModel.form.collectAsState()
    var paymentMethod by remember { mutableStateOf(AgentPaymentMethod.DYNAMIC_QR) }
    var stateMenuOpen by remember { mutableStateOf(false) }
    var step by remember { mutableStateOf(0) } // 0 details, 1 plan, 2 payment

    LaunchedEffect(Unit) {
        viewModel.resetForm()
    }

    val bookAmt = form.bookSaleAmount.toIntOrNull() ?: 0
    val bookMonths = BookRedeemCalculator.monthsForSale(bookAmt)
    val houseStreet = listOf(form.houseNo, form.street)
        .filter { it.isNotBlank() }
        .joinToString(", ")

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            CompactTopBar(
                title = {
                    Text(
                        "New registration",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (step > 0) step-- else onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            if (step == 1) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, Color(0xFFE8E0D6))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "TOTAL AMOUNT",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF9E9E9E),
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "₹${form.plan.totalRupees}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Marigold
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = { step = 2 },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Marigold)
                    ) { Text("Next", fontWeight = FontWeight.Bold) }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            FigmaRegistrationStepper(step = step)

            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MarigoldContainer)
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Text(
                    "Agent: $agentName",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Marigold
                )
            }

            when (step) {
                0 -> {
                    AccentSectionTitle("Devotee Details")
                    OutlinedTextField(
                        value = form.name,
                        onValueChange = { viewModel.updateForm { f -> f.copy(name = it, error = null) } },
                        label = { Text("Full Name *") },
                        leadingIcon = { Icon(Icons.Default.Person, null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = form.phone,
                        onValueChange = {
                            viewModel.updateForm { f ->
                                f.copy(phone = it.filter(Char::isDigit).take(10), error = null)
                            }
                        },
                        label = { Text("Phone *") },
                        leadingIcon = { Icon(Icons.Default.Phone, null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = houseStreet,
                        onValueChange = { raw ->
                            val parts = raw.split(",", limit = 2)
                            viewModel.updateForm { f ->
                                f.copy(
                                    houseNo = parts[0].trim(),
                                    street = parts.getOrElse(1) { "" }.trim()
                                )
                            }
                        },
                        label = { Text("House No. & Street") },
                        leadingIcon = { Icon(Icons.Default.Home, null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = form.villageTown,
                        onValueChange = {
                            viewModel.updateForm { f -> f.copy(villageTown = it, error = null) }
                        },
                        label = { Text("Village/Town *") },
                        leadingIcon = { Icon(Icons.Default.LocationCity, null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = form.district,
                            onValueChange = {
                                viewModel.updateForm { f -> f.copy(district = it, error = null) }
                            },
                            label = { Text("District") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = form.pincode,
                            onValueChange = {
                                viewModel.updateForm { f ->
                                    f.copy(pincode = it.filter(Char::isDigit).take(6), error = null)
                                }
                            },
                            label = { Text("Pincode") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                    ExposedDropdownMenuBox(
                        expanded = stateMenuOpen,
                        onExpandedChange = { stateMenuOpen = it }
                    ) {
                        OutlinedTextField(
                            value = form.state.ifBlank { IndianStates.DEFAULT },
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("State *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = stateMenuOpen) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = stateMenuOpen,
                            onDismissRequest = { stateMenuOpen = false }
                        ) {
                            IndianStates.ALL.forEach { state ->
                                DropdownMenuItem(
                                    text = { Text(state) },
                                    onClick = {
                                        viewModel.updateForm { f -> f.copy(state = state) }
                                        stateMenuOpen = false
                                    }
                                )
                            }
                        }
                    }
                    form.error?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                    Button(
                        onClick = {
                            when {
                                form.name.isBlank() ->
                                    viewModel.updateForm { it.copy(error = "Name required") }
                                form.phone.length != 10 ->
                                    viewModel.updateForm { it.copy(error = "Valid phone required") }
                                form.villageTown.isBlank() ->
                                    viewModel.updateForm { it.copy(error = "Village / town required") }
                                else -> {
                                    viewModel.updateForm { it.copy(error = null) }
                                    step = 1
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Marigold)
                    ) { Text("Next", fontWeight = FontWeight.Bold) }
                }

                1 -> {
                    AccentSectionTitle("Choose Plan")
                    SubscriptionPlan.entries.filter { it.years != 4 }.forEach { plan ->
                        val selected = form.plan == plan
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (selected) MarigoldContainer else Color.White)
                                .border(
                                    width = if (selected) 2.dp else 1.dp,
                                    color = if (selected) Marigold else Color(0xFFE0D5C8),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .selectable(
                                    selected = selected,
                                    onClick = { viewModel.updateForm { f -> f.copy(plan = plan) } },
                                    role = Role.RadioButton
                                )
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selected,
                                onClick = { viewModel.updateForm { f -> f.copy(plan = plan) } },
                                colors = RadioButtonDefaults.colors(selectedColor = Marigold)
                            )
                            Column(Modifier.padding(start = 4.dp).weight(1f)) {
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(plan.labelEn, fontWeight = FontWeight.Bold)
                                    Text(
                                        "₹${plan.totalRupees}",
                                        fontWeight = FontWeight.Bold,
                                        color = if (selected) Marigold else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(
                                    "Magazine ₹${plan.magazineRupees} + postage ₹${plan.postageRupees}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF9E9E9E)
                                )
                                Spacer(Modifier.height(6.dp))
                                MetaChip(
                                    text = "${plan.giftBooks} gift book${if (plan.giftBooks == 1) "" else "s"}"
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(72.dp))
                }

                else -> {
                    AccentSectionTitle("Payment Method")
                    AgentPaymentMethod.entries.forEach { method ->
                        val selected = paymentMethod == method
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (selected) MarigoldContainer else Color.White)
                                .border(
                                    width = if (selected) 2.dp else 1.dp,
                                    color = if (selected) Marigold else Color(0xFFE0D5C8),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .selectable(
                                    selected = selected,
                                    onClick = { paymentMethod = method },
                                    role = Role.RadioButton
                                )
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (selected) Marigold else Color(0xFFF0EBE4)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    paymentMethodIcon(method),
                                    contentDescription = null,
                                    tint = if (selected) Color.White else Color(0xFF6D6D6D),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column(Modifier.padding(start = 12.dp).weight(1f)) {
                                Text(
                                    paymentMethodTitle(method),
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    paymentMethodSubtitle(
                                        method,
                                        if (method == AgentPaymentMethod.BOOK_REDEEM) bookAmt
                                        else form.plan.totalRupees
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF9E9E9E)
                                )
                            }
                            RadioButton(
                                selected = selected,
                                onClick = { paymentMethod = method },
                                colors = RadioButtonDefaults.colors(selectedColor = Marigold)
                            )
                        }
                    }
                    if (paymentMethod == AgentPaymentMethod.BOOK_REDEEM) {
                        OutlinedTextField(
                            value = form.bookSaleAmount,
                            onValueChange = {
                                viewModel.updateForm { f ->
                                    f.copy(
                                        bookSaleAmount = it.filter(Char::isDigit).take(7),
                                        error = null
                                    )
                                }
                            },
                            label = { Text("Book sale amount (₹) *") },
                            supportingText = {
                                Text(
                                    if (bookMonths > 0)
                                        "Eligible: $bookMonths month(s) free magazine"
                                    else
                                        "₹1000 = 1 month"
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                    form.error?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                    Button(
                        onClick = {
                            val err = viewModel.validateRegistrationForm(
                                forBookRedeem = paymentMethod == AgentPaymentMethod.BOOK_REDEEM
                            )
                            if (err != null) {
                                viewModel.updateForm { it.copy(error = err) }
                            } else {
                                onCollectPayment(paymentMethod)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Marigold)
                    ) {
                        Text(
                            when (paymentMethod) {
                                AgentPaymentMethod.DYNAMIC_QR ->
                                    "Pay ₹${form.plan.totalRupees} via Razorpay UPI →"
                                AgentPaymentMethod.CASH ->
                                    "Collect ₹${form.plan.totalRupees} Cash by Hand →"
                                AgentPaymentMethod.BOOK_REDEEM ->
                                    "Redeem $bookMonths month(s) via Books →"
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
                    TextButton(
                        onClick = onBack,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancel", color = Color(0xFF9E9E9E))
                    }
                }
            }

            if (step < 2) {
                TextButton(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancel", color = Color(0xFF9E9E9E))
                }
            }
        }
    }
}

private fun paymentMethodTitle(method: AgentPaymentMethod): String = when (method) {
    AgentPaymentMethod.DYNAMIC_QR -> "Razorpay UPI QR"
    AgentPaymentMethod.CASH -> "Cash by Hand"
    AgentPaymentMethod.BOOK_REDEEM -> "Redeem by Books"
}

private fun paymentMethodSubtitle(method: AgentPaymentMethod, amountRupees: Int): String = when (method) {
    AgentPaymentMethod.DYNAMIC_QR -> "PhonePe / GPay / Paytm · exact ₹$amountRupees"
    AgentPaymentMethod.CASH -> "Collect cash ₹$amountRupees"
    AgentPaymentMethod.BOOK_REDEEM -> "₹1000 books = 1 month magazine"
}

private fun paymentMethodIcon(method: AgentPaymentMethod): ImageVector = when (method) {
    AgentPaymentMethod.DYNAMIC_QR -> Icons.Default.QrCode2
    AgentPaymentMethod.CASH -> Icons.Default.AttachMoney
    AgentPaymentMethod.BOOK_REDEEM -> Icons.Default.MenuBook
}
