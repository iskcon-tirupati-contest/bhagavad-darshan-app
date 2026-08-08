package com.iskcon.bhagavaddarshan.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iskcon.bhagavaddarshan.data.SubscriptionPlan

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: SubscriptionViewModel,
    onDone: () -> Unit,
    onBack: () -> Unit
) {
    val form by viewModel.form.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.resetForm()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New registration") },
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
            Text("Devotee details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            OutlinedTextField(
                value = form.name,
                onValueChange = { viewModel.updateForm { f -> f.copy(name = it) } },
                label = { Text("Name *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = form.phone,
                onValueChange = { viewModel.updateForm { f -> f.copy(phone = it.filter(Char::isDigit).take(10)) } },
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
                onValueChange = { viewModel.updateForm { f -> f.copy(villageTown = it) } },
                label = { Text("Village / Town") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = form.mandal,
                onValueChange = { viewModel.updateForm { f -> f.copy(mandal = it) } },
                label = { Text("Mandal") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = form.district,
                onValueChange = { viewModel.updateForm { f -> f.copy(district = it) } },
                label = { Text("District / City") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = form.pincode,
                    onValueChange = { viewModel.updateForm { f -> f.copy(pincode = it.filter(Char::isDigit).take(6)) } },
                    label = { Text("Pincode") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = form.state,
                    onValueChange = { viewModel.updateForm { f -> f.copy(state = it) } },
                    label = { Text("State") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Spacer(Modifier.height(8.dp))
            Text("Subscription plan (flyer)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            SubscriptionPlan.entries.forEach { plan ->
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
                        Text("${plan.labelTe} — ₹${plan.totalRupees}")
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

            OutlinedTextField(
                value = form.paymentRef,
                onValueChange = { viewModel.updateForm { f -> f.copy(paymentRef = it) } },
                label = { Text("UPI / transaction ID") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = form.collectorName,
                onValueChange = { viewModel.updateForm { f -> f.copy(collectorName = it) } },
                label = { Text("Collector name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = form.notes,
                onValueChange = { viewModel.updateForm { f -> f.copy(notes = it) } },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth()
            )

            form.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }
            form.savedReceiptNo?.let {
                Text(
                    "Saved as receipt #$it",
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Button(
                onClick = { viewModel.saveRegistration { onDone() } },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text("Save registration")
            }
        }
    }
}
