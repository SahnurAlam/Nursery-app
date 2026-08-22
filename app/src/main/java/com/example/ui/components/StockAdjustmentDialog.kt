package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.Plant
import com.example.data.model.StockLogType

@Composable
fun StockAdjustmentDialog(
    plant: Plant,
    onDismiss: () -> Unit,
    onConfirm: (quantityChange: Int, reason: String, type: String) -> Unit
) {
    var isAddition by remember { mutableStateOf(true) }
    var qtyString by remember { mutableStateOf("10") }
    var reason by remember { mutableStateOf("") }
    var selectedTypeIndex by remember { mutableIntStateOf(0) }

    val additionTypes = listOf(
        Pair("Stock In (Purchase)", StockLogType.STOCK_IN),
        Pair("Count Correction (+)", StockLogType.ADJUSTMENT)
    )

    val reductionTypes = listOf(
        Pair("Stock Out (Manual)", StockLogType.STOCK_OUT),
        Pair("Damage / Rotten", StockLogType.DAMAGE),
        Pair("Count Correction (-)", StockLogType.ADJUSTMENT)
    )

    val currentTypes = if (isAddition) additionTypes else reductionTypes
    val qtyVal = qtyString.toIntOrNull() ?: 0
    val effectiveChange = if (isAddition) qtyVal else -qtyVal
    val previewQuantity = (plant.quantity + effectiveChange).coerceAtLeast(0)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Update Stock",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = plant.plantName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Toggle Add / Remove
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (isAddition) {
                        Button(
                            onClick = { },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("Stock IN (+)")
                        }
                        OutlinedButton(
                            onClick = {
                                isAddition = false
                                selectedTypeIndex = 0
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("Stock OUT (-)")
                        }
                    } else {
                        OutlinedButton(
                            onClick = {
                                isAddition = true
                                selectedTypeIndex = 0
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("Stock IN (+)")
                        }
                        Button(
                            onClick = { },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("Stock OUT (-)")
                        }
                    }
                }

                // Current & New Quantity Preview Card
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Current Stock", style = MaterialTheme.typography.labelSmall)
                            Text(
                                "${plant.quantity} units",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text("➔", style = MaterialTheme.typography.titleMedium)
                        Column(horizontalAlignment = Alignment.End) {
                            Text("New Stock", style = MaterialTheme.typography.labelSmall)
                            Text(
                                "$previewQuantity units",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isAddition) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                // Quantity Input with step buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = {
                            val cur = qtyString.toIntOrNull() ?: 1
                            if (cur > 1) qtyString = (cur - 1).toString()
                        }
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease")
                    }

                    OutlinedTextField(
                        value = qtyString,
                        onValueChange = { qtyString = it.filter { c -> c.isDigit() } },
                        label = { Text("Quantity") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("stock_adjustment_quantity_input"),
                        singleLine = true
                    )

                    IconButton(
                        onClick = {
                            val cur = qtyString.toIntOrNull() ?: 0
                            qtyString = (cur + 1).toString()
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Increase")
                    }
                }

                // Reason / Note
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Reason / Remarks (Optional)") },
                    placeholder = { Text("e.g. New delivery from wholesaler") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (qtyVal > 0) {
                        val type = currentTypes.getOrNull(selectedTypeIndex)?.second ?: if (isAddition) StockLogType.STOCK_IN else StockLogType.STOCK_OUT
                        onConfirm(effectiveChange, reason, type)
                    }
                },
                enabled = qtyVal > 0,
                modifier = Modifier.testTag("confirm_stock_adjustment_button")
            ) {
                Text("Update Stock")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
