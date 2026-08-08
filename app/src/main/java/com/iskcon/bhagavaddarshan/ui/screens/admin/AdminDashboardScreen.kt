package com.iskcon.bhagavaddarshan.ui.screens.admin

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iskcon.bhagavaddarshan.BhagavadDarshanApp
import com.iskcon.bhagavaddarshan.data.Analytics
import com.iskcon.bhagavaddarshan.data.DashboardStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    app: BhagavadDarshanApp,
    onAgents: () -> Unit,
    onBackToAgentHome: () -> Unit,
    onLogout: () -> Unit
) {
    var stats by remember { mutableStateOf<DashboardStats?>(null) }
    var loading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    fun refresh() {
        loading = true
        scope.launch {
            stats = withContext(Dispatchers.IO) {
                Analytics.buildDashboard(app.repository, app.database.agentDao())
            }
            loading = false
        }
    }

    LaunchedEffect(Unit) { refresh() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Admin Analytics")
                        Text("Decision dashboard", style = MaterialTheme.typography.labelMedium)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackToAgentHome) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                    IconButton(onClick = onAgents) {
                        Icon(Icons.Default.Groups, contentDescription = "Agents")
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (loading && s == null) {
                Text("Loading analytics…")
                return@Column
            }
            if (s == null) {
                Text("No data yet")
                return@Column
            }

            Text("Registrations snapshot", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricTile("Total all", s.totalAll.toString(), Modifier.weight(1f))
                MetricTile("Last 12 mo", s.last12Months.toString(), Modifier.weight(1f))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricTile("Today", s.todayCount.toString(), Modifier.weight(1f))
                MetricTile("This month", s.monthCount.toString(), Modifier.weight(1f))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricTile("This year", s.yearCount.toString(), Modifier.weight(1f))
                MetricTile("Expire next mo", s.expiringNextMonth.toString(), Modifier.weight(1f))
            }

            Text("Sales trend (6 months)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            SalesTrendChart(s.monthlyCounts, MaterialTheme.colorScheme.primary)
            val trend = if (s.monthlyCounts.size >= 2) {
                val last = s.monthlyCounts.last().second
                val prev = s.monthlyCounts[s.monthlyCounts.size - 2].second
                when {
                    last > prev -> "Increasing vs last month"
                    last < prev -> "Decreasing vs last month"
                    else -> "Flat vs last month"
                }
            } else "—"
            Text(trend, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)

            Text("By flyer plan (this year)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            listOf(1, 2, 3, 5).forEach { y ->
                Text("$y year plan: ${s.byPlanYears[y] ?: 0}")
            }
            Text("Book-redeem subscriptions: ${s.byPlanMonthsBook}")

            Text("Subscription drop rate", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Last month: ${"%.1f".format(s.dropLastMonth)}%")
            Text("Last 6 months: ${"%.1f".format(s.dropLast6Months)}%")
            Text("Last year: ${"%.1f".format(s.dropLastYear)}%")

            Text("Best performer (YTD)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                if (s.bestAgentName != null)
                    "${s.bestAgentName} — ${s.bestAgentCount} registrations"
                else "No agent-attributed sales yet"
            )

            TextButton(onClick = onAgents) { Text("Manage agents") }
        }
    }
}

@Composable
private fun MetricTile(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(label, style = MaterialTheme.typography.labelLarge)
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SalesTrendChart(points: List<Pair<String, Int>>, lineColor: Color) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        ) {
            if (points.isEmpty()) return@Canvas
            val maxV = max(1, points.maxOf { it.second })
            val stepX = size.width / (points.size - 1).coerceAtLeast(1)
            val linePath = Path()
            points.forEachIndexed { i, (_, v) ->
                val x = i * stepX
                val y = size.height - (v / maxV.toFloat()) * size.height * 0.9f - size.height * 0.05f
                if (i == 0) linePath.moveTo(x, y) else linePath.lineTo(x, y)
                drawCircle(lineColor, radius = 6f, center = Offset(x, y))
            }
            drawPath(linePath, color = lineColor, style = Stroke(width = 4f))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            points.forEach { (label, _) ->
                Text(label, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
