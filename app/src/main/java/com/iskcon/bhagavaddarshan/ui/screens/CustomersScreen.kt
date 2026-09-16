package com.iskcon.bhagavaddarshan.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iskcon.bhagavaddarshan.data.Subscription
import com.iskcon.bhagavaddarshan.data.isExpiredStatus
import com.iskcon.bhagavaddarshan.data.listDateRangeLabel
import com.iskcon.bhagavaddarshan.data.statusLabelShort
import com.iskcon.bhagavaddarshan.ui.components.ActiveGreenChip
import com.iskcon.bhagavaddarshan.ui.components.CompactTopBar
import com.iskcon.bhagavaddarshan.ui.components.EmptyState
import com.iskcon.bhagavaddarshan.ui.components.InitialsAvatar
import com.iskcon.bhagavaddarshan.ui.components.ListBottomSafeGap
import com.iskcon.bhagavaddarshan.ui.components.MetaChip
import com.iskcon.bhagavaddarshan.ui.components.SoftSearchField
import com.iskcon.bhagavaddarshan.ui.components.premiumCardSurface
import com.iskcon.bhagavaddarshan.ui.theme.AmberPending
import com.iskcon.bhagavaddarshan.ui.theme.AmberPendingContainer
import com.iskcon.bhagavaddarshan.ui.theme.InkSoft
import com.iskcon.bhagavaddarshan.ui.theme.Marigold
import com.iskcon.bhagavaddarshan.ui.theme.Outline
import com.iskcon.bhagavaddarshan.ui.theme.SoftRed
import com.iskcon.bhagavaddarshan.ui.theme.SoftRedContainer
import com.iskcon.bhagavaddarshan.util.UiSounds

private enum class CustomerFilter(val key: String, val label: String) {
    ALL("ALL", "All"),
    ACTIVE("ACTIVE", "Active"),
    EXPIRED("EXPIRED", "Expired"),
    PENDING("PENDING", "Pending"),
    EXPIRING_NEXT("EXPIRING_NEXT", "Expiring Next Mo"),
    NOT_SURE("NOT_SURE", "Not Sure"),
    SURE("SURE", "Sure");

    companion object {
        fun fromKey(key: String): CustomerFilter =
            entries.firstOrNull { it.key == key } ?: ALL
    }
}

private fun Subscription.expiresNextCalendarMonth(): Boolean {
    val end = runCatching { java.time.LocalDate.parse(endDate.take(10)) }.getOrNull() ?: return false
    val next = java.time.LocalDate.now().plusMonths(1)
    return end.year == next.year && end.month == next.month
}

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun CustomersScreen(
    viewModel: SubscriptionViewModel,
    @Suppress("UNUSED_PARAMETER") isAdmin: Boolean,
    onAdd: () -> Unit,
    onEdit: (Long) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val savedFilterKey by viewModel.customerListFilter.collectAsState()
    val filter = remember(savedFilterKey) { CustomerFilter.fromKey(savedFilterKey) }
    val list by viewModel.scopedSubscriptions.collectAsState()
    var inactiveId by remember { mutableStateOf<Long?>(null) }
    val context = LocalContext.current

    val filtered = remember(list, query, filter) {
        list.asSequence()
            .filter {
                when (filter) {
                    CustomerFilter.ALL -> true
                    CustomerFilter.ACTIVE -> it.status.contains("active", true)
                    CustomerFilter.EXPIRED -> it.isExpiredStatus()
                    CustomerFilter.PENDING ->
                        it.status.contains("pending", true) || it.status.contains("failed", true)
                    CustomerFilter.EXPIRING_NEXT -> it.expiresNextCalendarMonth()
                    CustomerFilter.NOT_SURE -> it.unsure
                    CustomerFilter.SURE -> !it.unsure
                }
            }
            .filter {
                query.isBlank() ||
                    it.name.contains(query, true) ||
                    it.phone.contains(query) ||
                    it.receiptNo.toString().contains(query)
            }
            .toList()
    }

    Scaffold(
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        topBar = {
            CompactTopBar(
                title = {
                    Text(
                        "Customers · ${filtered.size}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    UiSounds.click(context)
                    onAdd()
                },
                containerColor = Marigold,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add customer")
            }
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
                placeholder = "Search name / phone / receipt"
            )
            Spacer(Modifier.height(10.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CustomerFilter.entries.forEach { f ->
                    val selected = filter == f
                    FilterChip(
                        selected = selected,
                        onClick = {
                            UiSounds.click(context)
                            viewModel.setCustomerListFilter(f.key)
                        },
                        label = { Text(f.label) },
                        border = if (selected) null else BorderStroke(1.dp, Outline),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Marigold,
                            selectedLabelColor = Color.White,
                            containerColor = Color.White,
                            labelColor = InkSoft
                        ),
                        elevation = FilterChipDefaults.filterChipElevation(
                            elevation = if (selected) 6.dp else 2.dp
                        )
                    )
                }
            }
            if (filtered.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.People,
                    title = "No customers found",
                    subtitle = if (query.isBlank()) "Tap + to register a devotee."
                    else "Try a different search or filter."
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(top = 12.dp, bottom = ListBottomSafeGap + 88.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filtered, key = { it.id }) { item ->
                        CustomerRow(
                            item = item,
                            onEdit = {
                                UiSounds.click(context)
                                onEdit(item.id)
                            },
                            onMarkInactive = {
                                UiSounds.click(context)
                                inactiveId = item.id
                            }
                        )
                    }
                }
            }
        }
    }

    inactiveId?.let { id ->
        AlertDialog(
            onDismissRequest = { inactiveId = null },
            title = { Text("Mark inactive?") },
            text = { Text("This moves the customer to Expired. You can still open the record later.") },
            confirmButton = {
                TextButton(onClick = {
                    UiSounds.click(context)
                    viewModel.markInactive(id)
                    inactiveId = null
                }) { Text("Mark inactive", color = SoftRed) }
            },
            dismissButton = {
                TextButton(onClick = {
                    UiSounds.click(context)
                    inactiveId = null
                }) { Text("Cancel") }
            }
        )
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun CustomerRow(
    item: Subscription,
    onEdit: () -> Unit,
    onMarkInactive: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    val expired = item.isExpiredStatus()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .premiumCardSurface(shape = shape, elevation = 8.dp)
            .combinedClickable(
                onClick = onEdit,
                onLongClick = onMarkInactive
            )
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                InitialsAvatar(
                    name = item.name,
                    containerColor = Marigold
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        item.name,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        item.phone,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (expired) {
                    MetaChip(
                        text = item.statusLabelShort(),
                        container = SoftRedContainer,
                        content = SoftRed
                    )
                } else if (item.status.contains("active", true)) {
                    ActiveGreenChip(item.statusLabelShort())
                } else {
                    MetaChip(
                        text = item.statusLabelShort(),
                        container = AmberPendingContainer,
                        content = AmberPending
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                item.listDateRangeLabel(),
                style = MaterialTheme.typography.bodyMedium,
                color = InkSoft,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
