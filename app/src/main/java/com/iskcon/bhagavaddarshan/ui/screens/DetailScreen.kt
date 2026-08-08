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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iskcon.bhagavaddarshan.data.Subscription

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    id: Long,
    viewModel: SubscriptionViewModel,
    onBack: () -> Unit
) {
    val item by viewModel.selected.collectAsState()

    LaunchedEffect(id) {
        viewModel.loadDetail(id)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(item?.name ?: "Subscription") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        val sub = item
        if (sub == null) {
            Text("Loading…", modifier = Modifier.padding(padding).padding(16.dp))
            return@Scaffold
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("Receipt #${sub.receiptNo}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            DetailLine("Phone", sub.phone)
            DetailLine("Address", buildAddress(sub))
            DetailLine("Plan", "${sub.planYears} year(s) · ₹${sub.totalAmount} (mag ₹${sub.magazineAmount} + post ₹${sub.postageAmount})")
            DetailLine("Gift books", sub.giftBooks.toString())
            DetailLine("Start month", sub.startMonth)
            DetailLine("Ends", sub.endDate)
            DetailLine("Status", sub.status)
            DetailLine("Payment ref", sub.paymentRef.ifBlank { "—" })
            DetailLine("Collector", sub.collectorName.ifBlank { "—" })
            if (sub.notes.isNotBlank()) DetailLine("Notes", sub.notes)

            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (sub.status == Subscription.Status.PENDING_PAYMENT) {
                    Button(
                        onClick = { viewModel.markActive(sub.id) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Mark paid / active")
                    }
                }
                OutlinedButton(
                    onClick = { viewModel.delete(sub.id, onBack) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Delete")
                }
            }
        }
    }
}

@Composable
private fun DetailLine(label: String, value: String) {
    Column(Modifier.padding(vertical = 4.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}

private fun buildAddress(sub: Subscription): String =
    listOf(
        sub.houseNo,
        sub.street,
        sub.villageTown,
        sub.mandal,
        sub.district,
        sub.pincode,
        sub.state
    ).filter { it.isNotBlank() }.joinToString(", ").ifBlank { "—" }
