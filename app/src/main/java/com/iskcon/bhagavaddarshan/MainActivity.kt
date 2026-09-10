package com.iskcon.bhagavaddarshan

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.iskcon.bhagavaddarshan.payment.AgentPaymentMethod
import com.iskcon.bhagavaddarshan.payment.CheckoutResult
import com.iskcon.bhagavaddarshan.payment.CheckoutResultBus
import com.iskcon.bhagavaddarshan.ui.components.AppUpdateGate
import com.iskcon.bhagavaddarshan.ui.navigation.AppRoute
import com.iskcon.bhagavaddarshan.ui.screens.CollectPaymentScreen
import com.iskcon.bhagavaddarshan.ui.screens.DetailScreen
import com.iskcon.bhagavaddarshan.ui.screens.LoginScreen
import com.iskcon.bhagavaddarshan.ui.screens.MainShell
import com.iskcon.bhagavaddarshan.ui.screens.QrPayMode
import com.iskcon.bhagavaddarshan.ui.screens.RegisterScreen
import com.iskcon.bhagavaddarshan.ui.screens.SubscriptionViewModel
import com.iskcon.bhagavaddarshan.ui.screens.SubscriptionViewModelFactory
import com.iskcon.bhagavaddarshan.ui.screens.admin.AdminAgentEditScreen
import com.iskcon.bhagavaddarshan.ui.screens.customer.CustomerCheckoutScreen
import com.iskcon.bhagavaddarshan.ui.screens.customer.CustomerShippingScreen
import com.iskcon.bhagavaddarshan.ui.screens.customer.CustomerLanguage
import com.iskcon.bhagavaddarshan.ui.screens.customer.CustomerCompleteProfileScreen
import com.iskcon.bhagavaddarshan.ui.screens.customer.CustomerRegisterScreen
import com.iskcon.bhagavaddarshan.ui.screens.agent.AddDevoteeScreen
import com.iskcon.bhagavaddarshan.ui.screens.customer.PaymentSuccessScreen
import com.iskcon.bhagavaddarshan.ui.navigation.CustomerTab
import com.iskcon.bhagavaddarshan.ui.theme.BhagavadDarshanTheme
import com.iskcon.bhagavaddarshan.util.UiSounds
import com.razorpay.Checkout
import com.razorpay.PaymentData
import com.razorpay.PaymentResultWithDataListener
import kotlinx.coroutines.launch
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class MainActivity : ComponentActivity(), PaymentResultWithDataListener {

    private var paymentReturnHandler: (() -> Unit)? = null
    private var skipResumeNotify = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = androidx.activity.SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = androidx.activity.SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )
        val app = application as BhagavadDarshanApp
        Checkout.preload(applicationContext)
        UiSounds.warmUp(applicationContext)

        setContent {
            BhagavadDarshanTheme {
                val navController = rememberNavController()
                var loggedIn by remember { mutableStateOf(app.session.isLoggedIn) }
                val vm: SubscriptionViewModel = viewModel(
                    factory = SubscriptionViewModelFactory(app.repository)
                )
                val scope = rememberCoroutineScope()
                var paymentReturnTick by remember { mutableIntStateOf(0) }

                paymentReturnHandler = {
                    paymentReturnTick++
                }
                LaunchedEffect(Unit) {
                    if (intent?.data?.host == "payment") {
                        paymentReturnTick++
                    }
                }

                LaunchedEffect(loggedIn, app.session.agentId, app.session.isAdmin, app.session.isCustomer) {
                    if (loggedIn && !app.session.isCustomer) {
                        vm.setScope(
                            agentId = app.session.agentId.takeIf { it > 0L },
                            isAdmin = app.session.isAdmin
                        )
                    }
                }

                var successBanner by remember { mutableStateOf<Pair<String, String>?>(null) }
                var openCustomerPlans by remember { mutableStateOf(false) }
                var needsProfileComplete by remember { mutableStateOf(false) }

                fun logout() {
                    app.session.logout()
                    loggedIn = false
                    openCustomerPlans = false
                    needsProfileComplete = false
                    navController.navigate(AppRoute.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }

                fun afterRegistrationPaid(subId: Long) {
                    scope.launch {
                        val sub = app.repository.getById(subId) ?: return@launch
                        val address = listOf(
                            sub.houseNo, sub.street, sub.villageTown, sub.mandal,
                            sub.district, sub.pincode, sub.state
                        ).filter { it.isNotBlank() }.joinToString(", ")
                        val planLabel = when {
                            sub.planYears >= 6 || sub.planMonths > 0 ->
                                "${com.iskcon.bhagavaddarshan.data.planDurationMonths(sub.planYears, sub.planMonths)} month plan"
                            sub.planYears > 0 -> "${sub.planYears} year plan"
                            else -> "${sub.planMonths} month(s)"
                        }
                        if (app.whatsAppClient.isConfigured()) {
                            app.whatsAppClient.sendRegistration(
                                phone = sub.phone,
                                name = sub.name,
                                receipt = sub.receiptNo.toString(),
                                plan = planLabel,
                                amount = "₹${sub.totalAmount}",
                                duration = "${sub.planMonths} months",
                                startMonth = sub.startMonth,
                                endDate = sub.endDate,
                                address = address
                            )
                        }
                        successBanner = sub.name to "Receipt #${sub.receiptNo} · $planLabel · ₹${sub.totalAmount}"
                    }
                    navController.popBackStack(AppRoute.Main.route, inclusive = false)
                }

                Box(Modifier.fillMaxSize()) {
                    // Prompt on login + every role (customer / agent / admin)
                    AppUpdateGate(app = app)
                    NavHost(
                            navController = navController,
                            startDestination = if (loggedIn) AppRoute.Main.route else AppRoute.Login.route,
                            modifier = Modifier.fillMaxSize(),
                            enterTransition = {
                                slideIntoContainer(
                                    AnimatedContentTransitionScope.SlideDirection.Left,
                                    animationSpec = tween(320)
                                ) + fadeIn(tween(220))
                            },
                            exitTransition = {
                                slideOutOfContainer(
                                    AnimatedContentTransitionScope.SlideDirection.Left,
                                    animationSpec = tween(320)
                                ) + fadeOut(tween(180))
                            },
                            popEnterTransition = {
                                slideIntoContainer(
                                    AnimatedContentTransitionScope.SlideDirection.Right,
                                    animationSpec = tween(320)
                                ) + fadeIn(tween(220))
                            },
                            popExitTransition = {
                                slideOutOfContainer(
                                    AnimatedContentTransitionScope.SlideDirection.Right,
                                    animationSpec = tween(320)
                                ) + fadeOut(tween(180))
                            }
                        ) {
                            composable(AppRoute.Login.route) {
                                LoginScreen(
                                    app = app,
                                    onLoggedIn = { needsComplete ->
                                        loggedIn = true
                                        openCustomerPlans = false
                                        needsProfileComplete = needsComplete
                                        if (needsComplete) {
                                            navController.navigate(AppRoute.CustomerCompleteProfile.route) {
                                                popUpTo(AppRoute.Login.route) { inclusive = true }
                                            }
                                        } else {
                                            navController.navigate(AppRoute.Main.route) {
                                                popUpTo(AppRoute.Login.route) { inclusive = true }
                                            }
                                        }
                                    },
                                    onRegister = {
                                        navController.navigate(AppRoute.CustomerRegister.route)
                                    }
                                )
                            }
                            composable(AppRoute.CustomerCompleteProfile.route) {
                                CustomerCompleteProfileScreen(
                                    app = app,
                                    onComplete = {
                                        needsProfileComplete = false
                                        navController.navigate(AppRoute.Main.route) {
                                            popUpTo(0) { inclusive = true }
                                        }
                                    }
                                )
                            }
                            composable(AppRoute.CustomerRegister.route) {
                                CustomerRegisterScreen(
                                    app = app,
                                    onBack = { navController.popBackStack() },
                                    onRegistered = {
                                        loggedIn = true
                                        openCustomerPlans = true
                                        navController.navigate(AppRoute.Main.route) {
                                            popUpTo(AppRoute.Login.route) { inclusive = true }
                                        }
                                    }
                                )
                            }
                            composable(AppRoute.Main.route) {
                                MainShell(
                                    app = app,
                                    viewModel = vm,
                                    onLogout = { logout() },
                                    onAddCustomer = { navController.navigate(AppRoute.Register.route) },
                                    onEditCustomer = { id ->
                                        navController.navigate(AppRoute.Detail.create(id))
                                    },
                                    onEditAgent = { id ->
                                        navController.navigate(AppRoute.AdminAgentEdit.create(id))
                                    },
                                    onCreateAgent = {
                                        navController.navigate(AppRoute.AdminAgentNew.route)
                                    },
                                    onCustomerCheckout = { years ->
                                        navController.navigate(AppRoute.CustomerShipping.create(years))
                                    },
                                    onCustomerSeva = { kind, amountPaise, title ->
                                        navController.navigate(AppRoute.CustomerSeva.create(kind, amountPaise, title))
                                    },
                                    initialCustomerTab = if (openCustomerPlans) CustomerTab.PLANS else CustomerTab.HOME,
                                    onAddDevotee = {
                                        navController.navigate(AppRoute.AddDevotee.route)
                                    },
                                    onEditDevotee = { id ->
                                        navController.navigate(AppRoute.EditDevotee.create(id))
                                    }
                                )
                            }
                            composable(AppRoute.AddDevotee.route) {
                                AddDevoteeScreen(
                                    app = app,
                                    onBack = { navController.popBackStack() },
                                    onBackToHome = {
                                        navController.popBackStack(AppRoute.Main.route, inclusive = false)
                                    }
                                )
                            }
                            composable(
                                route = AppRoute.EditDevotee.route,
                                arguments = listOf(navArgument("id") { type = NavType.LongType })
                            ) { entry ->
                                val id = entry.arguments?.getLong("id") ?: return@composable
                                AddDevoteeScreen(
                                    app = app,
                                    devoteeId = id,
                                    onBack = { navController.popBackStack() },
                                    onBackToHome = {
                                        navController.popBackStack(AppRoute.Main.route, inclusive = false)
                                    }
                                )
                            }
                            composable(
                                route = AppRoute.CustomerShipping.route,
                                arguments = listOf(
                                    navArgument("planYears") { type = NavType.IntType }
                                )
                            ) { entry ->
                                val years = entry.arguments?.getInt("planYears") ?: 12
                                CustomerShippingScreen(
                                    planYears = years,
                                    language = CustomerLanguage.fromStored(app.session.customerLanguage),
                                    onBack = { navController.popBackStack() },
                                    onContinue = { shippingPaise ->
                                        navController.navigate(
                                            AppRoute.CustomerCheckout.create(years, shippingPaise)
                                        )
                                    }
                                )
                            }
                            composable(
                                route = AppRoute.CustomerCheckout.route,
                                arguments = listOf(
                                    navArgument("planYears") { type = NavType.IntType },
                                    navArgument("shippingPaise") {
                                        type = NavType.IntType
                                        defaultValue = 0
                                    }
                                )
                            ) { entry ->
                                val years = entry.arguments?.getInt("planYears") ?: 12
                                val shippingPaise = entry.arguments?.getInt("shippingPaise") ?: 0
                                CustomerCheckoutScreen(
                                    app = app,
                                    language = CustomerLanguage.fromStored(app.session.customerLanguage),
                                    planYears = years,
                                    shippingPaise = shippingPaise,
                                    paymentReturnTick = paymentReturnTick,
                                    onBack = { navController.popBackStack() },
                                    onPaidSuccess = { detail ->
                                        openCustomerPlans = false
                                        val encoded = URLEncoder.encode(detail, StandardCharsets.UTF_8.toString())
                                        navController.navigate("${AppRoute.PaymentSuccess.route}?detail=$encoded") {
                                            popUpTo(AppRoute.Main.route) { inclusive = false }
                                        }
                                    }
                                )
                            }
                            composable(
                                route = "${AppRoute.CustomerSeva.route}?title={title}",
                                arguments = listOf(
                                    navArgument("kind") { type = NavType.StringType },
                                    navArgument("amountPaise") { type = NavType.IntType },
                                    navArgument("title") {
                                        type = NavType.StringType
                                        defaultValue = "Gita Daan"
                                    }
                                )
                            ) { entry ->
                                val kind = entry.arguments?.getString("kind").orEmpty()
                                val amountPaise = entry.arguments?.getInt("amountPaise") ?: 25000
                                val title = URLDecoder.decode(
                                    entry.arguments?.getString("title").orEmpty(),
                                    StandardCharsets.UTF_8.toString()
                                )
                                CustomerCheckoutScreen(
                                    app = app,
                                    language = CustomerLanguage.fromStored(app.session.customerLanguage),
                                    planYears = 1,
                                    sevaKind = kind,
                                    sevaTitle = title,
                                    sevaAmountPaise = amountPaise,
                                    paymentReturnTick = paymentReturnTick,
                                    onBack = { navController.popBackStack() },
                                    onPaidSuccess = { detail ->
                                        val encoded = URLEncoder.encode(detail, StandardCharsets.UTF_8.toString())
                                        navController.navigate("${AppRoute.PaymentSuccess.route}?detail=$encoded") {
                                            popUpTo(AppRoute.Main.route) { inclusive = false }
                                        }
                                    }
                                )
                            }
                            composable(
                                route = "${AppRoute.PaymentSuccess.route}?detail={detail}",
                                arguments = listOf(
                                    navArgument("detail") {
                                        type = NavType.StringType
                                        defaultValue = ""
                                    }
                                )
                            ) { entry ->
                                val detail = URLDecoder.decode(
                                    entry.arguments?.getString("detail").orEmpty(),
                                    StandardCharsets.UTF_8.toString()
                                )
                                PaymentSuccessScreen(
                                    language = CustomerLanguage.fromStored(app.session.customerLanguage),
                                    detail = detail.ifBlank { "Payment successful" },
                                    onContinue = {
                                        openCustomerPlans = false
                                        navController.navigate(AppRoute.Main.route) {
                                            popUpTo(0) { inclusive = true }
                                        }
                                    }
                                )
                            }
                            composable(AppRoute.Register.route) {
                                RegisterScreen(
                                    viewModel = vm,
                                    agentName = app.session.agentName,
                                    onDone = { navController.popBackStack() },
                                    onBack = { navController.popBackStack() },
                                    onCollectPayment = { method ->
                                        navController.navigate(
                                            AppRoute.CollectPayment.create(method.routeKey)
                                        )
                                    }
                                )
                            }
                            composable(
                                route = AppRoute.CollectPayment.route,
                                arguments = listOf(navArgument("method") { type = NavType.StringType })
                            ) { entry ->
                                val method = AgentPaymentMethod.fromRoute(
                                    entry.arguments?.getString("method").orEmpty()
                                )
                                CollectPaymentScreen(
                                    viewModel = vm,
                                    method = method,
                                    mode = QrPayMode.NEW_REGISTRATION,
                                    agentName = app.session.agentName,
                                    agentId = app.session.agentId,
                                    registeredBy = app.session.agentName.ifBlank { "self" },
                                    isAdmin = app.session.isAdmin,
                                    whatsAppClient = app.whatsAppClient,
                                    onBack = { navController.popBackStack() },
                                    onPaid = { subId -> afterRegistrationPaid(subId) }
                                )
                            }
                            composable(
                                route = AppRoute.CollectPaymentExisting.route,
                                arguments = listOf(
                                    navArgument("method") { type = NavType.StringType },
                                    navArgument("id") { type = NavType.LongType }
                                )
                            ) { entry ->
                                val method = AgentPaymentMethod.fromRoute(
                                    entry.arguments?.getString("method").orEmpty()
                                )
                                val id = entry.arguments?.getLong("id") ?: return@composable
                                CollectPaymentScreen(
                                    viewModel = vm,
                                    method = method,
                                    mode = QrPayMode.EXISTING_SUBSCRIPTION,
                                    existingSubId = id,
                                    agentName = app.session.agentName,
                                    agentId = app.session.agentId,
                                    registeredBy = app.session.agentName.ifBlank { "self" },
                                    isAdmin = app.session.isAdmin,
                                    whatsAppClient = app.whatsAppClient,
                                    onBack = { navController.popBackStack() },
                                    onPaid = { _ ->
                                        navController.popBackStack(AppRoute.Main.route, inclusive = false)
                                    }
                                )
                            }
                            composable(
                                route = AppRoute.Detail.route,
                                arguments = listOf(navArgument("id") { type = NavType.LongType })
                            ) { entry ->
                                val id = entry.arguments?.getLong("id") ?: return@composable
                                DetailScreen(
                                    id = id,
                                    viewModel = vm,
                                    app = app,
                                    onBack = { navController.popBackStack() },
                                    razorpayReady = true,
                                    onPayWithRazorpay = { subId, _, _, _ ->
                                        navController.navigate(
                                            AppRoute.CollectPaymentExisting.create(
                                                AgentPaymentMethod.DYNAMIC_QR.routeKey,
                                                subId
                                            )
                                        )
                                    }
                                )
                            }
                            composable(AppRoute.AdminAgentNew.route) {
                                AdminAgentEditScreen(
                                    app = app,
                                    agentId = null,
                                    onDone = { navController.popBackStack() },
                                    onBack = { navController.popBackStack() }
                                )
                            }
                            composable(
                                route = AppRoute.AdminAgentEdit.route,
                                arguments = listOf(navArgument("id") { type = NavType.LongType })
                            ) { entry ->
                                val id = entry.arguments?.getLong("id") ?: return@composable
                                AdminAgentEditScreen(
                                    app = app,
                                    agentId = id,
                                    onDone = { navController.popBackStack() },
                                    onBack = { navController.popBackStack() }
                                )
                            }
                        }

                        successBanner?.let { (name, detail) ->
                            com.iskcon.bhagavaddarshan.ui.components.PremiumSuccessDialog(
                                title = "Hare Krishna!",
                                subtitle = "$name\n$detail\nWhatsApp confirmation is being sent.",
                                confirmLabel = "Continue",
                                onDismiss = { successBanner = null }
                            )
                        }
                    }
            }
        }

        handlePaymentReturn(intent)
    }

    override fun onResume() {
        super.onResume()
        if (skipResumeNotify) {
            skipResumeNotify = false
            return
        }
        paymentReturnHandler?.invoke()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handlePaymentReturn(intent)
    }

    private fun handlePaymentReturn(intent: Intent?) {
        val data = intent?.data ?: return
        if (data.scheme == "bhagavaddarshan" && data.host == "payment") {
            paymentReturnHandler?.invoke()
        }
    }

    override fun onPaymentSuccess(paymentId: String?, data: PaymentData?) {
        val id = paymentId ?: data?.paymentId
        if (id.isNullOrBlank()) {
            CheckoutResultBus.publish(CheckoutResult.Failure("Payment id missing"))
        } else {
            CheckoutResultBus.publish(CheckoutResult.Success(id))
        }
    }

    override fun onPaymentError(code: Int, description: String?, data: PaymentData?) {
        val raw = description?.takeIf { it.isNotBlank() }.orEmpty()
        val message = when {
            raw.contains("BAD_REQUEST_ERROR") || raw.trimStart().startsWith("{") ->
                "Payment was cancelled or not completed"
            raw.isBlank() -> "Payment cancelled"
            else -> raw.take(160)
        }
        CheckoutResultBus.publish(CheckoutResult.Failure(message))
    }
}
