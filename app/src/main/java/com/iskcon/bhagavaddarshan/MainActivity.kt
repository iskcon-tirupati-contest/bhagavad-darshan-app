package com.iskcon.bhagavaddarshan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.iskcon.bhagavaddarshan.ui.theme.BhagavadDarshanTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as BhagavadDarshanApp

        setContent {
            BhagavadDarshanTheme {
                val navController = rememberNavController()
                var loggedIn by remember { mutableStateOf(app.session.isLoggedIn) }
                val vm: SubscriptionViewModel = viewModel(
                    factory = SubscriptionViewModelFactory(app.repository)
                )
                val scope = rememberCoroutineScope()

                LaunchedEffect(loggedIn, app.session.agentId, app.session.isAdmin) {
                    if (loggedIn) {
                        vm.setScope(
                            agentId = app.session.agentId.takeIf { it > 0L },
                            isAdmin = app.session.isAdmin
                        )
                    }
                }

                fun logout() {
                    app.session.logout()
                    loggedIn = false
                    navController.navigate(AppRoute.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }

                fun afterRegistrationPaid(subId: Long) {
                    scope.launch {
                        val sub = app.repository.getById(subId) ?: return@launch
                        app.whatsAppClient.sendRegistration(
                            phone = sub.phone,
                            name = sub.name,
                            receipt = sub.receiptNo.toString(),
                            plan = "${sub.planMonths} months"
                        )
                    }
                    navController.popBackStack(AppRoute.Main.route, inclusive = false)
                }

                Scaffold(modifier = Modifier.fillMaxSize(), contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0)) { _ ->
                    NavHost(
                        navController = navController,
                        startDestination = if (loggedIn) AppRoute.Main.route else AppRoute.Login.route,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        composable(AppRoute.Login.route) {
                            LoginScreen(app = app) {
                                loggedIn = true
                                navController.navigate(AppRoute.Main.route) {
                                    popUpTo(AppRoute.Login.route) { inclusive = true }
                                }
                            }
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
                }
            }
        }
    }
}
