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
import com.sahnurnursery.app.entity.PlantEntity
import com.sahnurnursery.app.viewmodel.NurseryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantsScreen(viewModel: NurseryViewModel) {
    val plants by viewModel.plants.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🌿 Plant Inventory", fontWeight = FontWeight.Bold) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Plant")
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
            items(plants) { plant ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(plant.plantName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Stock: ${plant.quantity}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                        Text("Category: ${plant.category} | Variety: ${plant.variety}", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Purchase: ₹${plant.purchasePrice}", style = MaterialTheme.typography.bodySmall)
                            Text("Selling: ₹${plant.sellingPrice}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        if (showDialog) {
            AddPlantDialog(
                onDismiss = { showDialog = false },
                onAdd = { name, category, variety, qty, pPrice, sPrice ->
                    viewModel.addPlant(
                        PlantEntity(
                            plantName = name,
                            category = category,
                            variety = variety,
                            quantity = qty,
                            purchasePrice = pPrice,
                            sellingPrice = sPrice
                        )
                    )
                    showDialog = false
                }
            )
        }
    }
}

@Composable
fun AddPlantDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String, Int, Double, Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Fruit Plants") }
    var variety by remember { mutableStateOf("") }
    var qty by remember { mutableStateOf("10") }
    var pPrice by remember { mutableStateOf("50") }
    var sPrice by remember { mutableStateOf("100") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Plant") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Plant Name") })
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category") })
                OutlinedTextField(value = variety, onValueChange = { variety = it }, label = { Text("Variety") })
                OutlinedTextField(value = qty, onValueChange = { qty = it }, label = { Text("Quantity") })
                OutlinedTextField(value = pPrice, onValueChange = { pPrice = it }, label = { Text("Purchase Price") })
                OutlinedTextField(value = sPrice, onValueChange = { sPrice = it }, label = { Text("Selling Price") })
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onAdd(
                            name,
                            category,
                            variety,
                            qty.toIntOrNull() ?: 0,
                            pPrice.toDoubleOrNull() ?: 0.0,
                            sPrice.toDoubleOrNull() ?: 0.0
                        )
                    }
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
