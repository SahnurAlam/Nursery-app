package com.sahnurnursery.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sahnurnursery.app.entity.SalesEntity
import com.sahnurnursery.app.viewmodel.NurseryViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesScreen(viewModel: NurseryViewModel) {
    val sales by viewModel.sales.collectAsStateWithLifecycle()
    val plants by viewModel.plants.collectAsStateWithLifecycle()
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("💰 Sales & Billing", fontWeight = FontWeight.Bold) })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Record Sale")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(sales) { sale ->
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

        if (showDialog) {
            AddSaleDialog(
                plants = plants.map { it.id to it.plantName },
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

@Composable
fun AddSaleDialog(
    plants: List<Pair<Long, String>>,
    onDismiss: () -> Unit,
    onAdd: (Long, String, String, Int, Double, String) -> Unit
) {
    var customerName by remember { mutableStateOf("Walk-in Customer") }
    var plantName by remember { mutableStateOf(plants.firstOrNull()?.second ?: "General Plant") }
    var plantId by remember { mutableStateOf(plants.firstOrNull()?.first ?: 1L) }
    var qty by remember { mutableStateOf("1") }
    var price by remember { mutableStateOf("150") }
    var paymentMethod by remember { mutableStateOf("Cash") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record New Sale") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = customerName, onValueChange = { customerName = it }, label = { Text("Customer Name") })
                OutlinedTextField(value = plantName, onValueChange = { plantName = it }, label = { Text("Plant Name") })
                OutlinedTextField(value = qty, onValueChange = { qty = it }, label = { Text("Quantity") })
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Unit Price") })
                OutlinedTextField(value = paymentMethod, onValueChange = { paymentMethod = it }, label = { Text("Payment Method") })
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onAdd(
                        plantId,
                        plantName,
                        customerName,
                        qty.toIntOrNull() ?: 1,
                        price.toDoubleOrNull() ?: 100.0,
                        paymentMethod
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
