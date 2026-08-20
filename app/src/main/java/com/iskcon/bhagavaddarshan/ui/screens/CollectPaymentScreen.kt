package com.iskcon.bhagavaddarshan.ui.screens

import android.graphics.Bitmap
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.iskcon.bhagavaddarshan.BhagavadDarshanApp
import com.iskcon.bhagavaddarshan.BuildConfig
import com.iskcon.bhagavaddarshan.data.Subscription
import com.iskcon.bhagavaddarshan.data.SubscriptionPlan
import com.iskcon.bhagavaddarshan.payment.AgentPaymentMethod
import com.iskcon.bhagavaddarshan.payment.BookRedeemCalculator
import com.iskcon.bhagavaddarshan.payment.RazorpayQrService
import com.iskcon.bhagavaddarshan.payment.UpiQrEncoder
import com.iskcon.bhagavaddarshan.ui.components.CompactTopBar
import com.iskcon.bhagavaddarshan.ui.components.ListBottomSafeGap
import com.iskcon.bhagavaddarshan.ui.components.PaymentIssueDialog
import com.iskcon.bhagavaddarshan.ui.components.PulsingDot
import com.iskcon.bhagavaddarshan.ui.theme.Leaf
import com.iskcon.bhagavaddarshan.ui.theme.LeafContainer
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

enum class QrPayMode { NEW_REGISTRATION, EXISTING_SUBSCRIPTION }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectPaymentScreen(
    viewModel: SubscriptionViewModel,
    method: AgentPaymentMethod,
    mode: QrPayMode,
    existingSubId: Long? = null,
    agentName: String,
    agentId: Long,
    registeredBy: String = agentName.ifBlank { "self" },
    isAdmin: Boolean = false,
    whatsAppClient: com.iskcon.bhagavaddarshan.whatsapp.WhatsAppClient? = null,
    onBack: () -> Unit,
    onPaid: (Long) -> Unit
) {
    val form by viewModel.form.collectAsState()
    val selected by viewModel.selected.collectAsState()

    val plan: SubscriptionPlan
    val devoteeName: String
    val phone: String
    when (mode) {
        QrPayMode.NEW_REGISTRATION -> {
            plan = form.plan
            devoteeName = form.name
            phone = form.phone
        }
        QrPayMode.EXISTING_SUBSCRIPTION -> {
            val sub = selected
            plan = SubscriptionPlan.fromYears(sub?.planYears ?: 1)
            devoteeName = sub?.name.orEmpty()
            phone = sub?.phone.orEmpty()
        }
    }

    val bookAmt = form.bookSaleAmount.toIntOrNull() ?: 0
    val bookMonths = BookRedeemCalculator.monthsForSale(bookAmt)

    fun activate(
        paymentRef: String,
        proofPath: String = "",
        bookAmount: Int = 0,
        months: Int? = null
    ) {
        when (mode) {
            QrPayMode.NEW_REGISTRATION -> viewModel.saveRegistrationAfterPayment(
                paymentId = paymentRef,
                collectorName = agentName,
                agentId = agentId,
                paymentMethod = method.routeKey,
                paymentProofPath = proofPath,
                bookSaleAmount = bookAmount,
                planMonthsOverride = months,
                source = if (method == AgentPaymentMethod.BOOK_REDEEM)
                    Subscription.Source.BOOK_REDEEM else Subscription.Source.COLLECTOR,
                registeredBy = registeredBy,
                onSuccess = { id -> onPaid(id) }
            )
            QrPayMode.EXISTING_SUBSCRIPTION -> existingSubId?.let {
                viewModel.markPaidWithRef(it, paymentRef, method.routeKey, proofPath) { onPaid(it) }
            } ?: onPaid(0L)
        }
    }

    when (method) {
        AgentPaymentMethod.CASH -> SimpleConfirmScaffold(
            title = "Cash by hand",
            icon = Icons.Default.AttachMoney,
            amountLabel = "₹${plan.totalRupees}",
            subtitle = "$devoteeName · $phone\n${plan.labelEn}",
            body = "Collect cash ₹${plan.totalRupees} from the devotee, then confirm.",
            confirmLabel = "Cash received · Activate",
            onBack = onBack,
            onConfirm = { activate("CASH_${System.currentTimeMillis()}") }
        )
        AgentPaymentMethod.BOOK_REDEEM -> SimpleConfirmScaffold(
            title = "Book sales redeem",
            icon = Icons.Default.MenuBook,
            amountLabel = "₹$bookAmt → $bookMonths month(s)",
            subtitle = "$devoteeName · $phone",
            body = "Books sold ₹$bookAmt. Magazine entitlement: $bookMonths month(s).",
            confirmLabel = "Confirm redeem · Activate",
            onBack = onBack,
            onConfirm = {
                activate(
                    paymentRef = "BOOK_REDEEM_$bookAmt",
                    bookAmount = bookAmt,
                    months = bookMonths
                )
            }
        )
        AgentPaymentMethod.DYNAMIC_QR -> DynamicQrContent(
            viewModel = viewModel,
            mode = mode,
            existingSubId = existingSubId,
            plan = plan,
            devoteeName = devoteeName,
            phone = phone,
            isAdmin = isAdmin,
            whatsAppClient = whatsAppClient,
            onBack = onBack,
            onActivate = { ref, proof -> activate(ref, proof) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SimpleConfirmScaffold(
    title: String,
    icon: ImageVector,
    amountLabel: String,
    subtitle: String,
    body: String,
    confirmLabel: String,
    onBack: () -> Unit,
    onConfirm: () -> Unit
) {
    Scaffold(
        topBar = {
            CompactTopBar(
                title = { Text(title, style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            }
            Text(amountLabel, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text(subtitle, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyLarge)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(body, textAlign = TextAlign.Center, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.height(4.dp))
            Button(
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp)
            ) { Text(confirmLabel, style = MaterialTheme.typography.titleMedium) }
            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) { Text("Cancel") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DynamicQrContent(
    viewModel: SubscriptionViewModel,
    mode: QrPayMode,
    existingSubId: Long?,
    plan: SubscriptionPlan,
    devoteeName: String,
    phone: String,
    isAdmin: Boolean,
    whatsAppClient: com.iskcon.bhagavaddarshan.whatsapp.WhatsAppClient?,
    onBack: () -> Unit,
    onActivate: (paymentRef: String, proofPath: String) -> Unit
) {
    val app = LocalContext.current.applicationContext as BhagavadDarshanApp
    val token = app.session.authToken
    val qrService = remember { RazorpayQrService() }
    var loading by remember { mutableStateOf(true) }
    var imageUrl by remember { mutableStateOf<String?>(null) }
    var localQr by remember { mutableStateOf<Bitmap?>(null) }
    var qrId by remember { mutableStateOf<String?>(null) }
    var statusText by remember { mutableStateOf("Preparing QR…") }
    var paid by remember { mutableStateOf(false) }
    var pollEnabled by remember { mutableStateOf(false) }
    var showIssue by remember { mutableStateOf(false) }
    var retryKey by remember { mutableStateOf(0) }

    fun startQr() {
        loading = true
        imageUrl = null
        localQr = null
        qrId = null
        paid = false
        pollEnabled = false
        statusText = "Preparing QR…"
    }

    LaunchedEffect(plan, devoteeName, retryKey) {
        if (mode == QrPayMode.EXISTING_SUBSCRIPTION && existingSubId != null) {
            viewModel.loadDetail(existingSubId)
        }
        startQr()
        val description = "Bhagavad Darshan ${plan.years} yr · $devoteeName"
        try {
            val createdViaApi = if (token.isNotBlank()) {
                app.api.createStaffQr(
                    token,
                    plan.totalPaise,
                    description,
                    devoteeName,
                    phone
                ).getOrNull()
            } else null
            if (createdViaApi != null) {
                qrId = createdViaApi.optString("qrId").ifBlank { createdViaApi.optString("id") }
                imageUrl = createdViaApi.optString("imageUrl")
                    .ifBlank { createdViaApi.optString("image_url") }
                    .ifBlank { null }
                val qrString = createdViaApi.optString("qrString")
                    .ifBlank { createdViaApi.optString("upiUri") }
                if (imageUrl == null && qrString.isNotBlank()) {
                    localQr = UpiQrEncoder.encodeBitmap(qrString)
                }
                statusText = "Waiting for ₹${plan.totalRupees}…"
                pollEnabled = !qrId.isNullOrBlank()
            } else if (qrService.isConfigured()) {
                try {
                    val created = qrService.createFixedAmountQr(
                        plan.totalPaise, description, devoteeName, phone
                    )
                    qrId = created.id
                    imageUrl = created.imageUrl
                    statusText = "Waiting for ₹${plan.totalRupees}…"
                    pollEnabled = true
                } catch (e: Exception) {
                    localQr = UpiQrEncoder.encodeBitmap(
                        "BHAGAVAD-DARSHAN|AMT=${plan.totalRupees}|$devoteeName"
                    )
                    statusText = "Demo amount QR (Razorpay QR pending). Confirm after pay."
                }
            } else {
                val vpa = BuildConfig.UPI_VPA.trim()
                localQr = if (vpa.isNotBlank()) {
                    UpiQrEncoder.encodeBitmap(
                        UpiQrEncoder.buildUpiPayUri(vpa, "Bhagavad Darshan", plan.totalRupees, description)
                    )
                } else {
                    UpiQrEncoder.encodeBitmap("BHAGAVAD-DARSHAN|AMT=${plan.totalRupees}|$devoteeName")
                }
                statusText = "Show QR for ₹${plan.totalRupees}. Confirm after payment."
            }
        } finally {
            loading = false
        }
    }

    LaunchedEffect(pollEnabled, qrId) {
        val id = qrId ?: return@LaunchedEffect
        if (!pollEnabled) return@LaunchedEffect
        while (isActive && pollEnabled && !paid) {
            delay(3000)
            try {
                if (token.isNotBlank()) {
                    val st = app.api.staffQrStatus(token, id).getOrNull() ?: continue
                    val isPaid = st.optBoolean("paid") ||
                        st.optString("status").equals("paid", ignoreCase = true) ||
                        st.optString("qrStatus").equals("paid", ignoreCase = true)
                    if (isPaid) {
                        val paymentId = st.optString("paymentId")
                            .ifBlank { st.optString("payment_id") }
                            .ifBlank { id }
                        paid = true
                        pollEnabled = false
                        onActivate(paymentId, "")
                    }
                } else {
                    val st = qrService.fetchStatus(id)
                    if (st.paid) {
                        paid = true
                        pollEnabled = false
                        onActivate(st.paymentId ?: id, "")
                    }
                }
            } catch (_: Exception) { }
        }
    }

    DisposableEffect(Unit) { onDispose { pollEnabled = false } }

    Scaffold(
        topBar = {
            CompactTopBar(
                title = { Text("Dynamic amount QR", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = { pollEnabled = false; onBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
                .padding(bottom = ListBottomSafeGap),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("₹${plan.totalRupees}", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text("$devoteeName · $phone\n${plan.labelEn}", textAlign = TextAlign.Center)

            if (paid) {
                PaidCheckmark()
            } else {
                Box(
                    Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White)
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        loading -> Box(Modifier.size(260.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                        imageUrl != null -> AsyncImage(model = imageUrl, contentDescription = null, modifier = Modifier.size(260.dp))
                        localQr != null -> Image(bitmap = localQr!!.asImageBitmap(), contentDescription = null, modifier = Modifier.size(260.dp))
                        else -> Icon(Icons.Default.QrCode2, contentDescription = null, modifier = Modifier.size(120.dp))
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!paid && pollEnabled) {
                    PulsingDot(color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                }
                Text(statusText, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium)
            }

            if (!paid && !loading) {
                Button(
                    onClick = {
                        pollEnabled = false
                        onActivate("DYNAMIC_CONFIRM_${System.currentTimeMillis()}", "")
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(16.dp)
                ) { Text("Payment received · Activate", style = MaterialTheme.typography.titleMedium) }

                OutlinedButton(
                    onClick = {
                        pollEnabled = false
                        showIssue = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Payment could not be completed")
                }
            }
        }
    }

    if (showIssue && whatsAppClient != null) {
        PaymentIssueDialog(
            isAdmin = isAdmin,
            devoteeName = devoteeName,
            phone = phone,
            amountLabel = "₹${plan.totalRupees}",
            razorpayQrId = qrId,
            paymentRefHint = qrId,
            whatsApp = whatsAppClient,
            pendingSubId = existingSubId,
            markPaid = existingSubId?.let { id ->
                { paymentId ->
                    viewModel.markPaidWithRef(id, paymentId, "razorpay_qr", "") {}
                }
            },
            onManualActivate = { ref, proof ->
                showIssue = false
                onActivate(ref, proof)
            },
            onRetryPayment = {
                showIssue = false
                retryKey += 1
            },
            onDismiss = { showIssue = false }
        )
    } else if (showIssue) {
        PaymentIssueDialog(
            isAdmin = isAdmin,
            devoteeName = devoteeName,
            phone = phone,
            amountLabel = "₹${plan.totalRupees}",
            razorpayQrId = qrId,
            paymentRefHint = qrId,
            whatsApp = com.iskcon.bhagavaddarshan.whatsapp.WhatsAppClient(),
            onManualActivate = { ref, proof ->
                showIssue = false
                onActivate(ref, proof)
            },
            onRetryPayment = {
                showIssue = false
                retryKey += 1
            },
            onDismiss = { showIssue = false }
        )
    }
}

/** Small animated checkmark shown once a dynamic-QR payment is detected as paid. */
@Composable
private fun PaidCheckmark() {
    val transition = rememberInfiniteTransition(label = "paidPulse")
    val ring by transition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(900), repeatMode = RepeatMode.Reverse),
        label = "ringScale"
    )
    Box(
        Modifier.size(180.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier
                .size(180.dp)
                .graphicsLayer(scaleX = ring, scaleY = ring)
                .clip(CircleShape)
                .background(LeafContainer)
        )
        Box(
            Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(Leaf),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Check, contentDescription = "Paid", tint = Color.White, modifier = Modifier.size(48.dp))
        }
    }
}

@Composable
fun PaymentMethodPicker(
    amountRupees: Int,
    selected: AgentPaymentMethod,
    onSelect: (AgentPaymentMethod) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Payment method", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        AgentPaymentMethod.entries.forEach { method ->
            val isSelected = selected == method
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(selected = isSelected, onClick = { onSelect(method) }, role = Role.RadioButton),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 3.dp else 0.dp)
            ) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            methodIcon(method),
                            contentDescription = null,
                            tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column(Modifier.padding(start = 10.dp).weight(1f)) {
                        Text(method.label, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            when (method) {
                                AgentPaymentMethod.DYNAMIC_QR -> "PhonePe / GPay / Paytm · exact ₹$amountRupees"
                                AgentPaymentMethod.CASH -> "Collect cash ₹$amountRupees"
                                AgentPaymentMethod.BOOK_REDEEM -> "₹1000 books = 1 month magazine"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    RadioButton(
                        selected = isSelected,
                        onClick = { onSelect(method) },
                        colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }
    }
}

private fun methodIcon(method: AgentPaymentMethod): ImageVector = when (method) {
    AgentPaymentMethod.DYNAMIC_QR -> Icons.Default.QrCode2
    AgentPaymentMethod.CASH -> Icons.Default.AttachMoney
    AgentPaymentMethod.BOOK_REDEEM -> Icons.Default.MenuBook
}
