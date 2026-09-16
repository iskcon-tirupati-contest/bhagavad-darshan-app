package com.iskcon.bhagavaddarshan.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iskcon.bhagavaddarshan.BhagavadDarshanApp
import com.iskcon.bhagavaddarshan.data.Analytics
import com.iskcon.bhagavaddarshan.data.DashboardStats
import com.iskcon.bhagavaddarshan.data.PlanBreakdown
import com.iskcon.bhagavaddarshan.ui.components.CompactTopBar
import com.iskcon.bhagavaddarshan.ui.components.FigmaStatCard
import com.iskcon.bhagavaddarshan.ui.components.ListBottomSafeGap
import com.iskcon.bhagavaddarshan.ui.components.PremiumPanel
import com.iskcon.bhagavaddarshan.ui.components.PremiumScreenBackdrop
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
import java.util.Calendar
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
    @Suppress("UNUSED_PARAMETER") onAddCustomer: () -> Unit = {},
    @Suppress("UNUSED_PARAMETER") onReconcile: () -> Unit = {},
    @Suppress("UNUSED_PARAMETER") onViewExpiring: () -> Unit = {}
) {
    val currentYear = remember { Calendar.getInstance().get(Calendar.YEAR) }
    var stats by remember { mutableStateOf<DashboardStats?>(null) }
    var loading by remember { mutableStateOf(true) }
    var selectedYear by remember { mutableStateOf(currentYear) }
    var yearMenuOpen by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    fun refresh(year: Int = selectedYear) {
        UiSounds.click(context)
        loading = true
        scope.launch {
            val loaded = withContext(Dispatchers.IO) {
                if (isAdmin) {
                    Analytics.buildDashboard(
                        app.api,
                        app.session.authToken,
                        year = year
                    )
                } else {
                    Analytics.buildAgentDashboard(
                        app.api,
                        app.session.authToken,
                        agentId ?: 0L
                    )
                }
            }
            stats = loaded
            loading = false
        }
    }

    LaunchedEffect(isAdmin, agentId) { refresh(currentYear) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = Color.Transparent,
        topBar = {
            CompactTopBar(
                title = {
                    Text(
                        "Registration Dashboard",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
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
        PremiumScreenBackdrop(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
        Column(
            Modifier
                .fillMaxSize()
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

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FigmaStatCard(
                    value = "+%,d".format(s.todayCount),
                    label = "Today",
                    modifier = Modifier.weight(1f),
                    valueColor = Leaf,
                    containerColor = LeafContainer,
                    iconTint = Leaf
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
                    value = "%,d".format(s.totalAll),
                    label = "Total",
                    modifier = Modifier.weight(1f),
                    valueColor = TempleMaroon,
                    iconTint = TempleMaroon
                )
            }
            FigmaStatCard(
                value = "%,d".format(s.expiringNextMonth),
                label = "Expiring Next Month",
                modifier = Modifier.fillMaxWidth(),
                valueColor = Marigold,
                containerColor = AmberPendingContainer,
                iconTint = Marigold,
                compact = true
            )

            PremiumPanel {
                Column(Modifier.padding(16.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Registration trend",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        if (isAdmin) {
                            val years = remember(s.availableYears, currentYear) {
                                (s.availableYears + currentYear).distinct().sortedDescending()
                            }
                            ExposedDropdownMenuBox(
                                expanded = yearMenuOpen,
                                onExpandedChange = { yearMenuOpen = it },
                                modifier = Modifier.width(108.dp)
                            ) {
                                OutlinedTextField(
                                    value = selectedYear.toString(),
                                    onValueChange = {},
                                    readOnly = true,
                                    singleLine = true,
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = yearMenuOpen)
                                    },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth(),
                                    textStyle = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        textAlign = TextAlign.Center
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Marigold,
                                        unfocusedBorderColor = Outline.copy(alpha = 0.7f),
                                        focusedContainerColor = Color(0xFFFFF3E0),
                                        unfocusedContainerColor = Color(0xFFFFF8F0)
                                    )
                                )
                                ExposedDropdownMenu(
                                    expanded = yearMenuOpen,
                                    onDismissRequest = { yearMenuOpen = false }
                                ) {
                                    years.forEach { y ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    y.toString(),
                                                    fontWeight = if (y == selectedYear) {
                                                        FontWeight.Bold
                                                    } else {
                                                        FontWeight.Normal
                                                    }
                                                )
                                            },
                                            onClick = {
                                                yearMenuOpen = false
                                                selectedYear = y
                                                refresh(y)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    SalesTrendChart(s.monthlyCounts, Marigold)
                }
            }

            PremiumPanel {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "By Plan",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    ByPlanPieChart(
                        if (s.byPlan.isNotEmpty()) s.byPlan
                        else s.byPlanYears.map { (m, c) -> PlanBreakdown(m, "$m Mo", c) }
                    )
                }
            }

            Spacer(Modifier.height(ListBottomSafeGap))
        }
        }
    }
}

private val PlanPiePalette = listOf(
    Marigold,
    SacredGold,
    TempleMaroon,
    Leaf,
    Color(0xFF5C6BC0),
    Color(0xFF00897B)
)

@Composable
private fun ByPlanPieChart(byPlan: List<PlanBreakdown>) {
    val segments = byPlan.filter { it.count > 0 }
    if (segments.isEmpty()) {
        Text("No plan data", style = MaterialTheme.typography.bodyMedium, color = InkSoft)
        return
    }
    val total = segments.sumOf { it.count }.coerceAtLeast(1)

    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Canvas(Modifier.size(148.dp)) {
            val diameter = size.minDimension
            val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
            val arcSize = Size(diameter, diameter)
            var start = -90f
            segments.forEachIndexed { index, seg ->
                val sweep = 360f * (seg.count.toFloat() / total.toFloat())
                drawArc(
                    color = PlanPiePalette[index % PlanPiePalette.size],
                    startAngle = start,
                    sweepAngle = sweep.coerceAtLeast(0.5f),
                    useCenter = true,
                    topLeft = topLeft,
                    size = arcSize
                )
                start += sweep
            }
            // Soft hole for a donut look
            val hole = diameter * 0.48f
            drawCircle(
                color = Color.White,
                radius = hole / 2f,
                center = Offset(size.width / 2f, size.height / 2f)
            )
        }

        Column(
            Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            segments.forEachIndexed { index, seg ->
                val pct = (seg.count * 1000 / total) / 10.0
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(PlanPiePalette[index % PlanPiePalette.size])
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "${seg.label}",
                        style = MaterialTheme.typography.labelMedium,
                        color = InkSoft,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "${seg.count}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = TempleMaroon
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "${"%.0f".format(pct)}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = InkSoft,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
