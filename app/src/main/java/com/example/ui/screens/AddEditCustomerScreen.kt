package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Customer
import com.example.data.model.CustomerPurchase
import com.example.data.model.PurchasePlatforms
import com.example.ui.components.NurseryTopBar
import com.example.ui.viewmodel.NurseryViewModel
import com.example.util.FormatUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCustomerScreen(
    customerId: Long,
    viewModel: NurseryViewModel,
    onNavigateBack: () -> Unit
) {
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    val preferences by viewModel.userPreferences.collectAsStateWithLifecycle()

    var name by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var createdDate by remember { mutableStateOf(System.currentTimeMillis()) }

    var nameError by remember { mutableStateOf(false) }

    // Purchase history records
    val purchaseHistory = remember { mutableStateListOf<CustomerPurchase>() }

    // Dialog state for adding/editing a purchase
    var editingPurchaseIndex by remember { mutableStateOf<Int?>(null) }
    var showPurchaseDialog by remember { mutableStateOf(false) }
    var purchaseToDeleteIndex by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(customerId) {
        if (customerId > 0) {
            val cust = customers.find { it.id == customerId }
            if (cust != null) {
                name = cust.name
                mobile = cust.mobile
                address = cust.address
                notes = cust.notes
                createdDate = cust.createdDate
            }
            val existingPurchases = viewModel.getCustomerPurchasesList(customerId)
            purchaseHistory.clear()
            purchaseHistory.addAll(existingPurchases)
        }
    }

    Scaffold(
        topBar = {
            NurseryTopBar(
                title = if (customerId > 0) "Edit Customer" else "Add Customer",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Customer Information Section
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Customer Information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            if (it.isNotBlank()) nameError = false
                        },
                        label = { Text("Customer Name *") },
                        placeholder = { Text("e.g. Rafiqul Islam / Green Park Farm") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        isError = nameError,
                        supportingText = if (nameError) { { Text("Name is required") } } else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("customer_name_input"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = mobile,
                        onValueChange = { mobile = it },
                        label = { Text("Mobile / Phone Number") },
                        placeholder = { Text("+91 98765 43210") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("customer_mobile_input"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Delivery Address / Location") },
                        placeholder = { Text("e.g. Plot 14, River Garden, Sector 2") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("customer_address_input"),
                        minLines = 2,
                        maxLines = 4
                    )

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Customer Remarks / Preferences") },
                        placeholder = { Text("e.g. Landscaper contractor, prefers grafted mango saplings") },
                        leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("customer_notes_input"),
                        minLines = 2,
                        maxLines = 4
                    )
                }
            }

            // Purchase History Section
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Purchase History",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "${purchaseHistory.size} previous purchase record(s)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = {
                                editingPurchaseIndex = null
                                showPurchaseDialog = true
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("add_purchase_entry_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Add Purchase", style = MaterialTheme.typography.labelMedium)
                        }
                    }

                    if (purchaseHistory.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.History,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = "No previous purchases logged",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Tap '+ Add Purchase' above to record orders from Website, WhatsApp, Amazon, Facebook, etc.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    } else {
                        // Display list of purchase cards
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            purchaseHistory.forEachIndexed { index, purchase ->
                                PurchaseItemCard(
                                    purchase = purchase,
                                    currencySymbol = preferences.currencySymbol,
                                    onEdit = {
                                        editingPurchaseIndex = index
                                        showPurchaseDialog = true
                                    },
                                    onDelete = {
                                        purchaseToDeleteIndex = index
                                    }
                                )
                            }
                        }

                        // Summary of purchases in this form
                        val totalAmount = purchaseHistory.sumOf { it.purchasePrice * it.quantity }
                        val totalItems = purchaseHistory.sumOf { it.quantity }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Total Logged: $totalItems item(s)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = FormatUtils.formatCurrency(totalAmount, preferences.currencySymbol),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Save Customer Button
            Button(
                onClick = {
                    if (name.isBlank()) {
                        nameError = true
                        return@Button
                    }
                    val customer = Customer(
                        id = customerId,
                        name = name.trim(),
                        mobile = mobile.trim(),
                        address = address.trim(),
                        notes = notes.trim(),
                        createdDate = createdDate
                    )
                    viewModel.saveCustomerWithPurchases(customer, purchaseHistory.toList()) {
                        onNavigateBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_customer_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (customerId > 0) "Update Customer" else "Save Customer",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    // Add / Edit Purchase Dialog
    if (showPurchaseDialog) {
        val initialPurchase = editingPurchaseIndex?.let { purchaseHistory.getOrNull(it) }
        PurchaseEntryDialog(
            initialPurchase = initialPurchase,
            currencySymbol = preferences.currencySymbol,
            onDismiss = {
                showPurchaseDialog = false
                editingPurchaseIndex = null
            },
            onConfirm = { newPurchase ->
                if (editingPurchaseIndex != null && editingPurchaseIndex!! in purchaseHistory.indices) {
                    purchaseHistory[editingPurchaseIndex!!] = newPurchase
                } else {
                    purchaseHistory.add(newPurchase)
                }
                showPurchaseDialog = false
                editingPurchaseIndex = null
            }
        )
    }

    // Delete purchase item confirmation
    purchaseToDeleteIndex?.let { index ->
        AlertDialog(
            onDismissRequest = { purchaseToDeleteIndex = null },
            title = { Text("Delete Purchase Record?") },
            text = {
                val item = purchaseHistory.getOrNull(index)
                Text("Remove purchase of \"${item?.productName ?: "Product"}\" (${item?.platform}) from this customer?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (index in purchaseHistory.indices) {
                            purchaseHistory.removeAt(index)
                        }
                        purchaseToDeleteIndex = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { purchaseToDeleteIndex = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun PurchaseItemCard(
    purchase: CustomerPurchase,
    currencySymbol: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
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
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Purchase", modifier = Modifier.size(16.dp))
                    }
                    IconButton(
                        onClick = onDelete,
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
                    text = "${purchase.quantity} pcs @ $currencySymbol ${purchase.purchasePrice} = $currencySymbol ${purchase.purchasePrice * purchase.quantity}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = FormatUtils.formatDate(purchase.purchaseDate),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (purchase.remarks.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Note: ${purchase.remarks}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
                )
            }
        }
    }
}

@Composable
fun PlatformBadge(platform: String) {
    val (bg, fg) = getPlatformColors(platform)
    Surface(
        color = bg,
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = platform,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = fg,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

fun getPlatformColors(platform: String): Pair<Color, Color> {
    return when (platform) {
        PurchasePlatforms.WEBSITE -> Pair(Color(0xFFEDE7F6), Color(0xFF512DA8))
        PurchasePlatforms.FACEBOOK -> Pair(Color(0xFFE3F2FD), Color(0xFF1565C0))
        PurchasePlatforms.INSTAGRAM -> Pair(Color(0xFFFCE4EC), Color(0xFFC2185B))
        PurchasePlatforms.WHATSAPP -> Pair(Color(0xFFE8F5E9), Color(0xFF2E7D32))
        PurchasePlatforms.AMAZON -> Pair(Color(0xFFFFF3E0), Color(0xFFE65100))
        PurchasePlatforms.PHONE_CALL -> Pair(Color(0xFFE0F7FA), Color(0xFF00695C))
        PurchasePlatforms.DIRECT_ORDER -> Pair(Color(0xFFE8F8F5), Color(0xFF0E6655))
        else -> Pair(Color(0xFFECEFF1), Color(0xFF455A64))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseEntryDialog(
    initialPurchase: CustomerPurchase?,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onConfirm: (CustomerPurchase) -> Unit
) {
    var platform by remember { mutableStateOf(initialPurchase?.platform ?: PurchasePlatforms.DIRECT_ORDER) }
    var productName by remember { mutableStateOf(initialPurchase?.productName ?: "") }
    var quantityText by remember { mutableStateOf(initialPurchase?.quantity?.toString() ?: "1") }
    var priceText by remember { mutableStateOf(initialPurchase?.purchasePrice?.let { if (it == 0.0) "" else it.toString() } ?: "") }
    var remarks by remember { mutableStateOf(initialPurchase?.remarks ?: "") }
    var purchaseDate by remember { mutableStateOf(initialPurchase?.purchaseDate ?: System.currentTimeMillis()) }

    var dateString by remember {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        mutableStateOf(sdf.format(Date(initialPurchase?.purchaseDate ?: System.currentTimeMillis())))
    }

    var platformDropdownExpanded by remember { mutableStateOf(false) }
    var productNameError by remember { mutableStateOf(false) }
    var quantityError by remember { mutableStateOf(false) }
    var priceError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialPurchase != null) "Edit Purchase Record" else "Add Previous Purchase",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Platform Selection Dropdown
                ExposedDropdownMenuBox(
                    expanded = platformDropdownExpanded,
                    onExpandedChange = { platformDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = platform,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Purchase Source / Platform *") },
                        leadingIcon = { Icon(Icons.Default.Storefront, contentDescription = null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = platformDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = platformDropdownExpanded,
                        onDismissRequest = { platformDropdownExpanded = false }
                    ) {
                        PurchasePlatforms.ALL.forEach { option ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        PlatformBadge(platform = option)
                                        Spacer(Modifier.width(8.dp))
                                        Text(option)
                                    }
                                },
                                onClick = {
                                    platform = option
                                    platformDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Product / Plant Name
                OutlinedTextField(
                    value = productName,
                    onValueChange = {
                        productName = it
                        if (it.isNotBlank()) productNameError = false
                    },
                    label = { Text("Product / Plant Name *") },
                    placeholder = { Text("e.g. Miyazaki Mango Grafted") },
                    leadingIcon = { Icon(Icons.Default.ShoppingBag, contentDescription = null) },
                    isError = productNameError,
                    supportingText = if (productNameError) { { Text("Product name is required") } } else null,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Quantity
                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = {
                            quantityText = it.filter { char -> char.isDigit() }
                            if (quantityText.isNotBlank()) quantityError = false
                        },
                        label = { Text("Quantity *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = quantityError,
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    // Price per Unit
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = {
                            priceText = it
                            if (it.isNotBlank()) priceError = false
                        },
                        label = { Text("Price ($currencySymbol) *") },
                        leadingIcon = { Icon(Icons.Default.Payments, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = priceError,
                        modifier = Modifier.weight(1.3f),
                        singleLine = true
                    )
                }

                // Purchase Date
                OutlinedTextField(
                    value = dateString,
                    onValueChange = { input ->
                        dateString = input
                        try {
                            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            val parsed = sdf.parse(input)
                            if (parsed != null) {
                                purchaseDate = parsed.time
                            }
                        } catch (_: Exception) {}
                    },
                    label = { Text("Purchase Date (DD/MM/YYYY)") },
                    leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Remarks
                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    label = { Text("Remarks / Notes (Optional)") },
                    placeholder = { Text("e.g. Courier delivery, online paid") },
                    leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qty = quantityText.toIntOrNull() ?: 0
                    val price = priceText.toDoubleOrNull() ?: -1.0

                    if (productName.isBlank()) {
                        productNameError = true
                        return@Button
                    }
                    if (qty <= 0) {
                        quantityError = true
                        return@Button
                    }
                    if (price < 0.0) {
                        priceError = true
                        return@Button
                    }

                    val purchase = CustomerPurchase(
                        id = initialPurchase?.id ?: 0,
                        customerId = initialPurchase?.customerId ?: 0,
                        platform = platform,
                        productName = productName.trim(),
                        quantity = qty,
                        purchasePrice = price,
                        purchaseDate = purchaseDate,
                        remarks = remarks.trim()
                    )
                    onConfirm(purchase)
                }
            ) {
                Text(if (initialPurchase != null) "Update" else "Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
