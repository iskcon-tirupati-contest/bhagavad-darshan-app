package com.iskcon.bhagavaddarshan.ui.screens.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iskcon.bhagavaddarshan.BhagavadDarshanApp
import com.iskcon.bhagavaddarshan.ui.components.CompactTopBar
import com.iskcon.bhagavaddarshan.ui.components.EmptyState
import com.iskcon.bhagavaddarshan.ui.components.ListBottomSafeGap
import com.iskcon.bhagavaddarshan.ui.components.PressableOutlineButton
import com.iskcon.bhagavaddarshan.ui.components.PressablePrimaryButton
import com.iskcon.bhagavaddarshan.ui.components.ShadowCard
import com.iskcon.bhagavaddarshan.ui.components.SoftSearchField
import com.iskcon.bhagavaddarshan.ui.components.StaggeredEntrance
import com.iskcon.bhagavaddarshan.ui.theme.Leaf
import com.iskcon.bhagavaddarshan.ui.theme.Marigold
import com.iskcon.bhagavaddarshan.util.UiSounds
import kotlinx.coroutines.launch

private data class AdminComplaintRow(
    val id: Long,
    val name: String,
    val phone: String,
    val categoryLabel: String,
    val deviceModel: String,
    val message: String,
    val status: String,
    val source: String,
    val createdAt: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminComplaintsScreen(app: BhagavadDarshanApp) {
    var rows by remember { mutableStateOf<List<AdminComplaintRow>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var query by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf("open") } // open | resolved | all
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val token = app.session.authToken

    fun load() {
        loading = true
        error = null
        scope.launch {
            val statusParam = when (statusFilter) {
                "all" -> null
                else -> statusFilter
            }
            val result = app.api.listAdminComplaints(
                token = token,
                q = query.trim(),
                status = statusParam
            )
            loading = false
            result.onSuccess { json ->
                val arr = json.optJSONArray("complaints")
                val list = mutableListOf<AdminComplaintRow>()
                if (arr != null) {
                    for (i in 0 until arr.length()) {
                        val o = arr.getJSONObject(i)
                        list += AdminComplaintRow(
                            id = o.optLong("id"),
                            name = o.optString("name"),
                            phone = o.optString("phone"),
                            categoryLabel = o.optString("categoryLabel").ifBlank {
                                o.optString("category")
                            },
                            deviceModel = o.optString("deviceModel"),
                            message = o.optString("message"),
                            status = o.optString("status"),
                            source = o.optString("source"),
                            createdAt = o.optString("createdAt")
                        )
                    }
                }
                rows = list
            }.onFailure {
                error = it.message ?: "Could not load complaints"
            }
        }
    }

    LaunchedEffect(statusFilter, query) { load() }

    Scaffold(
        topBar = {
            CompactTopBar(
                title = {
                    Text("📝 Complaints", fontWeight = FontWeight.Bold)
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            SoftSearchField(
                value = query,
                onValueChange = { query = it },
                placeholder = "Search name, phone, issue"
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("open" to "Open", "resolved" to "Resolved", "all" to "All").forEach { (key, label) ->
                    FilterChip(
                        selected = statusFilter == key,
                        onClick = { statusFilter = key },
                        label = { Text(label) }
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(8.dp))
            }
            when {
                loading && rows.isEmpty() -> {
                    Column(
                        Modifier.fillMaxWidth().weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = Marigold)
                    }
                }
                rows.isEmpty() -> {
                    EmptyState(
                        icon = Icons.Default.ReportProblem,
                        title = "No complaints",
                        subtitle = when (statusFilter) {
                            "open" -> "No open complaints right now"
                            "resolved" -> "No resolved complaints yet"
                            else -> "Complaints from download page & app will show here"
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = ListBottomSafeGap),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        itemsIndexed(rows, key = { _, r -> r.id }) { index, row ->
                            StaggeredEntrance(index = index) {
                                ComplaintCard(
                                    row = row,
                                    onResolve = {
                                        UiSounds.click(context)
                                        scope.launch {
                                            app.api.updateComplaintStatus(
                                                token,
                                                row.id,
                                                if (row.status == "open") "resolved" else "open"
                                            ).onSuccess {
                                                UiSounds.success(context)
                                                load()
                                            }.onFailure {
                                                UiSounds.error(context)
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ComplaintCard(
    row: AdminComplaintRow,
    onResolve: () -> Unit
) {
    val isOpen = row.status == "open"
    ShadowCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(row.name.ifBlank { "Unknown" }, fontWeight = FontWeight.SemiBold)
                Text(
                    if (isOpen) "OPEN" else row.status.uppercase(),
                    color = if (isOpen) Marigold else Leaf,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Text("📱 ${row.phone}", style = MaterialTheme.typography.bodyMedium)
            if (row.deviceModel.isNotBlank()) {
                Text("📱 Model: ${row.deviceModel}", style = MaterialTheme.typography.bodySmall)
            }
            Text(
                "⚠️ ${row.categoryLabel.ifBlank { "Issue" }}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            if (row.message.isNotBlank()) {
                Text(row.message, style = MaterialTheme.typography.bodySmall)
            }
            Text(
                when (row.source) {
                    "agent_support" -> "Source: Agent support"
                    "app" -> "Source: Customer app"
                    else -> "Source: ${row.source}"
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            if (isOpen) {
                PressablePrimaryButton(
                    text = "✅ Mark resolved",
                    onClick = onResolve,
                    compact = true,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                PressableOutlineButton(
                    text = "Reopen",
                    onClick = onResolve,
                    compact = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
