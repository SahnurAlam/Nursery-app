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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Sale
import com.example.ui.components.EmptyStateView
import com.example.ui.components.MetricCard
import com.example.ui.components.NurseryTopBar
import com.example.ui.components.ReceiptDialog
import com.example.ui.components.SearchHistoryRow
import com.example.ui.components.SearchInputBar
import com.example.ui.components.SortHeaderRow
import com.example.ui.navigation.Screen
import com.example.ui.viewmodel.NurseryViewModel
import com.example.ui.viewmodel.SaleSortOption
import com.example.util.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesScreen(
    viewModel: NurseryViewModel,
    onNavigateTo: (String) -> Unit
) {
    val sales by viewModel.sales.collectAsStateWithLifecycle()
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    val dateFilter by viewModel.salesDateFilter.collectAsStateWithLifecycle()
    val searchQuery by viewModel.salesSearchQuery.collectAsStateWithLifecycle()
    val saleSortOption by viewModel.saleSortOption.collectAsStateWithLifecycle()
    val searchHistory by viewModel.searchHistory.collectAsStateWithLifecycle()
    val preferences by viewModel.userPreferences.collectAsStateWithLifecycle()

    var selectedSaleForReceipt by remember { mutableStateOf<Sale?>(null) }
    var saleToDelete by remember { mutableStateOf<Sale?>(null) }

    val filterOptions = listOf("All", "Today", "This Week", "This Month")

    val totalFilteredRevenue = sales.sumOf { it.amount }
    val totalFilteredUnits = sales.sumOf { it.quantity }

    Scaffold(
        topBar = {
            NurseryTopBar(title = "Sales & Invoices")
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateTo(Screen.CreateSale.createRoute()) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("create_sale_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Sale Entry")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            SearchInputBar(
                query = searchQuery,
                onQueryChange = {
                    viewModel.setSalesSearchQuery(it)
                    if (it.trim().length >= 3) {
                        viewModel.recordSearch(it.trim(), "SALES")
                    }
                },
                onSearch = { submitted ->
                    viewModel.setSalesSearchQuery(submitted)
                    viewModel.recordSearch(submitted, "SALES")
                },
                placeholder = "Search by customer, plant or payment...",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Search History Chips Row
            SearchHistoryRow(
                history = searchHistory,
                onSelectQuery = { selected ->
                    viewModel.setSalesSearchQuery(selected)
                    viewModel.recordSearch(selected, "SALES")
                },
                onDeleteItem = { id -> viewModel.deleteSearchHistoryItem(id) },
                onClearAll = { viewModel.clearSearchHistory() },
                modifier = Modifier.padding(bottom = 4.dp)
            )

            // Date Filters Row
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filterOptions) { opt ->
                    val isSelected = opt == dateFilter
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setSalesDateFilter(opt) },
                        label = { Text(opt) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            // Summary Stats Card for Filtered Sales
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Filtered Revenue ($dateFilter)",
                            style = MaterialTheme.typography.labelSmall
                        )
                        Text(
                            text = FormatUtils.formatCurrency(totalFilteredRevenue, preferences.currencySymbol),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Units Sold",
                            style = MaterialTheme.typography.labelSmall
                        )
                        Text(
                            text = "$totalFilteredUnits plants (${sales.size} sales)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Sales Sorting and Count Row
            SortHeaderRow(
                itemCountText = "${sales.size} ${if (sales.size == 1) "transaction" else "transactions"}",
                currentSort = saleSortOption,
                sortOptions = SaleSortOption.entries,
                getSortName = { it.displayName },
                onSortSelected = { viewModel.setSaleSortOption(it) },
                testTagPrefix = "sales_sort"
            )

            // Sales List
            if (sales.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.ReceiptLong,
                    title = "No Sales Found",
                    subtitle = if (searchQuery.isNotBlank() || dateFilter != "All")
                        "No sales matching your current filters"
                    else
                        "Tap the '+' button below to record your first plant sale",
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp, top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(sales, key = { it.id }) { sale ->
                        SaleCard(
                            sale = sale,
                            currencySymbol = preferences.currencySymbol,
                            onViewReceipt = { selectedSaleForReceipt = sale },
                            onDelete = { saleToDelete = sale }
                        )
                    }
                }
            }
        }
    }

    selectedSaleForReceipt?.let { sale ->
        val matchedCustomer = customers.find { it.id == sale.customerId }
        ReceiptDialog(
            sale = sale,
            preferences = preferences,
            customerMobile = matchedCustomer?.mobile.orEmpty(),
            customerAddress = matchedCustomer?.address.orEmpty(),
            onDismiss = { selectedSaleForReceipt = null }
        )
    }

    saleToDelete?.let { sale ->
        AlertDialog(
            onDismissRequest = { saleToDelete = null },
            title = { Text("Delete Sale Record?") },
            text = { Text("Are you sure you want to delete Invoice #INV-${sale.id.toString().padStart(5, '0')} for ${sale.customerName}? (Note: plant stock will remain as is).") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteSale(sale)
                        saleToDelete = null
                    },
                    modifier = Modifier.testTag("confirm_delete_sale_button")
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { saleToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SaleCard(
    sale: Sale,
    currencySymbol: String,
    onViewReceipt: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewReceipt() }
            .testTag("sale_card_${sale.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    Column {
                        Text(
                            text = sale.getItemsSummary(),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Buyer: ${sale.customerName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                            text = { Text("View / Share Memo") },
                            leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onViewReceipt()
                            }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Delete Sale", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                            onClick = {
                                menuExpanded = false
                                onDelete()
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val itemsCount = sale.getSaleItems().size
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = if (itemsCount > 1) "${sale.quantity} units ($itemsCount items) • ${sale.paymentMethod}" else "${sale.quantity} units • ${sale.paymentMethod}",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = FormatUtils.formatCurrency(sale.amount, currencySymbol),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = FormatUtils.formatDateTime(sale.date),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            if (sale.discount > 0 || sale.discountPercent > 0) {
                val pctDisplay = if (sale.discountPercent > 0) {
                    "${"%.2f".format(sale.discountPercent).trimEnd('0').trimEnd('.')}%"
                } else ""
                Text(
                    text = "🏷️ Discount applied: " + (if (pctDisplay.isNotBlank()) "$pctDisplay (-$currencySymbol${"%.2f".format(sale.discount)})" else "-$currencySymbol${"%.2f".format(sale.discount)}"),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
