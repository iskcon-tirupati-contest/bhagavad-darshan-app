package com.iskcon.bhagavaddarshan.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.iskcon.bhagavaddarshan.BhagavadDarshanApp
import com.iskcon.bhagavaddarshan.data.Agent
import com.iskcon.bhagavaddarshan.ui.components.ActiveGreenChip
import com.iskcon.bhagavaddarshan.ui.components.CompactTopBar
import com.iskcon.bhagavaddarshan.ui.components.EmptyState
import com.iskcon.bhagavaddarshan.ui.components.InitialsAvatar
import com.iskcon.bhagavaddarshan.ui.components.ListBottomSafeGap
import com.iskcon.bhagavaddarshan.ui.components.MetaChip
import com.iskcon.bhagavaddarshan.ui.components.StaggeredEntrance
import com.iskcon.bhagavaddarshan.ui.theme.AmberPending
import com.iskcon.bhagavaddarshan.ui.theme.AmberPendingContainer
import com.iskcon.bhagavaddarshan.ui.theme.Leaf
import com.iskcon.bhagavaddarshan.ui.theme.Marigold
import com.iskcon.bhagavaddarshan.ui.theme.SoftRed
import com.iskcon.bhagavaddarshan.ui.theme.SoftRedContainer
import com.iskcon.bhagavaddarshan.util.FormValidators
import com.iskcon.bhagavaddarshan.util.UiSounds
import kotlinx.coroutines.launch
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAgentsScreen(
    app: BhagavadDarshanApp,
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    onCreate: () -> Unit,
    showBack: Boolean = true
) {
    var agents by remember { mutableStateOf<List<Agent>>(emptyList()) }
    var monthCounts by remember { mutableStateOf<Map<Long, Long>>(emptyMap()) }
    var query by remember { mutableStateOf("") }
    var deleteId by remember { mutableStateOf<Long?>(null) }
    var reloadTick by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()
    val token = app.session.authToken
    val context = LocalContext.current

    suspend fun reload() {
        val json = app.api.listAgents(token).getOrThrow()
        val arr = json.optJSONArray("agents") ?: return
        val list = mutableListOf<Agent>()
        val counts = mutableMapOf<Long, Long>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val agent = o.toAgent()
            list += agent
            counts[agent.id] = o.optLong("thisMonthCount")
        }
        agents = list
        monthCounts = counts
    }

    LaunchedEffect(reloadTick) {
        runCatching { reload() }
    }

    val filtered = remember(agents, query) {
        if (query.isBlank()) agents
        else agents.filter {
            it.name.contains(query, true) || it.phone.contains(query)
        }
    }

    val activeCount = agents.count { it.active }
    val monthTotal = monthCounts.values.sum()

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            CompactTopBar(
                title = {
                    Text(
                        "Agents",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    if (showBack) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { /* filter optional */ }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    UiSounds.click(context)
                    onCreate()
                },
                containerColor = Marigold,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add agent", modifier = Modifier.size(28.dp))
            }
        }
    ) { padding ->
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                top = 8.dp,
                bottom = ListBottomSafeGap + 88.dp
            )
        ) {
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Search agents…") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )
            }
            item {
                Card(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = AmberPendingContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Text(
                        "$activeCount active agents · $monthTotal registrations this month",
                        modifier = Modifier.padding(14.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = AmberPending
                    )
                }
            }
            if (filtered.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Default.Groups,
                        title = "No agents found",
                        subtitle = "Tap + to register a field agent."
                    )
                }
            }
            itemsIndexed(filtered, key = { _, it -> it.id }) { index, agent ->
                StaggeredEntrance(index = index) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                UiSounds.click(context)
                                onEdit(agent.id)
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            Modifier
                                .border(1.dp, Color(0xFFE8E0D6), RoundedCornerShape(12.dp))
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            InitialsAvatar(
                                name = agent.name,
                                containerColor = Marigold
                            )
                            Column(Modifier.padding(start = 12.dp).weight(1f)) {
                                Text(
                                    agent.name,
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    agent.phone,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF6D6D6D)
                                )
                                Text(
                                    "This month: ${monthCounts[agent.id] ?: 0}",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Marigold
                                )
                                if (agent.address.isNotBlank()) {
                                    Text(
                                        agent.address,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF9E9E9E)
                                    )
                                }
                            }
                            if (agent.active) {
                                ActiveGreenChip("Active")
                            } else {
                                MetaChip(
                                    text = "Inactive",
                                    container = AmberPendingContainer,
                                    content = AmberPending
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    deleteId?.let { id ->
        AlertDialog(
            onDismissRequest = { deleteId = null },
            title = { Text("Delete agent?") },
            text = { Text("Login will be removed. Customer records stay.") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        app.api.deleteAgent(token, id)
                        reloadTick += 1
                        deleteId = null
                    }
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { deleteId = null }) { Text("Cancel") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAgentEditScreen(
    app: BhagavadDarshanApp,
    agentId: Long?,
    onDone: () -> Unit,
    onBack: () -> Unit
) {
    val isNew = agentId == null
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var active by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var planStats by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    LaunchedEffect(agentId) {
        if (agentId != null) {
            val json = app.api.getAgent(app.session.authToken, agentId).getOrNull()
            val a = json?.optJSONObject("agent") ?: json
            if (a != null) {
                name = a.optString("name")
                phone = a.optString("phone")
                address = a.optString("address")
                active = a.optBoolean("active", true)
                val month = a.optLong("thisMonthCount")
                if (month > 0L) planStats = "This month: $month registrations"
            }
        }
    }

    fun save() {
        FormValidators.name(name)?.let { error = it; return }
        FormValidators.phone(phone)?.let { error = it; return }
        if (isNew) FormValidators.password(password)?.let { error = it; return }
        if (!isNew && password.isNotBlank()) FormValidators.password(password)?.let { error = it; return }
        scope.launch {
            val body = JSONObject().apply {
                put("name", name.trim())
                put("phone", phone)
                put("address", address.trim())
                put("role", Agent.Role.AGENT)
                put("active", active)
                if (password.isNotBlank()) put("password", password)
            }
            val result = if (isNew) {
                app.api.createAgent(app.session.authToken, body)
            } else {
                app.api.updateAgent(app.session.authToken, agentId!!, body)
            }
            result.fold(
                onSuccess = { onDone() },
                onFailure = { error = it.message ?: "Could not save agent" }
            )
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            CompactTopBar(
                title = {
                    Text(
                        if (isNew) "Register agent" else "Edit Agent",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (planStats.isNotBlank()) {
                Card(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = AmberPendingContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Text(
                        planStats,
                        modifier = Modifier.padding(14.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = AmberPending,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    Modifier
                        .border(1.dp, Color(0xFFE8E0D6), RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it; error = null },
                        label = { Text("Name *") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it.filter(Char::isDigit).take(10); error = null },
                        label = { Text("Mobile *") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Address") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; error = null },
                        label = { Text(if (isNew) "Password *" else "New password (optional)") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    Modifier
                        .border(1.dp, Color(0xFFE8E0D6), RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Status", fontWeight = FontWeight.SemiBold)
                        Text(
                            if (active) "Agent can log in and register devotees"
                            else "Agent login is disabled",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF9E9E9E)
                        )
                    }
                    Switch(
                        checked = active,
                        onCheckedChange = { active = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Leaf
                        )
                    )
                }
            }

            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            Button(
                onClick = { save() },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Marigold)
            ) {
                Text(
                    if (isNew) "Create Agent" else "Save Changes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (!isNew && active) {
                OutlinedButton(
                    onClick = { active = false },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SoftRed),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SoftRed)
                ) {
                    Text("Deactivate", fontWeight = FontWeight.SemiBold)
                }
            }

            if (!isNew && !active) {
                Card(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = SoftRedContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Text(
                        "This agent is inactive. Toggle Status or Save Changes to keep deactivated.",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftRed
                    )
                }
            }
        }
    }
}

private fun JSONObject.toAgent(): Agent = Agent(
    id = optLong("id"),
    name = optString("name"),
    phone = optString("phone"),
    passwordHash = "",
    address = optString("address"),
    role = optString("role").ifBlank { Agent.Role.AGENT },
    active = optBoolean("active", true)
)
