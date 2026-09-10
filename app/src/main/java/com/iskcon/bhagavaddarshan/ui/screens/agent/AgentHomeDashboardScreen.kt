package com.iskcon.bhagavaddarshan.ui.screens.agent

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.ViewWeek
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iskcon.bhagavaddarshan.BhagavadDarshanApp
import com.iskcon.bhagavaddarshan.data.Analytics
import com.iskcon.bhagavaddarshan.data.DashboardStats
import com.iskcon.bhagavaddarshan.ui.components.AgentHeaderTitle
import com.iskcon.bhagavaddarshan.ui.components.FigmaStatCard
import com.iskcon.bhagavaddarshan.ui.components.GradientTopBar
import com.iskcon.bhagavaddarshan.ui.components.PressableFab
import com.iskcon.bhagavaddarshan.ui.components.ShadowCard
import com.iskcon.bhagavaddarshan.ui.screens.admin.SalesTrendChart
import com.iskcon.bhagavaddarshan.ui.theme.AgentHeroBrush
import com.iskcon.bhagavaddarshan.ui.theme.Leaf
import com.iskcon.bhagavaddarshan.ui.theme.LeafContainer
import com.iskcon.bhagavaddarshan.ui.theme.Marigold
import com.iskcon.bhagavaddarshan.ui.theme.TempleGreen
import com.iskcon.bhagavaddarshan.util.UiSounds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private enum class TrendPeriod(val api: String, val label: String) {
    WEEK("week", "Week"),
    MONTH("month", "Month"),
    YEAR("year", "Year")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentHomeDashboardScreen(
    app: BhagavadDarshanApp,
    agentId: Long,
    agentName: String,
    onLogout: () -> Unit,
    onAddDevotee: () -> Unit
) {
    var stats by remember { mutableStateOf<DashboardStats?>(null) }
    var loading by remember { mutableStateOf(true) }
    var period by remember { mutableStateOf(TrendPeriod.WEEK) }
    var slideForward by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    fun refresh(selected: TrendPeriod = period) {
        loading = true
        scope.launch {
            stats = withContext(Dispatchers.IO) {
                runCatching {
                    Analytics.buildAgentDashboard(
                        app.api,
                        app.session.authToken,
                        agentId,
                        selected.api
                    )
                }.getOrNull()
            }
            loading = false
        }
    }

    fun setPeriod(next: TrendPeriod, forward: Boolean) {
        if (next == period) return
        slideForward = forward
        UiSounds.click(context)
        period = next
    }

    fun shiftPeriod(delta: Int) {
        val values = TrendPeriod.entries
        val idx = values.indexOf(period)
        val next = values.getOrNull(idx + delta) ?: return
        setPeriod(next, forward = delta > 0)
    }

    LaunchedEffect(agentId, period) { refresh(period) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            PressableFab(onClick = {
                UiSounds.click(context)
                onAddDevotee()
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add devotee", tint = androidx.compose.ui.graphics.Color.White)
            }
        },
        topBar = {
            GradientTopBar(
                brush = AgentHeroBrush,
                title = {
                    AgentHeaderTitle(
                        title = "🙏 Hare Krishna",
                        subtitle = agentName.ifBlank { "Field agent" },
                        singleLine = true
                    )
                },
                actions = {
                    IconButton(onClick = { refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = TempleGreen)
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout", tint = TempleGreen)
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
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (loading && s == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Marigold)
                }
                return@Column
            }
            if (s == null) {
                Text("No data yet", style = MaterialTheme.typography.bodyLarge)
                return@Column
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    Icons.Default.VolunteerActivism,
                    contentDescription = null,
                    tint = TempleGreen,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    "Devotees referred",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = TempleGreen
                )
            }

            // Perfect 2×2 — Today · Week · Month · Total
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FigmaStatCard(
                    value = "+%,d".format(s.todayCount),
                    label = "Today",
                    modifier = Modifier.weight(1f),
                    valueColor = Leaf,
                    containerColor = LeafContainer,
                    icon = Icons.Default.Today,
                    iconTint = Leaf,
                    compact = true
                )
                FigmaStatCard(
                    value = "%,d".format(s.weekCount),
                    label = "Week",
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.ViewWeek,
                    compact = true
                )
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FigmaStatCard(
                    value = "%,d".format(s.monthCount),
                    label = "Month",
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.CalendarMonth,
                    compact = true
                )
                FigmaStatCard(
                    value = "%,d".format(s.totalAll),
                    label = "Total",
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Groups,
                    compact = true
                )
            }

            ShadowCard(modifier = Modifier.fillMaxWidth().weight(1f)) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.ShowChart,
                            contentDescription = null,
                            tint = Marigold,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            "Registrations trend",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = { shiftPeriod(-1) },
                            enabled = period != TrendPeriod.WEEK,
                            modifier = Modifier.height(36.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous")
                        }
                        Text(
                            when (period) {
                                TrendPeriod.WEEK -> "📅 Week"
                                TrendPeriod.MONTH -> "🗓️ Month"
                                TrendPeriod.YEAR -> "📆 Year"
                            },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Marigold
                        )
                        IconButton(
                            onClick = { shiftPeriod(1) },
                            enabled = period != TrendPeriod.YEAR,
                            modifier = Modifier.height(36.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next")
                        }
                    }

                    var dragAcc by remember { mutableFloatStateOf(0f) }
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .pointerInput(period) {
                                detectHorizontalDragGestures(
                                    onDragEnd = {
                                        when {
                                            dragAcc < -80f -> shiftPeriod(1)
                                            dragAcc > 80f -> shiftPeriod(-1)
                                        }
                                        dragAcc = 0f
                                    },
                                    onHorizontalDrag = { _, amount ->
                                        dragAcc += amount
                                    }
                                )
                            }
                    ) {
                        AnimatedContent(
                            targetState = period,
                            transitionSpec = {
                                if (slideForward) {
                                    (slideInHorizontally(tween(320)) { it } + fadeIn(tween(220)))
                                        .togetherWith(slideOutHorizontally(tween(280)) { -it / 3 } + fadeOut(tween(180)))
                                } else {
                                    (slideInHorizontally(tween(320)) { -it } + fadeIn(tween(220)))
                                        .togetherWith(slideOutHorizontally(tween(280)) { it / 3 } + fadeOut(tween(180)))
                                }
                            },
                            label = "trendPeriod",
                            modifier = Modifier.fillMaxSize()
                        ) { current ->
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                if (loading) {
                                    CircularProgressIndicator(color = Marigold, strokeWidth = 2.dp)
                                } else if (s.monthlyCounts.isEmpty()) {
                                    Text(
                                        "No registrations in ${current.label.lowercase()} yet",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    SalesTrendChart(s.monthlyCounts, Marigold, chartHeight = 110.dp)
                                }
                            }
                        }
                    }
                    Text(
                        "👉 Swipe or arrows · Week · Month · Year",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
