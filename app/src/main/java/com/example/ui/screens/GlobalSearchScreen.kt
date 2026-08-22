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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.model.Customer
import com.example.data.model.Expense
import com.example.data.model.Plant
import com.example.data.model.Sale
import com.example.ui.components.EmptyStateView
import com.example.ui.components.NurseryTopBar
import com.example.ui.components.ReceiptDialog
import com.example.ui.components.SearchHistoryCard
import com.example.ui.components.SearchHistoryRow
import com.example.ui.components.SearchInputBar
import com.example.ui.components.StockBadge
import com.example.ui.navigation.Screen
import com.example.ui.theme.ExpenseRed
import com.example.ui.viewmodel.NurseryViewModel
import com.example.util.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalSearchScreen(
    viewModel: NurseryViewModel,
    onNavigateBack: () -> Unit,
    onNavigateTo: (String) -> Unit
) {
    val query by viewModel.globalSearchQuery.collectAsStateWithLifecycle()
    val results by viewModel.globalSearchResults.collectAsStateWithLifecycle()
    val searchHistory by viewModel.searchHistory.collectAsStateWithLifecycle()
    val preferences by viewModel.userPreferences.collectAsStateWithLifecycle()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var selectedSaleForReceipt by remember { mutableStateOf<Sale?>(null) }

    val tabs = listOf(
        "Plants (${results.matchingPlants.size})",
        "Customers (${results.matchingCustomers.size})",
        "Sales (${results.matchingSales.size})",
        "Expenses (${results.matchingExpenses.size})"
    )

    Scaffold(
        topBar = {
            NurseryTopBar(
                title = "Global Search",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            SearchInputBar(
                query = query,
                onQueryChange = {
                    viewModel.setGlobalSearchQuery(it)
                    if (it.trim().length >= 3) {
                        viewModel.recordSearch(it.trim(), "GLOBAL")
                    }
                },
                onSearch = { submitted ->
                    viewModel.setGlobalSearchQuery(submitted)
                    viewModel.recordSearch(submitted, "GLOBAL")
                },
                placeholder = "Search plants, buyers, invoices & expenses...",
                modifier = Modifier.padding(16.dp)
            )

            if (query.isBlank()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    if (searchHistory.isNotEmpty()) {
                        item {
                            SearchHistoryCard(
                                history = searchHistory,
                                onSelectQuery = { selected ->
                                    viewModel.setGlobalSearchQuery(selected)
                                    viewModel.recordSearch(selected, "GLOBAL")
                                },
                                onDeleteItem = { id -> viewModel.deleteSearchHistoryItem(id) },
                                onClearAll = { viewModel.clearSearchHistory() }
                            )
                        }
                    }

                    item {
                        EmptyStateView(
                            icon = Icons.Default.Search,
                            title = "Search Nursery Records",
                            subtitle = "Type a plant name, customer phone, invoice note or expense category to find matching records instantly."
                        )
                    }
                }
            } else {
                SearchHistoryRow(
                    history = searchHistory,
                    onSelectQuery = { selected ->
                        viewModel.setGlobalSearchQuery(selected)
                        viewModel.recordSearch(selected, "GLOBAL")
                    },
                    onDeleteItem = { id -> viewModel.deleteSearchHistoryItem(id) },
                    onClearAll = { viewModel.clearSearchHistory() }
                )

                Spacer(Modifier.height(4.dp))

                TabRow(selectedTabIndex = selectedTabIndex) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(title, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    when (selectedTabIndex) {
                        0 -> {
                            if (results.matchingPlants.isEmpty()) {
                                item {
                                    EmptyStateView(
                                        icon = Icons.Default.LocalFlorist,
                                        title = "No Plants Found",
                                        subtitle = "No plant matching \"$query\""
                                    )
                                }
                            } else {
                                items(results.matchingPlants) { plant ->
                                    ElevatedCard(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onNavigateTo(Screen.AddEditPlant.createRoute(plant.id)) },
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(plant.plantName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                                Text("${plant.category} • ${plant.variety}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                            StockBadge(quantity = plant.quantity, lowThreshold = plant.lowStockThreshold)
                                        }
                                    }
                                }
                            }
                        }

                        1 -> {
                            if (results.matchingCustomers.isEmpty()) {
                                item {
                                    EmptyStateView(
                                        icon = Icons.Default.People,
                                        title = "No Customers Found",
                                        subtitle = "No customer matching \"$query\""
                                    )
                                }
                            } else {
                                items(results.matchingCustomers) { customer ->
                                    ElevatedCard(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onNavigateTo(Screen.CustomerDetail.createRoute(customer.id)) },
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.tertiaryContainer),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(customer.name.take(1).uppercase(), fontWeight = FontWeight.Bold)
                                            }
                                            Spacer(Modifier.width(12.dp))
                                            Column {
                                                Text(customer.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                                if (customer.mobile.isNotBlank()) {
                                                    Text(customer.mobile, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        2 -> {
                            if (results.matchingSales.isEmpty()) {
                                item {
                                    EmptyStateView(
                                        icon = Icons.Default.ReceiptLong,
                                        title = "No Sales Found",
                                        subtitle = "No invoice matching \"$query\""
                                    )
                                }
                            } else {
                                items(results.matchingSales) { sale ->
                                    ElevatedCard(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { selectedSaleForReceipt = sale },
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(sale.plantName, fontWeight = FontWeight.Bold)
                                                Text("${sale.customerName} • ${sale.quantity} units", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                            Text(
                                                text = FormatUtils.formatCurrency(sale.amount, preferences.currencySymbol),
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        3 -> {
                            if (results.matchingExpenses.isEmpty()) {
                                item {
                                    EmptyStateView(
                                        icon = Icons.Default.AttachMoney,
                                        title = "No Expenses Found",
                                        subtitle = "No expense matching \"$query\""
                                    )
                                }
                            } else {
                                items(results.matchingExpenses) { expense ->
                                    ElevatedCard(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(expense.description.ifBlank { expense.category }, fontWeight = FontWeight.Bold)
                                                Text(expense.category, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                            Text(
                                                text = "- ${FormatUtils.formatCurrency(expense.amount, preferences.currencySymbol)}",
                                                fontWeight = FontWeight.Bold,
                                                color = ExpenseRed
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
    }

    selectedSaleForReceipt?.let { sale ->
        ReceiptDialog(
            sale = sale,
            preferences = preferences,
            onDismiss = { selectedSaleForReceipt = null }
        )
    }
}
