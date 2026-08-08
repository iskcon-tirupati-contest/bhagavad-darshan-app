package com.iskcon.bhagavaddarshan.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.iskcon.bhagavaddarshan.payment.RazorpayPaymentApi
import com.iskcon.bhagavaddarshan.ui.components.CompactTopBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReconcileScreen(
    app: BhagavadDarshanApp,
    viewModel: SubscriptionViewModel,
    isAdmin: Boolean
) {
    val pending by viewModel.pendingScoped.collectAsState()
    var busyId by remember { mutableStateOf<Long?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val api = remember { RazorpayPaymentApi() }

    fun reconcileOne(sub: Subscription) {
        busyId = sub.id
        message = null
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    if (!api.isConfigured()) error("Razorpay keys missing")
                    val paymentId = sub.paymentRef.trim()
                    val qrId = sub.razorpayQrId.trim()
                    when {
                        paymentId.startsWith("pay_") -> {
                            val info = api.fetchPayment(paymentId).getOrThrow()
                            if (info.captured || info.status == "authorized") {
                                app.repository.markPaid(sub.id, info.id, sub.paymentMethod.ifBlank { "razorpay" })
                                "paid"
                            } else {
                                app.repository.update(sub.copy(status = Subscription.Status.PAYMENT_FAILED))
                                app.whatsAppClient.sendPaymentFailed(
                                    sub.phone, sub.name, "₹${sub.totalAmount}"
                                )
                                "failed"
                            }
                        }
                        qrId.isNotBlank() || paymentId.startsWith("qr_") -> {
                            val id = qrId.ifBlank { paymentId }
                            val payments = api.fetchQrPayments(id).getOrThrow()
                            val captured = payments.firstOrNull {
                                it.captured || it.status == "authorized" || it.status == "captured"
                            }
                            if (captured != null) {
                                app.repository.markPaid(
                                    sub.id,
                                    captured.id,
                                    sub.paymentMethod.ifBlank { "razorpay_qr" }
                                )
                                "paid"
                            } else {
                                app.repository.update(sub.copy(status = Subscription.Status.PAYMENT_FAILED))
                                app.whatsAppClient.sendPaymentFailed(
                                    sub.phone, sub.name, "₹${sub.totalAmount}"
                                )
                                "failed"
                            }
                        }
                        else -> {
                            app.repository.update(sub.copy(status = Subscription.Status.PAYMENT_FAILED))
                            app.whatsAppClient.sendPaymentFailed(
                                sub.phone, sub.name, "₹${sub.totalAmount}"
                            )
                            "no_ref"
                        }
                    }
                }
            }
            busyId = null
            message = result.fold(
                onSuccess = {
                    when (it) {
                        "paid" -> "Marked paid & activated: ${sub.name}"
                        "failed" -> "Still unpaid — WhatsApp sent: ${sub.name}"
                        else -> "No payment ref — WhatsApp reminder sent: ${sub.name}"
                    }
                },
                onFailure = { "Error ${sub.name}: ${it.message}" }
            )
            viewModel.refreshPending()
        }
    }

    Scaffold(
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        topBar = {
            CompactTopBar(
                title = {
                    Text(
                        if (isAdmin) "Reconcile · ${pending.size}" else "My pending · ${pending.size}",
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshPending() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Checks Razorpay for each failed/pending order. If paid → activate. " +
                    "If unpaid → WhatsApp the devotee.",
                style = MaterialTheme.typography.bodyMedium
            )
            if (pending.isNotEmpty()) {
                Button(
                    onClick = { pending.forEach { reconcileOne(it) } },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Reconcile all (${pending.size})")
                }
            }
            message?.let {
                Text(it, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(pending, key = { it.id }) { sub ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(14.dp)) {
                            Text(sub.name, fontWeight = FontWeight.Bold)
                            Text("${sub.phone} · ₹${sub.totalAmount} · ${sub.status}")
                            Text(
                                "Ref: ${sub.paymentRef.ifBlank { "—" }} · QR: ${sub.razorpayQrId.ifBlank { "—" }}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text("By: ${sub.registeredBy.ifBlank { "self" }}")
                            Button(
                                onClick = { reconcileOne(sub) },
                                enabled = busyId != sub.id
                            ) {
                                Text(if (busyId == sub.id) "Checking…" else "Check Razorpay")
                            }
                        }
                    }
                }
            }
            if (pending.isEmpty()) {
                Text("No pending or failed payments.", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}
