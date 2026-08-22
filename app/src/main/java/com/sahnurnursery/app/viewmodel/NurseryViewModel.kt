package com.sahnurnursery.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sahnurnursery.app.database.AppDatabase
import com.sahnurnursery.app.entity.*
import com.sahnurnursery.app.repository.NurseryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NurseryViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getInstance(application)
    private val repository = NurseryRepository(
        database.plantDao(),
        database.customerDao(),
        database.salesDao(),
        database.expenseDao(),
        database.stockDao()
    )

    val plants: StateFlow<List<PlantEntity>> = repository.allPlants
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lowStockPlants: StateFlow<List<PlantEntity>> = repository.lowStockPlants
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customers: StateFlow<List<CustomerEntity>> = repository.allCustomers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sales: StateFlow<List<SalesEntity>> = repository.allSales
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenses: StateFlow<List<ExpenseEntity>> = repository.allExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stockLogs: StateFlow<List<StockEntity>> = repository.allStockLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addPlant(plant: PlantEntity) = viewModelScope.launch {
        repository.insertPlant(plant)
    }

    fun updatePlant(plant: PlantEntity) = viewModelScope.launch {
        repository.updatePlant(plant)
    }

    fun deletePlant(plant: PlantEntity) = viewModelScope.launch {
        repository.deletePlant(plant)
    }

    fun addCustomer(customer: CustomerEntity) = viewModelScope.launch {
        repository.insertCustomer(customer)
    }

    fun recordSale(sale: SalesEntity) = viewModelScope.launch {
        repository.recordSale(sale)
    }

    fun addExpense(expense: ExpenseEntity) = viewModelScope.launch {
        repository.insertExpense(expense)
    }
}

class NurseryViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NurseryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NurseryViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
