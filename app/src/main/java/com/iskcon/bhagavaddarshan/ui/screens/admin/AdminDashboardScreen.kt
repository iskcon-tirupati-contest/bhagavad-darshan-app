package com.iskcon.bhagavaddarshan.ui.screens.admin

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iskcon.bhagavaddarshan.BhagavadDarshanApp
import com.iskcon.bhagavaddarshan.data.Analytics
import com.iskcon.bhagavaddarshan.data.DashboardStats
import com.iskcon.bhagavaddarshan.ui.components.AnimatedCounter
import com.iskcon.bhagavaddarshan.ui.components.SectionLabel
import com.iskcon.bhagavaddarshan.ui.components.StaggeredEntrance
import com.iskcon.bhagavaddarshan.ui.components.TempleHeroBanner
import com.iskcon.bhagavaddarshan.ui.theme.TempleHeroBrush
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
                Analytics.buildDashboard(app.api, app.session.authToken)
            }
            loading = false
        }
    }

    LaunchedEffect(Unit) { refresh() }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        val s = stats
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            TempleHeroBanner(brush = TempleHeroBrush) {
                Column {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                "Admin Analytics",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                "Decision dashboard",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                        Row {
                            IconButton(onClick = onBackToAgentHome) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                            }
                            IconButton(onClick = { refresh() }) {
                                Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.White)
                            }
                            IconButton(onClick = onAgents) {
                                Icon(Icons.Default.Groups, contentDescription = "Agents", tint = Color.White)
                            }
                            IconButton(onClick = onLogout) {
                                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout", tint = Color.White)
                            }
                        }
                    }
                }
            }

            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (loading && s == null) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                    return@Column
                }
                if (s == null) {
                    Text("No data yet", style = MaterialTheme.typography.bodyLarge)
                    return@Column
                }

                SectionLabel("Registrations snapshot")
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricTile("Total all", s.totalAll, Modifier.weight(1f), 0)
                    MetricTile("Last 12 mo", s.last12Months, Modifier.weight(1f), 1)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricTile("Today", s.todayCount, Modifier.weight(1f), 2)
                    MetricTile("This month", s.monthCount, Modifier.weight(1f), 3)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricTile("This year", s.yearCount, Modifier.weight(1f), 4)
                    MetricTile("Expire next mo", s.expiringNextMonth, Modifier.weight(1f), 5)
                }

                Card(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        SectionLabel("Sales trend (6 months)")
                        androidx.compose.foundation.layout.Spacer(Modifier.height(8.dp))
                        SalesTrendChart(s.monthlyCounts, MaterialTheme.colorScheme.primary)
                        val trend = if (s.monthlyCounts.size >= 2) {
                            val last = s.monthlyCounts.last().second
                            val prev = s.monthlyCounts[s.monthlyCounts.size - 2].second
                            when {
                                last > prev -> "▲ Increasing vs last month"
                                last < prev -> "▼ Decreasing vs last month"
                                else -> "▬ Flat vs last month"
                            }
                        } else "—"
                        androidx.compose.foundation.layout.Spacer(Modifier.height(4.dp))
                        Text(trend, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    }
                }

                Card(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        SectionLabel("By flyer plan (this year)")
                        androidx.compose.foundation.layout.Spacer(Modifier.height(4.dp))
                        listOf(1, 2, 3, 5).forEach { y ->
                            Text("$y year plan: ${s.byPlanYears[y] ?: 0}", style = MaterialTheme.typography.bodyMedium)
                        }
                        Text("Book-redeem subscriptions: ${s.byPlanMonthsBook}", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                Card(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        SectionLabel("Subscription drop rate")
                        androidx.compose.foundation.layout.Spacer(Modifier.height(4.dp))
                        Text("Last month: ${"%.1f".format(s.dropLastMonth)}%", style = MaterialTheme.typography.bodyMedium)
                        Text("Last 6 months: ${"%.1f".format(s.dropLast6Months)}%", style = MaterialTheme.typography.bodyMedium)
                        Text("Last year: ${"%.1f".format(s.dropLastYear)}%", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                Card(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        SectionLabel("Best performer (YTD)")
                        androidx.compose.foundation.layout.Spacer(Modifier.height(4.dp))
                        Text(
                            if (s.bestAgentName != null)
                                "${s.bestAgentName} — ${s.bestAgentCount} registrations"
                            else "No agent-attributed sales yet",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                TextButton(onClick = onAgents) { Text("Manage agents") }
            }
        }
    }
}

@Composable
private fun MetricTile(label: String, value: Long, modifier: Modifier = Modifier, index: Int = 0) {
    StaggeredEntrance(index = index, modifier = modifier) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(Modifier.padding(14.dp)) {
                Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                AnimatedCounter(
                    target = value,
                    style = MaterialTheme.typography.headlineMedium.copy(color = MaterialTheme.colorScheme.onPrimaryContainer)
                )
            }
        }
    }
}

/**
 * Animated area + line chart. Reveals with a bottom-anchored grow-in and draws a soft gradient
 * fill under the line so the trend reads at a glance.
 */
@Composable
fun SalesTrendChart(points: List<Pair<String, Int>>, lineColor: Color) {
    val reveal = remember { Animatable(0f) }
    LaunchedEffect(points) {
        reveal.snapTo(0f)
        reveal.animateTo(1f, animationSpec = tween(700))
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(150.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .alpha(reveal.value)
            ) {
                if (points.isEmpty()) return@Canvas
                val maxV = max(1, points.maxOf { it.second })
                val stepX = size.width / (points.size - 1).coerceAtLeast(1)

                val linePath = Path()
                val fillPath = Path()
                val coords = points.mapIndexed { i, (_, v) ->
                    val x = i * stepX
                    val y = size.height - (v / maxV.toFloat()) * size.height * 0.82f - size.height * 0.08f
                    Offset(x, y)
                }
                coords.forEachIndexed { i, o ->
                    if (i == 0) {
                        linePath.moveTo(o.x, o.y)
                        fillPath.moveTo(o.x, size.height)
                        fillPath.lineTo(o.x, o.y)
                    } else {
                        linePath.lineTo(o.x, o.y)
                        fillPath.lineTo(o.x, o.y)
                    }
                }
                if (coords.isNotEmpty()) {
                    fillPath.lineTo(coords.last().x, size.height)
                    fillPath.close()
                }

                // Faint horizontal guide line at the midpoint for scale reference.
                drawLine(
                    color = lineColor.copy(alpha = 0.12f),
                    start = Offset(0f, size.height * 0.5f),
                    end = Offset(size.width, size.height * 0.5f),
                    strokeWidth = 1.5f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                )

                drawPath(
                    fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(lineColor.copy(alpha = 0.28f), lineColor.copy(alpha = 0f))
                    )
                )
                drawPath(
                    linePath,
                    color = lineColor,
                    style = Stroke(width = 6f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
                coords.forEach { o ->
                    drawCircle(Color.White, radius = 9f, center = o)
                    drawCircle(lineColor, radius = 6f, center = o)
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            points.forEach { (label, _) ->
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
        }
    }
}

