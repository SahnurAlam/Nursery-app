package com.example.ui.screens

import android.content.Context
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.NurseryLogo
import com.example.ui.components.NurseryTopBar
import com.example.ui.viewmodel.NurseryViewModel
import com.example.util.ExportUtils
import com.example.util.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: NurseryViewModel,
    onNavigateBack: () -> Unit
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

    val totalSalesAmt = filteredSales.sumOf { it.amount }
    val totalExpenseAmt = filteredExpenses.sumOf { it.amount }
    val netProfit = totalSalesAmt - totalExpenseAmt

    Scaffold(
        topBar = {
            NurseryTopBar(
                title = "Reports & Export Center",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Nursery Brand Header
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        NurseryLogo(
                            customLogoPath = preferences.customLogoPath,
                            size = 52.dp,
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = preferences.nurseryName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Proprietor: ${preferences.ownerName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Official Business Statements & Reports",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            // Period Filter Chips
            item {
                Column {
                    Text(
                        text = "Select Reporting Period",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(periods) { p ->
                            val isSelected = p == selectedPeriod
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedPeriod = p },
                                label = { Text(p) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }
                }
            }

            // Report 1: Sales Statement (CSV & Text)
            item {
                ReportCard(
                    title = "Sales Report ($selectedPeriod)",
                    subtitle = "${filteredSales.size} transactions • Total ${FormatUtils.formatCurrency(totalSalesAmt, preferences.currencySymbol)}",
                    icon = Icons.Default.ReceiptLong,
                    onShareCsv = {
                        val csv = ExportUtils.generateSalesCsv(filteredSales, preferences.currencySymbol)
                        ExportUtils.shareFile(
                            context = context,
                            content = csv,
                            fileName = "Sales_Report_${selectedPeriod.replace(" ", "_")}.csv",
                            mimeType = "text/csv",
                            title = "Share Sales CSV"
                        )
                    },
                    onShareText = {
                        val text = buildString {
                            appendLine("📊 SALES REPORT - ${preferences.nurseryName}")
                            appendLine("Period: $selectedPeriod")
                            appendLine("Total Revenue: ${FormatUtils.formatCurrency(totalSalesAmt, preferences.currencySymbol)}")
                            appendLine("Orders: ${filteredSales.size}")
                            appendLine("----------------------------------------")
                            filteredSales.forEach { s ->
                                appendLine("• ${FormatUtils.formatShortDate(s.date)}: ${s.plantName} (${s.quantity} units) to ${s.customerName} - ${FormatUtils.formatCurrency(s.amount, preferences.currencySymbol)}")
                            }
                        }
                        ExportUtils.shareText(context, text, "Sales Report - ${preferences.nurseryName}")
                    }
                )
            }

            // Report 2: Expenses Statement
            item {
                ReportCard(
                    title = "Expense Report ($selectedPeriod)",
                    subtitle = "${filteredExpenses.size} entries • Total ${FormatUtils.formatCurrency(totalExpenseAmt, preferences.currencySymbol)}",
                    icon = Icons.Default.AttachMoney,
                    onShareCsv = {
                        val csv = ExportUtils.generateExpensesCsv(filteredExpenses, preferences.currencySymbol)
                        ExportUtils.shareFile(
                            context = context,
                            content = csv,
                            fileName = "Expenses_Report_${selectedPeriod.replace(" ", "_")}.csv",
                            mimeType = "text/csv",
                            title = "Share Expense CSV"
                        )
                    },
                    onShareText = {
                        val text = buildString {
                            appendLine("💸 EXPENSE REPORT - ${preferences.nurseryName}")
                            appendLine("Period: $selectedPeriod")
                            appendLine("Total Expense: ${FormatUtils.formatCurrency(totalExpenseAmt, preferences.currencySymbol)}")
                            appendLine("----------------------------------------")
                            filteredExpenses.forEach { e ->
                                appendLine("• ${FormatUtils.formatShortDate(e.date)}: [${e.category}] ${e.description} - ${FormatUtils.formatCurrency(e.amount, preferences.currencySymbol)}")
                            }
                        }
                        ExportUtils.shareText(context, text, "Expense Report - ${preferences.nurseryName}")
                    }
                )
            }

            // Report 3: Inventory Stock & Valuation
            item {
                val totalQty = plants.sumOf { it.quantity }
                val totalRetail = plants.sumOf { it.quantity * it.sellingPrice }
                ReportCard(
                    title = "Inventory Valuation & Stock",
                    subtitle = "${plants.size} plant varieties • $totalQty plants • Stock Value: ${FormatUtils.formatCurrency(totalRetail, preferences.currencySymbol)}",
                    icon = Icons.Default.LocalFlorist,
                    onShareCsv = {
                        val csv = ExportUtils.generateInventoryCsv(plants, preferences.currencySymbol)
                        ExportUtils.shareFile(
                            context = context,
                            content = csv,
                            fileName = "Inventory_Stock_Report.csv",
                            mimeType = "text/csv",
                            title = "Share Inventory CSV"
                        )
                    },
                    onShareText = {
                        val text = buildString {
                            appendLine("🌿 INVENTORY REPORT - ${preferences.nurseryName}")
                            appendLine("Total Varieties: ${plants.size} | Total Plants: $totalQty")
                            appendLine("Total Retail Value: ${FormatUtils.formatCurrency(totalRetail, preferences.currencySymbol)}")
                            appendLine("----------------------------------------")
                            plants.forEach { p ->
                                appendLine("• ${p.plantName} (${p.category}): ${p.quantity} in stock @ ${FormatUtils.formatCurrency(p.sellingPrice, preferences.currencySymbol)}")
                            }
                        }
                        ExportUtils.shareText(context, text, "Inventory Report - ${preferences.nurseryName}")
                    }
                )
            }

            // Report 4: Profit & Loss Statement
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.TrendingUp, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("Profit & Loss Statement ($selectedPeriod)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("Net Profit: ${FormatUtils.formatCurrency(netProfit, preferences.currencySymbol)}", style = MaterialTheme.typography.bodyMedium)
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        Button(
                            onClick = {
                                val pnl = ExportUtils.generatePnLText(
                                    nurseryName = preferences.nurseryName,
                                    period = selectedPeriod,
                                    totalSales = totalSalesAmt,
                                    totalExpenses = totalExpenseAmt,
                                    netProfit = netProfit,
                                    currencySymbol = preferences.currencySymbol,
                                    salesCount = filteredSales.size,
                                    expenseCount = filteredExpenses.size
                                )
                                ExportUtils.shareText(
                                    context = context,
                                    text = pnl,
                                    title = "P&L Statement - ${preferences.nurseryName}"
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Share Complete P&L Statement")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReportCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onShareCsv: () -> Unit,
    onShareText: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(Modifier.height(14.dp))
            HorizontalDivider()
            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onShareCsv,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Export CSV")
                }

                FilledTonalButton(
                    onClick = onShareText,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Share Text")
                }
            }
        }
    }
}
