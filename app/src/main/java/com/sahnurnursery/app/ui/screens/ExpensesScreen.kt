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
import com.sahnurnursery.app.entity.ExpenseEntity
import com.sahnurnursery.app.viewmodel.NurseryViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen(viewModel: NurseryViewModel) {
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("💸 Nursery Expenses", fontWeight = FontWeight.Bold) })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Expense")
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
            items(expenses) { expense ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(expense.category, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text("₹%.2f".format(expense.amount), color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                        }
                        if (expense.description.isNotBlank()) {
                            Text(expense.description, style = MaterialTheme.typography.bodyMedium)
                        }
                        Text("Paid via: ${expense.paymentMethod} | Date: ${dateFormat.format(Date(expense.date))}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        if (showDialog) {
            AddExpenseDialog(
                onDismiss = { showDialog = false },
                onAdd = { category, amount, description, method ->
                    viewModel.addExpense(
                        ExpenseEntity(
                            category = category,
                            amount = amount,
                            description = description,
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
fun AddExpenseDialog(
    onDismiss: () -> Unit,
    onAdd: (String, Double, String, String) -> Unit
) {
    var category by remember { mutableStateOf("Fertilizer") }
    var amount by remember { mutableStateOf("500") }
    var description by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("Cash") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Expense") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category") })
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount (₹)") })
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") })
                OutlinedTextField(value = paymentMethod, onValueChange = { paymentMethod = it }, label = { Text("Payment Method") })
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onAdd(
                        category,
                        amount.toDoubleOrNull() ?: 0.0,
                        description,
                        paymentMethod
                    )
                }
            ) {
                Text("Save Expense")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
