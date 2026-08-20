package com.iskcon.bhagavaddarshan.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iskcon.bhagavaddarshan.BhagavadDarshanApp
import com.iskcon.bhagavaddarshan.ui.components.CompactBottomBar
import com.iskcon.bhagavaddarshan.ui.navigation.AdminTab
import com.iskcon.bhagavaddarshan.ui.navigation.AgentTab
import com.iskcon.bhagavaddarshan.ui.navigation.CustomerTab
import com.iskcon.bhagavaddarshan.ui.screens.admin.AdminAgentsScreen
import com.iskcon.bhagavaddarshan.ui.screens.customer.CustomerHelpScreen
import com.iskcon.bhagavaddarshan.ui.screens.customer.CustomerHomeScreen
import com.iskcon.bhagavaddarshan.ui.screens.customer.CustomerLanguage
import com.iskcon.bhagavaddarshan.ui.screens.customer.CustomerMagazinesScreen
import com.iskcon.bhagavaddarshan.ui.screens.customer.CustomerPlansScreen
import com.iskcon.bhagavaddarshan.ui.screens.customer.CustomerProfileScreen
import com.iskcon.bhagavaddarshan.ui.screens.customer.SacredBook
import com.iskcon.bhagavaddarshan.ui.screens.customer.customerTabLabel
import com.iskcon.bhagavaddarshan.ui.theme.UxCream
import com.iskcon.bhagavaddarshan.ui.theme.UxGold200
import com.iskcon.bhagavaddarshan.ui.theme.UxInk
import com.iskcon.bhagavaddarshan.ui.theme.UxSaffron

@Composable
fun MainShell(
    app: BhagavadDarshanApp,
    viewModel: SubscriptionViewModel,
    onLogout: () -> Unit,
    onAddCustomer: () -> Unit,
    onEditCustomer: (Long) -> Unit,
    onEditAgent: (Long) -> Unit,
    onCreateAgent: () -> Unit,
    onCustomerCheckout: (planYears: Int) -> Unit = {},
    onCustomerSeva: (kind: String, amountPaise: Int, title: String) -> Unit = { _, _, _ -> },
    initialCustomerTab: CustomerTab = CustomerTab.HOME
) {
    when {
        app.session.isCustomer -> CustomerShell(
            app = app,
            onLogout = onLogout,
            onCustomerCheckout = onCustomerCheckout,
            onCustomerSeva = onCustomerSeva,
            initialTab = initialCustomerTab
        )
        app.session.isAdmin -> AdminShell(
            app = app,
            viewModel = viewModel,
            onLogout = onLogout,
            onAddCustomer = onAddCustomer,
            onEditCustomer = onEditCustomer,
            onEditAgent = onEditAgent,
            onCreateAgent = onCreateAgent
        )
        else -> AgentShell(
            app = app,
            viewModel = viewModel,
            onLogout = onLogout,
            onAddCustomer = onAddCustomer,
            onEditCustomer = onEditCustomer
        )
    }
}

@Composable
private fun CustomerShell(
    app: BhagavadDarshanApp,
    onLogout: () -> Unit,
    onCustomerCheckout: (planYears: Int) -> Unit,
    onCustomerSeva: (kind: String, amountPaise: Int, title: String) -> Unit,
    initialTab: CustomerTab = CustomerTab.HOME
) {
    var tab by rememberSaveable { mutableIntStateOf(initialTab.ordinal) }
    val tabs = CustomerTab.entries
    var language by rememberSaveable {
        mutableStateOf(CustomerLanguage.fromStored(app.session.customerLanguage))
    }

    Scaffold(
        contentWindowInsets = WindowInsets.statusBars,
        containerColor = UxCream,
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFFDED4C8),
                tonalElevation = 0.dp,
                windowInsets = WindowInsets.navigationBars
            ) {
                tabs.forEachIndexed { index, t ->
                    val selected = tab == index
                    NavigationBarItem(
                        selected = selected,
                        onClick = { tab = index },
                        icon = {
                            Icon(
                                customerIcon(t),
                                contentDescription = t.label,
                                tint = if (selected) UxSaffron else UxInk.copy(alpha = 0.40f)
                            )
                        },
                        label = {
                            Text(
                                customerTabLabel(language, t.label.lowercase()).uppercase(),
                                color = if (selected) UxSaffron else Color(0xFF6B6259),
                                fontSize = 9.sp,
                                maxLines = 1
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = UxSaffron,
                            selectedTextColor = UxSaffron,
                            unselectedIconColor = Color(0xFF6B6259),
                            unselectedTextColor = Color(0xFF6B6259),
                            indicatorColor = UxGold200.copy(alpha = 0.55f)
                        )
                    )
                }
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            AnimatedContent(
                targetState = tabs[tab],
                label = "customerTab",
                transitionSpec = {
                    (fadeIn(tween(220)) togetherWith fadeOut(tween(140)))
                }
            ) { current ->
                when (current) {
                    CustomerTab.HOME -> CustomerHomeScreen(
                        app = app,
                        language = language,
                        onLogout = onLogout,
                        onRenew = { tab = CustomerTab.PLANS.ordinal },
                        onBookPlan = { tab = CustomerTab.PLANS.ordinal },
                        onProfile = { tab = CustomerTab.PROFILE.ordinal },
                        onArchive = { tab = CustomerTab.MAGAZINES.ordinal },
                        onDonate = { amountPaise, title ->
                            onCustomerSeva("donation", amountPaise, title)
                        },
                        onBuyBook = { book: SacredBook ->
                            onCustomerSeva("book", book.priceRupees * 100, book.title)
                        }
                    )
                    CustomerTab.PLANS -> CustomerPlansScreen(
                        app = app,
                        language = language,
                        onCheckout = onCustomerCheckout
                    )
                    CustomerTab.MAGAZINES -> CustomerMagazinesScreen(language = language)
                    CustomerTab.HELP -> CustomerHelpScreen(app = app, language = language)
                    CustomerTab.PROFILE -> CustomerProfileScreen(
                        app = app,
                        language = language,
                        onLogout = onLogout,
                        onHelp = { tab = CustomerTab.HELP.ordinal },
                        onLanguageChange = {
                            language = it
                            app.session.customerLanguage = it.storedValue()
                        }
                    )
                }
            }
        }
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
            CompactBottomBar() {
                tabs.forEachIndexed { index, t ->
                    AnimatedNavItem(
                        selected = tab == index,
                        onClick = { tab = index },
                        icon = adminIcon(t),
                        label = t.label
                    )
                }
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            AnimatedContent(
                targetState = tabs[tab],
                label = "adminTab",
                transitionSpec = {
                    (fadeIn(tween(220)) togetherWith fadeOut(tween(140)))
                }
            ) { current ->
                when (current) {
                    AdminTab.HOME -> HomeDashboardScreen(
                        app = app,
                        isAdmin = true,
                        agentId = null,
                        agentName = app.session.agentName,
                        onLogout = onLogout,
                        onAddCustomer = onAddCustomer,
                        onReconcile = { tab = tabs.indexOf(AdminTab.RECONCILE) },
                        onViewExpiring = { tab = tabs.indexOf(AdminTab.CUSTOMERS) }
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
            CompactBottomBar() {
                tabs.forEachIndexed { index, t ->
                    AnimatedNavItem(
                        selected = tab == index,
                        onClick = { tab = index },
                        icon = agentIcon(t),
                        label = t.label
                    )
                }
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            AnimatedContent(
                targetState = tabs[tab],
                label = "agentTab",
                transitionSpec = {
                    (fadeIn(tween(220)) togetherWith fadeOut(tween(140)))
                }
            ) { current ->
                when (current) {
                    AgentTab.HOME -> HomeDashboardScreen(
                        app = app,
                        isAdmin = false,
                        agentId = app.session.agentId,
                        agentName = app.session.agentName,
                        onLogout = onLogout,
                        onAddCustomer = onAddCustomer,
                        onReconcile = { tab = tabs.indexOf(AgentTab.RECONCILE) },
                        onViewExpiring = { tab = tabs.indexOf(AgentTab.CUSTOMERS) }
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
}

/** Bottom-nav item whose icon gently scales up when selected, on a pill-shaped indicator. */
@Composable
private fun androidx.compose.foundation.layout.RowScope.AnimatedNavItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    label: String
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.12f else 1f,
        animationSpec = tween(180),
        label = "navIconScale"
    )
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = {
            Icon(
                icon,
                contentDescription = label,
                modifier = Modifier
                    .size(20.dp)
                    .graphicsLayer(scaleX = scale, scaleY = scale)
            )
        },
        label = { Text(label, fontSize = 10.sp, maxLines = 1) },
        alwaysShowLabel = true,
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            // Soft saffron circle behind selected icon (Figma Plans tab)
            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
            unselectedIconColor = Color(0xFF9E9E9E),
            unselectedTextColor = Color(0xFF9E9E9E)
        )
    )
}

private fun adminIcon(tab: AdminTab): ImageVector = when (tab) {
    AdminTab.HOME -> Icons.Default.Home
    AdminTab.CUSTOMERS -> Icons.Default.People
    AdminTab.AGENTS -> Icons.Default.Groups
    AdminTab.PLANS -> Icons.Default.Star
    AdminTab.RECONCILE -> Icons.Default.Payment
}

private fun agentIcon(tab: AgentTab): ImageVector = when (tab) {
    AgentTab.HOME -> Icons.Default.Home
    AgentTab.CUSTOMERS -> Icons.Default.People
    AgentTab.PROFILE -> Icons.Default.Person
    AgentTab.RECONCILE -> Icons.Default.Payment
}

private fun customerIcon(tab: CustomerTab): ImageVector = when (tab) {
    CustomerTab.HOME -> Icons.Default.Home
    CustomerTab.PLANS -> Icons.Default.Star
    CustomerTab.MAGAZINES -> Icons.AutoMirrored.Filled.MenuBook
    CustomerTab.HELP -> Icons.Default.Help
    CustomerTab.PROFILE -> Icons.Default.Person
}
