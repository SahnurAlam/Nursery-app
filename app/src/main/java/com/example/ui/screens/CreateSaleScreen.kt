package com.example.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Customer
import com.example.data.model.PaymentMethods
import com.example.data.model.Plant
import com.example.data.model.Sale
import com.example.ui.components.NurseryTopBar
import com.example.ui.components.ReceiptDialog
import com.example.ui.theme.InStockGreen
import com.example.ui.theme.LowStockRed
import com.example.ui.viewmodel.NurseryViewModel
import com.example.util.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSaleScreen(
    initialPlantId: Long = 0L,
    initialCustomerId: Long = 0L,
    viewModel: NurseryViewModel,
    onNavigateBack: () -> Unit
) {
    val plants by viewModel.plants.collectAsStateWithLifecycle()
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    val preferences by viewModel.userPreferences.collectAsStateWithLifecycle()

    var selectedPlant by remember { mutableStateOf<Plant?>(null) }
    var plantDropdownExpanded by remember { mutableStateOf(false) }

    var selectedCustomer by remember { mutableStateOf<Customer?>(null) }
    var customerNameInput by remember { mutableStateOf("Walk-in Customer") }
    var customerDropdownExpanded by remember { mutableStateOf(false) }

    var quantityText by remember { mutableStateOf("1") }
    var unitPriceText by remember { mutableStateOf("0") }
    var discountText by remember { mutableStateOf("0") }
    var selectedPaymentMethod by remember { mutableStateOf<String>("Cash") }
    var notes by remember { mutableStateOf("") }

    var generatedSaleForReceipt by remember { mutableStateOf<Sale?>(null) }

    LaunchedEffect(initialPlantId, plants) {
        if (initialPlantId > 0 && selectedPlant == null) {
            val p = plants.find { it.id == initialPlantId }
            if (p != null) {
                selectedPlant = p
                unitPriceText = p.sellingPrice.toString()
            }
        }
    }

    LaunchedEffect(initialCustomerId, customers) {
        if (initialCustomerId > 0 && selectedCustomer == null) {
            val c = customers.find { it.id == initialCustomerId }
            if (c != null) {
                selectedCustomer = c
                customerNameInput = c.name
            }
        }
    }

    val quantity = quantityText.toIntOrNull() ?: 1
    val unitPrice = unitPriceText.toDoubleOrNull() ?: 0.0
    val discount = discountText.toDoubleOrNull() ?: 0.0
    val subtotal = quantity * unitPrice
    val grandTotal = (subtotal - discount).coerceAtLeast(0.0)

    val maxAvailableStock = selectedPlant?.quantity ?: 0
    val isStockExceeded = selectedPlant != null && quantity > maxAvailableStock

    Scaffold(
        topBar = {
            NurseryTopBar(
                title = "Record New Sale",
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
            // STEP 1: Select Plant
            Text(
                text = "1. Select Plant",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            ExposedDropdownMenuBox(
                expanded = plantDropdownExpanded,
                onExpandedChange = { plantDropdownExpanded = !plantDropdownExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedPlant?.plantName ?: "Tap to choose a plant...",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Plant from Inventory *") },
                    leadingIcon = { Icon(Icons.Default.LocalFlorist, contentDescription = null) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = plantDropdownExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                        .testTag("sale_plant_picker")
                )
                ExposedDropdownMenu(
                    expanded = plantDropdownExpanded,
                    onDismissRequest = { plantDropdownExpanded = false }
                ) {
                    if (plants.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No plants in inventory") },
                            onClick = { plantDropdownExpanded = false }
                        )
                    } else {
                        plants.forEach { plant ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(plant.plantName, fontWeight = FontWeight.Bold)
                                            Text(
                                                "${plant.category} • ${plant.quantity} in stock",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (plant.quantity <= 5) LowStockRed else InStockGreen
                                            )
                                        }
                                        Text(
                                            text = "${preferences.currencySymbol} ${plant.sellingPrice}",
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                },
                                onClick = {
                                    selectedPlant = plant
                                    unitPriceText = plant.sellingPrice.toString()
                                    plantDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            if (selectedPlant != null) {
                Surface(
                    color = if (isStockExceeded) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Available in Nursery: $maxAvailableStock units",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isStockExceeded) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (isStockExceeded) {
                            Text(
                                text = "⚠️ Not enough stock!",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // STEP 2: Customer
            Text(
                text = "2. Customer Information",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            ExposedDropdownMenuBox(
                expanded = customerDropdownExpanded,
                onExpandedChange = { customerDropdownExpanded = !customerDropdownExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = customerNameInput,
                    onValueChange = {
                        customerNameInput = it
                        selectedCustomer = null
                    },
                    label = { Text("Customer Name / Buyer") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = customerDropdownExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                        .testTag("sale_customer_picker")
                )
                ExposedDropdownMenu(
                    expanded = customerDropdownExpanded,
                    onDismissRequest = { customerDropdownExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("🚶 Walk-in Customer (General)") },
                        onClick = {
                            selectedCustomer = null
                            customerNameInput = "Walk-in Customer"
                            customerDropdownExpanded = false
                        }
                    )
                    customers.forEach { customer ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(customer.name, fontWeight = FontWeight.Bold)
                                    if (customer.mobile.isNotBlank()) {
                                        Text(customer.mobile, style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            },
                            onClick = {
                                selectedCustomer = customer
                                customerNameInput = customer.name
                                customerDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // STEP 3: Pricing & Quantity
            Text(
                text = "3. Quantity & Pricing",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (quantity > 1) quantityText = (quantity - 1).toString()
                    }
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Decrease")
                }

                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { quantityText = it.filter { c -> c.isDigit() } },
                    label = { Text("Qty (Units)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("sale_quantity_input"),
                    singleLine = true
                )

                IconButton(
                    onClick = {
                        quantityText = (quantity + 1).toString()
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Increase")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = unitPriceText,
                    onValueChange = { unitPriceText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Unit Price (${preferences.currencySymbol})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("sale_unit_price_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = discountText,
                    onValueChange = { discountText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Discount (${preferences.currencySymbol})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("sale_discount_input"),
                    singleLine = true
                )
            }

            // STEP 4: Payment Method
            Text(
                text = "4. Payment Method",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                PaymentMethods.ALL.take(4).forEach { method ->
                    val isSelected = selectedPaymentMethod == method
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedPaymentMethod = method },
                        label = { Text(method, style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Invoice Notes / Remarks (Optional)") },
                placeholder = { Text("e.g. 50% paid in advance, delivery tomorrow") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Grand Total Bill Summary Card
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Subtotal ($quantity x ${FormatUtils.formatCurrency(unitPrice, preferences.currencySymbol)}):", style = MaterialTheme.typography.bodyMedium)
                        Text(FormatUtils.formatCurrency(subtotal, preferences.currencySymbol), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }

                    if (discount > 0) {
                        Spacer(Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Discount:", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                            Text("- ${FormatUtils.formatCurrency(discount, preferences.currencySymbol)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TOTAL AMOUNT:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = FormatUtils.formatCurrency(grandTotal, preferences.currencySymbol),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Submit Button
            Button(
                onClick = {
                    val plant = selectedPlant ?: return@Button
                    val sale = Sale(
                        plantId = plant.id,
                        plantName = plant.plantName,
                        customerId = selectedCustomer?.id ?: 0L,
                        customerName = customerNameInput.ifBlank { "Walk-in Customer" },
                        quantity = quantity,
                        unitPrice = unitPrice,
                        discount = discount,
                        amount = grandTotal,
                        paymentMethod = selectedPaymentMethod,
                        notes = notes.trim(),
                        date = System.currentTimeMillis()
                    )

                    viewModel.createSale(sale) { newSaleId ->
                        val savedSale = sale.copy(id = newSaleId)
                        generatedSaleForReceipt = savedSale
                    }
                },
                enabled = selectedPlant != null && quantity > 0 && grandTotal >= 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("complete_sale_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.AddShoppingCart, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Complete Sale & Generate Memo",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    // Digital Cash Memo / Receipt dialog
    generatedSaleForReceipt?.let { sale ->
        ReceiptDialog(
            sale = sale,
            preferences = preferences,
            onDismiss = {
                generatedSaleForReceipt = null
                onNavigateBack()
            }
        )
    }
}
