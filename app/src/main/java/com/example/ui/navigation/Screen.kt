package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    // Bottom Bar / Main Navigation Destinations
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Dashboard)
    object Plants : Screen("plants", "Plants", Icons.Default.LocalFlorist)
    object Sales : Screen("sales", "Sales", Icons.Default.ReceiptLong)
    object Expenses : Screen("expenses", "Expenses", Icons.Default.AttachMoney)
    object ProfitCalculator : Screen("profit_calculator", "Profit", Icons.Default.TrendingUp)

    // Secondary & Feature Destinations
    object StockTracking : Screen("stock_tracking", "Stock Tracking", Icons.Default.Inventory2)
    object Customers : Screen("customers", "Customers", Icons.Default.People)
    object Reports : Screen("reports", "Reports & Export", Icons.Default.Assessment)
    object GlobalSearch : Screen("global_search", "Search System", Icons.Default.Search)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)

    // Form / Detail Screens
    object AddEditPlant : Screen("add_edit_plant?plantId={plantId}", "Plant Details") {
        fun createRoute(plantId: Long = 0L) = "add_edit_plant?plantId=$plantId"
    }

    object AddEditCustomer : Screen("add_edit_customer?customerId={customerId}", "Customer Details") {
        fun createRoute(customerId: Long = 0L) = "add_edit_customer?customerId=$customerId"
    }

    object CustomerDetail : Screen("customer_detail/{customerId}", "Customer History") {
        fun createRoute(customerId: Long) = "customer_detail/$customerId"
    }

    object CreateSale : Screen("create_sale?plantId={plantId}&customerId={customerId}", "New Sale") {
        fun createRoute(plantId: Long = 0L, customerId: Long = 0L) = "create_sale?plantId=$plantId&customerId=$customerId"
    }

    object AddExpense : Screen("add_expense", "Add Expense")

    companion object {
        val bottomNavItems = listOf(
            Dashboard,
            Plants,
            Sales,
            Expenses,
            ProfitCalculator
        )
    }
}
