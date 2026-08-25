package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.Customer
import com.example.data.model.CustomerPurchase
import com.example.data.model.Expense
import com.example.data.model.Plant
import com.example.data.model.PurchasePlatforms
import com.example.data.model.Sale
import com.example.data.model.SearchHistory
import com.example.data.model.StockLog
import com.example.data.model.StockLogType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Plant::class,
        Customer::class,
        CustomerPurchase::class,
        Sale::class,
        Expense::class,
        StockLog::class,
        SearchHistory::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun plantDao(): PlantDao
    abstract fun customerDao(): CustomerDao
    abstract fun customerPurchaseDao(): CustomerPurchaseDao
    abstract fun saleDao(): SaleDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun stockLogDao(): StockLogDao
    abstract fun searchHistoryDao(): SearchHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sahnur_nursery_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        seedInitialData(database)
                    }
                }
            }
        }

        suspend fun seedInitialData(database: AppDatabase) {
            val now = System.currentTimeMillis()
            val day = 86400000L

            val samplePlants = listOf(
                Plant(
                    plantName = "Mango - Amrapali Grafted",
                    category = "Fruit Plants",
                    variety = "Hybrid Dwarf",
                    quantity = 45,
                    purchasePrice = 120.0,
                    sellingPrice = 250.0,
                    notes = "Sweet, high yield, ready for planting",
                    lowStockThreshold = 10,
                    createdDate = now - 15 * day
                ),
                Plant(
                    plantName = "Guava - Thai Super Red",
                    category = "Fruit Plants",
                    variety = "Sweet Crispy",
                    quantity = 28,
                    purchasePrice = 80.0,
                    sellingPrice = 180.0,
                    notes = "Fast growing, all-season fruit bearer",
                    lowStockThreshold = 10,
                    createdDate = now - 12 * day
                ),
                Plant(
                    plantName = "Lemon - Kagzi Seedless",
                    category = "Fruit Plants",
                    variety = "Baramasi (12 months)",
                    quantity = 6,
                    purchasePrice = 50.0,
                    sellingPrice = 120.0,
                    notes = "Low stock! High juicy yield",
                    lowStockThreshold = 10,
                    createdDate = now - 10 * day
                ),
                Plant(
                    plantName = "Rose - Kashmiri Red Velvet",
                    category = "Flowering Plants",
                    variety = "Hybrid Tea",
                    quantity = 60,
                    purchasePrice = 40.0,
                    sellingPrice = 90.0,
                    notes = "Fragrant long lasting blooms",
                    lowStockThreshold = 15,
                    createdDate = now - 20 * day
                ),
                Plant(
                    plantName = "Bougainvillea - Multi-Color",
                    category = "Flowering Plants",
                    variety = "Grafted Tricolor",
                    quantity = 18,
                    purchasePrice = 75.0,
                    sellingPrice = 160.0,
                    notes = "Loves full sun, drought tolerant",
                    lowStockThreshold = 8,
                    createdDate = now - 18 * day
                ),
                Plant(
                    plantName = "Monstera Deliciosa",
                    category = "Indoor & Ornamental",
                    variety = "Swiss Cheese",
                    quantity = 14,
                    purchasePrice = 200.0,
                    sellingPrice = 450.0,
                    notes = "Large split leaves, air purifying",
                    lowStockThreshold = 5,
                    createdDate = now - 8 * day
                ),
                Plant(
                    plantName = "Snake Plant - Sansevieria",
                    category = "Indoor & Ornamental",
                    variety = "Laurentii Gold Edge",
                    quantity = 4,
                    purchasePrice = 90.0,
                    sellingPrice = 220.0,
                    notes = "Low stock alert. Top indoor cleaner",
                    lowStockThreshold = 8,
                    createdDate = now - 7 * day
                ),
                Plant(
                    plantName = "Areca Palm",
                    category = "Indoor & Ornamental",
                    variety = "Clumping Palm",
                    quantity = 22,
                    purchasePrice = 150.0,
                    sellingPrice = 320.0,
                    notes = "3-4 feet height potted",
                    lowStockThreshold = 6,
                    createdDate = now - 6 * day
                ),
                Plant(
                    plantName = "Tulsi - Holy Basil",
                    category = "Medicinal & Herbal",
                    variety = "Krishna & Rama",
                    quantity = 35,
                    purchasePrice = 20.0,
                    sellingPrice = 50.0,
                    notes = "Traditional medicinal sacred herb",
                    lowStockThreshold = 10,
                    createdDate = now - 25 * day
                ),
                Plant(
                    plantName = "Aloe Vera - Barbadensis",
                    category = "Medicinal & Herbal",
                    variety = "Giant Leaf",
                    quantity = 30,
                    purchasePrice = 30.0,
                    sellingPrice = 70.0,
                    notes = "Skincare & organic fertilizer use",
                    lowStockThreshold = 10,
                    createdDate = now - 20 * day
                ),
                Plant(
                    plantName = "Ficus Bonsai",
                    category = "Bonsai & Succulents",
                    variety = "Tiger Bark Microcarpa",
                    quantity = 5,
                    purchasePrice = 600.0,
                    sellingPrice = 1400.0,
                    notes = "5-year-old shaped bonsai in ceramic pot",
                    lowStockThreshold = 5,
                    createdDate = now - 14 * day
                ),
                Plant(
                    plantName = "Mahogany Timber Sapling",
                    category = "Timber & Forestry",
                    variety = "African Swietenia",
                    quantity = 120,
                    purchasePrice = 25.0,
                    sellingPrice = 65.0,
                    notes = "Commercial high-grade wood sapling",
                    lowStockThreshold = 25,
                    createdDate = now - 30 * day
                )
            )

            database.plantDao().insertAll(samplePlants)

            val sampleCustomers = listOf(
                Customer(
                    name = "Rahim",
                    mobile = "+91 98765 12345",
                    address = "Plot 18, Lake Garden, Sector 1",
                    notes = "Exotic fruit sapling enthusiast",
                    createdDate = now - 25 * day
                ),
                Customer(
                    name = "Rafiqul Islam",
                    mobile = "+91 98765 43210",
                    address = "Green Valley Gardens, Sector 4",
                    notes = "Regular landscaping contractor, buys in bulk",
                    createdDate = now - 20 * day
                ),
                Customer(
                    name = "Ananya Mukherjee",
                    mobile = "+91 98301 22334",
                    address = "Apartment 4B, Orchid Heights",
                    notes = "Loves indoor plants and ceramic pots",
                    createdDate = now - 14 * day
                ),
                Customer(
                    name = "Tariqul Hasan",
                    mobile = "+91 97480 11998",
                    address = "Farmhouse Plot 12, River Road",
                    notes = "Fruit orchard grower",
                    createdDate = now - 10 * day
                ),
                Customer(
                    name = "Soma Chatterjee",
                    mobile = "+91 94330 87654",
                    address = "Bonsai Club House, Lake View",
                    notes = "Collects exotic succulents and flowering shrubs",
                    createdDate = now - 5 * day
                )
            )

            database.customerDao().insertAll(sampleCustomers)

            val samplePurchases = listOf(
                CustomerPurchase(
                    customerId = 1,
                    platform = PurchasePlatforms.WHATSAPP,
                    productName = "Miyazaki Mango",
                    quantity = 1,
                    purchasePrice = 999.0,
                    purchaseDate = now - 15 * day,
                    remarks = "Express delivery via WhatsApp order"
                ),
                CustomerPurchase(
                    customerId = 1,
                    platform = PurchasePlatforms.FACEBOOK,
                    productName = "Thai Longan",
                    quantity = 2,
                    purchasePrice = 1699.0,
                    purchaseDate = now - 10 * day,
                    remarks = "Inquired from Facebook page ad"
                ),
                CustomerPurchase(
                    customerId = 1,
                    platform = PurchasePlatforms.DIRECT_ORDER,
                    productName = "Guava Plant",
                    quantity = 1,
                    purchasePrice = 749.0,
                    purchaseDate = now - 5 * day,
                    remarks = "Farm visit direct pickup"
                ),
                CustomerPurchase(
                    customerId = 2,
                    platform = PurchasePlatforms.PHONE_CALL,
                    productName = "Mango - Amrapali Grafted",
                    quantity = 10,
                    purchasePrice = 240.0,
                    purchaseDate = now - 18 * day,
                    remarks = "Commercial orchard planting"
                ),
                CustomerPurchase(
                    customerId = 3,
                    platform = PurchasePlatforms.INSTAGRAM,
                    productName = "Monstera Deliciosa",
                    quantity = 2,
                    purchasePrice = 450.0,
                    purchaseDate = now - 8 * day,
                    remarks = "Instagram DM order"
                )
            )

            database.customerPurchaseDao().insertPurchases(samplePurchases)

            val sampleSales = listOf(
                Sale(
                    customerId = 1,
                    customerName = "Rafiqul Islam",
                    plantId = 1,
                    plantName = "Mango - Amrapali Grafted",
                    quantity = 10,
                    unitPrice = 250.0,
                    discount = 100.0,
                    amount = 2400.0,
                    paymentMethod = "UPI / GPay",
                    notes = "Bulk farm discount applied",
                    date = now - 2 * day
                ),
                Sale(
                    customerId = 2,
                    customerName = "Ananya Mukherjee",
                    plantId = 6,
                    plantName = "Monstera Deliciosa",
                    quantity = 2,
                    unitPrice = 450.0,
                    discount = 0.0,
                    amount = 900.0,
                    paymentMethod = "Cash",
                    notes = "Gift wrap requested",
                    date = now - 1 * day
                ),
                Sale(
                    customerId = 3,
                    customerName = "Tariqul Hasan",
                    plantId = 2,
                    plantName = "Guava - Thai Super Red",
                    quantity = 5,
                    unitPrice = 180.0,
                    discount = 50.0,
                    amount = 850.0,
                    paymentMethod = "UPI / GPay",
                    notes = "Delivered to farm gate",
                    date = now - 5 * 3600000L
                ),
                Sale(
                    customerId = 4,
                    customerName = "Soma Chatterjee",
                    plantId = 11,
                    plantName = "Ficus Bonsai",
                    quantity = 1,
                    unitPrice = 1400.0,
                    discount = 100.0,
                    amount = 1300.0,
                    paymentMethod = "Card",
                    notes = "Includes ceramic tray and care guide",
                    date = now - 2 * 3600000L
                )
            )

            database.saleDao().insertAll(sampleSales)

            val sampleExpenses = listOf(
                Expense(
                    category = "Fertilizer",
                    amount = 1200.0,
                    description = "Organic Vermicompost 50kg bags & Bone Meal",
                    paymentMethod = "UPI / GPay",
                    date = now - 4 * day
                ),
                Expense(
                    category = "Labour",
                    amount = 1600.0,
                    description = "Daily potting & weeding wages (4 workers)",
                    paymentMethod = "Cash",
                    date = now - 3 * day
                ),
                Expense(
                    category = "Packaging",
                    amount = 450.0,
                    description = "Nursery polybags, jute wraps and twine",
                    paymentMethod = "Cash",
                    date = now - 2 * day
                ),
                Expense(
                    category = "Courier",
                    amount = 350.0,
                    description = "Customer plant transport tempo fare",
                    paymentMethod = "Cash",
                    date = now - 1 * day
                ),
                Expense(
                    category = "Electricity & Water",
                    amount = 800.0,
                    description = "Drip irrigation motor pump electricity bill",
                    paymentMethod = "UPI / GPay",
                    date = now - 6 * 3600000L
                )
            )

            database.expenseDao().insertAll(sampleExpenses)

            val sampleLogs = listOf(
                StockLog(
                    plantId = 1,
                    plantName = "Mango - Amrapali Grafted",
                    type = StockLogType.STOCK_IN,
                    quantityChanged = 50,
                    remainingStock = 55,
                    reason = "Fresh stock received from wholesale nursery",
                    date = now - 10 * day
                ),
                StockLog(
                    plantId = 1,
                    plantName = "Mango - Amrapali Grafted",
                    type = StockLogType.SALE,
                    quantityChanged = -10,
                    remainingStock = 45,
                    reason = "Sale to Rafiqul Islam",
                    date = now - 2 * day
                ),
                StockLog(
                    plantId = 7,
                    plantName = "Snake Plant - Sansevieria",
                    type = StockLogType.DAMAGE,
                    quantityChanged = -2,
                    remainingStock = 4,
                    reason = "Heavy rain root rot damaged",
                    date = now - 3 * day
                )
            )

            database.stockLogDao().insertAll(sampleLogs)

            val sampleSearches = listOf(
                SearchHistory(query = "Mango", searchType = "PLANTS", timestamp = now - 2 * 3600000L),
                SearchHistory(query = "Bonsai", searchType = "GLOBAL", timestamp = now - 4 * 3600000L),
                SearchHistory(query = "Rafiqul", searchType = "CUSTOMERS", timestamp = now - 6 * 3600000L),
                SearchHistory(query = "Fertilizer", searchType = "EXPENSES", timestamp = now - 8 * 3600000L),
                SearchHistory(query = "Monstera", searchType = "GLOBAL", timestamp = now - 12 * 3600000L)
            )
            sampleSearches.forEach {
                database.searchHistoryDao().insertSearch(it)
            }
        }
    }
}
