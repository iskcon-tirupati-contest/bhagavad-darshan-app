package com.iskcon.bhagavaddarshan.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.iskcon.bhagavaddarshan.data.Subscription
import com.iskcon.bhagavaddarshan.payment.RazorpayPaymentApi
import com.iskcon.bhagavaddarshan.whatsapp.WhatsAppClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

enum class DebitClaim { NONE, DEBITED, NOT_DEBITED }

/**
 * Shown when Dynamic QR did not complete cleanly.
 * - Amount debited → Reconcile (Razorpay) and/or manual UPI proof photo
 * - Amount not debited → retry payment
 */
@Composable
fun PaymentIssueDialog(
    isAdmin: Boolean,
    devoteeName: String,
    phone: String,
    amountLabel: String,
    razorpayQrId: String?,
    paymentRefHint: String?,
    whatsApp: WhatsAppClient,
    onManualActivate: (paymentRef: String, proofPath: String) -> Unit,
    onRetryPayment: () -> Unit,
    onDismiss: () -> Unit,
    /** Optional existing pending row for reconcile mark-paid. */
    pendingSubId: Long? = null,
    markPaid: ((paymentId: String) -> Unit)? = null
) {
    var claim by remember { mutableStateOf(DebitClaim.NONE) }
    var txnId by remember { mutableStateOf("") }
    var proofPath by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val api = remember { RazorpayPaymentApi() }

    val photoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        runCatching {
            val dir = File(context.filesDir, "payment_proofs").also { it.mkdirs() }
            val out = File(dir, "upi_${System.currentTimeMillis()}.jpg")
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(out).use { output -> input.copyTo(output) }
            }
            proofPath = out.absolutePath
        }
    }

    fun reconcileNow() {
        busy = true
        status = null
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    if (!api.isConfigured()) {
                        error("Payment check runs via cloud API in store builds")
                    }
                    val qr = razorpayQrId?.trim().orEmpty()
                    val pay = paymentRefHint?.trim().orEmpty()
                    when {
                        pay.startsWith("pay_") -> {
                            val info = api.fetchPayment(pay).getOrThrow()
                            if (info.captured || info.status == "authorized" || info.status == "captured") {
                                "paid:${info.id}"
                            } else "unpaid"
                        }
                        qr.isNotBlank() || pay.startsWith("qr_") -> {
                            val id = qr.ifBlank { pay }
                            val payments = api.fetchQrPayments(id).getOrThrow()
                            val hit = payments.firstOrNull {
                                it.captured || it.status == "captured" || it.status == "authorized"
                            }
                            if (hit != null) "paid:${hit.id}" else "unpaid"
                        }
                        else -> "no_ref"
                    }
                }
            }
            busy = false
            val value = result.getOrNull()
            when {
                result.isSuccess && value?.startsWith("paid:") == true -> {
                    val payId = value.removePrefix("paid:")
                    status = "Razorpay confirmed paid"
                    markPaid?.invoke(payId)
                    onManualActivate(payId, "")
                }
                result.isSuccess && value == "unpaid" -> status = "Razorpay: not paid yet"
                result.isSuccess -> status = "No Razorpay reference to check"
                else -> status = "Reconcile error: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    Dialog(onDismissRequest = { if (!busy) onDismiss() }) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "Payment could not be completed",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Payment could not be completed due to technical reasons.\n$devoteeName · $phone · $amountLabel",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                )

                ClaimRadio(
                    selected = claim == DebitClaim.DEBITED,
                    title = "Amount debited",
                    subtitle = "Devotee says money left their account",
                    onClick = { claim = DebitClaim.DEBITED }
                )
                ClaimRadio(
                    selected = claim == DebitClaim.NOT_DEBITED,
                    title = "Amount not debited",
                    subtitle = "No money left the account · retry payment",
                    onClick = { claim = DebitClaim.NOT_DEBITED }
                )

                when (claim) {
                    DebitClaim.DEBITED -> {
                        if (isAdmin) {
                            Button(
                                onClick = { reconcileNow() },
                                enabled = !busy,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                if (busy) CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                                else Text("Reconcile now (check Razorpay)")
                            }
                        }
                        OutlinedTextField(
                            value = txnId,
                            onValueChange = { txnId = it },
                            label = { Text("UPI / transaction ID") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedButton(
                            onClick = { photoLauncher.launch("image/*") },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (proofPath.isBlank()) "Attach UPI screenshot" else "Screenshot attached ✓")
                        }
                        Button(
                            onClick = {
                                when {
                                    txnId.isBlank() && proofPath.isBlank() ->
                                        status = "Enter UPI ID or attach screenshot"
                                    else -> onManualActivate(
                                        txnId.ifBlank { "MANUAL_UPI_${System.currentTimeMillis()}" },
                                        proofPath
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Mark paid manually · Send confirmation")
                        }
                    }
                    DebitClaim.NOT_DEBITED -> {
                        Button(
                            onClick = onRetryPayment,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Try again · same subscription")
                        }
                    }
                    DebitClaim.NONE -> Unit
                }

                status?.let {
                    Text(it, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                }

                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text("Close")
                }
            }
        }
    }
}

@Composable
private fun ClaimRadio(
    selected: Boolean,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Column(Modifier.padding(start = 6.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f))
        }
    }
}
