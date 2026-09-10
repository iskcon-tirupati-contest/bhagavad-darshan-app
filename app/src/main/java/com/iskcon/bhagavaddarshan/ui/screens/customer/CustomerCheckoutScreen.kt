package com.iskcon.bhagavaddarshan.ui.screens.customer

import android.app.Activity
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
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iskcon.bhagavaddarshan.BhagavadDarshanApp
import com.iskcon.bhagavaddarshan.BuildConfig
import com.iskcon.bhagavaddarshan.data.SubscriptionPlan
import com.iskcon.bhagavaddarshan.network.BdApi
import com.iskcon.bhagavaddarshan.payment.CheckoutResult
import com.iskcon.bhagavaddarshan.payment.CheckoutResultBus
import com.iskcon.bhagavaddarshan.ui.components.AnimatedCard
import com.iskcon.bhagavaddarshan.ui.components.CompactTopBar
import com.iskcon.bhagavaddarshan.ui.components.PaymentMethodRow
import com.iskcon.bhagavaddarshan.ui.components.RadiantGoldButton
import com.iskcon.bhagavaddarshan.ui.components.customerFieldColors
import com.iskcon.bhagavaddarshan.ui.screens.friendlyError
import com.iskcon.bhagavaddarshan.ui.theme.EditChocolate
import com.iskcon.bhagavaddarshan.ui.theme.EditCream
import com.iskcon.bhagavaddarshan.ui.theme.EditMuted
import com.iskcon.bhagavaddarshan.ui.theme.EditPopularBg
import com.iskcon.bhagavaddarshan.ui.theme.EditPopularBorder
import com.iskcon.bhagavaddarshan.ui.theme.EditTerracotta
import com.iskcon.bhagavaddarshan.ui.theme.Inter
import com.iskcon.bhagavaddarshan.ui.theme.PlayfairDisplay
import com.iskcon.bhagavaddarshan.ui.theme.TulsiGreen
import com.iskcon.bhagavaddarshan.ui.theme.UxCream
import com.iskcon.bhagavaddarshan.ui.theme.UxGold200
import com.iskcon.bhagavaddarshan.ui.theme.UxGold400
import com.iskcon.bhagavaddarshan.ui.theme.UxGold500
import com.iskcon.bhagavaddarshan.ui.theme.UxGold600
import com.iskcon.bhagavaddarshan.ui.theme.UxInk
import com.iskcon.bhagavaddarshan.ui.theme.UxSaffron
import com.razorpay.Checkout
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerCheckoutScreen(
    app: BhagavadDarshanApp,
    language: CustomerLanguage = CustomerLanguage.ENGLISH,
    planYears: Int,
    shippingPaise: Int = 0,
    @Suppress("UNUSED_PARAMETER") paymentReturnTick: Int,
    onBack: () -> Unit,
    onPaidSuccess: (detail: String) -> Unit,
    sevaKind: String? = null,
    sevaTitle: String? = null,
    sevaAmountPaise: Int? = null
) {
    val isSeva = !sevaKind.isNullOrBlank()
    val plan = remember(planYears) { SubscriptionPlan.fromYears(planYears) }
    val expressFeeRupees = (shippingPaise / 100).coerceAtLeast(0)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val api = remember { BdApi() }
    val token = app.session.authToken

    val displayTitle = when {
        isSeva -> sevaTitle?.ifBlank { "Gita Daan" } ?: "Gita Daan"
        else -> "${plan.months}${tr(language, " Month Subscription", " నెలల సభ్యత్వం")}"
    }
    val displayRupees = when {
        isSeva -> (sevaAmountPaise ?: CustomerCatalog.GITA_DAAN_RUPEES * 100) / 100
        else -> plan.totalRupees + expressFeeRupees
    }
    val amountLabel = if (BuildConfig.TEST_PAYMENTS) "₹1 (test)" else "₹$displayRupees"

    var preparing by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var orderId by remember { mutableStateOf<String?>(null) }
    var keyId by remember { mutableStateOf<String?>(null) }
    var amountPaise by remember { mutableStateOf(0) }
    var subscriptionId by remember { mutableStateOf(app.session.subscriptionId) }
    var busy by remember { mutableStateOf(false) }
    var paid by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf("Preparing secure checkout…") }
    var showComplaint by remember { mutableStateOf(false) }
    var complaintText by remember { mutableStateOf("") }
    var complaintDone by remember { mutableStateOf(false) }
    var pendingUpiPaymentId by remember { mutableStateOf<String?>(null) }
    var awaitingUpi by remember { mutableStateOf(false) }
    var customerName by remember { mutableStateOf(app.session.agentName.ifBlank { "Devotee" }) }
    var deliveryAddress by remember { mutableStateOf("") }

    LaunchedEffect(token) {
        if (token.isBlank()) return@LaunchedEffect
        api.getMe(token).onSuccess { json ->
            val c = json.optJSONObject("customer") ?: return@onSuccess
            customerName = c.optString("name").ifBlank { app.session.agentName }.ifBlank { "Devotee" }
            deliveryAddress = listOf(
                listOf(c.optString("house_no"), c.optString("street")).filter { it.isNotBlank() }.joinToString(", "),
                c.optString("village_town"),
                listOf(c.optString("district"), c.optString("state"), c.optString("pincode"))
                    .filter { it.isNotBlank() }
                    .joinToString(", ")
            ).filter { it.isNotBlank() }.joinToString("\n")
        }
    }

    LaunchedEffect(planYears, shippingPaise, token, sevaKind, sevaAmountPaise) {
        preparing = true
        error = null
        if (token.isBlank()) {
            error = "Please login again"
            preparing = false
            return@LaunchedEffect
        }
        val result = if (isSeva) {
            api.createPaymentOrder(
                token = token,
                planYears = 1,
                purpose = sevaKind ?: "donation",
                amountPaise = sevaAmountPaise ?: (CustomerCatalog.GITA_DAAN_RUPEES * 100),
                notes = displayTitle
            )
        } else {
            api.createPaymentOrder(
                token = token,
                planYears = planYears,
                subscriptionId = subscriptionId.takeIf { it > 0L },
                shippingPaise = shippingPaise
            )
        }
        result.onSuccess { json ->
            orderId = json.optString("orderId")
            keyId = json.optString("keyId")
            amountPaise = json.optInt("amountPaise")
            subscriptionId = json.optLong("subscriptionId")
            if (subscriptionId > 0L) app.session.bindSubscription(subscriptionId)
            statusText = tr(language, "Open secure Razorpay checkout", "సురక్షిత Razorpay చెల్లింపును తెరవండి")
        }.onFailure {
            error = friendlyError(it)
        }
        preparing = false
    }

    DisposableEffect(Unit) { onDispose { } }

    fun checkoutOptions(): JSONObject = JSONObject().apply {
        put("name", "ISKCON Tirupati")
        put("description", if (isSeva) displayTitle else "Bhagavad Darshan · ${plan.labelEn}")
        put("currency", "INR")
        put("amount", amountPaise)
        put("order_id", orderId)
        put("method", "upi")
        put("theme", JSONObject().put("color", "#D36B1D"))
        put("retry", JSONObject().put("enabled", true).put("max_count", 1))
        put(
            "prefill",
            JSONObject()
                .put("contact", app.session.agentPhone)
                .put("name", app.session.agentName.ifBlank { "Devotee" })
        )
    }

    fun successDetail(json: JSONObject): String {
        val sub = json.optJSONObject("subscription")
        if (isSeva) {
            val kind = when (sevaKind?.lowercase()) {
                "book" -> "book"
                "donation" -> "donation"
                else -> "donation"
            }
            return JSONObject()
                .put("kind", kind)
                .put("reference", orderId.orEmpty().ifBlank { "BD-SEVA" })
                .put("itemTitle", displayTitle)
                .put("amountRupees", displayRupees)
                .put("customerName", customerName)
                .put("address", deliveryAddress)
                .put("planLabel", "$displayTitle · ₹$displayRupees")
                .put("endDate", "")
                .toString()
        }
        val receipt = sub?.optLong("receipt_no") ?: 0L
        val years = plan.years
        val segmentEnd = sub?.optString("end_date").orEmpty()
        // Full coverage through farthest end (includes carry-over from earlier plans).
        val coverageEnd = json.optString("coverageEndDate").ifBlank { segmentEnd }
        val previousEnd = json.optString("previousEndDate")
        val carryOverDays = json.optInt("carryOverDays", 0)
        val start = sub?.optString("start_date").orEmpty().ifBlank {
            sub?.optString("start_month").orEmpty()
        }
        val year = java.time.LocalDate.now().year
        val reference = if (receipt > 0L) "BD-$year-$receipt" else orderId.orEmpty()
        return JSONObject()
            .put("kind", "subscription")
            .put("reference", reference)
            .put("planLabel", "${plan.months} Month Plan")
            .put("planYears", years)
            .put("amountRupees", displayRupees)
            .put("shippingPaise", shippingPaise)
            .put("startDate", start)
            .put("endDate", coverageEnd)
            .put("segmentEndDate", segmentEnd)
            .put("previousEndDate", previousEnd)
            .put("carryOverDays", carryOverDays)
            .put("customerName", customerName)
            .put("address", deliveryAddress)
            .toString()
    }

    fun openRazorpay() {
        val activity = context as? Activity ?: run {
            error = "Checkout needs an activity context"
            return
        }
        if (orderId.isNullOrBlank()) {
            error = "Order not ready"
            return
        }
        val key = keyId ?: run {
            error = "Payment key missing"
            return
        }
        busy = true
        error = null
        showComplaint = false
        awaitingUpi = false
        pendingUpiPaymentId = null
        statusText = "Opening Razorpay…"
        runCatching {
            Checkout().apply { setKeyID(key) }.open(activity, checkoutOptions())
        }.onFailure {
            busy = false
            error = friendlyError(it)
            statusText = "Could not open payment"
        }
    }

    LaunchedEffect(awaitingUpi, paymentReturnTick) {
        val order = orderId ?: return@LaunchedEffect
        if (!awaitingUpi || paid) return@LaunchedEffect
        repeat(48) {
            if (!isActive || paid || !awaitingUpi) return@LaunchedEffect
            delay(2500)
            val st = api.orderPaymentStatus(token, order).getOrNull() ?: return@repeat
            if (st.optBoolean("paid")) {
                val payId = st.optString("paymentId")
                statusText = "Verifying payment…"
                val confirm = api.confirmPayment(
                    token = token,
                    orderId = order,
                    paymentId = payId
                )
                awaitingUpi = false
                busy = false
                confirm.onSuccess { json ->
                    paid = true
                    onPaidSuccess(successDetail(json))
                }.onFailure {
                    error = friendlyError(it)
                    showComplaint = true
                    statusText = "Payment could not be verified"
                }
                return@LaunchedEffect
            }
            val status = st.optString("status")
            if (status.equals("failed", true) || status.equals("expired", true)) {
                awaitingUpi = false
                busy = false
                statusText = "Payment not completed"
                error = "Payment was not completed"
                showComplaint = true
                return@LaunchedEffect
            }
        }
        if (!paid && awaitingUpi) {
            awaitingUpi = false
            busy = false
            statusText = "Waiting for payment"
            error = "Come back after paying, or tap the UPI app again."
            showComplaint = true
        }
    }

    LaunchedEffect(Unit) {
        CheckoutResultBus.results.collect { result ->
            when (result) {
                is CheckoutResult.Success -> {
                    statusText = "Verifying payment…"
                    val confirm = api.confirmPayment(
                        token = token,
                        orderId = orderId.orEmpty(),
                        paymentId = result.paymentId
                    )
                    busy = false
                    confirm.onSuccess { json ->
                        paid = true
                        onPaidSuccess(successDetail(json))
                    }.onFailure {
                        error = friendlyError(it)
                        showComplaint = true
                        statusText = "Payment could not be verified"
                    }
                }
                is CheckoutResult.Failure -> {
                    busy = false
                    awaitingUpi = false
                    statusText = "Payment not completed"
                    error = tr(language, "Payment was not completed. Open Razorpay again to retry.", "చెల్లింపు పూర్తి కాలేదు. మళ్లీ Razorpay తెరిచి ప్రయత్నించండి.")
                    showComplaint = true
                    scope.launch {
                        api.failPayment(
                            token = token,
                            orderId = orderId.orEmpty(),
                            message = result.message
                        )
                    }
                }
            }
        }
    }

    Scaffold(
        containerColor = UxCream,
        contentWindowInsets = WindowInsets.navigationBars,
        topBar = {
            CompactTopBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = EditChocolate)
                    }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                    containerColor = EditCream,
                    titleContentColor = EditChocolate,
                    navigationIconContentColor = EditChocolate
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(UxCream)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column {
                Text(
                    "CONFIRM & PAY",
                    fontFamily = Inter,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.6.sp,
                    color = EditTerracotta
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = if (isSeva) {
                        buildAnnotatedString { append(displayTitle) }
                    } else {
                        buildAnnotatedString {
                            withStyle(SpanStyle(fontSize = 38.sp, fontWeight = FontWeight.Bold)) {
                                append(plan.months.toString())
                            }
                            withStyle(SpanStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold)) {
                                append(tr(language, " Month plan", " నెలల ప్లాన్"))
                            }
                        }
                    },
                    fontFamily = PlayfairDisplay,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = EditChocolate,
                    lineHeight = 34.sp
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(EditPopularBg)
                    .border(1.5.dp, EditPopularBorder, RoundedCornerShape(22.dp))
                    .padding(20.dp)
            ) {
                Text(
                    text = if (isSeva) {
                        buildAnnotatedString { append(tr(language, "Donation summary", "దాన వివరాలు")) }
                    } else {
                        buildAnnotatedString {
                            withStyle(SpanStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold)) {
                                append(plan.months.toString())
                            }
                            withStyle(SpanStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold)) {
                                append(tr(language, " Month Magazine Plan", " నెలల మ్యాగజైన్ ప్లాన్"))
                            }
                        }
                    },
                    fontFamily = PlayfairDisplay,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = EditChocolate
                )
                Text(
                    if (isSeva) tr(language, "ISKCON Tirupati seva", "ఇస్కాన్ తిరుపతి సేవ") else tr(language, "Bhagavad Darshan · monthly at your door", "భగవద్ దర్శన్ · ప్రతి నెల మీ ఇంటి వద్దకు"),
                    fontFamily = Inter,
                    fontSize = 16.sp,
                    color = EditMuted
                )
                Spacer(Modifier.height(16.dp))
                if (!isSeva) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            tr(language, "Plan", "ప్లాన్"),
                            color = EditMuted,
                            fontFamily = Inter,
                            fontSize = 16.sp
                        )
                        Text(
                            "₹${plan.totalRupees}",
                            fontWeight = FontWeight.Bold,
                            color = EditChocolate,
                            fontFamily = Inter,
                            fontSize = 16.sp
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            if (expressFeeRupees > 0)
                                tr(language, "Express shipping", "ఎక్స్‌ప్రెస్ షిప్పింగ్")
                            else
                                tr(language, "Standard shipping", "స్టాండర్డ్ షిప్పింగ్"),
                            color = EditMuted,
                            fontFamily = Inter,
                            fontSize = 16.sp
                        )
                        Text(
                            if (expressFeeRupees > 0) "₹$expressFeeRupees" else "INCLUDED",
                            fontWeight = FontWeight.Bold,
                            color = if (expressFeeRupees > 0) EditChocolate else TulsiGreen,
                            fontFamily = Inter,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                }
                Text(
                    amountLabel,
                    fontFamily = PlayfairDisplay,
                    fontWeight = FontWeight.Bold,
                    fontSize = 36.sp,
                    color = EditChocolate
                )
            }

            Text(
                tr(language, "Pay securely", "సురక్షితంగా చెల్లించండి"),
                fontFamily = PlayfairDisplay,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = EditChocolate
            )
            Text(
                statusText,
                fontFamily = Inter,
                fontSize = 15.sp,
                color = EditMuted
            )

            if (preparing) {
                CircularProgressIndicator(color = UxSaffron, modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                error?.let {
                    Text(it, color = UxSaffron, textAlign = TextAlign.Center, fontFamily = Inter)
                }

                val canPay = !busy && !paid && orderId != null
                PaymentMethodRow(
                    title = "Razorpay",
                    subtitle = tr(language, "Open secure checkout", "సురక్షిత చెల్లింపును తెరవండి"),
                    icon = Icons.Default.AccountBalanceWallet,
                    iconTint = UxGold600,
                    enabled = canPay,
                    onClick = { openRazorpay() }
                )

                if (busy) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp).align(Alignment.CenterHorizontally),
                        strokeWidth = 2.dp,
                        color = UxSaffron
                    )
                }

                if (showComplaint && !complaintDone) {
                    AnimatedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Payment issue? Raise a complaint", fontFamily = PlayfairDisplay, fontWeight = FontWeight.Bold, color = UxInk)
                            OutlinedTextField(
                                value = complaintText,
                                onValueChange = { complaintText = it },
                                label = { Text("Describe the issue") },
                                modifier = Modifier.fillMaxWidth().height(120.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = customerFieldColors()
                            )
                            RadiantGoldButton(
                                text = "Submit complaint",
                                onClick = {
                                    if (complaintText.trim().length < 10) {
                                        error = "Please write at least 10 characters"
                                        return@RadiantGoldButton
                                    }
                                    scope.launch {
                                        api.createComplaint(
                                            token = token,
                                            message = complaintText.trim(),
                                            category = "payment",
                                            transactionRef = orderId.orEmpty()
                                        ).onSuccess {
                                            complaintDone = true
                                            error = null
                                            statusText = "Complaint submitted. We will help you soon."
                                        }.onFailure {
                                            error = friendlyError(it)
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
                if (complaintDone) {
                    Text("Complaint submitted.", color = UxGold500, fontFamily = Inter)
                }

                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = UxGold500,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.size(6.dp))
                        Text(
                            tr(language, "SECURE PAYMENT", "సురక్షిత చెల్లింపు"),
                            fontFamily = Inter,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.6.sp,
                            color = UxGold500
                        )
                    }
                    Text(
                        "By paying, you support the spiritual outreach of ISKCON Tirupati Temple.",
                        fontFamily = Inter,
                        fontSize = 13.sp,
                        color = EditMuted,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
