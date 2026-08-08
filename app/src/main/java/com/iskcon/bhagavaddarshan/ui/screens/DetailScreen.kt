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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iskcon.bhagavaddarshan.BhagavadDarshanApp
import com.iskcon.bhagavaddarshan.data.Subscription
import com.iskcon.bhagavaddarshan.data.SubscriptionPlan
import kotlinx.coroutines.launch

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
        val plan = SubscriptionPlan.fromYears(sub.planYears)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                "Receipt #${sub.receiptNo}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            DetailLine("Phone", sub.phone)
            DetailLine("Registered by", sub.registeredBy.ifBlank { "self" })
            DetailLine("Address", buildAddress(sub))
            DetailLine(
                "Plan",
                "${sub.planYears} year(s) · ₹${sub.totalAmount} (mag ₹${sub.magazineAmount} + post ₹${sub.postageAmount})"
            )
            DetailLine("Gift books", sub.giftBooks.toString())
            DetailLine("Start month", sub.startMonth)
            DetailLine("Ends", sub.endDate)
            DetailLine("Status", sub.status)
            DetailLine("Payment ref", sub.paymentRef.ifBlank { "—" })
            DetailLine("Collector", sub.collectorName.ifBlank { "—" })
            if (sub.notes.isNotBlank()) DetailLine("Notes", sub.notes)

            msg?.let { Text(it, color = MaterialTheme.colorScheme.primary) }

            Spacer(Modifier.height(8.dp))
            if (!editingAddress) {
                OutlinedButton(
                    onClick = { editingAddress = true },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Change address") }
            } else {
                OutlinedTextField(value = houseNo, onValueChange = { houseNo = it }, label = { Text("House") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = street, onValueChange = { street = it }, label = { Text("Street") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = village, onValueChange = { village = it }, label = { Text("Village/Town") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = mandal, onValueChange = { mandal = it }, label = { Text("Mandal") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = district, onValueChange = { district = it }, label = { Text("District") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = pincode, onValueChange = { pincode = it.filter(Char::isDigit).take(6) }, label = { Text("Pincode") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = state, onValueChange = { state = it }, label = { Text("State") }, modifier = Modifier.fillMaxWidth())
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
                            app.whatsAppClient.sendAddressChange(updated.phone, updated.name)
                            viewModel.loadDetail(id)
                            editingAddress = false
                            msg = "Address saved · WhatsApp notified"
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Save address") }
            }

            Spacer(Modifier.height(16.dp))
            if (sub.status == Subscription.Status.PENDING_PAYMENT && razorpayReady) {
                Button(
                    onClick = { onPayWithRazorpay(sub.id, plan, sub.name, sub.phone) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Show payment QR · ₹${plan.totalRupees}")
                }
                Spacer(Modifier.height(8.dp))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (sub.status == Subscription.Status.PENDING_PAYMENT ||
                    sub.status == Subscription.Status.PAYMENT_FAILED
                ) {
                    OutlinedButton(
                        onClick = { viewModel.markActive(sub.id) },
                        modifier = Modifier.weight(1f)
                    ) { Text("Mark paid") }
                }
                OutlinedButton(
                    onClick = { viewModel.delete(sub.id, onBack) },
                    modifier = Modifier.weight(1f)
                ) { Text("Delete") }
            }
        }
    }
}

@Composable
private fun DetailLine(label: String, value: String) {
    Column(Modifier.padding(vertical = 2.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}

private fun buildAddress(sub: Subscription): String =
    listOf(sub.houseNo, sub.street, sub.villageTown, sub.mandal, sub.district, sub.pincode, sub.state)
        .filter { it.isNotBlank() }
        .joinToString(", ")
