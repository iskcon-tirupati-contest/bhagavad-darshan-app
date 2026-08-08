package com.iskcon.bhagavaddarshan.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iskcon.bhagavaddarshan.BhagavadDarshanApp
import com.iskcon.bhagavaddarshan.ui.components.CompactBottomBar
import com.iskcon.bhagavaddarshan.ui.navigation.AdminTab
import com.iskcon.bhagavaddarshan.ui.navigation.AgentTab
import com.iskcon.bhagavaddarshan.ui.screens.admin.AdminAgentsScreen

@Composable
fun MainShell(
    app: BhagavadDarshanApp,
    viewModel: SubscriptionViewModel,
    onLogout: () -> Unit,
    onAddCustomer: () -> Unit,
    onEditCustomer: (Long) -> Unit,
    onEditAgent: (Long) -> Unit,
    onCreateAgent: () -> Unit
) {
    if (app.session.isAdmin) {
        AdminShell(
            app = app,
            viewModel = viewModel,
            onLogout = onLogout,
            onAddCustomer = onAddCustomer,
            onEditCustomer = onEditCustomer,
            onEditAgent = onEditAgent,
            onCreateAgent = onCreateAgent
        )
    } else {
        AgentShell(
            app = app,
            viewModel = viewModel,
            onLogout = onLogout,
            onAddCustomer = onAddCustomer,
            onEditCustomer = onEditCustomer
        )
    }
}

@Composable
private fun AdminShell(
    app: BhagavadDarshanApp,
    viewModel: SubscriptionViewModel,
    onLogout: () -> Unit,
    onAddCustomer: () -> Unit,
    onEditCustomer: (Long) -> Unit,
    onEditAgent: (Long) -> Unit,
    onCreateAgent: () -> Unit
) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val tabs = AdminTab.entries

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            CompactBottomBar(
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                tabs.forEachIndexed { index, t ->
                    NavigationBarItem(
                        selected = tab == index,
                        onClick = { tab = index },
                        icon = {
                            Icon(
                                adminIcon(t),
                                contentDescription = t.label,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = { Text(t.label, fontSize = 9.sp, maxLines = 1) },
                        alwaysShowLabel = true,
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            when (tabs[tab]) {
                AdminTab.HOME -> HomeDashboardScreen(
                    app = app,
                    isAdmin = true,
                    agentId = null,
                    agentName = app.session.agentName,
                    onLogout = onLogout
                )
                AdminTab.CUSTOMERS -> CustomersScreen(
                    viewModel = viewModel,
                    isAdmin = true,
                    onAdd = onAddCustomer,
                    onEdit = onEditCustomer
                )
                AdminTab.AGENTS -> AdminAgentsScreen(
                    app = app,
                    onBack = {},
                    onEdit = onEditAgent,
                    onCreate = onCreateAgent,
                    showBack = false
                )
                AdminTab.PLANS -> PlansScreen(app = app)
                AdminTab.RECONCILE -> ReconcileScreen(
                    app = app,
                    viewModel = viewModel,
                    isAdmin = true
                )
            }
        }
    }
}

@Composable
private fun AgentShell(
    app: BhagavadDarshanApp,
    viewModel: SubscriptionViewModel,
    onLogout: () -> Unit,
    onAddCustomer: () -> Unit,
    onEditCustomer: (Long) -> Unit
) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val tabs = AgentTab.entries

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            CompactBottomBar(
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                tabs.forEachIndexed { index, t ->
                    NavigationBarItem(
                        selected = tab == index,
                        onClick = { tab = index },
                        icon = {
                            Icon(
                                agentIcon(t),
                                contentDescription = t.label,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = { Text(t.label, fontSize = 9.sp, maxLines = 1) },
                        alwaysShowLabel = true,
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            when (tabs[tab]) {
                AgentTab.HOME -> HomeDashboardScreen(
                    app = app,
                    isAdmin = false,
                    agentId = app.session.agentId,
                    agentName = app.session.agentName,
                    onLogout = onLogout
                )
                AgentTab.CUSTOMERS -> CustomersScreen(
                    viewModel = viewModel,
                    isAdmin = false,
                    onAdd = onAddCustomer,
                    onEdit = onEditCustomer
                )
                AgentTab.PROFILE -> ProfileScreen(app = app, onLogout = onLogout)
                AgentTab.RECONCILE -> ReconcileScreen(
                    app = app,
                    viewModel = viewModel,
                    isAdmin = false
                )
            }
        }
    }
}

private fun adminIcon(tab: AdminTab): ImageVector = when (tab) {
    AdminTab.HOME -> Icons.Default.Home
    AdminTab.CUSTOMERS -> Icons.Default.People
    AdminTab.AGENTS -> Icons.Default.Groups
    AdminTab.PLANS -> Icons.Default.Sell
    AdminTab.RECONCILE -> Icons.Default.Payment
}

private fun agentIcon(tab: AgentTab): ImageVector = when (tab) {
    AgentTab.HOME -> Icons.Default.Home
    AgentTab.CUSTOMERS -> Icons.Default.People
    AgentTab.PROFILE -> Icons.Default.Person
    AgentTab.RECONCILE -> Icons.Default.Payment
}
