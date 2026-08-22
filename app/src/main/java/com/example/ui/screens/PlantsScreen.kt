package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Plant
import com.example.data.model.PlantCategories
import com.example.ui.components.CategoryFilterRow
import com.example.ui.components.EmptyStateView
import com.example.ui.components.NurseryTopBar
import com.example.ui.components.SearchHistoryRow
import com.example.ui.components.SearchInputBar
import com.example.ui.components.SortHeaderRow
import com.example.ui.components.StockAdjustmentDialog
import com.example.ui.components.StockBadge
import com.example.ui.navigation.Screen
import com.example.ui.theme.LowStockOrange
import com.example.ui.theme.LowStockOrangeBadge
import com.example.ui.theme.LowStockOrangeBorder
import com.example.ui.theme.LowStockOrangeContainer
import com.example.ui.theme.LowStockOrangeText
import com.example.ui.theme.LowStockRed
import com.example.ui.theme.LowStockRedContainer
import com.example.ui.viewmodel.NurseryViewModel
import com.example.ui.viewmodel.PlantSortOption
import com.example.util.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantsScreen(
    viewModel: NurseryViewModel,
    onNavigateTo: (String) -> Unit
) {
    val plants by viewModel.plants.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedPlantCategory.collectAsStateWithLifecycle()
    val searchQuery by viewModel.plantSearchQuery.collectAsStateWithLifecycle()
    val onlyLowStock by viewModel.onlyLowStock.collectAsStateWithLifecycle()
    val plantSortOption by viewModel.plantSortOption.collectAsStateWithLifecycle()
    val searchHistory by viewModel.searchHistory.collectAsStateWithLifecycle()
    val preferences by viewModel.userPreferences.collectAsStateWithLifecycle()

    var plantForStockAdjust by remember { mutableStateOf<Plant?>(null) }
    var plantToDelete by remember { mutableStateOf<Plant?>(null) }

    val categories = listOf("All") + PlantCategories.ALL

    Scaffold(
        topBar = {
            NurseryTopBar(
                title = "Plant Inventory",
                actions = {
                    IconButton(
                        onClick = { viewModel.toggleLowStockFilter(!onlyLowStock) },
                        modifier = Modifier.testTag("toggle_low_stock_filter_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Filter Low Stock",
                            tint = if (onlyLowStock) LowStockRed else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateTo(Screen.AddEditPlant.createRoute()) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_plant_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add New Plant")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Input
            SearchInputBar(
                query = searchQuery,
                onQueryChange = {
                    viewModel.setPlantSearchQuery(it)
                    if (it.trim().length >= 3) {
                        viewModel.recordSearch(it.trim(), "PLANTS")
                    }
                },
                onSearch = { submitted ->
                    viewModel.setPlantSearchQuery(submitted)
                    viewModel.recordSearch(submitted, "PLANTS")
                },
                placeholder = "Search plant name, variety or category...",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Search History Chips Row (if user has searches)
            SearchHistoryRow(
                history = searchHistory,
                onSelectQuery = { selected ->
                    viewModel.setPlantSearchQuery(selected)
                    viewModel.recordSearch(selected, "PLANTS")
                },
                onDeleteItem = { id -> viewModel.deleteSearchHistoryItem(id) },
                onClearAll = { viewModel.clearSearchHistory() },
                modifier = Modifier.padding(bottom = 4.dp)
            )

            // Category Chips Row
            CategoryFilterRow(
                categories = categories,
                selectedCategory = selectedCategory,
                onCategorySelected = { viewModel.setPlantCategory(it) },
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Low Stock Active Banner Filter
            if (onlyLowStock) {
                Surface(
                    color = LowStockRedContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⚠️ Showing only Low Stock & Depleted items",
                            style = MaterialTheme.typography.labelMedium,
                            color = LowStockRed,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(onClick = { viewModel.toggleLowStockFilter(false) }) {
                            Text("Clear", color = LowStockRed)
                        }
                    }
                }
            }

            // Inventory Sorting and Count Row
            SortHeaderRow(
                itemCountText = "${plants.size} ${if (plants.size == 1) "variety" else "varieties"}",
                currentSort = plantSortOption,
                sortOptions = PlantSortOption.entries,
                getSortName = { it.displayName },
                onSortSelected = { viewModel.setPlantSortOption(it) },
                testTagPrefix = "plant_sort"
            )

            // Plant List
            if (plants.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.LocalFlorist,
                    title = "No Plants Found",
                    subtitle = if (searchQuery.isNotBlank() || selectedCategory != "All" || onlyLowStock)
                        "Try changing your filter or search keyword"
                    else
                        "Tap the '+' button below to add your first nursery plant",
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp, top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(plants, key = { it.id }) { plant ->
                        PlantCard(
                            plant = plant,
                            currencySymbol = preferences.currencySymbol,
                            onEdit = { onNavigateTo(Screen.AddEditPlant.createRoute(plant.id)) },
                            onDelete = { plantToDelete = plant },
                            onAdjustStock = { plantForStockAdjust = plant },
                            onCreateSale = { onNavigateTo(Screen.CreateSale.createRoute(plantId = plant.id)) }
                        )
                    }
                }
            }
        }
    }

    // Stock Adjustment Dialog
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

    // Delete Confirmation Dialog
    plantToDelete?.let { plant ->
        AlertDialog(
            onDismissRequest = { plantToDelete = null },
            title = { Text("Delete Plant?") },
            text = { Text("Are you sure you want to remove \"${plant.plantName}\" from inventory? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deletePlant(plant)
                        plantToDelete = null
                    },
                    modifier = Modifier.testTag("confirm_delete_plant_button")
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { plantToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun PlantCard(
    plant: Plant,
    currencySymbol: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAdjustStock: () -> Unit,
    onCreateSale: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("plant_card_${plant.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Plant Name & Category Badge & Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = plant.plantName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (plant.variety.isNotBlank()) {
                        Text(
                            text = "Variety: ${plant.variety}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = plant.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Options")
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit Plant") },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onEdit()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Update Stock (+/-)") },
                            leadingIcon = { Icon(Icons.Default.Inventory2, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onAdjustStock()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("New Sale for this Plant") },
                            leadingIcon = { Icon(Icons.Default.AddShoppingCart, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onCreateSale()
                            }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Delete Plant", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                            onClick = {
                                menuExpanded = false
                                onDelete()
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // Stock Badge & Price Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StockBadge(quantity = plant.quantity, lowThreshold = plant.lowStockThreshold)

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${FormatUtils.formatCurrency(plant.sellingPrice, currencySymbol)} / plant",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (plant.purchasePrice > 0) {
                        Text(
                            text = "Cost: ${FormatUtils.formatCurrency(plant.purchasePrice, currencySymbol)} (+${"%.0f".format(plant.profitMargin)}% margin)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (plant.notes.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "📝 ${plant.notes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))

            // Bottom Actions: Quick stock update & Quick Sale
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onAdjustStock,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Inventory2, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Stock (+/-)", style = MaterialTheme.typography.labelMedium)
                }

                FilledTonalButton(
                    onClick = onCreateSale,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Sell Plant", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}
