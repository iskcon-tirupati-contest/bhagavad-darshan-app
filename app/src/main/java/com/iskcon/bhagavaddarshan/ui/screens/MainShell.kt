package com.iskcon.bhagavaddarshan.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iskcon.bhagavaddarshan.BhagavadDarshanApp
import com.iskcon.bhagavaddarshan.R
import com.iskcon.bhagavaddarshan.ui.components.AgentNavBarGreen
import com.iskcon.bhagavaddarshan.ui.components.AgentNavBarSelected
import com.iskcon.bhagavaddarshan.ui.components.CompactBottomBar
import com.iskcon.bhagavaddarshan.ui.components.PressableOutlineButton
import com.iskcon.bhagavaddarshan.ui.components.PressablePrimaryButton
import com.iskcon.bhagavaddarshan.ui.navigation.AdminTab
import com.iskcon.bhagavaddarshan.ui.navigation.AgentTab
import com.iskcon.bhagavaddarshan.ui.navigation.CustomerTab
import com.iskcon.bhagavaddarshan.ui.screens.admin.AdminAgentsScreen
import com.iskcon.bhagavaddarshan.ui.screens.admin.AdminComplaintsScreen
import com.iskcon.bhagavaddarshan.ui.screens.agent.AgentHomeDashboardScreen
import com.iskcon.bhagavaddarshan.ui.screens.agent.DevoteesScreen
import com.iskcon.bhagavaddarshan.ui.screens.customer.CustomerHelpScreen
import com.iskcon.bhagavaddarshan.ui.screens.customer.CustomerHomeScreen
import com.iskcon.bhagavaddarshan.ui.screens.customer.CustomerLanguage
import com.iskcon.bhagavaddarshan.ui.screens.customer.CustomerMagazinesScreen
import com.iskcon.bhagavaddarshan.ui.screens.customer.CustomerPlansScreen
import com.iskcon.bhagavaddarshan.ui.screens.customer.CustomerProfileScreen
import com.iskcon.bhagavaddarshan.ui.screens.customer.SacredBook
import com.iskcon.bhagavaddarshan.ui.screens.customer.customerPageTitle
import com.iskcon.bhagavaddarshan.ui.screens.customer.customerTabLabel
import com.iskcon.bhagavaddarshan.ui.screens.customer.tr
import com.iskcon.bhagavaddarshan.util.UiSounds
import com.iskcon.bhagavaddarshan.ui.theme.AppSerif
import com.iskcon.bhagavaddarshan.ui.theme.ChromeBar
import com.iskcon.bhagavaddarshan.ui.theme.ChromeBarBorder
import com.iskcon.bhagavaddarshan.ui.theme.EditCream
import com.iskcon.bhagavaddarshan.ui.theme.EditMuted
import com.iskcon.bhagavaddarshan.ui.theme.EditPeach
import com.iskcon.bhagavaddarshan.ui.theme.EditTerracotta
import com.iskcon.bhagavaddarshan.ui.theme.Montserrat
import com.iskcon.bhagavaddarshan.ui.theme.TempleGreen
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
    initialCustomerTab: CustomerTab = CustomerTab.HOME,
    onAddDevotee: () -> Unit = onAddCustomer,
    onEditDevotee: (Long) -> Unit = {}
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
            onLogout = onLogout,
            onAddDevotee = onAddDevotee,
            onEditDevotee = onEditDevotee
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
    var showLogoutConfirm by rememberSaveable { mutableStateOf(false) }
    val currentTab = tabs[tab.coerceIn(0, tabs.lastIndex)]
    val pageTitle = customerPageTitle(language, currentTab.label)

    if (showLogoutConfirm) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = {
                Text(
                    tr(language, "Confirm logout", "లాగౌట్ నిర్ధారించండి"),
                    fontFamily = AppSerif,
                    fontWeight = FontWeight.Bold,
                    color = EditMuted
                )
            },
            text = {
                Text(
                    tr(language, "Do you want to logout now?", "ఇప్పుడు లాగౌట్ కావాలా?"),
                    fontFamily = Montserrat,
                    color = EditMuted
                )
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showLogoutConfirm = false }) {
                    Text(tr(language, "Cancel", "రద్దు"), color = EditMuted)
                }
            },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    showLogoutConfirm = false
                    onLogout()
                }) {
                    Text(tr(language, "Logout", "లాగౌట్"), color = TempleGreen, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = EditCream,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ChromeBar)
                    .statusBarsPadding()
                    .drawBehind {
                        val y = size.height - 1.dp.toPx()
                        drawLine(
                            color = ChromeBarBorder,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(R.drawable.bd_logo),
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        pageTitle,
                        fontFamily = AppSerif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TempleGreen,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )
                    androidx.compose.material3.IconButton(onClick = { showLogoutConfirm = true }) {
                        Icon(
                            Icons.AutoMirrored.Filled.Logout,
                            contentDescription = tr(language, "Logout", "లాగౌట్"),
                            tint = TempleGreen,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = ChromeBar,
                tonalElevation = 0.dp,
                windowInsets = WindowInsets.navigationBars,
                modifier = Modifier.drawBehind {
                    drawLine(
                        color = ChromeBarBorder,
                        start = Offset(0f, 0f),
                        end = Offset(size.width, 0f),
                        strokeWidth = 1.dp.toPx()
                    )
                }
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
                                modifier = Modifier.size(24.dp),
                                tint = if (selected) TempleGreen else EditMuted
                            )
                        },
                        label = {
                            Text(
                                customerTabLabel(language, t.label.lowercase()).uppercase(),
                                color = if (selected) TempleGreen else EditMuted,
                                fontFamily = Montserrat,
                                fontSize = 12.sp,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                                maxLines = 1
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = TempleGreen,
                            selectedTextColor = TempleGreen,
                            unselectedIconColor = EditMuted,
                            unselectedTextColor = EditMuted,
                            indicatorColor = TempleGreen.copy(alpha = 0.12f)
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
                    (fadeIn(animationSpec = tween(180)) togetherWith fadeOut(animationSpec = tween(120)))
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
                        onLogout = onLogout,
                        language = language,
                        onLanguageChange = {
                            language = it
                            app.session.customerLanguage = it.name
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
    val context = LocalContext.current
    var previousTab by remember { mutableIntStateOf(tab) }
    val slideForward = tab >= previousTab

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            CompactBottomBar() {
                tabs.forEachIndexed { index, t ->
                    AnimatedNavItem(
                        selected = tab == index,
                        onClick = {
                            if (tab != index) {
                                UiSounds.click(context)
                                previousTab = tab
                                tab = index
                            }
                        },
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
                    if (slideForward) {
                        (slideInHorizontally(tween(320)) { full -> full } + fadeIn(tween(220)))
                            .togetherWith(slideOutHorizontally(tween(280)) { full -> -full / 4 } + fadeOut(tween(180)))
                    } else {
                        (slideInHorizontally(tween(320)) { full -> -full } + fadeIn(tween(220)))
                            .togetherWith(slideOutHorizontally(tween(280)) { full -> full / 4 } + fadeOut(tween(180)))
                    }
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
                        onReconcile = {
                            UiSounds.click(context)
                            previousTab = tab
                            tab = tabs.indexOf(AdminTab.RECONCILE)
                        },
                        onViewExpiring = {
                            UiSounds.click(context)
                            previousTab = tab
                            tab = tabs.indexOf(AdminTab.CUSTOMERS)
                        }
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
                    AdminTab.COMPLAINTS -> AdminComplaintsScreen(app = app)
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
    onLogout: () -> Unit,
    onAddDevotee: () -> Unit,
    onEditDevotee: (Long) -> Unit
) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val tabs = AgentTab.entries
    val context = LocalContext.current
    var showLogoutConfirm by remember { mutableStateOf(false) }

    fun requestLogout() {
        UiSounds.click(context)
        showLogoutConfirm = true
    }

    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = { Text("Logout?", fontWeight = FontWeight.Bold) },
            text = { Text("Do you want to logout now?") },
            dismissButton = {
                PressableOutlineButton(
                    text = "Cancel",
                    onClick = {
                        UiSounds.click(context)
                        showLogoutConfirm = false
                    }
                )
            },
            confirmButton = {
                PressablePrimaryButton(
                    text = "Logout",
                    onClick = {
                        UiSounds.click(context)
                        showLogoutConfirm = false
                        onLogout()
                    }
                )
            }
        )
    }

    var previousTab by remember { mutableIntStateOf(tab) }
    val slideForward = tab >= previousTab

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            CompactBottomBar(
                containerColor = AgentNavBarGreen,
                contentColor = AgentNavBarSelected
            ) {
                tabs.forEachIndexed { index, t ->
                    AnimatedNavItem(
                        selected = tab == index,
                        onClick = {
                            if (tab != index) {
                                UiSounds.click(context)
                                previousTab = tab
                                tab = index
                            }
                        },
                        icon = agentIcon(t),
                        label = t.label,
                        darkBar = true,
                        showLabel = false
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
                    if (slideForward) {
                        (slideInHorizontally(tween(320)) { full -> full } + fadeIn(tween(220)))
                            .togetherWith(slideOutHorizontally(tween(280)) { full -> -full / 4 } + fadeOut(tween(180)))
                    } else {
                        (slideInHorizontally(tween(320)) { full -> -full } + fadeIn(tween(220)))
                            .togetherWith(slideOutHorizontally(tween(280)) { full -> full / 4 } + fadeOut(tween(180)))
                    }
                }
            ) { current ->
                when (current) {
                    AgentTab.HOME -> AgentHomeDashboardScreen(
                        app = app,
                        agentId = app.session.agentId,
                        agentName = app.session.agentName,
                        onLogout = ::requestLogout,
                        onAddDevotee = onAddDevotee
                    )
                    AgentTab.DEVOTEES -> DevoteesScreen(
                        app = app,
                        agentId = app.session.agentId,
                        onAddDevotee = onAddDevotee,
                        onEditDevotee = onEditDevotee,
                        onLogout = ::requestLogout
                    )
                    AgentTab.PROFILE -> ProfileScreen(app = app, onLogout = ::requestLogout)
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
    label: String,
    darkBar: Boolean = false,
    showLabel: Boolean = true
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.12f else 1f,
        animationSpec = tween(180),
        label = "navIconScale"
    )
    val selectedColor = if (darkBar) AgentNavBarSelected else MaterialTheme.colorScheme.primary
    val unselected = if (darkBar) Color(0xFF6B7F74) else Color(0xFF9E9E9E)
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = {
            Icon(
                icon,
                contentDescription = label,
                modifier = Modifier
                    .size(if (showLabel) 20.dp else 24.dp)
                    .graphicsLayer(scaleX = scale, scaleY = scale)
            )
        },
        label = if (showLabel) {
            { Text(label, fontSize = 10.sp, maxLines = 1) }
        } else {
            { }
        },
        alwaysShowLabel = showLabel,
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = selectedColor,
            selectedTextColor = selectedColor,
            indicatorColor = selectedColor.copy(alpha = if (darkBar) 0.22f else 0.14f),
            unselectedIconColor = unselected,
            unselectedTextColor = unselected
        )
    )
}

private fun adminIcon(tab: AdminTab): ImageVector = when (tab) {
    AdminTab.HOME -> Icons.Default.Home
    AdminTab.CUSTOMERS -> Icons.Default.People
    AdminTab.AGENTS -> Icons.Default.Groups
    AdminTab.COMPLAINTS -> Icons.Default.ReportProblem
    AdminTab.PLANS -> Icons.Default.Star
    AdminTab.RECONCILE -> Icons.Default.Payment
}

private fun agentIcon(tab: AgentTab): ImageVector = when (tab) {
    AgentTab.HOME -> Icons.Default.Home
    AgentTab.DEVOTEES -> Icons.Default.People
    AgentTab.PROFILE -> Icons.Default.Person
}

private fun customerIcon(tab: CustomerTab): ImageVector = when (tab) {
    CustomerTab.HOME -> Icons.Default.Home
    CustomerTab.PLANS -> Icons.Default.Star
    CustomerTab.MAGAZINES -> Icons.AutoMirrored.Filled.MenuBook
    CustomerTab.HELP -> Icons.Default.Help
    CustomerTab.PROFILE -> Icons.Default.Person
}
