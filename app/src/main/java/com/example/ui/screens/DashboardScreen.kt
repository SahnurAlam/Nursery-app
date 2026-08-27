package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.Plant
import com.example.data.model.Sale
import com.example.ui.components.MetricCard
import com.example.ui.components.NurseryTopBar
import com.example.ui.components.ReceiptDialog
import com.example.ui.components.SectionHeader
import com.example.ui.components.StockAdjustmentDialog
import com.example.ui.components.StockBadge
import com.example.ui.navigation.Screen
import com.example.ui.theme.BlueAccent
import com.example.ui.theme.BlueAccentContainer
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.LowStockOrange
import com.example.ui.theme.LowStockOrangeBadge
import com.example.ui.theme.LowStockOrangeBorder
import com.example.ui.theme.LowStockOrangeContainer
import com.example.ui.theme.LowStockOrangeText
import com.example.ui.theme.LowStockRed
import com.example.ui.theme.LowStockRedContainer
import com.example.ui.theme.MediumStockOrange
import com.example.ui.theme.ProfitGreen
import com.example.ui.theme.PurpleAccent
import com.example.ui.theme.PurpleAccentContainer
import com.example.ui.viewmodel.NurseryViewModel
import com.example.util.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: NurseryViewModel,
    onNavigateTo: (String) -> Unit
) {
    val uiState by viewModel.dashboardUiState.collectAsStateWithLifecycle()
    val preferences by viewModel.userPreferences.collectAsStateWithLifecycle()

    var selectedSaleForReceipt by remember { mutableStateOf<Sale?>(null) }
    var plantForStockAdjust by remember { mutableStateOf<Plant?>(null) }

    Scaffold(
        topBar = {
            NurseryTopBar(
                title = preferences.nurseryName,
                subtitle = "Welcome Back",
                actions = {
                    IconButton(
                        onClick = { onNavigateTo(Screen.GlobalSearch.route) },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                            .testTag("dashboard_search_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Global Search",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = { onNavigateTo(Screen.Settings.route) },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                            .testTag("dashboard_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // High-contrast Top Highlights Grid (Total Inventory & Today's Sales)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Dark Green Highlight Card (Total Inventory)
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(136.dp)
                            .clickable { onNavigateTo(Screen.Plants.route) },
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White.copy(alpha = 0.18f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Inventory2,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Total Inventory",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.75f),
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = "${uiState.totalStockQuantity}",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "${uiState.totalPlantCount} varieties",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.65f)
                                )
                            }
                        }
                    }

                    // Clean White Highlight Card (Today's Sales)
                    OutlinedCard(
                        modifier = Modifier
                            .weight(1f)
                            .height(136.dp)
                            .clickable {
                                viewModel.setSalesDateFilter("Today")
                                onNavigateTo(Screen.Sales.route)
                            },
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TrendingUp,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Today's Sales",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = FormatUtils.formatCurrency(uiState.todaySales, preferences.currencySymbol),
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Recorded today",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }

            // Low Stock Alert Banner (if any)
            if (uiState.lowStockPlants.isNotEmpty()) {
                item {
                    LowStockAlertBanner(
                        lowStockPlants = uiState.lowStockPlants,
                        onViewAll = {
                            viewModel.toggleLowStockFilter(true)
                            onNavigateTo(Screen.Plants.route)
                        },
                        onQuickRestock = { plant ->
                            plantForStockAdjust = plant
                        }
                    )
                }
            }

            // Hero Nursery Management Banner
            item {
                HeroBanner(
                    nurseryName = preferences.nurseryName,
                    ownerName = preferences.ownerName,
                    onAddSale = { onNavigateTo(Screen.CreateSale.createRoute()) }
                )
            }

            // Quick Actions Section
            item {
                SectionHeader(title = "Quick Actions")
            }

            item {
                QuickActionsGrid(
                    onNewSale = { onNavigateTo(Screen.CreateSale.createRoute()) },
                    onAddPlant = { onNavigateTo(Screen.AddEditPlant.createRoute()) },
                    onAddExpense = { onNavigateTo(Screen.AddExpense.route) },
                    onAddCustomer = { onNavigateTo(Screen.AddEditCustomer.createRoute()) },
                    onStockTracking = { onNavigateTo(Screen.StockTracking.route) },
                    onReports = { onNavigateTo(Screen.Reports.route) }
                )
            }

            // Main Financial & Stock Overview
            item {
                SectionHeader(title = "Financial Overview")
            }

            // Metrics 2x2
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MetricCard(
                            title = "This Month Sales",
                            value = FormatUtils.formatCurrency(uiState.monthSales, preferences.currencySymbol),
                            subtitle = "Gross Revenue",
                            icon = Icons.Default.ReceiptLong,
                            accentColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                viewModel.setSalesDateFilter("This Month")
                                onNavigateTo(Screen.Sales.route)
                            }
                        )

                        MetricCard(
                            title = "Monthly Expenses",
                            value = FormatUtils.formatCurrency(uiState.monthExpenses, preferences.currencySymbol),
                            subtitle = "Labour & Soil",
                            icon = Icons.Default.TrendingDown,
                            accentColor = ExpenseRed,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateTo(Screen.Expenses.route) }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MetricCard(
                            title = "Net Profit (Month)",
                            value = FormatUtils.formatCurrency(uiState.netProfit, preferences.currencySymbol),
                            subtitle = if (uiState.netProfit >= 0) "Profitable" else "Loss",
                            icon = Icons.Default.AttachMoney,
                            accentColor = if (uiState.netProfit >= 0) ProfitGreen else ExpenseRed,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateTo(Screen.ProfitCalculator.route) }
                        )

                        MetricCard(
                            title = "Total Customers",
                            value = "${uiState.totalCustomers}",
                            subtitle = "Buyers & Contracts",
                            icon = Icons.Default.People,
                            accentColor = BlueAccent,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigateTo(Screen.Customers.route) }
                        )
                    }
                }
            }

            // Recent Sales Section
            if (uiState.recentSales.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Recent Transactions",
                        trailing = {
                            TextButton(
                                onClick = { onNavigateTo(Screen.Sales.route) },
                                modifier = Modifier.testTag("dashboard_view_all_sales_button")
                            ) {
                                Text(
                                    text = "View All",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Icon(
                                    Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    )
                }

                items(uiState.recentSales) { sale ->
                    RecentSaleItem(
                        sale = sale,
                        currencySymbol = preferences.currencySymbol,
                        onClick = { selectedSaleForReceipt = sale }
                    )
                }
            }
        }
    }

    // Receipt Dialog Preview
    selectedSaleForReceipt?.let { sale ->
        ReceiptDialog(
            sale = sale,
            preferences = preferences,
            onDismiss = { selectedSaleForReceipt = null }
        )
    }

    // Stock Adjust Dialog
    plantForStockAdjust?.let { plant ->
        StockAdjustmentDialog(
            plant = plant,
            onDismiss = { plantForStockAdjust = null },
            onConfirm = { qtyChange, reason, type ->
                viewModel.adjustStock(plant.id, qtyChange, reason, type)
                plantForStockAdjust = null
            }
        )
    }
}

@Composable
fun HeroBanner(
    nurseryName: String,
    ownerName: String,
    onAddSale: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Image(
                painter = painterResource(id = R.drawable.nursery_hero_banner),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(24.dp)),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.94f),
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.75f)
                            )
                        )
                    )
                    .padding(16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "🌱 $nurseryName",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "Proprietor: $ownerName",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = onAddSale,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.testTag("hero_create_sale_button")
                        ) {
                            Icon(
                                Icons.Default.AddShoppingCart,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "New Sale Entry",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }

                    Spacer(Modifier.width(12.dp))

                    Surface(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .border(2.dp, Color.White.copy(alpha = 0.8f), CircleShape),
                        color = Color.White,
                        shadowElevation = 4.dp
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.app_logo_fg_1787660205866),
                            contentDescription = "App Logo",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LowStockAlertBanner(
    lowStockPlants: List<Plant>,
    onViewAll: () -> Unit,
    onQuickRestock: (Plant) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        color = LowStockOrangeContainer,
        border = BorderStroke(1.dp, LowStockOrangeBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
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
                            .background(LowStockOrange),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Low Stock",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Low Stock Alert",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = LowStockOrangeText
                        )
                        Text(
                            text = "${lowStockPlants.size} varieties below threshold",
                            style = MaterialTheme.typography.labelSmall,
                            color = LowStockOrangeText.copy(alpha = 0.85f)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = LowStockOrangeBadge,
                    modifier = Modifier
                        .clickable { onViewAll() }
                        .testTag("view_all_low_stock_button")
                ) {
                    Text(
                        text = "View",
                        color = LowStockOrangeText,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(lowStockPlants) { plant ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, LowStockOrangeBorder.copy(alpha = 0.6f)),
                        modifier = Modifier.clickable { onQuickRestock(plant) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Column {
                                Text(
                                    text = plant.plantName,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Text(
                                    text = "Only ${plant.quantity} left",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = LowStockOrange
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Restock",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionsGrid(
    onNewSale: () -> Unit,
    onAddPlant: () -> Unit,
    onAddExpense: () -> Unit,
    onAddCustomer: () -> Unit,
    onStockTracking: () -> Unit,
    onReports: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuickActionTile(
                icon = Icons.Default.LocalFlorist,
                label = "Add Plant",
                iconTint = MaterialTheme.colorScheme.primary,
                iconContainerColor = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.weight(1f),
                onClick = onAddPlant
            )
            QuickActionTile(
                icon = Icons.Default.AddShoppingCart,
                label = "New Sale",
                iconTint = BlueAccent,
                iconContainerColor = BlueAccentContainer,
                modifier = Modifier.weight(1f),
                onClick = onNewSale
            )
            QuickActionTile(
                icon = Icons.Default.TrendingDown,
                label = "Expenses",
                iconTint = ExpenseRed,
                iconContainerColor = LowStockRedContainer,
                modifier = Modifier.weight(1f),
                onClick = onAddExpense
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuickActionTile(
                icon = Icons.Default.PersonAdd,
                label = "Add Customer",
                iconTint = MaterialTheme.colorScheme.tertiary,
                iconContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                modifier = Modifier.weight(1f),
                onClick = onAddCustomer
            )
            QuickActionTile(
                icon = Icons.Default.Inventory,
                label = "Stock Log",
                iconTint = PurpleAccent,
                iconContainerColor = PurpleAccentContainer,
                modifier = Modifier.weight(1f),
                onClick = onStockTracking
            )
            QuickActionTile(
                icon = Icons.Default.Assessment,
                label = "Reports",
                iconTint = ProfitGreen,
                iconContainerColor = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.weight(1f),
                onClick = onReports
            )
        }
    }
}

@Composable
fun QuickActionTile(
    icon: ImageVector,
    label: String,
    iconTint: Color,
    iconContainerColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    OutlinedCard(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(iconContainerColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun RecentSaleItem(
    sale: Sale,
    currencySymbol: String,
    onClick: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column {
                    Text(
                        text = sale.getItemsSummary(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${sale.customerName} • ${sale.quantity} items",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = FormatUtils.formatCurrency(sale.amount, currencySymbol),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = FormatUtils.formatShortDate(sale.date),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
