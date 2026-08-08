package com.iskcon.bhagavaddarshan.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iskcon.bhagavaddarshan.BhagavadDarshanApp
import com.iskcon.bhagavaddarshan.data.Analytics
import com.iskcon.bhagavaddarshan.data.DashboardStats
import com.iskcon.bhagavaddarshan.ui.components.CompactTopBar
import com.iskcon.bhagavaddarshan.ui.screens.admin.SalesTrendChart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeDashboardScreen(
    app: BhagavadDarshanApp,
    isAdmin: Boolean,
    agentId: Long?,
    agentName: String,
    onLogout: () -> Unit
) {
    var stats by remember { mutableStateOf<DashboardStats?>(null) }
    var loading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    fun refresh() {
        loading = true
        scope.launch {
            stats = withContext(Dispatchers.IO) {
                if (isAdmin) {
                    Analytics.buildDashboard(app.repository, app.database.agentDao())
                } else {
                    Analytics.buildAgentDashboard(
                        app.repository,
                        app.database.agentDao(),
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
                    Text(
                        if (isAdmin) "Temple overview" else "My stats",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                actions = {
                    IconButton(onClick = { refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        val s = stats
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (loading && s == null) {
                Text("Loading analytics…")
                return@Column
            }
            if (s == null) {
                Text("No data yet")
                return@Column
            }

            Text("Registrations", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                MetricTile("Total", s.totalAll.toString(), Modifier.weight(1f))
                MetricTile("Last 12 mo", s.last12Months.toString(), Modifier.weight(1f))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                MetricTile("Today", s.todayCount.toString(), Modifier.weight(1f))
                MetricTile("This month", s.monthCount.toString(), Modifier.weight(1f))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                MetricTile("This year", s.yearCount.toString(), Modifier.weight(1f))
                MetricTile("Expire next mo", s.expiringNextMonth.toString(), Modifier.weight(1f))
            }

            Text("Sales trend (6 months)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            SalesTrendChart(s.monthlyCounts, MaterialTheme.colorScheme.primary)

            Text("By flyer plan", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            listOf(1, 2, 3, 5).forEach { y ->
                Text("$y year plan: ${s.byPlanYears[y] ?: 0}")
            }
            Text("Book-redeem: ${s.byPlanMonthsBook}")

            if (isAdmin) {
                Text("Best performer (YTD)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(
                    if (s.bestAgentName != null)
                        "${s.bestAgentName} — ${s.bestAgentCount} registrations"
                    else "Most records are self-registered (website / PDF import)"
                )
            }

            Text("Drop rate", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text("Last month: ${"%.1f".format(s.dropLastMonth)}%")
            Text("Last 6 months: ${"%.1f".format(s.dropLast6Months)}%")
            Text("Last year: ${"%.1f".format(s.dropLastYear)}%")
        }
    }
}

@Composable
private fun MetricTile(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}
