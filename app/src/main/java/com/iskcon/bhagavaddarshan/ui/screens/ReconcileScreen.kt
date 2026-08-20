package com.iskcon.bhagavaddarshan.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iskcon.bhagavaddarshan.BhagavadDarshanApp
import com.iskcon.bhagavaddarshan.data.Subscription
import com.iskcon.bhagavaddarshan.payment.RazorpayPaymentApi
import com.iskcon.bhagavaddarshan.ui.components.CompactTopBar
import com.iskcon.bhagavaddarshan.ui.components.ListBottomSafeGap
import com.iskcon.bhagavaddarshan.ui.components.MetaChip
import com.iskcon.bhagavaddarshan.ui.components.StaggeredEntrance
import com.iskcon.bhagavaddarshan.ui.theme.AmberPending
import com.iskcon.bhagavaddarshan.ui.theme.AmberPendingContainer
import com.iskcon.bhagavaddarshan.ui.theme.Leaf
import com.iskcon.bhagavaddarshan.ui.theme.Marigold
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
    var waBusyId by remember { mutableStateOf<Long?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val api = remember { RazorpayPaymentApi() }

    fun reconcileOne(sub: Subscription) {
        busyId = sub.id
        message = null
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                try {
                    if (!api.isConfigured()) {
                        error("Use cloud reconcile — client Razorpay secret is not shipped in store builds")
                    }
                    suspend fun maybeWaFailed() {
                        if (app.whatsAppClient.isConfigured()) {
                            app.whatsAppClient.sendPaymentFailed(
                                sub.phone, sub.name, "₹${sub.totalAmount}"
                            )
                        }
                    }
                    val paymentId = sub.paymentRef.trim()
                    val qrId = sub.razorpayQrId.trim()
                    val outcome = when {
                        paymentId.startsWith("pay_") -> {
                            val info = api.fetchPayment(paymentId).getOrThrow()
                            if (info.captured || info.status == "authorized") {
                                app.repository.markPaid(sub.id, info.id, sub.paymentMethod.ifBlank { "razorpay" })
                                "paid"
                            } else {
                                app.repository.update(sub.copy(status = Subscription.Status.PAYMENT_FAILED))
                                maybeWaFailed()
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
                                maybeWaFailed()
                                "failed"
                            }
                        }
                        else -> {
                            app.repository.update(sub.copy(status = Subscription.Status.PAYMENT_FAILED))
                            maybeWaFailed()
                            "no_ref"
                        }
                    }
                    Result.success(outcome)
                } catch (e: Throwable) {
                    Result.failure(e)
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

    fun whatsAppOne(sub: Subscription) {
        waBusyId = sub.id
        message = null
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    if (!app.whatsAppClient.isConfigured()) {
                        error("WhatsApp runs on the server in store builds")
                    }
                    app.whatsAppClient.sendPaymentFailed(
                        sub.phone, sub.name, "₹${sub.totalAmount}"
                    ).getOrThrow()
                }
            }
            waBusyId = null
            message = result.fold(
                onSuccess = { "WhatsApp sent: ${sub.name}" },
                onFailure = { "WhatsApp failed ${sub.name}: ${it.message}" }
            )
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            CompactTopBar(
                title = {
                    Text(
                        if (isAdmin) "Reconcile · ${pending.size}" else "My pending · ${pending.size}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = AmberPendingContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    "Checks Razorpay for each failed/pending order. If paid → activate. " +
                        "If unpaid → WhatsApp the devotee.",
                    modifier = Modifier.padding(14.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = AmberPending,
                    fontWeight = FontWeight.Medium
                )
            }

            if (pending.isNotEmpty()) {
                Button(
                    onClick = { pending.forEach { reconcileOne(it) } },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Marigold)
                ) {
                    Text("Reconcile all (${pending.size})", fontWeight = FontWeight.Bold)
                }
            }

            AnimatedVisibility(
                visible = message != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Text(
                    message.orEmpty(),
                    color = Leaf,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (pending.isEmpty()) {
                Card(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFE8E0D6), RoundedCornerShape(12.dp))
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Leaf,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "All Settled",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "No pending payments to reconcile.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF9E9E9E)
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        bottom = ListBottomSafeGap
                    )
                ) {
                    itemsIndexed(pending, key = { _, it -> it.id }) { index, sub ->
                        StaggeredEntrance(index = index) {
                            Card(
                                Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                Column(
                                    Modifier
                                        .border(1.dp, Color(0xFFE8E0D6), RoundedCornerShape(12.dp))
                                        .padding(14.dp)
                                ) {
                                    Row(
                                        Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            sub.name,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium,
                                            modifier = Modifier.weight(1f)
                                        )
                                        MetaChip(
                                            text = "Pending",
                                            container = AmberPendingContainer,
                                            content = AmberPending
                                        )
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        "Receipt #${sub.receiptNo} · ₹${sub.totalAmount}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFF6D6D6D)
                                    )
                                    Spacer(Modifier.height(12.dp))
                                    Row(
                                        Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Button(
                                            onClick = { reconcileOne(sub) },
                                            enabled = busyId != sub.id,
                                            modifier = Modifier.weight(1f).height(44.dp),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Marigold)
                                        ) {
                                            if (busyId == sub.id) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(16.dp),
                                                    strokeWidth = 2.dp,
                                                    color = Color.White
                                                )
                                                Spacer(Modifier.width(8.dp))
                                            }
                                            Text(
                                                if (busyId == sub.id) "Checking…" else "Check Razorpay",
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                        OutlinedButton(
                                            onClick = { whatsAppOne(sub) },
                                            enabled = waBusyId != sub.id,
                                            modifier = Modifier.weight(1f).height(44.dp),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Marigold),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Marigold)
                                        ) {
                                            Text(
                                                if (waBusyId == sub.id) "Sending…" else "WhatsApp",
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
