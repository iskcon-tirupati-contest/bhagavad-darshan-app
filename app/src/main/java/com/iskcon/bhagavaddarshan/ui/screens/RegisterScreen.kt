package com.iskcon.bhagavaddarshan.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iskcon.bhagavaddarshan.data.IndianStates
import com.iskcon.bhagavaddarshan.data.SubscriptionPlan
import com.iskcon.bhagavaddarshan.payment.AgentPaymentMethod
import com.iskcon.bhagavaddarshan.payment.BookRedeemCalculator
import com.iskcon.bhagavaddarshan.ui.components.CompactTopBar

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

    LaunchedEffect(Unit) {
        viewModel.resetForm()
    }

    val bookAmt = form.bookSaleAmount.toIntOrNull() ?: 0
    val bookMonths = BookRedeemCalculator.monthsForSale(bookAmt)

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            CompactTopBar(
                title = {
                    Text("New registration", style = MaterialTheme.typography.titleMedium)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "Agent: $agentName",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
            )
            Text("Devotee details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            OutlinedTextField(
                value = form.name,
                onValueChange = { viewModel.updateForm { f -> f.copy(name = it, error = null) } },
                label = { Text("Name *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = form.phone,
                onValueChange = {
                    viewModel.updateForm { f ->
                        f.copy(phone = it.filter(Char::isDigit).take(10), error = null)
                    }
                },
                label = { Text("Phone *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = form.houseNo,
                    onValueChange = { viewModel.updateForm { f -> f.copy(houseNo = it) } },
                    label = { Text("House no.") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = form.street,
                    onValueChange = { viewModel.updateForm { f -> f.copy(street = it) } },
                    label = { Text("Street") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }
            OutlinedTextField(
                value = form.villageTown,
                onValueChange = { viewModel.updateForm { f -> f.copy(villageTown = it, error = null) } },
                label = { Text("Village / Town *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = form.district,
                onValueChange = { viewModel.updateForm { f -> f.copy(district = it, error = null) } },
                label = { Text("District / City") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = form.pincode,
                onValueChange = {
                    viewModel.updateForm { f ->
                        f.copy(pincode = it.filter(Char::isDigit).take(6), error = null)
                    }
                },
                label = { Text("Pincode") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
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
                    singleLine = true
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

            if (paymentMethod != AgentPaymentMethod.BOOK_REDEEM) {
                Spacer(Modifier.height(8.dp))
                Text("Subscription plan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                SubscriptionPlan.entries.filter { it.years != 4 }.forEach { plan ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = form.plan == plan,
                                onClick = { viewModel.updateForm { f -> f.copy(plan = plan) } },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = form.plan == plan,
                            onClick = { viewModel.updateForm { f -> f.copy(plan = plan) } }
                        )
                        Column(Modifier.padding(start = 8.dp)) {
                            Text("${plan.labelEn} — ₹${plan.totalRupees}")
                            Text(
                                "Magazine ₹${plan.magazineRupees} + postage ₹${plan.postageRupees} · ${plan.giftBooks} book gift(s)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                            )
                        }
                    }
                }
                Text(
                    "Total due: ₹${form.plan.totalRupees}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.height(8.dp))
            PaymentMethodPicker(
                amountRupees = if (paymentMethod == AgentPaymentMethod.BOOK_REDEEM) bookAmt
                else form.plan.totalRupees,
                selected = paymentMethod,
                onSelect = { paymentMethod = it }
            )

            if (paymentMethod == AgentPaymentMethod.BOOK_REDEEM) {
                OutlinedTextField(
                    value = form.bookSaleAmount,
                    onValueChange = {
                        viewModel.updateForm { f ->
                            f.copy(bookSaleAmount = it.filter(Char::isDigit).take(7), error = null)
                        }
                    },
                    label = { Text("Book sale amount (₹) *") },
                    supportingText = {
                        Text(
                            if (bookMonths > 0)
                                "Eligible: $bookMonths month(s) free magazine (₹1000 = 1 month)"
                            else
                                "Enter ₹1000 or more · ₹1000=1mo, ₹2000=2mo, ₹3000=3mo…"
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Text(
                    when (paymentMethod) {
                        AgentPaymentMethod.DYNAMIC_QR ->
                            "Continue · Dynamic QR ₹${form.plan.totalRupees}"
                        AgentPaymentMethod.TEMPLE_QR ->
                            "Continue · Temple QR ₹${form.plan.totalRupees}"
                        AgentPaymentMethod.CASH ->
                            "Continue · Cash ₹${form.plan.totalRupees}"
                        AgentPaymentMethod.BOOK_REDEEM ->
                            "Continue · Redeem $bookMonths month(s)"
                    }
                )
            }
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Text("Cancel")
            }
        }
    }
}
