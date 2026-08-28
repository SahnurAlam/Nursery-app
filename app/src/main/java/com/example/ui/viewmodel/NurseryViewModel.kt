package com.example.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppThemeMode
import com.example.data.local.UserPreferences
import com.example.data.local.UserPreferencesRepository
import com.example.data.model.Customer
import com.example.data.model.CustomerPurchase
import com.example.data.model.Expense
import com.example.data.model.Plant
import com.example.data.model.Sale
import com.example.data.model.SearchHistory
import com.example.data.model.StockLog
import com.example.data.repository.NurseryRepository
import com.example.util.LogoBrandingManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

enum class PlantSortOption(val displayName: String) {
    NAME_ASC("Name (A to Z)"),
    NAME_DESC("Name (Z to A)"),
    PRICE_LOW_TO_HIGH("Price: Low to High"),
    PRICE_HIGH_TO_LOW("Price: High to Low"),
    QUANTITY_LOW_TO_HIGH("Stock: Low to High"),
    QUANTITY_HIGH_TO_LOW("Stock: High to Low"),
    RECENTLY_ADDED("Recently Added")
}

enum class SaleSortOption(val displayName: String) {
    DATE_NEWEST("Date: Newest First"),
    DATE_OLDEST("Date: Oldest First"),
    AMOUNT_HIGH_TO_LOW("Amount: High to Low"),
    AMOUNT_LOW_TO_HIGH("Amount: Low to High"),
    QUANTITY_HIGH_TO_LOW("Quantity: High to Low"),
    CUSTOMER_NAME_ASC("Customer (A to Z)")
}

data class DashboardUiState(
    val totalPlantCount: Int = 0,
    val totalStockQuantity: Int = 0,
    val totalInventoryCost: Double = 0.0,
    val totalInventoryRetail: Double = 0.0,
    val totalCustomers: Int = 0,
    val todaySales: Double = 0.0,
    val monthSales: Double = 0.0,
    val monthExpenses: Double = 0.0,
    val netProfit: Double = 0.0,
    val lowStockPlants: List<Plant> = emptyList(),
    val recentSales: List<Sale> = emptyList()
)

data class GlobalSearchResults(
    val query: String = "",
    val matchingPlants: List<Plant> = emptyList(),
    val matchingCustomers: List<Customer> = emptyList(),
    val matchingSales: List<Sale> = emptyList(),
    val matchingExpenses: List<Expense> = emptyList()
)

class NurseryViewModel(
    private val repository: NurseryRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    // Preferences
    val userPreferences: StateFlow<UserPreferences> = preferencesRepository.userPreferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )

    // Plants State & Filters
    private val _selectedPlantCategory = MutableStateFlow("All")
    val selectedPlantCategory: StateFlow<String> = _selectedPlantCategory.asStateFlow()

    private val _plantSearchQuery = MutableStateFlow("")
    val plantSearchQuery: StateFlow<String> = _plantSearchQuery.asStateFlow()

    private val _onlyLowStock = MutableStateFlow(false)
    val onlyLowStock: StateFlow<Boolean> = _onlyLowStock.asStateFlow()

    private val _plantSortOption = MutableStateFlow(PlantSortOption.NAME_ASC)
    val plantSortOption: StateFlow<PlantSortOption> = _plantSortOption.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val plants: StateFlow<List<Plant>> = combine(
        repository.allPlants,
        _selectedPlantCategory,
        _plantSearchQuery,
        _onlyLowStock,
        _plantSortOption
    ) { allPlants, category, query, lowStockOnly, sortOption ->
        val filtered = allPlants.filter { plant ->
            val matchesCategory = (category == "All" || plant.category == category)
            val matchesQuery = query.isBlank() ||
                    plant.plantName.contains(query, ignoreCase = true) ||
                    plant.variety.contains(query, ignoreCase = true) ||
                    plant.category.contains(query, ignoreCase = true)
            val matchesLowStock = !lowStockOnly || plant.isLowStock || plant.isOutOfStock
            matchesCategory && matchesQuery && matchesLowStock
        }
        when (sortOption) {
            PlantSortOption.NAME_ASC -> filtered.sortedBy { it.plantName.lowercase() }
            PlantSortOption.NAME_DESC -> filtered.sortedByDescending { it.plantName.lowercase() }
            PlantSortOption.PRICE_LOW_TO_HIGH -> filtered.sortedBy { it.sellingPrice }
            PlantSortOption.PRICE_HIGH_TO_LOW -> filtered.sortedByDescending { it.sellingPrice }
            PlantSortOption.QUANTITY_LOW_TO_HIGH -> filtered.sortedBy { it.quantity }
            PlantSortOption.QUANTITY_HIGH_TO_LOW -> filtered.sortedByDescending { it.quantity }
            PlantSortOption.RECENTLY_ADDED -> filtered.sortedByDescending { it.id }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val lowStockPlants: StateFlow<List<Plant>> = repository.lowStockPlants
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Customers State & Search
    private val _customerSearchQuery = MutableStateFlow("")
    val customerSearchQuery: StateFlow<String> = _customerSearchQuery.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val customers: StateFlow<List<Customer>> = _customerSearchQuery.flatMapLatest { query ->
        if (query.isBlank()) repository.allCustomers else repository.searchCustomers(query)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Sales State & Filter
    private val _salesDateFilter = MutableStateFlow("All") // All, Today, This Week, This Month
    val salesDateFilter: StateFlow<String> = _salesDateFilter.asStateFlow()

    private val _salesSearchQuery = MutableStateFlow("")
    val salesSearchQuery: StateFlow<String> = _salesSearchQuery.asStateFlow()

    private val _saleSortOption = MutableStateFlow(SaleSortOption.DATE_NEWEST)
    val saleSortOption: StateFlow<SaleSortOption> = _saleSortOption.asStateFlow()

    val sales: StateFlow<List<Sale>> = combine(
        repository.allSales,
        _salesDateFilter,
        _salesSearchQuery,
        _saleSortOption
    ) { allSales, filter, query, sortOption ->
        val now = System.currentTimeMillis()
        val dayMillis = 86400000L
        val filtered = allSales.filter { sale ->
            val matchesTime = when (filter) {
                "Today" -> sale.date >= (now - dayMillis)
                "This Week" -> sale.date >= (now - 7 * dayMillis)
                "This Month" -> sale.date >= (now - 30 * dayMillis)
                else -> true
            }
            val matchesQuery = query.isBlank() ||
                    sale.customerName.contains(query, ignoreCase = true) ||
                    sale.plantName.contains(query, ignoreCase = true) ||
                    sale.paymentMethod.contains(query, ignoreCase = true)
            matchesTime && matchesQuery
        }
        when (sortOption) {
            SaleSortOption.DATE_NEWEST -> filtered.sortedByDescending { it.date }
            SaleSortOption.DATE_OLDEST -> filtered.sortedBy { it.date }
            SaleSortOption.AMOUNT_HIGH_TO_LOW -> filtered.sortedByDescending { it.amount }
            SaleSortOption.AMOUNT_LOW_TO_HIGH -> filtered.sortedBy { it.amount }
            SaleSortOption.QUANTITY_HIGH_TO_LOW -> filtered.sortedByDescending { it.quantity }
            SaleSortOption.CUSTOMER_NAME_ASC -> filtered.sortedBy { it.customerName.lowercase() }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Expenses State & Filter
    private val _expenseCategoryFilter = MutableStateFlow("All")
    val expenseCategoryFilter: StateFlow<String> = _expenseCategoryFilter.asStateFlow()

    private val _expenseSearchQuery = MutableStateFlow("")
    val expenseSearchQuery: StateFlow<String> = _expenseSearchQuery.asStateFlow()

    val expenses: StateFlow<List<Expense>> = combine(
        repository.allExpenses,
        _expenseCategoryFilter,
        _expenseSearchQuery
    ) { allExpenses, category, query ->
        allExpenses.filter { exp ->
            val matchesCategory = (category == "All" || exp.category == category)
            val matchesQuery = query.isBlank() ||
                    exp.description.contains(query, ignoreCase = true) ||
                    exp.category.contains(query, ignoreCase = true) ||
                    exp.paymentMethod.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Stock Logs
    val stockLogs: StateFlow<List<StockLog>> = repository.allStockLogs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Dashboard UI State
    val dashboardUiState: StateFlow<DashboardUiState> = combine(
        repository.totalPlantCount,
        repository.totalQuantityInStock,
        repository.totalInventoryCost,
        repository.totalInventoryRetailValue,
        repository.totalCustomerCount,
        repository.getTodaySales(),
        repository.getMonthSales(),
        repository.getMonthExpenses(),
        repository.lowStockPlants,
        repository.allSales
    ) { values ->
        val count = values[0] as Int
        val qty = values[1] as Int
        val cost = values[2] as Double
        val retail = values[3] as Double
        val custCount = values[4] as Int
        val todaySale = values[5] as Double
        val monthSale = values[6] as Double
        val monthExp = values[7] as Double
        @Suppress("UNCHECKED_CAST")
        val lowStock = values[8] as List<Plant>
        @Suppress("UNCHECKED_CAST")
        val recentSales = (values[9] as List<Sale>).take(5)

        DashboardUiState(
            totalPlantCount = count,
            totalStockQuantity = qty,
            totalInventoryCost = cost,
            totalInventoryRetail = retail,
            totalCustomers = custCount,
            todaySales = todaySale,
            monthSales = monthSale,
            monthExpenses = monthExp,
            netProfit = monthSale - monthExp,
            lowStockPlants = lowStock,
            recentSales = recentSales
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )

    // Global Search State
    private val _globalSearchQuery = MutableStateFlow("")
    val globalSearchQuery: StateFlow<String> = _globalSearchQuery.asStateFlow()

    val globalSearchResults: StateFlow<GlobalSearchResults> = combine(
        _globalSearchQuery,
        repository.allPlants,
        repository.allCustomers,
        repository.allSales,
        repository.allExpenses
    ) { query, allPlants, allCust, allSales, allExp ->
        if (query.isBlank()) {
            GlobalSearchResults()
        } else {
            val q = query.trim()
            val mPlants = allPlants.filter {
                it.plantName.contains(q, ignoreCase = true) ||
                        it.category.contains(q, ignoreCase = true) ||
                        it.variety.contains(q, ignoreCase = true)
            }
            val mCust = allCust.filter {
                it.name.contains(q, ignoreCase = true) ||
                        it.mobile.contains(q, ignoreCase = true) ||
                        it.address.contains(q, ignoreCase = true)
            }
            val mSales = allSales.filter {
                it.customerName.contains(q, ignoreCase = true) ||
                        it.plantName.contains(q, ignoreCase = true) ||
                        it.notes.contains(q, ignoreCase = true)
            }
            val mExp = allExp.filter {
                it.description.contains(q, ignoreCase = true) ||
                        it.category.contains(q, ignoreCase = true)
            }
            GlobalSearchResults(
                query = q,
                matchingPlants = mPlants,
                matchingCustomers = mCust,
                matchingSales = mSales,
                matchingExpenses = mExp
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GlobalSearchResults()
    )

    // Search History State
    val searchHistory: StateFlow<List<SearchHistory>> = repository.allRecentSearches
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun recordSearch(query: String, type: String = "GLOBAL") {
        if (query.trim().length >= 2) {
            viewModelScope.launch {
                repository.recordSearch(query.trim(), type)
            }
        }
    }

    fun deleteSearchHistoryItem(id: Long) {
        viewModelScope.launch {
            repository.deleteSearchById(id)
        }
    }

    fun clearSearchHistory(type: String? = null) {
        viewModelScope.launch {
            repository.clearSearchHistory(type)
            _userMessage.value = "Search history cleared"
        }
    }

    // Toast / Feedback message
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun showMessage(msg: String) {
        _userMessage.value = msg
    }

    // ----------------- PLANT ACTIONS -----------------
    fun setPlantCategory(category: String) {
        _selectedPlantCategory.value = category
    }

    fun setPlantSearchQuery(query: String) {
        _plantSearchQuery.value = query
    }

    fun toggleLowStockFilter(onlyLow: Boolean) {
        _onlyLowStock.value = onlyLow
    }

    fun setPlantSortOption(sortOption: PlantSortOption) {
        _plantSortOption.value = sortOption
    }

    fun savePlant(plant: Plant, onFinished: () -> Unit = {}) {
        viewModelScope.launch {
            if (plant.id == 0L) {
                repository.insertPlant(plant)
                _userMessage.value = "Plant \"${plant.plantName}\" added successfully"
            } else {
                repository.updatePlant(plant)
                _userMessage.value = "Plant \"${plant.plantName}\" updated"
            }
            onFinished()
        }
    }

    fun deletePlant(plant: Plant) {
        viewModelScope.launch {
            repository.deletePlant(plant)
            _userMessage.value = "Plant deleted"
        }
    }

    fun adjustStock(plantId: Long, quantityChange: Int, reason: String, type: String) {
        viewModelScope.launch {
            repository.adjustPlantStock(plantId, quantityChange, reason, type)
            _userMessage.value = "Stock updated successfully"
        }
    }

    // ----------------- CUSTOMER ACTIONS -----------------
    fun setCustomerSearchQuery(query: String) {
        _customerSearchQuery.value = query
    }

    fun saveCustomer(customer: Customer, onFinished: () -> Unit = {}) {
        viewModelScope.launch {
            if (customer.id == 0L) {
                repository.insertCustomer(customer)
                _userMessage.value = "Customer \"${customer.name}\" added"
            } else {
                repository.updateCustomer(customer)
                _userMessage.value = "Customer updated"
            }
            onFinished()
        }
    }

    fun saveCustomerWithPurchases(
        customer: Customer,
        purchases: List<CustomerPurchase>,
        onFinished: () -> Unit = {}
    ) {
        viewModelScope.launch {
            repository.saveCustomerWithPurchases(customer, purchases)
            _userMessage.value = if (customer.id == 0L) {
                "Customer \"${customer.name}\" and purchase history saved"
            } else {
                "Customer \"${customer.name}\" updated"
            }
            onFinished()
        }
    }

    fun deleteCustomer(customer: Customer) {
        viewModelScope.launch {
            repository.deleteCustomer(customer)
            _userMessage.value = "Customer removed"
        }
    }

    fun getCustomerPurchasesFlow(customerId: Long): Flow<List<CustomerPurchase>> =
        repository.getPurchasesByCustomer(customerId)

    suspend fun getCustomerPurchasesList(customerId: Long): List<CustomerPurchase> =
        repository.getPurchasesByCustomerList(customerId)

    fun addCustomerPurchase(purchase: CustomerPurchase, onFinished: () -> Unit = {}) {
        viewModelScope.launch {
            repository.insertCustomerPurchase(purchase)
            _userMessage.value = "Purchase record added"
            onFinished()
        }
    }

    fun updateCustomerPurchase(purchase: CustomerPurchase, onFinished: () -> Unit = {}) {
        viewModelScope.launch {
            repository.updateCustomerPurchase(purchase)
            _userMessage.value = "Purchase record updated"
            onFinished()
        }
    }

    fun deleteCustomerPurchase(purchase: CustomerPurchase) {
        viewModelScope.launch {
            repository.deleteCustomerPurchase(purchase)
            _userMessage.value = "Purchase record removed"
        }
    }

    fun deleteCustomerPurchaseById(id: Long) {
        viewModelScope.launch {
            repository.deleteCustomerPurchaseById(id)
            _userMessage.value = "Purchase record removed"
        }
    }

    fun getCustomerSales(customerId: Long) = repository.getSalesByCustomer(customerId)

    // ----------------- SALES ACTIONS -----------------
    fun setSalesDateFilter(filter: String) {
        _salesDateFilter.value = filter
    }

    fun setSalesSearchQuery(query: String) {
        _salesSearchQuery.value = query
    }

    fun setSaleSortOption(sortOption: SaleSortOption) {
        _saleSortOption.value = sortOption
    }

    fun createSale(sale: Sale, onSuccess: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val saleId = repository.recordSale(sale)
            _userMessage.value = "Sale recorded successfully! Invoice #$saleId"
            onSuccess(saleId)
        }
    }

    fun deleteSale(sale: Sale) {
        viewModelScope.launch {
            repository.deleteSale(sale)
            _userMessage.value = "Sale record deleted"
        }
    }

    // ----------------- EXPENSE ACTIONS -----------------
    fun setExpenseCategoryFilter(category: String) {
        _expenseCategoryFilter.value = category
    }

    fun setExpenseSearchQuery(query: String) {
        _expenseSearchQuery.value = query
    }

    fun addExpense(expense: Expense, onFinished: () -> Unit = {}) {
        viewModelScope.launch {
            repository.insertExpense(expense)
            _userMessage.value = "Expense recorded"
            onFinished()
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
            _userMessage.value = "Expense deleted"
        }
    }

    // ----------------- GLOBAL SEARCH -----------------
    fun setGlobalSearchQuery(query: String) {
        _globalSearchQuery.value = query
    }

    // ----------------- SETTINGS & BACKUP -----------------
    fun updateThemeMode(mode: AppThemeMode) {
        viewModelScope.launch {
            preferencesRepository.updateThemeMode(mode)
        }
    }

    fun updateCurrency(symbol: String) {
        viewModelScope.launch {
            preferencesRepository.updateCurrencySymbol(symbol)
            _userMessage.value = "Currency updated to $symbol"
        }
    }

    fun updateNurseryProfile(name: String, owner: String, phone: String, address: String) {
        viewModelScope.launch {
            preferencesRepository.updateNurseryProfile(name, owner, phone, address)
            _userMessage.value = "Nursery profile updated"
        }
    }

    fun saveCustomLogoFromUri(uri: Uri, context: Context, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            val nurseryName = userPreferences.value.nurseryName
            val savedPath = LogoBrandingManager.saveLogoAndGenerateLauncherAssets(
                context = context,
                sourceUri = uri,
                nurseryName = nurseryName
            )
            if (savedPath != null) {
                preferencesRepository.updateCustomLogoPath(savedPath)
                withContext(Dispatchers.Main) {
                    _userMessage.value = "Nursery logo & launcher icon updated successfully!"
                    onResult(true)
                }
            } else {
                withContext(Dispatchers.Main) {
                    _userMessage.value = "Failed to save logo image."
                    onResult(false)
                }
            }
        }
    }

    fun removeCustomLogo(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            LogoBrandingManager.resetToDefaultLogo(context)
            preferencesRepository.updateCustomLogoPath(null)
            withContext(Dispatchers.Main) {
                _userMessage.value = "Nursery branding & launcher icon reset to default"
            }
        }
    }

    fun updateCustomLogoPath(path: String?) {
        viewModelScope.launch {
            preferencesRepository.updateCustomLogoPath(path)
        }
    }

    fun updateInvoiceCustomization(notes: String, footer: String) {
        viewModelScope.launch {
            preferencesRepository.updateInvoiceCustomization(notes, footer)
            _userMessage.value = "Invoice customization saved successfully!"
        }
    }

    fun resetInvoiceCustomization() {
        viewModelScope.launch {
            preferencesRepository.resetInvoiceCustomization()
            _userMessage.value = "Invoice customization reset to defaults"
        }
    }

    suspend fun getExportJsonString(): String {
        val prefs = userPreferences.value
        return repository.exportToJsonString(
            nurseryName = prefs.nurseryName,
            invoiceNotes = prefs.invoiceNotes,
            invoiceFooter = prefs.invoiceFooter
        )
    }

    suspend fun getExportCustomerDataJsonString(): String {
        return repository.exportCustomerDataJsonString()
    }

    fun restoreFromJson(json: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.importFromJsonString(json)
            if (success) {
                try {
                    val root = org.json.JSONObject(json)
                    if (root.has("invoiceNotes") || root.has("invoiceFooter")) {
                        val notes = root.optString("invoiceNotes", UserPreferences.DEFAULT_INVOICE_NOTES)
                        val footer = root.optString("invoiceFooter", UserPreferences.DEFAULT_INVOICE_FOOTER)
                        preferencesRepository.updateInvoiceCustomization(notes, footer)
                    }
                } catch (e: Exception) {
                    // non-fatal
                }
                _userMessage.value = "Database backup restored successfully!"
            } else {
                _userMessage.value = "Failed to restore backup. Invalid JSON file."
            }
            onResult(success)
        }
    }
}
