package com.iskcon.bhagavaddarshan.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iskcon.bhagavaddarshan.BhagavadDarshanApp
import com.iskcon.bhagavaddarshan.data.Analytics
import com.iskcon.bhagavaddarshan.data.DashboardStats
import com.iskcon.bhagavaddarshan.ui.components.AccentSectionTitle
import com.iskcon.bhagavaddarshan.ui.components.CompactTopBar
import com.iskcon.bhagavaddarshan.ui.components.FigmaStatCard
import com.iskcon.bhagavaddarshan.ui.components.ListBottomSafeGap
import com.iskcon.bhagavaddarshan.ui.screens.admin.SalesTrendChart
import com.iskcon.bhagavaddarshan.ui.theme.AmberPendingContainer
import com.iskcon.bhagavaddarshan.ui.theme.InkSoft
import com.iskcon.bhagavaddarshan.ui.theme.Leaf
import com.iskcon.bhagavaddarshan.ui.theme.LeafContainer
import com.iskcon.bhagavaddarshan.ui.theme.Marigold
import com.iskcon.bhagavaddarshan.ui.theme.Outline
import com.iskcon.bhagavaddarshan.ui.theme.SacredGold
import com.iskcon.bhagavaddarshan.ui.theme.TempleMaroon
import com.iskcon.bhagavaddarshan.util.UiSounds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeDashboardScreen(
    app: BhagavadDarshanApp,
    isAdmin: Boolean,
    agentId: Long?,
    @Suppress("UNUSED_PARAMETER") agentName: String,
    onLogout: () -> Unit,
    onAddCustomer: () -> Unit = {},
    onReconcile: () -> Unit = {},
    onViewExpiring: () -> Unit = {}
) {
    var stats by remember { mutableStateOf<DashboardStats?>(null) }
    var loading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    fun refresh() {
        UiSounds.click(context)
        loading = true
        scope.launch {
            stats = withContext(Dispatchers.IO) {
                if (isAdmin) {
                    Analytics.buildDashboard(app.api, app.session.authToken)
                } else {
                    Analytics.buildAgentDashboard(
                        app.api,
                        app.session.authToken,
                        agentId ?: 0L
                    )
                }
            }
            loading = false
        }
    }

    LaunchedEffect(isAdmin, agentId) { refresh() }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            CompactTopBar(
                title = {
                    Column {
                        Text(
                            "Temple Overview",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Bhagavad Darshan · all registrations",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                    IconButton(onClick = {
                        UiSounds.click(context)
                        onLogout()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout")
                    }
                }
            )
        }
    ) { padding ->
        val s = stats
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (loading && s == null) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Marigold)
                }
                return@Column
            }
            if (s == null) {
                Text("No data yet", style = MaterialTheme.typography.bodyLarge)
                return@Column
            }

            AccentSectionTitle("Registrations")

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FigmaStatCard(
                    value = "%,d".format(s.totalAll),
                    label = "Total",
                    modifier = Modifier.weight(1f)
                )
                FigmaStatCard(
                    value = "%,d".format(s.last12Months),
                    label = "Last 12 Mo",
                    modifier = Modifier.weight(1f)
                )
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FigmaStatCard(
                    value = "+%,d".format(s.todayCount),
                    label = "Today",
                    modifier = Modifier.weight(1f),
                    valueColor = Leaf,
                    containerColor = LeafContainer
                )
                FigmaStatCard(
                    value = "%,d".format(s.monthCount),
                    label = "This Month",
                    modifier = Modifier.weight(1f)
                )
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FigmaStatCard(
                    value = "%,d".format(s.yearCount),
                    label = "This Year",
                    modifier = Modifier.weight(1f)
                )
                FigmaStatCard(
                    value = "%,d".format(s.expiringNextMonth),
                    label = "Expiring Next Mo",
                    modifier = Modifier.weight(1f),
                    valueColor = Marigold,
                    containerColor = AmberPendingContainer
                )
            }

            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = BorderStroke(1.dp, Outline)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "Sales trend",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    SalesTrendChart(s.monthlyCounts, Marigold)
                }
            }

            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                border = BorderStroke(1.dp, Outline)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "By Plan",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    ByPlanStackedBar(s.byPlanYears)
                }
            }

            Text(
                "QUICK ACTIONS",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = InkSoft,
                letterSpacing = 0.8.sp
            )
            Row(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        UiSounds.click(context)
                        onAddCustomer()
                    },
                    border = BorderStroke(1.5.dp, Marigold),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Marigold)
                ) {
                    Text("+ Add Customer")
                }
                OutlinedButton(onClick = {
                    UiSounds.click(context)
                    onReconcile()
                }) {
                    Text("Reconcile")
                }
                OutlinedButton(onClick = {
                    UiSounds.click(context)
                    onViewExpiring()
                }) {
                    Text("View Expiring")
                }
            }

            Spacer(Modifier.height(ListBottomSafeGap))
        }
    }
}

@Composable
private fun ByPlanStackedBar(byPlanYears: Map<Int, Int>) {
    val segments = listOf(
        Triple(1, "1 Yr", Marigold),
        Triple(3, "3 Yr", SacredGold),
        Triple(5, "Life", TempleMaroon)
    )
    val counts = segments.map { (years, _, _) -> (byPlanYears[years] ?: 0).coerceAtLeast(0) }
    val total = counts.sum().coerceAtLeast(1)

    Box(
        Modifier
            .fillMaxWidth()
            .height(14.dp)
            .clip(RoundedCornerShape(50))
            .background(Color(0xFFF0EBE4))
    ) {
        Row(Modifier.fillMaxSize()) {
            segments.forEachIndexed { index, (_, _, color) ->
                val weight = counts[index].toFloat() / total.toFloat()
                if (weight > 0f) {
                    Box(
                        Modifier
                            .weight(weight)
                            .fillMaxSize()
                            .background(color)
                    )
                }
            }
        }
    }

    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        segments.forEachIndexed { index, (_, label, color) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    "$label · ${counts[index]}",
                    style = MaterialTheme.typography.labelMedium,
                    color = InkSoft
                )
            }
        }
    }
}
