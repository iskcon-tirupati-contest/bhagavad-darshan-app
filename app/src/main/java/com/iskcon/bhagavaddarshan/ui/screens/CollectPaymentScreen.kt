package com.iskcon.bhagavaddarshan.ui.screens

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.iskcon.bhagavaddarshan.BuildConfig
import com.iskcon.bhagavaddarshan.R
import com.iskcon.bhagavaddarshan.data.Subscription
import com.iskcon.bhagavaddarshan.data.SubscriptionPlan
import com.iskcon.bhagavaddarshan.payment.AgentPaymentMethod
import com.iskcon.bhagavaddarshan.payment.BookRedeemCalculator
import com.iskcon.bhagavaddarshan.payment.RazorpayQrService
import com.iskcon.bhagavaddarshan.payment.UpiQrEncoder
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.io.File
import java.io.FileOutputStream

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
            amountLabel = "₹${plan.totalRupees}",
            subtitle = "$devoteeName · $phone\n${plan.labelEn}",
            body = "Collect cash ₹${plan.totalRupees} from the devotee, then confirm.",
            confirmLabel = "Cash received · Activate",
            onBack = onBack,
            onConfirm = { activate("CASH_${System.currentTimeMillis()}") }
        )
        AgentPaymentMethod.BOOK_REDEEM -> SimpleConfirmScaffold(
            title = "Book sales redeem",
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
        AgentPaymentMethod.TEMPLE_QR -> TempleStaticFlow(
            plan = plan,
            devoteeName = devoteeName,
            phone = phone,
            onBack = onBack,
            onActivate = { txn, proof -> activate(txn, proof) }
        )
        AgentPaymentMethod.DYNAMIC_QR -> DynamicQrContent(
            viewModel = viewModel,
            mode = mode,
            existingSubId = existingSubId,
            plan = plan,
            devoteeName = devoteeName,
            phone = phone,
            onBack = onBack,
            onActivate = { ref -> activate(ref) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SimpleConfirmScaffold(
    title: String,
    amountLabel: String,
    subtitle: String,
    body: String,
    confirmLabel: String,
    onBack: () -> Unit,
    onConfirm: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(amountLabel, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text(subtitle, textAlign = TextAlign.Center)
            Text(body, textAlign = TextAlign.Center)
            Button(onClick = onConfirm, modifier = Modifier.fillMaxWidth()) { Text(confirmLabel) }
            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Cancel") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TempleStaticFlow(
    plan: SubscriptionPlan,
    devoteeName: String,
    phone: String,
    onBack: () -> Unit,
    onActivate: (txnId: String, proofPath: String) -> Unit
) {
    var step by remember { mutableStateOf(0) } // 0 = show QR, 1 = proof
    var txnId by remember { mutableStateOf("") }
    var proofPath by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    val photoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        try {
            val dir = File(context.filesDir, "proofs").apply { mkdirs() }
            val out = File(dir, "temple_${System.currentTimeMillis()}.jpg")
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(out).use { output -> input.copyTo(output) }
            }
            proofPath = out.absolutePath
            error = null
        } catch (e: Exception) {
            error = "Could not save photo: ${e.message}"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (step == 0) "Temple static QR" else "Payment proof") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (step == 1) step = 0 else onBack()
                    }) {
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
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (step == 0) {
                Text("Ask devotee to pay", style = MaterialTheme.typography.titleMedium)
                Text("₹${plan.totalRupees}", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text("$devoteeName · $phone\n${plan.labelEn}", textAlign = TextAlign.Center)
                Image(
                    painter = painterResource(R.drawable.temple_static_qr),
                    contentDescription = "Temple QR",
                    modifier = Modifier.size(280.dp),
                    contentScale = ContentScale.Fit
                )
                Text("(Dummy QR — replace with original temple QR later)", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f))
                Button(onClick = { step = 1 }, modifier = Modifier.fillMaxWidth()) {
                    Text("Amount received · Enter UPI proof")
                }
            } else {
                Text("Enter UPI / transaction ID (text) or attach a screenshot.", textAlign = TextAlign.Center)
                OutlinedTextField(
                    value = txnId,
                    onValueChange = { txnId = it; error = null },
                    label = { Text("UPI / Transaction ID") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedButton(onClick = { photoLauncher.launch("image/*") }, modifier = Modifier.fillMaxWidth()) {
                    Text(if (proofPath.isBlank()) "Attach payment screenshot" else "Screenshot attached ✓")
                }
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                Button(
                    onClick = {
                        when {
                            txnId.isBlank() && proofPath.isBlank() ->
                                error = "Enter transaction ID or attach a photo"
                            else -> onActivate(
                                txnId.ifBlank { "TEMPLE_PHOTO_${System.currentTimeMillis()}" },
                                proofPath
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Confirm · Activate subscription")
                }
            }
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
    onBack: () -> Unit,
    onActivate: (String) -> Unit
) {
    val qrService = remember { RazorpayQrService() }
    var loading by remember { mutableStateOf(true) }
    var imageUrl by remember { mutableStateOf<String?>(null) }
    var localQr by remember { mutableStateOf<Bitmap?>(null) }
    var qrId by remember { mutableStateOf<String?>(null) }
    var statusText by remember { mutableStateOf("Preparing QR…") }
    var paid by remember { mutableStateOf(false) }
    var pollEnabled by remember { mutableStateOf(false) }

    LaunchedEffect(plan, devoteeName) {
        if (mode == QrPayMode.EXISTING_SUBSCRIPTION && existingSubId != null) {
            viewModel.loadDetail(existingSubId)
        }
        loading = true
        val description = "Bhagavad Darshan ${plan.years} yr · $devoteeName"
        try {
            if (qrService.isConfigured()) {
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
                val st = qrService.fetchStatus(id)
                if (st.paid) {
                    paid = true
                    pollEnabled = false
                    onActivate(st.paymentId ?: id)
                }
            } catch (_: Exception) { }
        }
    }

    DisposableEffect(Unit) { onDispose { pollEnabled = false } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dynamic amount QR") },
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
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("₹${plan.totalRupees}", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text("$devoteeName · $phone\n${plan.labelEn}", textAlign = TextAlign.Center)
            when {
                loading -> CircularProgressIndicator()
                imageUrl != null -> AsyncImage(model = imageUrl, contentDescription = null, modifier = Modifier.size(280.dp))
                localQr != null -> Image(bitmap = localQr!!.asImageBitmap(), contentDescription = null, modifier = Modifier.size(280.dp))
            }
            Text(statusText, textAlign = TextAlign.Center)
            if (!paid && !loading) {
                Button(
                    onClick = {
                        pollEnabled = false
                        onActivate("DYNAMIC_CONFIRM_${System.currentTimeMillis()}")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Payment received · Activate") }
            }
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
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(selected = selected == method, onClick = { onSelect(method) }, role = Role.RadioButton),
                colors = CardDefaults.cardColors(
                    containerColor = if (selected == method) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surface
                )
            ) {
                Column(Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = selected == method, onClick = null)
                        Text(method.label, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 4.dp))
                    }
                    Text(
                        when (method) {
                            AgentPaymentMethod.DYNAMIC_QR -> "QR for exact ₹$amountRupees"
                            AgentPaymentMethod.TEMPLE_QR -> "Temple QR · then enter UPI proof"
                            AgentPaymentMethod.CASH -> "Collect cash ₹$amountRupees"
                            AgentPaymentMethod.BOOK_REDEEM -> "₹1000 books = 1 month magazine"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}
