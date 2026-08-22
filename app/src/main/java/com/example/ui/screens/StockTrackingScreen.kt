package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Plant
import com.example.data.model.StockLog
import com.example.data.model.StockLogType
import com.example.ui.components.EmptyStateView
import com.example.ui.components.MetricCard
import com.example.ui.components.NurseryTopBar
import com.example.ui.components.StockAdjustmentDialog
import com.example.ui.navigation.Screen
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.InStockGreen
import com.example.ui.theme.LowStockRed
import com.example.ui.theme.MediumStockOrange
import com.example.ui.viewmodel.NurseryViewModel
import com.example.util.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockTrackingScreen(
    viewModel: NurseryViewModel,
    onNavigateTo: (String) -> Unit
) {
    val stockLogs by viewModel.stockLogs.collectAsStateWithLifecycle()
    val plants by viewModel.plants.collectAsStateWithLifecycle()
    val lowStockPlants by viewModel.lowStockPlants.collectAsStateWithLifecycle()

    var selectedTypeFilter by remember { mutableStateOf("All") }
    var plantForStockAdjust by remember { mutableStateOf<Plant?>(null) }
    var showPlantPickerForAdjust by remember { mutableStateOf(false) }

    val filterOptions = listOf(
        "All",
        StockLogType.STOCK_IN,
        StockLogType.SALE,
        StockLogType.STOCK_OUT,
        StockLogType.DAMAGE,
        StockLogType.ADJUSTMENT
    )

    val filteredLogs = stockLogs.filter { log ->
        if (selectedTypeFilter == "All") true else log.type == selectedTypeFilter
    }

    val totalUnitsInStock = plants.sumOf { it.quantity }

    Scaffold(
        topBar = {
            NurseryTopBar(title = "Stock Tracking & History")
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showPlantPickerForAdjust = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("log_stock_movement_fab")
            ) {
                Icon(Icons.Default.Inventory2, contentDescription = "Log Stock Movement")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Metrics Summary Card
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricCard(
                            title = "Total Inventory Units",
                            value = "$totalUnitsInStock plants",
                            subtitle = "${plants.size} plant varieties",
                            icon = Icons.Default.Inventory,
                            accentColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )

                        MetricCard(
                            title = "Low Stock Alerts",
                            value = "${lowStockPlants.size} items",
                            subtitle = if (lowStockPlants.isEmpty()) "Stock healthy" else "Restock required",
                            icon = Icons.Default.Warning,
                            accentColor = if (lowStockPlants.isEmpty()) InStockGreen else LowStockRed,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                viewModel.toggleLowStockFilter(true)
                                onNavigateTo(Screen.Plants.route)
                            }
                        )
                    }
                }
            }

            // Filter Chips
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filterOptions) { opt ->
                        val isSelected = opt == selectedTypeFilter
                        val label = when (opt) {
                            StockLogType.STOCK_IN -> "Stock In (+)"
                            StockLogType.SALE -> "Sales (-)"
                            StockLogType.STOCK_OUT -> "Stock Out (-)"
                            StockLogType.DAMAGE -> "Damage / Rot"
                            StockLogType.ADJUSTMENT -> "Adjustments"
                            else -> "All Movements"
                        }
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedTypeFilter = opt },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }

            // History Log Header
            item {
                Text(
                    text = "Stock Movement History (${filteredLogs.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // Logs
            if (filteredLogs.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Default.History,
                        title = "No Stock Records Found",
                        subtitle = "Stock ins, outs, sales, and adjustments will appear here automatically."
                    )
                }
            } else {
                items(filteredLogs, key = { it.id }) { log ->
                    StockLogItem(log = log)
                }
            }
        }
    }

    // Plant selector modal for logging stock movement
    if (showPlantPickerForAdjust) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showPlantPickerForAdjust = false },
            title = { Text("Select Plant to Adjust Stock") },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(plants) { plant ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    plantForStockAdjust = plant
                                    showPlantPickerForAdjust = false
                                },
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = plant.plantName,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = plant.category,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = "${plant.quantity} in stock",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPlantPickerForAdjust = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Adjustment Dialog
    plantForStockAdjust?.let { plant ->
        StockAdjustmentDialog(
            plant = plant,
            onDismiss = { plantForStockAdjust = null },
            onConfirm = { qtyChange, reason, type ->
                viewModel.adjustStock(plant.id, qtyChange, reason, type)
                plantForStockAdjust = null
            }
        )
    }
}

@Composable
fun StockLogItem(log: StockLog) {
    val (icon, color, label) = when (log.type) {
        StockLogType.STOCK_IN -> Triple(Icons.Default.ArrowUpward, InStockGreen, "Stock In")
        StockLogType.SALE -> Triple(Icons.Default.ReceiptLong, MaterialTheme.colorScheme.primary, "Sale")
        StockLogType.STOCK_OUT -> Triple(Icons.Default.ArrowDownward, MediumStockOrange, "Stock Out")
        StockLogType.DAMAGE -> Triple(Icons.Default.ErrorOutline, ExpenseRed, "Damage")
        StockLogType.ADJUSTMENT -> Triple(Icons.Default.Tune, MaterialTheme.colorScheme.tertiary, "Count Adjust")
        else -> Triple(Icons.Default.History, MaterialTheme.colorScheme.onSurface, "Log")
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column {
                    Text(
                        text = log.plantName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$label • ${log.reason.ifBlank { "Regular log" }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = FormatUtils.formatDateTime(log.date),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                val deltaText = if (log.quantityChanged > 0) "+${log.quantityChanged}" else "${log.quantityChanged}"
                Text(
                    text = "$deltaText units",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    text = "${log.remainingStock} remaining",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
