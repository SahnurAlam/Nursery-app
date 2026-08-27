package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Customer
import com.example.data.model.PaymentMethods
import com.example.data.model.Plant
import com.example.data.model.Sale
import com.example.data.model.SaleItem
import com.example.ui.components.NurseryTopBar
import com.example.ui.components.ReceiptDialog
import com.example.ui.theme.InStockGreen
import com.example.ui.theme.LowStockRed
import com.example.ui.viewmodel.NurseryViewModel
import com.example.util.FormatUtils
import org.json.JSONArray

data class CartItemState(
    val plant: Plant,
    var quantityText: String = "1",
    var unitPriceText: String = plant.sellingPrice.toString()
) {
    val quantity: Int
        get() = quantityText.toIntOrNull() ?: 1

    val unitPrice: Double
        get() = unitPriceText.toDoubleOrNull() ?: plant.sellingPrice

    val subtotal: Double
        get() = quantity * unitPrice

    val isStockExceeded: Boolean
        get() = quantity > plant.quantity

    val maxStock: Int
        get() = plant.quantity
}

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

    val cartItems = remember { mutableStateListOf<CartItemState>() }
    var plantDropdownExpanded by remember { mutableStateOf(false) }

    var selectedCustomer by remember { mutableStateOf<Customer?>(null) }
    var customerNameInput by remember { mutableStateOf("Walk-in Customer") }
    var customerDropdownExpanded by remember { mutableStateOf(false) }

    var discountPercentInput by remember { mutableStateOf("0") }
    var selectedPaymentMethod by remember { mutableStateOf("Cash") }
    var notes by remember { mutableStateOf("") }
    var stockFeedbackMessage by remember { mutableStateOf<String?>(null) }

    var generatedSaleForReceipt by remember { mutableStateOf<Sale?>(null) }

    // Pre-populate initial plant if requested
    LaunchedEffect(initialPlantId, plants) {
        if (initialPlantId > 0 && cartItems.isEmpty() && plants.isNotEmpty()) {
            val p = plants.find { it.id == initialPlantId }
            if (p != null && p.quantity > 0) {
                cartItems.add(CartItemState(plant = p, quantityText = "1", unitPriceText = p.sellingPrice.toString()))
            }
        }
    }

    // Pre-populate initial customer if requested
    LaunchedEffect(initialCustomerId, customers) {
        if (initialCustomerId > 0 && selectedCustomer == null) {
            val c = customers.find { it.id == initialCustomerId }
            if (c != null) {
                selectedCustomer = c
                customerNameInput = c.name
            }
        }
    }

    // Helper to add or increment plant in cart with stock protection
    fun addPlantToCart(plant: Plant) {
        if (plant.quantity <= 0) {
            stockFeedbackMessage = "⚠️ ${plant.plantName} is out of stock!"
            return
        }
        val existingIndex = cartItems.indexOfFirst { it.plant.id == plant.id }
        if (existingIndex >= 0) {
            val currentItem = cartItems[existingIndex]
            if (currentItem.quantity < plant.quantity) {
                val newQty = currentItem.quantity + 1
                cartItems[existingIndex] = currentItem.copy(quantityText = newQty.toString())
                stockFeedbackMessage = "Updated ${plant.plantName} quantity to $newQty in sale"
            } else {
                stockFeedbackMessage = "⚠️ ${plant.plantName} has reached maximum available stock (${plant.quantity} units)"
            }
        } else {
            cartItems.add(
                CartItemState(
                    plant = plant,
                    quantityText = "1",
                    unitPriceText = plant.sellingPrice.toString()
                )
            )
            stockFeedbackMessage = "Added ${plant.plantName} to sale"
        }
    }

    // Discount percentage parsing and validation (0% to 100%, decimal supported)
    val parsedDiscountPercent = discountPercentInput.trim().toDoubleOrNull() ?: 0.0
    val isDiscountValid = discountPercentInput.trim().isEmpty() || (parsedDiscountPercent in 0.0..100.0)
    val effectiveDiscountPercent = if (isDiscountValid) parsedDiscountPercent.coerceIn(0.0, 100.0) else 0.0

    // Overall Totals
    val totalUnits = cartItems.sumOf { it.quantity }
    val totalSubtotal = cartItems.sumOf { it.subtotal }
    val totalDiscountAmount = totalSubtotal * (effectiveDiscountPercent / 100.0)
    val grandTotal = (totalSubtotal - totalDiscountAmount).coerceAtLeast(0.0)

    val hasStockError = cartItems.any { it.isStockExceeded }
    val isFormValid = cartItems.isNotEmpty() && !hasStockError && cartItems.all { it.quantity > 0 } && isDiscountValid && grandTotal >= 0

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
            // STEP 1: Customer Information
            Text(
                text = "1. Customer Information",
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

            // STEP 2: Selected Plants / Multi-Item Cart
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "2. Plant Items in Sale (${cartItems.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                if (cartItems.isNotEmpty()) {
                    Text(
                        text = "$totalUnits total plants",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Stock feedback alert banner if any
            stockFeedbackMessage?.let { msg ->
                Surface(
                    color = if (msg.startsWith("⚠️")) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (msg.startsWith("⚠️")) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { stockFeedbackMessage = null },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Dismiss", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // List of Cart Line Items
            if (cartItems.isEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "No plants added to this sale yet",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Tap '+ Add Plant' below to add plants from inventory",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            } else {
                cartItems.forEachIndexed { index, item ->
                    CartItemCard(
                        index = index,
                        item = item,
                        currencySymbol = preferences.currencySymbol,
                        discountPercent = effectiveDiscountPercent,
                        onQuantityChanged = { newQtyText ->
                            val parsed = newQtyText.toIntOrNull() ?: 1
                            if (parsed > item.maxStock) {
                                stockFeedbackMessage = "⚠️ Maximum available stock for ${item.plant.plantName} is ${item.maxStock} units"
                            }
                            cartItems[index] = item.copy(quantityText = newQtyText)
                        },
                        onUnitPriceChanged = { newPrice ->
                            cartItems[index] = item.copy(unitPriceText = newPrice)
                        },
                        onIncreaseQty = {
                            if (item.quantity < item.maxStock) {
                                val nextQty = item.quantity + 1
                                cartItems[index] = item.copy(quantityText = nextQty.toString())
                            } else {
                                stockFeedbackMessage = "⚠️ Reached stock limit (${item.maxStock} units) for ${item.plant.plantName}"
                            }
                        },
                        onDecreaseQty = {
                            if (item.quantity > 1) {
                                val prevQty = item.quantity - 1
                                cartItems[index] = item.copy(quantityText = prevQty.toString())
                            }
                        },
                        onRemove = {
                            cartItems.removeAt(index)
                            stockFeedbackMessage = "Removed ${item.plant.plantName} from sale"
                        }
                    )
                }
            }

            // ADD PLANT / ADD ANOTHER PLANT PICKER
            ExposedDropdownMenuBox(
                expanded = plantDropdownExpanded,
                onExpandedChange = { plantDropdownExpanded = !plantDropdownExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = { plantDropdownExpanded = true },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                        .testTag("add_plant_to_sale_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = if (cartItems.isEmpty()) "+ Add Plant from Inventory" else "+ Add Another Plant",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

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
                            val alreadyInCart = cartItems.any { it.plant.id == plant.id }
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(plant.plantName, fontWeight = FontWeight.Bold)
                                                if (alreadyInCart) {
                                                    Spacer(Modifier.width(6.dp))
                                                    Surface(
                                                        color = MaterialTheme.colorScheme.primaryContainer,
                                                        shape = RoundedCornerShape(4.dp)
                                                    ) {
                                                        Text(
                                                            text = "In Cart",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                        )
                                                    }
                                                }
                                            }
                                            Text(
                                                "${plant.category} • ${plant.quantity} in stock",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (plant.quantity <= 0) LowStockRed else if (plant.quantity <= 5) LowStockRed else InStockGreen
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
                                    addPlantToCart(plant)
                                    plantDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // STEP 3: Discount Percentage (%) Input & Quick Presets
            Text(
                text = "3. Discount Percentage (%)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Preset percentage chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val presets = listOf("0%", "5%", "10%", "15%", "20%", "25%", "50%")
                presets.forEach { preset ->
                    val cleanValue = preset.removeSuffix("%")
                    val isSelected = discountPercentInput == cleanValue
                    FilterChip(
                        selected = isSelected,
                        onClick = { discountPercentInput = cleanValue },
                        label = { Text(preset, style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            OutlinedTextField(
                value = discountPercentInput,
                onValueChange = { input ->
                    // Allow digits and decimal point only
                    val filtered = input.filter { c -> c.isDigit() || c == '.' }
                    discountPercentInput = filtered
                },
                label = { Text("Discount Percentage (%)") },
                placeholder = { Text("Enter 0 to 100 (e.g. 1, 2.5, 10, 25, 50)") },
                leadingIcon = { Icon(Icons.Default.Percent, contentDescription = null) },
                trailingIcon = {
                    Text(
                        text = "%",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = !isDiscountValid,
                supportingText = {
                    if (!isDiscountValid) {
                        Text(
                            text = "⚠️ Discount must be between 0% and 100%",
                            color = MaterialTheme.colorScheme.error
                        )
                    } else if (effectiveDiscountPercent > 0 && totalSubtotal > 0) {
                        Text(
                            text = "Calculated Discount Amount: -${preferences.currencySymbol} ${"%.2f".format(totalDiscountAmount)}",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sale_discount_percent_input"),
                singleLine = true
            )

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

            // Notes / Remarks
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Invoice Notes / Remarks (Optional)") },
                placeholder = { Text("e.g. 100% advance received, packed in crates") },
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
                        Text(
                            text = "Subtotal (${cartItems.size} items, $totalUnits units):",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = FormatUtils.formatCurrency(totalSubtotal, preferences.currencySymbol),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (effectiveDiscountPercent > 0) {
                        Spacer(Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val percentDisplay = "%.2f".format(effectiveDiscountPercent).trimEnd('0').trimEnd('.')
                            Text(
                                text = "Discount ($percentDisplay%):",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = "- ${FormatUtils.formatCurrency(totalDiscountAmount, preferences.currencySymbol)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
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

            // Validation alert if stock is exceeded
            if (hasStockError) {
                Text(
                    text = "⚠️ One or more items exceed available nursery stock. Please adjust quantities before completing the sale.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            }

            // Submit Button
            Button(
                onClick = {
                    if (!isFormValid) return@Button

                    val saleItems = cartItems.map { item ->
                        val itemSub = item.subtotal
                        val itemDisc = itemSub * (effectiveDiscountPercent / 100.0)
                        val itemNet = (itemSub - itemDisc).coerceAtLeast(0.0)
                        SaleItem(
                            plantId = item.plant.id,
                            plantName = item.plant.plantName,
                            quantity = item.quantity,
                            unitPrice = item.unitPrice,
                            discountPercent = effectiveDiscountPercent,
                            discount = itemDisc,
                            lineTotal = itemNet
                        )
                    }

                    val jsonArray = JSONArray()
                    saleItems.forEach { jsonArray.put(it.toJson()) }
                    val itemsJsonString = jsonArray.toString()

                    val primaryPlant = cartItems.first().plant
                    val summaryPlantName = when {
                        saleItems.size == 1 -> saleItems[0].plantName
                        saleItems.size == 2 -> "${saleItems[0].plantName}, ${saleItems[1].plantName}"
                        saleItems.size == 3 -> "${saleItems[0].plantName}, ${saleItems[1].plantName}, ${saleItems[2].plantName}"
                        else -> "${saleItems[0].plantName}, ${saleItems[1].plantName} + ${saleItems.size - 2} more"
                    }

                    val sale = Sale(
                        customerId = selectedCustomer?.id ?: 0L,
                        customerName = customerNameInput.ifBlank { "Walk-in Customer" },
                        plantId = primaryPlant.id,
                        plantName = summaryPlantName,
                        quantity = totalUnits,
                        unitPrice = if (saleItems.size == 1) saleItems[0].unitPrice else (totalSubtotal / totalUnits.coerceAtLeast(1)),
                        discountPercent = effectiveDiscountPercent,
                        discount = totalDiscountAmount,
                        amount = grandTotal,
                        paymentMethod = selectedPaymentMethod,
                        notes = notes.trim(),
                        date = System.currentTimeMillis(),
                        itemsJson = itemsJsonString
                    )

                    viewModel.createSale(sale) { newSaleId ->
                        val savedSale = sale.copy(id = newSaleId)
                        generatedSaleForReceipt = savedSale
                    }
                },
                enabled = isFormValid,
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

@Composable
fun CartItemCard(
    index: Int,
    item: CartItemState,
    currencySymbol: String,
    discountPercent: Double,
    onQuantityChanged: (String) -> Unit,
    onUnitPriceChanged: (String) -> Unit,
    onIncreaseQty: () -> Unit,
    onDecreaseQty: () -> Unit,
    onRemove: () -> Unit
) {
    val itemSubtotal = item.subtotal
    val itemDiscount = itemSubtotal * (discountPercent / 100.0)
    val itemLineTotal = (itemSubtotal - itemDiscount).coerceAtLeast(0.0)

    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("cart_item_card_${item.plant.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (item.isStockExceeded) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            if (item.isStockExceeded) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Plant name, category, and remove action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFlorist,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(Modifier.width(10.dp))

                    Column {
                        Text(
                            text = item.plant.plantName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${item.plant.category} • In Stock: ${item.maxStock} units",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (item.isStockExceeded) MaterialTheme.colorScheme.error else if (item.maxStock <= 5) LowStockRed else InStockGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                IconButton(
                    onClick = onRemove,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("cart_item_remove_${item.plant.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove Item",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            if (item.isStockExceeded) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "⚠️ Stock Limit Exceeded! Available: ${item.maxStock} units",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(12.dp))

            // Quantity + / - and inputs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDecreaseQty,
                    enabled = item.quantity > 1,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("cart_item_decrease_${item.plant.id}")
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Decrease")
                }

                OutlinedTextField(
                    value = item.quantityText,
                    onValueChange = { onQuantityChanged(it.filter { c -> c.isDigit() }) },
                    label = { Text("Qty") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .weight(0.9f)
                        .testTag("cart_item_qty_input_${item.plant.id}"),
                    singleLine = true
                )

                IconButton(
                    onClick = onIncreaseQty,
                    enabled = item.quantity < item.maxStock,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("cart_item_increase_${item.plant.id}")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Increase")
                }

                OutlinedTextField(
                    value = item.unitPriceText,
                    onValueChange = { onUnitPriceChanged(it.filter { c -> c.isDigit() || c == '.' }) },
                    label = { Text("Price ($currencySymbol)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .weight(1.3f)
                        .testTag("cart_item_price_input_${item.plant.id}"),
                    singleLine = true
                )
            }

            Spacer(Modifier.height(8.dp))

            // Line Total breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${item.quantity} × $currencySymbol${item.unitPrice} = $currencySymbol${"%.2f".format(itemSubtotal)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (discountPercent > 0) {
                        val pctFormatted = "%.2f".format(discountPercent).trimEnd('0').trimEnd('.')
                        Text(
                            text = "Disc ($pctFormatted%): -$currencySymbol${"%.2f".format(itemDiscount)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                Text(
                    text = "Line Total: " + FormatUtils.formatCurrency(itemLineTotal, currencySymbol),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
