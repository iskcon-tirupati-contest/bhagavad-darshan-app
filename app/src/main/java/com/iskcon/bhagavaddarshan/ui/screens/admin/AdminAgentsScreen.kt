package com.iskcon.bhagavaddarshan.ui.screens.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.iskcon.bhagavaddarshan.BhagavadDarshanApp
import com.iskcon.bhagavaddarshan.data.Agent
import com.iskcon.bhagavaddarshan.data.Analytics
import com.iskcon.bhagavaddarshan.data.PasswordHasher
import com.iskcon.bhagavaddarshan.ui.components.CompactTopBar
import com.iskcon.bhagavaddarshan.util.FormValidators
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.YearMonth

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
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        app.database.agentDao().observeAgentsOnly().collect { list ->
            agents = list
            val ym = YearMonth.now()
            val from = Analytics.monthStart(ym)
            val to = Analytics.monthEnd(ym)
            monthCounts = withContext(Dispatchers.IO) {
                list.associate { a ->
                    a.id to app.repository.countByAgentBetween(a.id, from, to)
                }
            }
        }
    }

    val filtered = remember(agents, query) {
        if (query.isBlank()) agents
        else agents.filter {
            it.name.contains(query, true) || it.phone.contains(query)
        }
    }

    Scaffold(
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        topBar = {
            CompactTopBar(
                title = { Text("Agents", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    if (showBack) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreate) {
                Icon(Icons.Default.Add, contentDescription = "Add agent")
            }
        }
    ) { padding ->
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Search agents") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            item {
                Text(
                    "Tap to edit · ${filtered.size} agents",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            items(filtered, key = { it.id }) { agent ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onEdit(agent.id) }
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Text(agent.name, fontWeight = FontWeight.Bold)
                        Text("${agent.phone} · ${if (agent.active) "Active" else "Inactive"}")
                        Text("This month: ${monthCounts[agent.id] ?: 0}")
                        if (agent.address.isNotBlank()) {
                            Text(agent.address, style = MaterialTheme.typography.bodySmall)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = { onEdit(agent.id) }) { Text("Edit") }
                            TextButton(onClick = { deleteId = agent.id }) { Text("Delete") }
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
                        withContext(Dispatchers.IO) {
                            app.database.agentDao().deleteById(id)
                        }
                        deleteId = null
                    }
                }) { Text("Delete") }
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

    androidx.compose.runtime.LaunchedEffect(agentId) {
        if (agentId != null) {
            val a = withContext(Dispatchers.IO) { app.database.agentDao().getById(agentId) }
            if (a != null) {
                name = a.name
                phone = a.phone
                address = a.address
                active = a.active
            }
            val ym = YearMonth.now()
            val list = withContext(Dispatchers.IO) {
                app.repository.listBetween(Analytics.monthStart(ym), Analytics.monthEnd(ym))
                    .filter { it.agentId == agentId }
            }
            val byPlan = list.groupingBy { it.planYears }.eachCount()
            planStats = "This month by plan: " + listOf(1, 2, 3, 5).joinToString { y ->
                "${y}y=${byPlan[y] ?: 0}"
            } + " · books=${list.count { it.bookSaleAmount > 0 }}"
        }
    }

    Scaffold(
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        topBar = {
            CompactTopBar(
                title = {
                    Text(
                        if (isNew) "Register agent" else "Edit agent",
                        style = MaterialTheme.typography.titleMedium
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
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (planStats.isNotBlank()) Text(planStats, style = MaterialTheme.typography.bodyMedium)
            OutlinedTextField(value = name, onValueChange = { name = it; error = null }, label = { Text("Name *") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it.filter(Char::isDigit).take(10); error = null },
                label = { Text("Mobile *") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Address") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(
                value = password,
                onValueChange = { password = it; error = null },
                label = { Text(if (isNew) "Password *" else "New password (optional)") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Active", modifier = Modifier.weight(1f))
                Switch(checked = active, onCheckedChange = { active = it })
            }
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Button(
                onClick = {
                    FormValidators.name(name)?.let { error = it; return@Button }
                    FormValidators.phone(phone)?.let { error = it; return@Button }
                    if (isNew) FormValidators.password(password)?.let { error = it; return@Button }
                    if (!isNew && password.isNotBlank()) FormValidators.password(password)?.let { error = it; return@Button }
                    scope.launch {
                        val result = withContext(Dispatchers.IO) {
                            val dao = app.database.agentDao()
                            if (isNew) {
                                if (dao.findByPhone(phone) != null) return@withContext "Mobile already registered"
                                dao.insert(
                                    Agent(
                                        name = name.trim(),
                                        phone = phone,
                                        passwordHash = PasswordHasher.hash(password),
                                        address = address.trim(),
                                        role = Agent.Role.AGENT,
                                        active = active
                                    )
                                )
                                null
                            } else {
                                val cur = dao.getById(agentId!!) ?: return@withContext "Agent not found"
                                val other = dao.findByPhone(phone)
                                if (other != null && other.id != cur.id) {
                                    return@withContext "Mobile already used by another agent"
                                }
                                dao.update(
                                    cur.copy(
                                        name = name.trim(),
                                        phone = phone,
                                        address = address.trim(),
                                        active = active,
                                        passwordHash = if (password.isBlank()) cur.passwordHash
                                        else PasswordHasher.hash(password)
                                    )
                                )
                                null
                            }
                        }
                        if (result != null) error = result else onDone()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isNew) "Create agent" else "Save changes")
            }
        }
    }
}
