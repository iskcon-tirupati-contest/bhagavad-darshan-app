package com.iskcon.bhagavaddarshan.ui.screens

import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iskcon.bhagavaddarshan.BhagavadDarshanApp
import com.iskcon.bhagavaddarshan.data.SubscriptionPlan
import com.iskcon.bhagavaddarshan.data.SubscriptionPlanEntity
import com.iskcon.bhagavaddarshan.ui.components.CompactTopBar
import com.iskcon.bhagavaddarshan.ui.components.EmptyState
import com.iskcon.bhagavaddarshan.ui.components.ListBottomSafeGap
import com.iskcon.bhagavaddarshan.ui.theme.Leaf
import com.iskcon.bhagavaddarshan.ui.theme.LeafContainer
import com.iskcon.bhagavaddarshan.ui.theme.SacredGoldLight
import kotlinx.coroutines.launch
import org.json.JSONObject

/**
 * Matches Figma "Subscription Plans" wireframe:
 * drag handle · title/price · breakdown · gift chip · Active/Hidden · ⋮ · round + FAB
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlansScreen(app: BhagavadDarshanApp) {
    var plans by remember { mutableStateOf<List<SubscriptionPlanEntity>>(emptyList()) }
    var editing by remember { mutableStateOf<SubscriptionPlanEntity?>(null) }
    var creating by remember { mutableStateOf(false) }
    var deleteId by remember { mutableStateOf<Long?>(null) }
    val scope = rememberCoroutineScope()
    val token = app.session.authToken

    suspend fun reload() {
        val json = app.api.listStaffPlans(token).getOrThrow()
        plans = json.toStaffPlanEntities()
    }

    LaunchedEffect(Unit) {
        runCatching { reload() }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CompactTopBar(
                title = {
                    Text(
                        "Subscription Plans",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { creating = true; editing = null },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = androidx.compose.foundation.shape.CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add plan", modifier = Modifier.size(28.dp))
            }
        }
    ) { padding ->
        if (plans.isEmpty()) {
            Column(Modifier.fillMaxSize().padding(padding)) {
                EmptyState(
                    icon = Icons.Default.Sell,
                    title = "No plans yet",
                    subtitle = "Tap + to add a flyer subscription plan."
                )
            }
        } else {
            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    top = 8.dp,
                    bottom = ListBottomSafeGap + 88.dp
                )
            ) {
                itemsIndexed(plans, key = { _, it -> it.id }) { _, plan ->
                    PlanWireframeCard(
                        plan = plan,
                        onEdit = { editing = plan; creating = false },
                        onDelete = { deleteId = plan.id },
                        onToggleHidden = {
                            scope.launch {
                                val body = plan.toStaffPlanBody().apply {
                                    put("active", !plan.active)
                                }
                                app.api.updatePlan(token, plan.id, body)
                                runCatching { reload() }
                            }
                        }
                    )
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
                    val body = entity.toStaffPlanBody()
                    if (entity.id == 0L) app.api.createPlan(token, body)
                    else app.api.updatePlan(token, entity.id, body)
                    runCatching { reload() }
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
                        app.api.deletePlan(token, id)
                        runCatching { reload() }
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

@Composable
private fun PlanWireframeCard(
    plan: SubscriptionPlanEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleHidden: () -> Unit
) {
    var menuOpen by remember { mutableStateOf(false) }
    val title = when {
        plan.labelTe.isNotBlank() -> plan.labelTe
        plan.years >= 6 -> "${plan.years} Months"
        plan.years == 1 -> "1 Month"
        else -> "${plan.years} Months"
    }
    val breakdown = if (plan.magazineAmount == 0 && plan.postageAmount == 0) {
        "One-time dispatch"
    } else {
        "Magazine ₹${plan.magazineAmount} + Postage ₹${plan.postageAmount}"
    }
    val giftLabel = when {
        plan.giftBooks <= 0 -> null
        plan.giftBooks == 1 -> "1 book"
        else -> "${plan.giftBooks} books"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                Icons.Default.DragHandle,
                contentDescription = "Reorder",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                modifier = Modifier
                    .padding(top = 2.dp, end = 4.dp)
                    .size(22.dp)
            )
            Column(Modifier.weight(1f)) {
                Text(
                    "$title · ₹${"%,d".format(plan.totalAmount)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    breakdown,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (giftLabel != null) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = SacredGoldLight
                        ) {
                            Text(
                                "🎁  $giftLabel",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFF5D4037),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (plan.active) LeafContainer else Color(0xFFEEEEEE)
                    ) {
                        Text(
                            if (plan.active) "Active" else "Hidden",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = if (plan.active) Leaf else Color(0xFF616161),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
            IconButton(onClick = { menuOpen = true }, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = "More",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                DropdownMenuItem(
                    text = { Text("Edit") },
                    onClick = { menuOpen = false; onEdit() }
                )
                DropdownMenuItem(
                    text = { Text(if (plan.active) "Hide plan" else "Show plan") },
                    onClick = { menuOpen = false; onToggleHidden() }
                )
                DropdownMenuItem(
                    text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                    onClick = { menuOpen = false; onDelete() }
                )
            }
        }
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
        mutableStateOf((initial?.magazineAmount ?: SubscriptionPlan.TWELVE_MONTHS.magazineRupees).toString())
    }
    var post by remember {
        mutableStateOf((initial?.postageAmount ?: SubscriptionPlan.TWELVE_MONTHS.postageRupees).toString())
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
                OutlinedTextField(value = years, onValueChange = { years = it.filter(Char::isDigit).take(2) }, label = { Text("Months (plan key)") }, singleLine = true, shape = RoundedCornerShape(8.dp))
                OutlinedTextField(value = mag, onValueChange = { mag = it.filter(Char::isDigit) }, label = { Text("Magazine ₹") }, singleLine = true, shape = RoundedCornerShape(8.dp))
                OutlinedTextField(value = post, onValueChange = { post = it.filter(Char::isDigit) }, label = { Text("Postage ₹") }, singleLine = true, shape = RoundedCornerShape(8.dp))
                OutlinedTextField(value = gifts, onValueChange = { gifts = it.filter(Char::isDigit) }, label = { Text("Gift books") }, singleLine = true, shape = RoundedCornerShape(8.dp))
                OutlinedTextField(value = label, onValueChange = { label = it }, label = { Text("Display label (e.g. 12 Months)") }, shape = RoundedCornerShape(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Active (visible)", Modifier.weight(1f))
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
                if (y !in 1..99 || m < 0 || p < 0) {
                    error = "Enter valid months and amounts"
                    return@Button
                }
                onSave(
                    SubscriptionPlanEntity(
                        id = initial?.id ?: 0L,
                        years = y,
                        magazineAmount = m,
                        postageAmount = p,
                        giftBooks = g,
                        labelTe = label.trim().ifBlank {
                            "$y Months"
                        },
                        active = active,
                        sortOrder = initial?.sortOrder ?: y
                    )
                )
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

private fun JSONObject.toStaffPlanEntities(): List<SubscriptionPlanEntity> {
    val arr = optJSONArray("plans") ?: return emptyList()
    return buildList {
        for (i in 0 until arr.length()) {
            add(arr.getJSONObject(i).toStaffPlanEntity())
        }
    }
}

private fun JSONObject.toStaffPlanEntity(): SubscriptionPlanEntity {
    val magPaise = when {
        has("magazinePaise") && !isNull("magazinePaise") -> optInt("magazinePaise")
        else -> optInt("magazineAmount") * 100
    }
    val postPaise = when {
        has("postagePaise") && !isNull("postagePaise") -> optInt("postagePaise")
        else -> optInt("postageAmount") * 100
    }
    return SubscriptionPlanEntity(
        id = optLong("id"),
        years = optInt("years"),
        magazineAmount = magPaise / 100,
        postageAmount = postPaise / 100,
        giftBooks = optInt("giftBooks"),
        labelTe = optString("label").ifBlank { optString("labelTe") },
        active = optBoolean("active", true),
        sortOrder = optInt("sortOrder")
    )
}

private fun SubscriptionPlanEntity.toStaffPlanBody(): JSONObject = JSONObject().apply {
    put("years", years)
    put("label", labelTe)
    put("magazinePaise", magazineAmount * 100)
    put("postagePaise", postageAmount * 100)
    put("giftBooks", giftBooks)
    put("active", active)
    put("sortOrder", sortOrder)
}
