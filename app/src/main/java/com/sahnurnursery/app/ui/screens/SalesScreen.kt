package com.sahnurnursery.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sahnurnursery.app.entity.SalesEntity
import com.sahnurnursery.app.model.UiState
import com.sahnurnursery.app.viewmodel.NurseryViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesScreen(viewModel: NurseryViewModel) {
    val sales by viewModel.sales.collectAsStateWithLifecycle()
    val plants by viewModel.plants.collectAsStateWithLifecycle()
    val operationState by viewModel.operationState.collectAsStateWithLifecycle()
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(title = { Text("💰 Sales & Billing", fontWeight = FontWeight.Bold) })
                if (operationState is UiState.Loading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Record Sale")
            }
        }
    ) { padding ->
        if (sales.isEmpty() && operationState !is UiState.Loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.ReceiptLong,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                    )
                    Text(
                        "No Sales Recorded Yet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Tap the + button to record a new sale in the database.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(sales, key = { it.id }) { sale ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(sale.plantName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Text("₹%.2f".format(sale.amount), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                            Text("Customer: ${sale.customerName} | Qty: ${sale.quantity}", style = MaterialTheme.typography.bodyMedium)
                            Text("Payment: ${sale.paymentMethod} | Date: ${dateFormat.format(Date(sale.date))}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }

        if (showDialog) {
            AddSaleDialog(
                plants = plants.map { Triple(it.id, it.plantName, it.sellingPrice) },
                onDismiss = { showDialog = false },
                onAdd = { plantId, plantName, customerName, qty, price, method ->
                    viewModel.recordSale(
                        SalesEntity(
                            plantId = plantId,
                            plantName = plantName,
                            customerName = customerName,
                            quantity = qty,
                            unitPrice = price,
                            amount = qty * price,
                            paymentMethod = method
                        )
                    )
                    showDialog = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSaleDialog(
    plants: List<Triple<Long, String, Double>>,
    onDismiss: () -> Unit,
    onAdd: (Long, String, String, Int, Double, String) -> Unit
) {
    var customerName by remember { mutableStateOf("Walk-in Customer") }
    var selectedPlant by remember { mutableStateOf(plants.firstOrNull()) }
    var plantName by remember { mutableStateOf(selectedPlant?.second ?: "General Plant") }
    var plantId by remember { mutableStateOf(selectedPlant?.first ?: 1L) }
    var qty by remember { mutableStateOf("1") }
    var price by remember { mutableStateOf(selectedPlant?.third?.toString() ?: "150") }
    var paymentMethod by remember { mutableStateOf("Cash") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record New Sale") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                if (plants.isNotEmpty()) {
                    ExposedDropdownMenuBox(
                        expanded = dropdownExpanded,
                        onExpandedChange = { dropdownExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = plantName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Select Plant *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false }
                        ) {
                            plants.forEach { item ->
                                DropdownMenuItem(
                                    text = { Text("${item.second} (₹${item.third})") },
                                    onClick = {
                                        selectedPlant = item
                                        plantId = item.first
                                        plantName = item.second
                                        price = item.third.toString()
                                        dropdownExpanded = false
                                        errorMessage = null
                                    }
                                )
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = plantName,
                        onValueChange = { plantName = it; errorMessage = null },
                        label = { Text("Plant Name *") }
                    )
                }

                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it; errorMessage = null },
                    label = { Text("Customer Name *") }
                )
                OutlinedTextField(
                    value = qty,
                    onValueChange = { qty = it; errorMessage = null },
                    label = { Text("Quantity *") }
                )
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it; errorMessage = null },
                    label = { Text("Unit Price (₹) *") }
                )
                OutlinedTextField(
                    value = paymentMethod,
                    onValueChange = { paymentMethod = it },
                    label = { Text("Payment Method") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (plantName.isBlank()) {
                        errorMessage = "Please select or enter a plant name"
                        return@Button
                    }
                    if (customerName.isBlank()) {
                        errorMessage = "Customer name cannot be empty"
                        return@Button
                    }
                    val parsedQty = qty.toIntOrNull()
                    if (parsedQty == null || parsedQty <= 0) {
                        errorMessage = "Please enter a positive quantity"
                        return@Button
                    }
                    val parsedPrice = price.toDoubleOrNull()
                    if (parsedPrice == null || parsedPrice <= 0) {
                        errorMessage = "Please enter a valid unit price"
                        return@Button
                    }
                    onAdd(
                        plantId,
                        plantName.trim(),
                        customerName.trim(),
                        parsedQty,
                        parsedPrice,
                        paymentMethod.trim()
                    )
                }
            ) {
                Text("Confirm Sale")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
