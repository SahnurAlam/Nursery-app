package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import com.example.data.model.Plant
import com.example.data.model.PlantCategories
import com.example.ui.components.NurseryTopBar
import com.example.ui.viewmodel.NurseryViewModel
import com.example.util.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditPlantScreen(
    plantId: Long,
    viewModel: NurseryViewModel,
    onNavigateBack: () -> Unit
) {
    val preferences by viewModel.userPreferences.collectAsStateWithLifecycle()

    var plantName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(PlantCategories.ALL.first()) }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var variety by remember { mutableStateOf("") }
    var quantityText by remember { mutableStateOf("0") }
    var purchasePriceText by remember { mutableStateOf("0") }
    var sellingPriceText by remember { mutableStateOf("0") }
    var lowStockThresholdText by remember { mutableStateOf("10") }
    var notes by remember { mutableStateOf("") }
    var createdDate by remember { mutableStateOf(System.currentTimeMillis()) }

    var nameError by remember { mutableStateOf(false) }
    var existingPlant by remember { mutableStateOf<Plant?>(null) }

    LaunchedEffect(plantId) {
        if (plantId > 0) {
            val plant = viewModel.plants.value.find { it.id == plantId }
            if (plant != null) {
                existingPlant = plant
                plantName = plant.plantName
                selectedCategory = plant.category
                variety = plant.variety
                quantityText = plant.quantity.toString()
                purchasePriceText = if (plant.purchasePrice > 0) plant.purchasePrice.toString() else ""
                sellingPriceText = if (plant.sellingPrice > 0) plant.sellingPrice.toString() else ""
                lowStockThresholdText = plant.lowStockThreshold.toString()
                notes = plant.notes
                createdDate = plant.createdDate
            }
        }
    }

    val purchasePrice = purchasePriceText.toDoubleOrNull() ?: 0.0
    val sellingPrice = sellingPriceText.toDoubleOrNull() ?: 0.0
    val profitPerUnit = (sellingPrice - purchasePrice).coerceAtLeast(0.0)
    val marginPercent = if (purchasePrice > 0) (profitPerUnit / purchasePrice) * 100 else 0.0

    Scaffold(
        topBar = {
            NurseryTopBar(
                title = if (plantId > 0) "Edit Plant" else "Add New Plant",
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Plant Name
            OutlinedTextField(
                value = plantName,
                onValueChange = {
                    plantName = it
                    if (it.isNotBlank()) nameError = false
                },
                label = { Text("Plant Name *") },
                placeholder = { Text("e.g. Mango - Amrapali Grafted") },
                isError = nameError,
                supportingText = if (nameError) { { Text("Plant name is required") } } else null,
                leadingIcon = { Icon(Icons.Default.LocalFlorist, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("plant_name_input"),
                singleLine = true
            )

            // Category Dropdown
            ExposedDropdownMenuBox(
                expanded = categoryDropdownExpanded,
                onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedCategory,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                        .testTag("plant_category_picker")
                )
                ExposedDropdownMenu(
                    expanded = categoryDropdownExpanded,
                    onDismissRequest = { categoryDropdownExpanded = false }
                ) {
                    PlantCategories.ALL.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat) },
                            onClick = {
                                selectedCategory = cat
                                categoryDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // Variety / Strain
            OutlinedTextField(
                value = variety,
                onValueChange = { variety = it },
                label = { Text("Variety / Breed / Pot Size") },
                placeholder = { Text("e.g. Thai Hybrid / 8 inch pot / Baramasi") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("plant_variety_input"),
                singleLine = true
            )

            // Quantity & Low Stock Threshold Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { quantityText = it.filter { c -> c.isDigit() } },
                    label = { Text("Stock Quantity *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("plant_quantity_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = lowStockThresholdText,
                    onValueChange = { lowStockThresholdText = it.filter { c -> c.isDigit() } },
                    label = { Text("Alert Below (Qty)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("plant_low_stock_input"),
                    singleLine = true
                )
            }

            // Purchase Price & Selling Price Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = purchasePriceText,
                    onValueChange = { purchasePriceText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Cost Price (${preferences.currencySymbol})") },
                    placeholder = { Text("0.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("plant_purchase_price_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = sellingPriceText,
                    onValueChange = { sellingPriceText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Selling Price (${preferences.currencySymbol}) *") },
                    placeholder = { Text("0.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("plant_selling_price_input"),
                    singleLine = true
                )
            }

            // Profit Calculator Live Card
            if (sellingPrice > 0) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Estimated Profit / Unit",
                                style = MaterialTheme.typography.labelSmall
                            )
                            Text(
                                text = FormatUtils.formatCurrency(profitPerUnit, preferences.currencySymbol),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        if (purchasePrice > 0) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Profit Margin",
                                    style = MaterialTheme.typography.labelSmall
                                )
                                Text(
                                    text = "+${"%.1f".format(marginPercent)}%",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            // Notes / Care info
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes / Care Tips / Supplier details") },
                placeholder = { Text("e.g. Sourced from Nadia farm, requires daily watering") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("plant_notes_input"),
                minLines = 3,
                maxLines = 5
            )

            Spacer(Modifier.height(10.dp))

            // Save Button
            Button(
                onClick = {
                    if (plantName.isBlank()) {
                        nameError = true
                        return@Button
                    }
                    val plant = Plant(
                        id = plantId,
                        plantName = plantName.trim(),
                        category = selectedCategory,
                        variety = variety.trim(),
                        quantity = quantityText.toIntOrNull() ?: 0,
                        purchasePrice = purchasePrice,
                        sellingPrice = sellingPrice,
                        notes = notes.trim(),
                        lowStockThreshold = lowStockThresholdText.toIntOrNull() ?: 10,
                        createdDate = createdDate
                    )
                    viewModel.savePlant(plant) {
                        onNavigateBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_plant_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (plantId > 0) "Save Changes" else "Add Plant to Inventory",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
