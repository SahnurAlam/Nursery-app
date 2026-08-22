package com.sahnurnursery.app.repository

import com.sahnurnursery.app.dao.*
import com.sahnurnursery.app.entity.*
import com.sahnurnursery.app.model.DatabaseResult
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

    suspend fun insertPlant(plant: PlantEntity): DatabaseResult<Long> = try {
        val id = plantDao.insertPlant(plant)
        DatabaseResult.Success(id, "Plant '${plant.plantName}' saved successfully.")
    } catch (e: Exception) {
        DatabaseResult.Error("Failed to save plant '${plant.plantName}': ${e.localizedMessage ?: "Database write error"}", e)
    }

    suspend fun updatePlant(plant: PlantEntity): DatabaseResult<Unit> = try {
        plantDao.updatePlant(plant)
        DatabaseResult.Success(Unit, "Plant '${plant.plantName}' updated successfully.")
    } catch (e: Exception) {
        DatabaseResult.Error("Failed to update plant '${plant.plantName}': ${e.localizedMessage ?: "Database update error"}", e)
    }

    suspend fun deletePlant(plant: PlantEntity): DatabaseResult<Unit> = try {
        plantDao.deletePlant(plant)
        DatabaseResult.Success(Unit, "Plant '${plant.plantName}' removed.")
    } catch (e: Exception) {
        DatabaseResult.Error("Failed to delete plant '${plant.plantName}': ${e.localizedMessage ?: "Database deletion error"}", e)
    }

    // Customers
    val allCustomers: Flow<List<CustomerEntity>> = customerDao.getAllCustomers()

    suspend fun insertCustomer(customer: CustomerEntity): DatabaseResult<Long> = try {
        val id = customerDao.insertCustomer(customer)
        DatabaseResult.Success(id, "Customer '${customer.name}' added successfully.")
    } catch (e: Exception) {
        DatabaseResult.Error("Failed to add customer '${customer.name}': ${e.localizedMessage ?: "Database error"}", e)
    }

    suspend fun updateCustomer(customer: CustomerEntity): DatabaseResult<Unit> = try {
        customerDao.updateCustomer(customer)
        DatabaseResult.Success(Unit, "Customer '${customer.name}' updated.")
    } catch (e: Exception) {
        DatabaseResult.Error("Failed to update customer '${customer.name}': ${e.localizedMessage ?: "Database error"}", e)
    }

    suspend fun deleteCustomer(customer: CustomerEntity): DatabaseResult<Unit> = try {
        customerDao.deleteCustomer(customer)
        DatabaseResult.Success(Unit, "Customer '${customer.name}' removed.")
    } catch (e: Exception) {
        DatabaseResult.Error("Failed to delete customer '${customer.name}': ${e.localizedMessage ?: "Database error"}", e)
    }

    // Sales
    val allSales: Flow<List<SalesEntity>> = salesDao.getAllSales()
    val totalRevenue: Flow<Double?> = salesDao.getTotalRevenue()

    suspend fun recordSale(sale: SalesEntity): DatabaseResult<Long> = try {
        val saleId = salesDao.insertSale(sale)
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
        DatabaseResult.Success(saleId, "Sale of ${sale.quantity}x ${sale.plantName} recorded successfully.")
    } catch (e: Exception) {
        DatabaseResult.Error("Failed to record sale for '${sale.plantName}': ${e.localizedMessage ?: "Database transaction error"}", e)
    }

    // Expenses
    val allExpenses: Flow<List<ExpenseEntity>> = expenseDao.getAllExpenses()
    val totalExpenses: Flow<Double?> = expenseDao.getTotalExpenseAmount()

    suspend fun insertExpense(expense: ExpenseEntity): DatabaseResult<Long> = try {
        val id = expenseDao.insertExpense(expense)
        DatabaseResult.Success(id, "Expense '${expense.category}' (₹${expense.amount}) saved.")
    } catch (e: Exception) {
        DatabaseResult.Error("Failed to save expense: ${e.localizedMessage ?: "Database error"}", e)
    }

    suspend fun deleteExpense(expense: ExpenseEntity): DatabaseResult<Unit> = try {
        expenseDao.deleteExpense(expense)
        DatabaseResult.Success(Unit, "Expense deleted.")
    } catch (e: Exception) {
        DatabaseResult.Error("Failed to delete expense: ${e.localizedMessage ?: "Database error"}", e)
    }

    // Stock Logs
    val allStockLogs: Flow<List<StockEntity>> = stockDao.getAllStockLogs()
}
