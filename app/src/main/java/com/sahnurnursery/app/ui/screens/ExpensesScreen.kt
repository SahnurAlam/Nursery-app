package com.sahnurnursery.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sahnurnursery.app.entity.ExpenseEntity
import com.sahnurnursery.app.model.UiState
import com.sahnurnursery.app.viewmodel.NurseryViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen(viewModel: NurseryViewModel) {
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()
    val operationState by viewModel.operationState.collectAsStateWithLifecycle()
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(title = { Text("💸 Nursery Expenses", fontWeight = FontWeight.Bold) })
                if (operationState is UiState.Loading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Expense")
            }
        }
    ) { padding ->
        if (expenses.isEmpty() && operationState !is UiState.Loading) {
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
                        Icons.Default.MoneyOff,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                    )
                    Text(
                        "No Expenses Recorded",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Tap the + button to log nursery maintenance, fertilizers, or tools.",
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
                items(expenses, key = { it.id }) { expense ->
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
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Expense") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it; errorMessage = null },
                    label = { Text("Category *") }
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it; errorMessage = null },
                    label = { Text("Amount (₹) *") }
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") }
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
                    if (category.isBlank()) {
                        errorMessage = "Category cannot be empty"
                        return@Button
                    }
                    val parsedAmount = amount.toDoubleOrNull()
                    if (parsedAmount == null || parsedAmount <= 0) {
                        errorMessage = "Please enter a valid expense amount"
                        return@Button
                    }
                    onAdd(
                        category.trim(),
                        parsedAmount,
                        description.trim(),
                        paymentMethod.trim()
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
