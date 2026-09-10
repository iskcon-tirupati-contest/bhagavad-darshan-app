package com.iskcon.bhagavaddarshan.ui.screens.agent

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iskcon.bhagavaddarshan.BhagavadDarshanApp
import com.iskcon.bhagavaddarshan.ui.components.AgentHeaderTitle
import com.iskcon.bhagavaddarshan.ui.components.EmptyState
import com.iskcon.bhagavaddarshan.ui.components.GradientTopBar
import com.iskcon.bhagavaddarshan.ui.components.InitialsAvatar
import com.iskcon.bhagavaddarshan.ui.components.ListBottomSafeGap
import com.iskcon.bhagavaddarshan.ui.components.PressableDeleteIconButton
import com.iskcon.bhagavaddarshan.ui.components.PressableDangerButton
import com.iskcon.bhagavaddarshan.ui.components.PressableFab
import com.iskcon.bhagavaddarshan.ui.components.PressableOutlineButton
import com.iskcon.bhagavaddarshan.ui.components.ShadowCard
import com.iskcon.bhagavaddarshan.ui.components.SoftSearchField
import com.iskcon.bhagavaddarshan.ui.components.StaggeredEntrance
import com.iskcon.bhagavaddarshan.ui.theme.AgentHeroBrush
import com.iskcon.bhagavaddarshan.ui.theme.InkSoft
import com.iskcon.bhagavaddarshan.ui.theme.Marigold
import com.iskcon.bhagavaddarshan.ui.theme.TempleGreen
import com.iskcon.bhagavaddarshan.util.UiSounds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class DevoteeRow(
    val id: Long,
    val name: String,
    val phone: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevoteesScreen(
    app: BhagavadDarshanApp,
    agentId: Long,
    onAddDevotee: () -> Unit,
    onEditDevotee: (Long) -> Unit,
    onLogout: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var rows by remember { mutableStateOf<List<DevoteeRow>>(emptyList()) }
    var pendingDelete by remember { mutableStateOf<DevoteeRow?>(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    fun load() {
        loading = true
        error = null
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                app.api.listDevotees(app.session.authToken, q = query.trim(), agentId = agentId)
            }
            loading = false
            result.onSuccess { json ->
                val arr = json.optJSONArray("devotees")
                val list = mutableListOf<DevoteeRow>()
                if (arr != null) {
                    for (i in 0 until arr.length()) {
                        val o = arr.getJSONObject(i)
                        list += DevoteeRow(
                            id = o.optLong("id"),
                            name = o.optString("name"),
                            phone = o.optString("phone")
                        )
                    }
                }
                rows = list
            }.onFailure { error = it.message }
        }
    }

    LaunchedEffect(query) { load() }

    pendingDelete?.let { row ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("🗑️ Delete?", fontWeight = FontWeight.Bold) },
            text = { Text("Do you want to delete this devotee?") },
            dismissButton = {
                PressableOutlineButton(
                    text = "No",
                    onClick = {
                        UiSounds.click(context)
                        pendingDelete = null
                    }
                )
            },
            confirmButton = {
                PressableDangerButton(
                    text = "Yes",
                    onClick = {
                        val id = row.id
                        pendingDelete = null
                        scope.launch {
                            val result = withContext(Dispatchers.IO) {
                                app.api.draftDevotee(app.session.authToken, id)
                            }
                            result.onSuccess {
                                UiSounds.delete(context)
                                Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show()
                                load()
                            }.onFailure {
                                UiSounds.error(context)
                                Toast.makeText(context, it.message ?: "Could not delete", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            PressableFab(onClick = {
                UiSounds.click(context)
                onAddDevotee()
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add devotee", tint = Color.White)
            }
        },
        topBar = {
            GradientTopBar(
                brush = AgentHeroBrush,
                title = { AgentHeaderTitle(title = "Devotees") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(
                            Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Logout",
                            tint = TempleGreen
                        )
                    }
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
                placeholder = "🔍 Search by name or phone"
            )
            Spacer(Modifier.height(12.dp))
            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(8.dp))
            }
            if (loading && rows.isEmpty()) {
                Column(
                    Modifier.fillMaxWidth().weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(color = Marigold)
                }
            } else if (rows.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.Add,
                    title = "No devotees yet",
                    subtitle = "Tap + to add name and mobile number",
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = ListBottomSafeGap + 72.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(rows, key = { _, r -> r.id }) { index, row ->
                        StaggeredEntrance(index = index) {
                            ShadowCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                onClick = {
                                    UiSounds.click(context)
                                    onEditDevotee(row.id)
                                }
                            ) {
                                Row(
                                    Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    InitialsAvatar(name = row.name)
                                    Spacer(Modifier.width(12.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(row.name, fontWeight = FontWeight.SemiBold)
                                        Text("📱 ${row.phone}", color = InkSoft, style = MaterialTheme.typography.bodyMedium)
                                    }
                                    PressableDeleteIconButton(
                                        onClick = { pendingDelete = row }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
