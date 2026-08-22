package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.MetricCard
import com.example.ui.components.NurseryTopBar
import com.example.ui.navigation.Screen
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ExpenseRedContainer
import com.example.ui.theme.ProfitGreen
import com.example.ui.theme.ProfitGreenContainer
import com.example.ui.viewmodel.NurseryViewModel
import com.example.util.ExportUtils
import com.example.util.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfitCalculatorScreen(
    viewModel: NurseryViewModel,
    onNavigateTo: (String) -> Unit
) {
    val sales by viewModel.sales.collectAsStateWithLifecycle()
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()
    val plants by viewModel.plants.collectAsStateWithLifecycle()
    val preferences by viewModel.userPreferences.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var selectedPeriod by remember { mutableStateOf("This Month") }
    val periods = listOf("Today", "Last 7 Days", "This Month", "All Time")

    val now = System.currentTimeMillis()
    val dayMillis = 86400000L

    val filteredSales = sales.filter { sale ->
        when (selectedPeriod) {
            "Today" -> sale.date >= (now - dayMillis)
            "Last 7 Days" -> sale.date >= (now - 7 * dayMillis)
            "This Month" -> sale.date >= (now - 30 * dayMillis)
            else -> true
        }
    }

    val filteredExpenses = expenses.filter { exp ->
        when (selectedPeriod) {
            "Today" -> exp.date >= (now - dayMillis)
            "Last 7 Days" -> exp.date >= (now - 7 * dayMillis)
            "This Month" -> exp.date >= (now - 30 * dayMillis)
            else -> true
        }
    }

    val totalRevenue = filteredSales.sumOf { it.amount }
    val totalExpense = filteredExpenses.sumOf { it.amount }
    val netProfit = totalRevenue - totalExpense
    val profitMargin = if (totalRevenue > 0) (netProfit / totalRevenue) * 100 else 0.0

    // Top Selling Plants by Revenue
    val plantRevenueMap = filteredSales.groupBy { it.plantName }
        .mapValues { (_, sList) -> Pair(sList.sumOf { it.amount }, sList.sumOf { it.quantity }) }
        .toList()
        .sortedByDescending { it.second.first }
        .take(5)

    // Expense Category Breakdown
    val expenseCategoryMap = filteredExpenses.groupBy { it.category }
        .mapValues { (_, eList) -> eList.sumOf { it.amount } }
        .toList()
        .sortedByDescending { it.second }

    // Simulation tool state
    var simQtyText by remember { mutableStateOf("50") }
    var simCostPriceText by remember { mutableStateOf("40") }
    var simSellPriceText by remember { mutableStateOf("120") }
    var simOtherCostText by remember { mutableStateOf("500") }

    val simQty = simQtyText.toIntOrNull() ?: 0
    val simCost = simCostPriceText.toDoubleOrNull() ?: 0.0
    val simSell = simSellPriceText.toDoubleOrNull() ?: 0.0
    val simOther = simOtherCostText.toDoubleOrNull() ?: 0.0
    val simTotalRevenue = simQty * simSell
    val simTotalCost = (simQty * simCost) + simOther
    val simNetProfit = simTotalRevenue - simTotalCost

    Scaffold(
        topBar = {
            NurseryTopBar(
                title = "Profit & Loss Analytics",
                actions = {
                    Button(
                        onClick = {
                            val text = ExportUtils.generatePnLText(
                                nurseryName = preferences.nurseryName,
                                period = selectedPeriod,
                                totalSales = totalRevenue,
                                totalExpenses = totalExpense,
                                netProfit = netProfit,
                                currencySymbol = preferences.currencySymbol,
                                salesCount = filteredSales.size,
                                expenseCount = filteredExpenses.size
                            )
                            ExportUtils.shareText(
                                context = context,
                                text = text,
                                title = "P&L Statement ($selectedPeriod) - ${preferences.nurseryName}"
                            )
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("export_pnl_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Share P&L")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Period Filter Chips
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(periods) { p ->
                        val isSelected = p == selectedPeriod
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedPeriod = p },
                            label = { Text(p, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }

            // Big Net Profit Hero Card
            item {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = if (netProfit >= 0) ProfitGreenContainer else ExpenseRedContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "NET PROFIT ($selectedPeriod)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (netProfit >= 0) ProfitGreen else ExpenseRed
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = FormatUtils.formatCurrency(netProfit, preferences.currencySymbol),
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = if (netProfit >= 0) ProfitGreen else ExpenseRed
                        )
                        Spacer(Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Text(
                                text = if (netProfit >= 0) "Profit Margin: +${"%.1f".format(profitMargin)}%" else "Operating at Loss",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (netProfit >= 0) ProfitGreen else ExpenseRed,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Revenue vs Expenses Breakdown Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = "Gross Revenue",
                        value = FormatUtils.formatCurrency(totalRevenue, preferences.currencySymbol),
                        subtitle = "${filteredSales.size} sales orders",
                        icon = Icons.Default.TrendingUp,
                        accentColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )

                    MetricCard(
                        title = "Total Expenses",
                        value = FormatUtils.formatCurrency(totalExpense, preferences.currencySymbol),
                        subtitle = "${filteredExpenses.size} expense entries",
                        icon = Icons.Default.TrendingDown,
                        accentColor = ExpenseRed,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Top Revenue Generating Plants
            if (plantRevenueMap.isNotEmpty()) {
                item {
                    Text(
                        text = "Top Revenue Generating Plants",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                items(plantRevenueMap) { (plantName, data) ->
                    val (revenue, units) = data
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
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.LocalFlorist,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text(plantName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                    Text("$units plants sold", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            Text(
                                text = FormatUtils.formatCurrency(revenue, preferences.currencySymbol),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Expense Distribution
            if (expenseCategoryMap.isNotEmpty()) {
                item {
                    Text(
                        text = "Major Expense Categories",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                items(expenseCategoryMap) { (category, amount) ->
                    val pct = if (totalExpense > 0) (amount / totalExpense).toFloat() else 0f
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(category, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                Text(
                                    FormatUtils.formatCurrency(amount, preferences.currencySymbol),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = ExpenseRed
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { pct },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = ExpenseRed,
                                trackColor = ExpenseRedContainer
                            )
                        }
                    }
                }
            }

            // Live Interactive Margin Simulator
            item {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Calculate, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Nursery Deal Margin Simulator",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "Simulate batch profit before buying or selling saplings",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = simQtyText,
                                onValueChange = { simQtyText = it.filter { c -> c.isDigit() } },
                                label = { Text("Quantity") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = simCostPriceText,
                                onValueChange = { simCostPriceText = it.filter { c -> c.isDigit() || c == '.' } },
                                label = { Text("Cost / Unit") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = simSellPriceText,
                                onValueChange = { simSellPriceText = it.filter { c -> c.isDigit() || c == '.' } },
                                label = { Text("Sell / Unit") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = simOtherCostText,
                                onValueChange = { simOtherCostText = it.filter { c -> c.isDigit() || c == '.' } },
                                label = { Text("Freight / Soil") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }

                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Estimated Deal Profit", style = MaterialTheme.typography.labelSmall)
                                    Text(
                                        text = FormatUtils.formatCurrency(simNetProfit, preferences.currencySymbol),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (simNetProfit >= 0) ProfitGreen else ExpenseRed
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Expected Revenue", style = MaterialTheme.typography.labelSmall)
                                    Text(
                                        text = FormatUtils.formatCurrency(simTotalRevenue, preferences.currencySymbol),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
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
