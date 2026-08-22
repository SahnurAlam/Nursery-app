package com.sahnurnursery.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Dashboard)
    object Plants : Screen("plants", "Plants", Icons.Default.LocalFlorist)
    object Sales : Screen("sales", "Sales", Icons.Default.ReceiptLong)
    object Expenses : Screen("expenses", "Expenses", Icons.Default.AttachMoney)
    object Stock : Screen("stock", "Stock Logs", Icons.Default.Inventory2)
    object Customers : Screen("customers", "Customers", Icons.Default.People)
    object Reports : Screen("reports", "Reports", Icons.Default.Assessment)
}
