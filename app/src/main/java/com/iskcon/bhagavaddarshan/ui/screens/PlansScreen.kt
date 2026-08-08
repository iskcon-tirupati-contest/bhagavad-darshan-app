package com.iskcon.bhagavaddarshan.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iskcon.bhagavaddarshan.BhagavadDarshanApp
import com.iskcon.bhagavaddarshan.data.SubscriptionPlan
import com.iskcon.bhagavaddarshan.data.SubscriptionPlanEntity
import com.iskcon.bhagavaddarshan.ui.components.CompactTopBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlansScreen(app: BhagavadDarshanApp) {
    val plans by app.database.planDao().observeAll().collectAsState(initial = emptyList())
    var editing by remember { mutableStateOf<SubscriptionPlanEntity?>(null) }
    var creating by remember { mutableStateOf(false) }
    var deleteId by remember { mutableStateOf<Long?>(null) }
    val scope = rememberCoroutineScope()

    Scaffold(
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        topBar = {
            CompactTopBar(
                title = { Text("Subscription plans", style = MaterialTheme.typography.titleMedium) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { creating = true; editing = null }) {
                Icon(Icons.Default.Add, contentDescription = "Add plan")
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
            items(plans, key = { it.id }) { plan ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp)) {
                        Text(
                            "${plan.years} year · ₹${plan.totalAmount}",
                            fontWeight = FontWeight.Bold
                        )
                        Text("Magazine ₹${plan.magazineAmount} + postage ₹${plan.postageAmount}")
                        Text("Gifts: ${plan.giftBooks} · ${if (plan.active) "Active" else "Hidden"}")
                        if (plan.labelTe.isNotBlank()) {
                            Text(plan.labelTe, style = MaterialTheme.typography.bodySmall)
                        }
                        Row {
                            IconButton(onClick = { editing = plan; creating = false }) {
                                Icon(Icons.Default.Edit, null)
                            }
                            IconButton(onClick = { deleteId = plan.id }) {
                                Icon(Icons.Default.Delete, null)
                            }
                        }
                    }
                }
            }
        }
    }

    if (creating || editing != null) {
        PlanEditDialog(
            initial = editing,
            onDismiss = { creating = false; editing = null },
            onSave = { entity ->
                scope.launch {
                    withContext(Dispatchers.IO) {
                        if (entity.id == 0L) app.database.planDao().insert(entity)
                        else app.database.planDao().update(entity)
                    }
                    creating = false
                    editing = null
                }
            }
        )
    }

    deleteId?.let { id ->
        AlertDialog(
            onDismissRequest = { deleteId = null },
            title = { Text("Delete plan?") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        withContext(Dispatchers.IO) { app.database.planDao().deleteById(id) }
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

@Composable
private fun PlanEditDialog(
    initial: SubscriptionPlanEntity?,
    onDismiss: () -> Unit,
    onSave: (SubscriptionPlanEntity) -> Unit
) {
    var years by remember {
        mutableStateOf((initial?.years ?: 1).toString())
    }
    var mag by remember {
        mutableStateOf((initial?.magazineAmount ?: SubscriptionPlan.ONE_YEAR.magazineRupees).toString())
    }
    var post by remember {
        mutableStateOf((initial?.postageAmount ?: SubscriptionPlan.ONE_YEAR.postageRupees).toString())
    }
    var gifts by remember { mutableStateOf((initial?.giftBooks ?: 0).toString()) }
    var label by remember { mutableStateOf(initial?.labelTe.orEmpty()) }
    var active by remember { mutableStateOf(initial?.active ?: true) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Add plan" else "Edit plan") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = years, onValueChange = { years = it.filter(Char::isDigit).take(1) }, label = { Text("Years") }, singleLine = true)
                OutlinedTextField(value = mag, onValueChange = { mag = it.filter(Char::isDigit) }, label = { Text("Magazine ₹") }, singleLine = true)
                OutlinedTextField(value = post, onValueChange = { post = it.filter(Char::isDigit) }, label = { Text("Postage ₹") }, singleLine = true)
                OutlinedTextField(value = gifts, onValueChange = { gifts = it.filter(Char::isDigit) }, label = { Text("Gift books") }, singleLine = true)
                OutlinedTextField(value = label, onValueChange = { label = it }, label = { Text("English label") })
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Active", Modifier.weight(1f))
                    Switch(checked = active, onCheckedChange = { active = it })
                }
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            Button(onClick = {
                val y = years.toIntOrNull() ?: 0
                val m = mag.toIntOrNull() ?: -1
                val p = post.toIntOrNull() ?: -1
                val g = gifts.toIntOrNull() ?: 0
                if (y !in 1..5 || m < 0 || p < 0) {
                    error = "Enter valid years and amounts"
                    return@Button
                }
                onSave(
                    SubscriptionPlanEntity(
                        id = initial?.id ?: 0L,
                        years = y,
                        magazineAmount = m,
                        postageAmount = p,
                        giftBooks = g,
                        labelTe = label.trim(),
                        active = active,
                        sortOrder = initial?.sortOrder ?: y
                    )
                )
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
