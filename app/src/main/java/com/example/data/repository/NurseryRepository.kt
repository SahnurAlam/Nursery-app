package com.example.data.repository

import androidx.room.withTransaction
import com.example.data.local.AppDatabase
import com.example.data.model.Customer
import com.example.data.model.CustomerPurchase
import com.example.data.model.Expense
import com.example.data.model.NurseryBackup
import com.example.data.model.Plant
import com.example.data.model.PurchasePlatforms
import com.example.data.model.Sale
import com.example.data.model.SearchHistory
import com.example.data.model.StockLog
import com.example.data.model.StockLogType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar

class NurseryRepository(private val database: AppDatabase) {

    private val plantDao = database.plantDao()
    private val customerDao = database.customerDao()
    private val customerPurchaseDao = database.customerPurchaseDao()
    private val saleDao = database.saleDao()
    private val expenseDao = database.expenseDao()
    private val stockLogDao = database.stockLogDao()
    private val searchHistoryDao = database.searchHistoryDao()

    // ----------------- PLANTS -----------------
    val allPlants: Flow<List<Plant>> = plantDao.getAllPlants()
    val lowStockPlants: Flow<List<Plant>> = plantDao.getLowStockPlants()
    val totalPlantCount: Flow<Int> = plantDao.getTotalPlantCount()
    val totalQuantityInStock: Flow<Int> = plantDao.getTotalQuantityInStock()
    val totalInventoryCost: Flow<Double> = plantDao.getTotalInventoryCost()
    val totalInventoryRetailValue: Flow<Double> = plantDao.getTotalInventoryRetailValue()

    fun getPlantFlowById(id: Long): Flow<Plant?> = plantDao.getPlantFlowById(id)
    suspend fun getPlantById(id: Long): Plant? = plantDao.getPlantById(id)
    fun searchPlants(query: String): Flow<List<Plant>> = plantDao.searchPlants(query)
    fun getPlantsByCategory(category: String): Flow<List<Plant>> = plantDao.getPlantsByCategory(category)

    suspend fun insertPlant(plant: Plant): Long = plantDao.insertPlant(plant)
    suspend fun updatePlant(plant: Plant) = plantDao.updatePlant(plant)
    suspend fun deletePlant(plant: Plant) = plantDao.deletePlant(plant)

    suspend fun adjustPlantStock(plantId: Long, quantityChange: Int, reason: String, type: String) {
        val plant = plantDao.getPlantById(plantId) ?: return
        val newQuantity = (plant.quantity + quantityChange).coerceAtLeast(0)
        plantDao.updatePlantQuantity(plantId, newQuantity)

        stockLogDao.insertLog(
            StockLog(
                plantId = plantId,
                plantName = plant.plantName,
                type = type,
                quantityChanged = quantityChange,
                remainingStock = newQuantity,
                reason = reason.ifBlank { "Manual stock adjustment" },
                date = System.currentTimeMillis()
            )
        )
    }

    // ----------------- CUSTOMERS -----------------
    val allCustomers: Flow<List<Customer>> = customerDao.getAllCustomers()
    val totalCustomerCount: Flow<Int> = customerDao.getTotalCustomerCount()

    fun getCustomerFlowById(id: Long): Flow<Customer?> = customerDao.getCustomerFlowById(id)
    suspend fun getCustomerById(id: Long): Customer? = customerDao.getCustomerById(id)
    fun searchCustomers(query: String): Flow<List<Customer>> = customerDao.searchCustomers(query)
    suspend fun insertCustomer(customer: Customer): Long = customerDao.insertCustomer(customer)
    suspend fun updateCustomer(customer: Customer) = customerDao.updateCustomer(customer)
    suspend fun deleteCustomer(customer: Customer) {
        database.withTransaction {
            customerPurchaseDao.deletePurchasesByCustomerId(customer.id)
            customerDao.deleteCustomer(customer)
        }
    }

    // ----------------- CUSTOMER PURCHASES -----------------
    val allCustomerPurchases: Flow<List<CustomerPurchase>> = customerPurchaseDao.getAllPurchases()
    fun getPurchasesByCustomer(customerId: Long): Flow<List<CustomerPurchase>> =
        customerPurchaseDao.getPurchasesByCustomerId(customerId)
    suspend fun getPurchasesByCustomerList(customerId: Long): List<CustomerPurchase> =
        customerPurchaseDao.getPurchasesByCustomerIdList(customerId)

    suspend fun insertCustomerPurchase(purchase: CustomerPurchase): Long =
        customerPurchaseDao.insertPurchase(purchase)

    suspend fun updateCustomerPurchase(purchase: CustomerPurchase) =
        customerPurchaseDao.updatePurchase(purchase)

    suspend fun deleteCustomerPurchase(purchase: CustomerPurchase) =
        customerPurchaseDao.deletePurchase(purchase)

    suspend fun deleteCustomerPurchaseById(id: Long) =
        customerPurchaseDao.deletePurchaseById(id)

    suspend fun saveCustomerWithPurchases(
        customer: Customer,
        purchases: List<CustomerPurchase>
    ): Long {
        return database.withTransaction {
            val customerId = if (customer.id == 0L) {
                customerDao.insertCustomer(customer)
            } else {
                customerDao.updateCustomer(customer)
                customer.id
            }

            customerPurchaseDao.deletePurchasesByCustomerId(customerId)
            val updatedPurchases = purchases.map {
                it.copy(id = 0, customerId = customerId)
            }
            if (updatedPurchases.isNotEmpty()) {
                customerPurchaseDao.insertPurchases(updatedPurchases)
            }
            customerId
        }
    }

    // ----------------- SALES -----------------
    val allSales: Flow<List<Sale>> = saleDao.getAllSales()
    val totalAllTimeSales: Flow<Double> = saleDao.getTotalAllTimeSalesAmount()

    fun getSalesByCustomer(customerId: Long): Flow<List<Sale>> = saleDao.getSalesByCustomer(customerId)
    fun getSalesByPlant(plantId: Long): Flow<List<Sale>> = saleDao.getSalesByPlant(plantId)
    fun getSalesByDateRange(startDate: Long, endDate: Long): Flow<List<Sale>> =
        saleDao.getSalesByDateRange(startDate, endDate)
    fun searchSales(query: String): Flow<List<Sale>> = saleDao.searchSales(query)

    suspend fun recordSale(sale: Sale): Long {
        return database.withTransaction {
            val saleId = saleDao.insertSale(sale)
            val items = sale.getSaleItems()

            if (items.isNotEmpty()) {
                for (item in items) {
                    if (item.plantId > 0) {
                        val plant = plantDao.getPlantById(item.plantId)
                        if (plant != null) {
                            val newQty = (plant.quantity - item.quantity).coerceAtLeast(0)
                            plantDao.updatePlantQuantity(plant.id, newQty)

                            stockLogDao.insertLog(
                                StockLog(
                                    plantId = plant.id,
                                    plantName = plant.plantName,
                                    type = StockLogType.SALE,
                                    quantityChanged = -item.quantity,
                                    remainingStock = newQty,
                                    reason = "Sold to ${sale.customerName} (Invoice #$saleId)",
                                    date = sale.date
                                )
                            )
                        }
                    }
                }
            } else if (sale.plantId > 0) {
                // Fallback for single-item
                val plant = plantDao.getPlantById(sale.plantId)
                if (plant != null) {
                    val newQty = (plant.quantity - sale.quantity).coerceAtLeast(0)
                    plantDao.updatePlantQuantity(plant.id, newQty)

                    stockLogDao.insertLog(
                        StockLog(
                            plantId = plant.id,
                            plantName = plant.plantName,
                            type = StockLogType.SALE,
                            quantityChanged = -sale.quantity,
                            remainingStock = newQty,
                            reason = "Sold to ${sale.customerName} (Invoice #$saleId)",
                            date = sale.date
                        )
                    )
                }
            }

            saleId
        }
    }

    suspend fun deleteSale(sale: Sale) {
        saleDao.deleteSale(sale)
    }

    // ----------------- EXPENSES -----------------
    val allExpenses: Flow<List<Expense>> = expenseDao.getAllExpenses()
    val totalAllTimeExpenses: Flow<Double> = expenseDao.getTotalAllTimeExpenses()

    fun getExpensesByCategory(category: String): Flow<List<Expense>> = expenseDao.getExpensesByCategory(category)
    fun getExpensesByDateRange(startDate: Long, endDate: Long): Flow<List<Expense>> =
        expenseDao.getExpensesByDateRange(startDate, endDate)
    fun searchExpenses(query: String): Flow<List<Expense>> = expenseDao.searchExpenses(query)

    suspend fun insertExpense(expense: Expense): Long = expenseDao.insertExpense(expense)
    suspend fun deleteExpense(expense: Expense) = expenseDao.deleteExpense(expense)

    // ----------------- STOCK LOGS -----------------
    val allStockLogs: Flow<List<StockLog>> = stockLogDao.getAllLogs()
    fun getStockLogsByPlant(plantId: Long): Flow<List<StockLog>> = stockLogDao.getLogsByPlant(plantId)
    fun getStockLogsByType(type: String): Flow<List<StockLog>> = stockLogDao.getLogsByType(type)

    // ----------------- TIME-BASED METRICS -----------------
    fun getTodayStartAndEnd(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val start = calendar.timeInMillis

        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val end = calendar.timeInMillis
        return Pair(start, end)
    }

    fun getMonthStartAndEnd(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val start = calendar.timeInMillis

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val end = calendar.timeInMillis
        return Pair(start, end)
    }

    fun getYearStartAndEnd(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_YEAR, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val start = calendar.timeInMillis

        calendar.set(Calendar.DAY_OF_YEAR, calendar.getActualMaximum(Calendar.DAY_OF_YEAR))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val end = calendar.timeInMillis
        return Pair(start, end)
    }

    fun getTodaySales(): Flow<Double> {
        val (start, end) = getTodayStartAndEnd()
        return saleDao.getTotalSalesAmountByDateRange(start, end)
    }

    fun getMonthSales(): Flow<Double> {
        val (start, end) = getMonthStartAndEnd()
        return saleDao.getTotalSalesAmountByDateRange(start, end)
    }

    fun getMonthExpenses(): Flow<Double> {
        val (start, end) = getMonthStartAndEnd()
        return expenseDao.getTotalExpenseAmountByDateRange(start, end)
    }

    fun getMonthNetProfit(): Flow<Double> {
        return combine(getMonthSales(), getMonthExpenses()) { sales, expenses ->
            sales - expenses
        }
    }

    // ----------------- SEARCH HISTORY -----------------
    val allRecentSearches: Flow<List<SearchHistory>> = searchHistoryDao.getRecentSearches(20)

    fun getRecentSearchesByType(type: String): Flow<List<SearchHistory>> =
        searchHistoryDao.getRecentSearchesByType(type, 15)

    suspend fun recordSearch(query: String, type: String = "GLOBAL") {
        val trimmed = query.trim()
        if (trimmed.length < 2) return
        searchHistoryDao.deleteSearchByQuery(trimmed, type)
        searchHistoryDao.insertSearch(
            SearchHistory(
                query = trimmed,
                searchType = type,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteSearchById(id: Long) {
        searchHistoryDao.deleteSearchById(id)
    }

    suspend fun clearSearchHistory(type: String? = null) {
        if (type == null) {
            searchHistoryDao.clearAllHistory()
        } else {
            searchHistoryDao.clearHistoryByType(type)
        }
    }

    // ----------------- BACKUP & RESTORE -----------------
    suspend fun exportToJsonString(
        nurseryName: String,
        invoiceNotes: String = "",
        invoiceFooter: String = ""
    ): String {
        val plants = plantDao.getAllPlantsList()
        val customers = customerDao.getAllCustomersList()
        val customerPurchases = customerPurchaseDao.getAllPurchasesList()
        val sales = saleDao.getAllSalesList()
        val expenses = expenseDao.getAllExpensesList()
        val stockLogs = stockLogDao.getAllStockLogsList()

        val root = JSONObject().apply {
            put("app", "Nursery Data Management")
            put("version", 1)
            put("nurseryName", nurseryName)
            if (invoiceNotes.isNotBlank()) put("invoiceNotes", invoiceNotes)
            if (invoiceFooter.isNotBlank()) put("invoiceFooter", invoiceFooter)
            put("exportDate", System.currentTimeMillis())

            put("plants", JSONArray().apply {
                plants.forEach { p ->
                    put(JSONObject().apply {
                        put("id", p.id)
                        put("plantName", p.plantName)
                        put("category", p.category)
                        put("variety", p.variety)
                        put("quantity", p.quantity)
                        put("purchasePrice", p.purchasePrice)
                        put("sellingPrice", p.sellingPrice)
                        put("imagePath", p.imagePath)
                        put("notes", p.notes)
                        put("lowStockThreshold", p.lowStockThreshold)
                        put("createdDate", p.createdDate)
                    })
                }
            })

            put("customers", JSONArray().apply {
                customers.forEach { c ->
                    put(JSONObject().apply {
                        put("id", c.id)
                        put("name", c.name)
                        put("mobile", c.mobile)
                        put("address", c.address)
                        put("notes", c.notes)
                        put("createdDate", c.createdDate)
                    })
                }
            })

            put("customerPurchases", JSONArray().apply {
                customerPurchases.forEach { cp ->
                    put(JSONObject().apply {
                        put("id", cp.id)
                        put("customerId", cp.customerId)
                        put("platform", cp.platform)
                        put("productName", cp.productName)
                        put("quantity", cp.quantity)
                        put("purchasePrice", cp.purchasePrice)
                        put("purchaseDate", cp.purchaseDate)
                        put("remarks", cp.remarks)
                    })
                }
            })

            put("sales", JSONArray().apply {
                sales.forEach { s ->
                    put(JSONObject().apply {
                        put("id", s.id)
                        put("customerId", s.customerId)
                        put("customerName", s.customerName)
                        put("plantId", s.plantId)
                        put("plantName", s.plantName)
                        put("quantity", s.quantity)
                        put("unitPrice", s.unitPrice)
                        put("discountPercent", s.discountPercent)
                        put("discount", s.discount)
                        put("amount", s.amount)
                        put("paymentMethod", s.paymentMethod)
                        put("notes", s.notes)
                        put("date", s.date)
                        put("itemsJson", s.itemsJson)
                    })
                }
            })

            put("expenses", JSONArray().apply {
                expenses.forEach { e ->
                    put(JSONObject().apply {
                        put("id", e.id)
                        put("category", e.category)
                        put("amount", e.amount)
                        put("description", e.description)
                        put("paymentMethod", e.paymentMethod)
                        put("date", e.date)
                    })
                }
            })

            put("stockLogs", JSONArray().apply {
                stockLogs.forEach { l ->
                    put(JSONObject().apply {
                        put("id", l.id)
                        put("plantId", l.plantId)
                        put("plantName", l.plantName)
                        put("type", l.type)
                        put("quantityChanged", l.quantityChanged)
                        put("remainingStock", l.remainingStock)
                        put("reason", l.reason)
                        put("date", l.date)
                    })
                }
            })
        }

        return root.toString(2)
    }

    suspend fun exportCustomerDataJsonString(): String {
        val customers = customerDao.getAllCustomersList()
        val allPurchases = customerPurchaseDao.getAllPurchasesList()
        val purchasesByCust = allPurchases.groupBy { it.customerId }

        val root = JSONObject().apply {
            put("exportType", "CUSTOMERS_ONLY")
            put("app", "Nursery Data Management")
            put("version", 1)
            put("exportDate", System.currentTimeMillis())
            put("totalCustomers", customers.size)

            put("customers", JSONArray().apply {
                customers.forEach { c ->
                    put(JSONObject().apply {
                        put("id", c.id)
                        put("name", c.name)
                        put("mobile", c.mobile)
                        put("address", c.address)
                        put("notes", c.notes)
                        put("createdDate", c.createdDate)

                        val custPurchases = purchasesByCust[c.id] ?: emptyList()
                        put("purchaseHistory", JSONArray().apply {
                            custPurchases.forEach { p ->
                                put(JSONObject().apply {
                                    put("id", p.id)
                                    put("platform", p.platform)
                                    put("productName", p.productName)
                                    put("quantity", p.quantity)
                                    put("purchasePrice", p.purchasePrice)
                                    put("purchaseDate", p.purchaseDate)
                                    put("remarks", p.remarks)
                                })
                            }
                        })
                    })
                }
            })
        }
        return root.toString(2)
    }

    suspend fun importFromJsonString(jsonString: String): Boolean {
        return try {
            val root = JSONObject(jsonString)

            // Check if this is a Customer-Only export file
            val isCustomerOnly = root.optString("exportType") == "CUSTOMERS_ONLY" ||
                    (root.has("customers") && !root.has("plants") && !root.has("expenses") && !root.has("stockLogs"))

            if (isCustomerOnly) {
                val customerList = mutableListOf<Customer>()
                val purchaseList = mutableListOf<CustomerPurchase>()

                val customersArray = root.getJSONArray("customers")
                for (i in 0 until customersArray.length()) {
                    val cObj = customersArray.getJSONObject(i)
                    val custId = cObj.optLong("id", (i + 1).toLong())
                    customerList.add(
                        Customer(
                            id = custId,
                            name = cObj.getString("name"),
                            mobile = cObj.optString("mobile", ""),
                            address = cObj.optString("address", ""),
                            notes = cObj.optString("notes", ""),
                            createdDate = cObj.optLong("createdDate", System.currentTimeMillis())
                        )
                    )

                    val purchaseArray = when {
                        cObj.has("purchaseHistory") -> cObj.getJSONArray("purchaseHistory")
                        cObj.has("purchases") -> cObj.getJSONArray("purchases")
                        else -> null
                    }

                    if (purchaseArray != null) {
                        for (j in 0 until purchaseArray.length()) {
                            val pObj = purchaseArray.getJSONObject(j)
                            purchaseList.add(
                                CustomerPurchase(
                                    id = pObj.optLong("id", 0),
                                    customerId = custId,
                                    platform = pObj.optString("platform", PurchasePlatforms.DIRECT_ORDER),
                                    productName = pObj.optString("productName", "Plant"),
                                    quantity = pObj.optInt("quantity", 1),
                                    purchasePrice = pObj.optDouble("purchasePrice", 0.0),
                                    purchaseDate = pObj.optLong("purchaseDate", System.currentTimeMillis()),
                                    remarks = pObj.optString("remarks", "")
                                )
                            )
                        }
                    }
                }

                // If customerPurchases is provided at root level
                if (root.has("customerPurchases")) {
                    val rootPurchaseArray = root.getJSONArray("customerPurchases")
                    for (k in 0 until rootPurchaseArray.length()) {
                        val pObj = rootPurchaseArray.getJSONObject(k)
                        purchaseList.add(
                            CustomerPurchase(
                                id = pObj.optLong("id", 0),
                                customerId = pObj.optLong("customerId", 0),
                                platform = pObj.optString("platform", PurchasePlatforms.DIRECT_ORDER),
                                productName = pObj.optString("productName", "Plant"),
                                quantity = pObj.optInt("quantity", 1),
                                purchasePrice = pObj.optDouble("purchasePrice", 0.0),
                                purchaseDate = pObj.optLong("purchaseDate", System.currentTimeMillis()),
                                remarks = pObj.optString("remarks", "")
                            )
                        )
                    }
                }

                database.withTransaction {
                    customerPurchaseDao.clearAll()
                    customerDao.clearAll()
                    customerDao.insertAll(customerList)
                    if (purchaseList.isNotEmpty()) {
                        customerPurchaseDao.insertPurchases(purchaseList)
                    }
                }
                return true
            }

            // Full database backup restore
            val plantList = mutableListOf<Plant>()
            val customerList = mutableListOf<Customer>()
            val purchaseList = mutableListOf<CustomerPurchase>()
            val salesList = mutableListOf<Sale>()
            val expenseList = mutableListOf<Expense>()
            val logList = mutableListOf<StockLog>()

            if (root.has("plants")) {
                val array = root.getJSONArray("plants")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    plantList.add(
                        Plant(
                            id = obj.optLong("id", 0),
                            plantName = obj.getString("plantName"),
                            category = obj.optString("category", "Other"),
                            variety = obj.optString("variety", ""),
                            quantity = obj.optInt("quantity", 0),
                            purchasePrice = obj.optDouble("purchasePrice", 0.0),
                            sellingPrice = obj.optDouble("sellingPrice", 0.0),
                            imagePath = obj.optString("imagePath", ""),
                            notes = obj.optString("notes", ""),
                            lowStockThreshold = obj.optInt("lowStockThreshold", 10),
                            createdDate = obj.optLong("createdDate", System.currentTimeMillis())
                        )
                    )
                }
            }

            if (root.has("customers")) {
                val array = root.getJSONArray("customers")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val custId = obj.optLong("id", 0)
                    customerList.add(
                        Customer(
                            id = custId,
                            name = obj.getString("name"),
                            mobile = obj.optString("mobile", ""),
                            address = obj.optString("address", ""),
                            notes = obj.optString("notes", ""),
                            createdDate = obj.optLong("createdDate", System.currentTimeMillis())
                        )
                    )

                    if (obj.has("purchaseHistory")) {
                        val pArray = obj.getJSONArray("purchaseHistory")
                        for (j in 0 until pArray.length()) {
                            val pObj = pArray.getJSONObject(j)
                            purchaseList.add(
                                CustomerPurchase(
                                    id = pObj.optLong("id", 0),
                                    customerId = custId,
                                    platform = pObj.optString("platform", PurchasePlatforms.DIRECT_ORDER),
                                    productName = pObj.optString("productName", "Plant"),
                                    quantity = pObj.optInt("quantity", 1),
                                    purchasePrice = pObj.optDouble("purchasePrice", 0.0),
                                    purchaseDate = pObj.optLong("purchaseDate", System.currentTimeMillis()),
                                    remarks = pObj.optString("remarks", "")
                                )
                            )
                        }
                    }
                }
            }

            if (root.has("customerPurchases")) {
                val array = root.getJSONArray("customerPurchases")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    purchaseList.add(
                        CustomerPurchase(
                            id = obj.optLong("id", 0),
                            customerId = obj.optLong("customerId", 0),
                            platform = obj.optString("platform", PurchasePlatforms.DIRECT_ORDER),
                            productName = obj.optString("productName", "Plant"),
                            quantity = obj.optInt("quantity", 1),
                            purchasePrice = obj.optDouble("purchasePrice", 0.0),
                            purchaseDate = obj.optLong("purchaseDate", System.currentTimeMillis()),
                            remarks = obj.optString("remarks", "")
                        )
                    )
                }
            }

            if (root.has("sales")) {
                val array = root.getJSONArray("sales")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    salesList.add(
                        Sale(
                            id = obj.optLong("id", 0),
                            customerId = obj.optLong("customerId", 0),
                            customerName = obj.optString("customerName", "Customer"),
                            plantId = obj.optLong("plantId", 0),
                            plantName = obj.optString("plantName", ""),
                            quantity = obj.optInt("quantity", 1),
                            unitPrice = obj.optDouble("unitPrice", 0.0),
                            discountPercent = obj.optDouble("discountPercent", 0.0),
                            discount = obj.optDouble("discount", 0.0),
                            amount = obj.optDouble("amount", 0.0),
                            paymentMethod = obj.optString("paymentMethod", "Cash"),
                            notes = obj.optString("notes", ""),
                            date = obj.optLong("date", System.currentTimeMillis()),
                            itemsJson = obj.optString("itemsJson", "")
                        )
                    )
                }
            }

            if (root.has("expenses")) {
                val array = root.getJSONArray("expenses")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    expenseList.add(
                        Expense(
                            id = obj.optLong("id", 0),
                            category = obj.getString("category"),
                            amount = obj.getDouble("amount"),
                            description = obj.optString("description", ""),
                            paymentMethod = obj.optString("paymentMethod", "Cash"),
                            date = obj.optLong("date", System.currentTimeMillis())
                        )
                    )
                }
            }

            if (root.has("stockLogs")) {
                val array = root.getJSONArray("stockLogs")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    logList.add(
                        StockLog(
                            id = obj.optLong("id", 0),
                            plantId = obj.getLong("plantId"),
                            plantName = obj.getString("plantName"),
                            type = obj.getString("type"),
                            quantityChanged = obj.getInt("quantityChanged"),
                            remainingStock = obj.getInt("remainingStock"),
                            reason = obj.optString("reason", ""),
                            date = obj.optLong("date", System.currentTimeMillis())
                        )
                    )
                }
            }

            database.withTransaction {
                plantDao.clearAll()
                customerDao.clearAll()
                customerPurchaseDao.clearAll()
                saleDao.clearAll()
                expenseDao.clearAll()
                stockLogDao.clearAll()

                plantDao.insertAll(plantList)
                customerDao.insertAll(customerList)
                if (purchaseList.isNotEmpty()) {
                    customerPurchaseDao.insertPurchases(purchaseList)
                }
                saleDao.insertAll(salesList)
                expenseDao.insertAll(expenseList)
                stockLogDao.insertAll(logList)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
