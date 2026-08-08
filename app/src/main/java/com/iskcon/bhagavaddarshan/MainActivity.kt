package com.iskcon.bhagavaddarshan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.iskcon.bhagavaddarshan.ui.navigation.AppRoute
import com.iskcon.bhagavaddarshan.ui.screens.DetailScreen
import com.iskcon.bhagavaddarshan.ui.screens.ExpiringScreen
import com.iskcon.bhagavaddarshan.ui.screens.HomeScreen
import com.iskcon.bhagavaddarshan.ui.screens.RegisterScreen
import com.iskcon.bhagavaddarshan.ui.screens.SearchScreen
import com.iskcon.bhagavaddarshan.ui.screens.SubscriptionViewModel
import com.iskcon.bhagavaddarshan.ui.screens.SubscriptionViewModelFactory
import com.iskcon.bhagavaddarshan.ui.theme.BhagavadDarshanTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as BhagavadDarshanApp
        setContent {
            BhagavadDarshanTheme {
                val navController = rememberNavController()
                val vm: SubscriptionViewModel = viewModel(
                    factory = SubscriptionViewModelFactory(app.repository)
                )
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = AppRoute.Home.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(AppRoute.Home.route) {
                            HomeScreen(
                                viewModel = vm,
                                onRegister = { navController.navigate(AppRoute.Register.route) },
                                onSearch = { navController.navigate(AppRoute.Search.route) },
                                onExpiring = { navController.navigate(AppRoute.Expiring.route) },
                                onOpen = { id -> navController.navigate(AppRoute.Detail.create(id)) }
                            )
                        }
                        composable(AppRoute.Register.route) {
                            RegisterScreen(
                                viewModel = vm,
                                onDone = { navController.popBackStack() },
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable(AppRoute.Search.route) {
                            SearchScreen(
                                viewModel = vm,
                                onBack = { navController.popBackStack() },
                                onOpen = { id -> navController.navigate(AppRoute.Detail.create(id)) }
                            )
                        }
                        composable(AppRoute.Expiring.route) {
                            ExpiringScreen(
                                viewModel = vm,
                                onBack = { navController.popBackStack() },
                                onOpen = { id -> navController.navigate(AppRoute.Detail.create(id)) }
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
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
