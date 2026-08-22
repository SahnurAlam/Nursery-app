package com.sahnurnursery.app.repository

import com.sahnurnursery.app.dao.*
import com.sahnurnursery.app.entity.*
import kotlinx.coroutines.flow.Flow

class NurseryRepository(
    private val plantDao: PlantDao,
    private val customerDao: CustomerDao,
    private val salesDao: SalesDao,
    private val expenseDao: ExpenseDao,
    private val stockDao: StockDao
) {
    // Plants
    val allPlants: Flow<List<PlantEntity>> = plantDao.getAllPlants()
    val lowStockPlants: Flow<List<PlantEntity>> = plantDao.getLowStockPlants()
    val totalStock: Flow<Int?> = plantDao.getTotalStockQuantity()

    suspend fun insertPlant(plant: PlantEntity) = plantDao.insertPlant(plant)
    suspend fun updatePlant(plant: PlantEntity) = plantDao.updatePlant(plant)
    suspend fun deletePlant(plant: PlantEntity) = plantDao.deletePlant(plant)

    // Customers
    val allCustomers: Flow<List<CustomerEntity>> = customerDao.getAllCustomers()
    suspend fun insertCustomer(customer: CustomerEntity) = customerDao.insertCustomer(customer)
    suspend fun updateCustomer(customer: CustomerEntity) = customerDao.updateCustomer(customer)
    suspend fun deleteCustomer(customer: CustomerEntity) = customerDao.deleteCustomer(customer)

    // Sales
    val allSales: Flow<List<SalesEntity>> = salesDao.getAllSales()
    val totalRevenue: Flow<Double?> = salesDao.getTotalRevenue()
    suspend fun recordSale(sale: SalesEntity) {
        salesDao.insertSale(sale)
        plantDao.updateStock(sale.plantId, -sale.quantity)
        stockDao.insertLog(
            StockEntity(
                plantId = sale.plantId,
                plantName = sale.plantName,
                type = "SALE",
                quantityChanged = -sale.quantity,
                remainingStock = 0,
                reason = "Sale to ${sale.customerName}"
            )
        )
    }

    // Expenses
    val allExpenses: Flow<List<ExpenseEntity>> = expenseDao.getAllExpenses()
    val totalExpenses: Flow<Double?> = expenseDao.getTotalExpenseAmount()
    suspend fun insertExpense(expense: ExpenseEntity) = expenseDao.insertExpense(expense)
    suspend fun deleteExpense(expense: ExpenseEntity) = expenseDao.deleteExpense(expense)

    // Stock Logs
    val allStockLogs: Flow<List<StockEntity>> = stockDao.getAllStockLogs()
}
