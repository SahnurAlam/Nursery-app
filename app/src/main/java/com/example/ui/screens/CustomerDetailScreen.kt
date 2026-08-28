package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.CustomerPurchase
import com.example.data.model.Sale
import com.example.ui.components.EmptyStateView
import com.example.ui.components.NurseryTopBar
import com.example.ui.components.ReceiptDialog
import com.example.ui.navigation.Screen
import com.example.ui.viewmodel.NurseryViewModel
import com.example.util.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDetailScreen(
    customerId: Long,
    viewModel: NurseryViewModel,
    onNavigateBack: () -> Unit,
    onNavigateTo: (String) -> Unit
) {
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    val allSales by viewModel.sales.collectAsStateWithLifecycle()
    val customerPurchases: List<CustomerPurchase> by viewModel.getCustomerPurchasesFlow(customerId).collectAsStateWithLifecycle(initialValue = emptyList<CustomerPurchase>())
    val preferences by viewModel.userPreferences.collectAsStateWithLifecycle()

    val customer = customers.find { it.id == customerId }
    val customerSales = allSales.filter { it.customerId == customerId }

    // Aggregate summary
    val posTotalSpent = customerSales.sumOf { it.amount }
    val loggedPurchasesTotalSpent = customerPurchases.sumOf { it.purchasePrice * it.quantity }
    val grandTotalSpent = posTotalSpent + loggedPurchasesTotalSpent

    val posTotalUnits = customerSales.sumOf { it.quantity }
    val loggedPurchasesTotalUnits = customerPurchases.sumOf { it.quantity }
    val grandTotalUnits = posTotalUnits + loggedPurchasesTotalUnits

    val totalPurchasesCount = customerSales.size + customerPurchases.size

    var selectedSaleForReceipt by remember { mutableStateOf<Sale?>(null) }
    var editingPurchase by remember { mutableStateOf<CustomerPurchase?>(null) }
    var showAddPurchaseDialog by remember { mutableStateOf(false) }
    var purchaseToDelete by remember { mutableStateOf<CustomerPurchase?>(null) }

    val context = LocalContext.current

    Scaffold(
        topBar = {
            NurseryTopBar(
                title = customer?.name ?: "Customer Profile",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack,
                actions = {
                    if (customer != null) {
                        IconButton(
                            onClick = { onNavigateTo(Screen.AddEditCustomer.createRoute(customer.id)) },
                            modifier = Modifier.testTag("edit_customer_profile_button")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Customer")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        if (customer == null) {
            EmptyStateView(
                icon = Icons.Default.History,
                title = "Customer Not Found",
                subtitle = "This customer may have been deleted",
                modifier = Modifier.padding(innerPadding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Profile Header Card
                item {
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
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = customer.name.take(1).uppercase(),
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }

                                Spacer(Modifier.width(16.dp))

                                Column {
                                    Text(
                                        text = customer.name,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Customer since ${FormatUtils.formatDate(customer.createdDate)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            if (customer.mobile.isNotBlank() || customer.address.isNotBlank() || customer.notes.isNotBlank()) {
                                Spacer(Modifier.height(14.dp))
                                HorizontalDivider()
                                Spacer(Modifier.height(14.dp))
                            }

                            if (customer.mobile.isNotBlank()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Phone,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(text = customer.mobile, style = MaterialTheme.typography.bodyMedium)
                                    }

                                    FilledTonalButton(
                                        onClick = {
                                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                                data = Uri.parse("tel:${customer.mobile}")
                                            }
                                            context.startActivity(intent)
                                        }
                                    ) {
                                        Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Call")
                                    }
                                }
                            }

                            if (customer.address.isNotBlank()) {
                                Spacer(Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.Top) {
                                    Icon(
                                        Icons.Default.LocationOn,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = customer.address,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            if (customer.notes.isNotBlank()) {
                                Spacer(Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.Top) {
                                    Icon(
                                        Icons.Default.Notes,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = customer.notes,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { onNavigateTo(Screen.CreateSale.createRoute(customerId = customer.id)) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("New POS Sale", style = MaterialTheme.typography.labelMedium)
                                }

                                OutlinedButton(
                                    onClick = { showAddPurchaseDialog = true },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Add Purchase", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                }

                // Purchase Stats Summary
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("Total Amount Spent", style = MaterialTheme.typography.labelSmall)
                                Text(
                                    text = FormatUtils.formatCurrency(grandTotalSpent, preferences.currencySymbol),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("Total Purchases", style = MaterialTheme.typography.labelSmall)
                                Text(
                                    text = "$totalPurchasesCount orders ($grandTotalUnits plants)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }

                // Section Header: Complete Purchase History
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Purchase History ($totalPurchasesCount)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // 1. Platform Purchase History Records
                if (customerPurchases.isNotEmpty()) {
                    item {
                        Text(
                            text = "Logged Orders (${customerPurchases.size})",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    items(customerPurchases, key = { "purchase_${it.id}" }) { purchase ->
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        PlatformBadge(platform = purchase.platform)
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = purchase.productName,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Row {
                                        IconButton(
                                            onClick = { editingPurchase = purchase },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit Purchase", modifier = Modifier.size(16.dp))
                                        }
                                        IconButton(
                                            onClick = { purchaseToDelete = purchase },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Delete Purchase",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${purchase.quantity} pcs @ ${preferences.currencySymbol} ${purchase.purchasePrice} each",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Text(
                                        text = FormatUtils.formatCurrency(purchase.purchasePrice * purchase.quantity, preferences.currencySymbol),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = "Date: ${FormatUtils.formatDate(purchase.purchaseDate)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                                )

                                if (purchase.remarks.isNotBlank()) {
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = "Remarks: ${purchase.remarks}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. Direct POS Sales Records
                if (customerSales.isNotEmpty()) {
                    item {
                        Text(
                            text = "POS Nursery Sales (${customerSales.size})",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    items(customerSales, key = { "sale_${it.id}" }) { sale ->
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedSaleForReceipt = sale },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        PlatformBadge(platform = "Direct Order")
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = sale.getItemsSummary(),
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    val saleItems = sale.getSaleItems()
                                    val detailSub = if (saleItems.size > 1) {
                                        "${sale.quantity} units (${saleItems.size} plants) • ${sale.paymentMethod}"
                                    } else {
                                        "${sale.quantity} pcs @ ${preferences.currencySymbol} ${sale.unitPrice} • ${sale.paymentMethod}"
                                    }
                                    Text(
                                        text = detailSub,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = FormatUtils.formatDateTime(sale.date),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                    if (sale.notes.isNotBlank()) {
                                        Text(
                                            text = "Remarks: ${sale.notes}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                        )
                                    }
                                    if (sale.discount > 0 || sale.discountPercent > 0) {
                                        val pctDisplay = if (sale.discountPercent > 0) {
                                            "${"%.2f".format(sale.discountPercent).trimEnd('0').trimEnd('.')}%"
                                        } else ""
                                        Text(
                                            text = "Discount: " + (if (pctDisplay.isNotBlank()) "$pctDisplay (-${preferences.currencySymbol}${"%.2f".format(sale.discount)})" else "-${preferences.currencySymbol}${"%.2f".format(sale.discount)}"),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = FormatUtils.formatCurrency(sale.amount, preferences.currencySymbol),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = "View Memo ➔",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }

                // If completely empty
                if (customerPurchases.isEmpty() && customerSales.isEmpty()) {
                    item {
                        EmptyStateView(
                            icon = Icons.Default.History,
                            title = "No Purchases Yet",
                            subtitle = "Tap '+ Add Purchase' above to log previous orders from Website, WhatsApp, Amazon, etc. or create a new POS sale."
                        )
                    }
                }
            }
        }
    }

    // Receipt dialog for POS sales
    selectedSaleForReceipt?.let { sale ->
        ReceiptDialog(
            sale = sale,
            preferences = preferences,
            customerMobile = customer?.mobile.orEmpty(),
            customerAddress = customer?.address.orEmpty(),
            onDismiss = { selectedSaleForReceipt = null }
        )
    }

    // Add Purchase Dialog
    if (showAddPurchaseDialog) {
        PurchaseEntryDialog(
            initialPurchase = null,
            currencySymbol = preferences.currencySymbol,
            onDismiss = { showAddPurchaseDialog = false },
            onConfirm = { newPurchase ->
                viewModel.addCustomerPurchase(newPurchase.copy(customerId = customerId))
                showAddPurchaseDialog = false
            }
        )
    }

    // Edit Purchase Dialog
    editingPurchase?.let { purchase ->
        PurchaseEntryDialog(
            initialPurchase = purchase,
            currencySymbol = preferences.currencySymbol,
            onDismiss = { editingPurchase = null },
            onConfirm = { updatedPurchase ->
                viewModel.updateCustomerPurchase(updatedPurchase)
                editingPurchase = null
            }
        )
    }

    // Delete purchase item confirmation
    purchaseToDelete?.let { purchase ->
        AlertDialog(
            onDismissRequest = { purchaseToDelete = null },
            title = { Text("Delete Purchase Record?") },
            text = { Text("Remove purchase of \"${purchase.productName}\" (${purchase.platform}) from this customer?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteCustomerPurchase(purchase)
                        purchaseToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { purchaseToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
