package com.sahnurnursery.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sahnurnursery.app.database.AppDatabase
import com.sahnurnursery.app.entity.*
import com.sahnurnursery.app.model.DatabaseResult
import com.sahnurnursery.app.model.UiFeedback
import com.sahnurnursery.app.model.UiState
import com.sahnurnursery.app.repository.NurseryRepository
import kotlinx.coroutines.flow.*
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

    private val _operationState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val operationState: StateFlow<UiState<Unit>> = _operationState.asStateFlow()

    private val _uiFeedback = MutableStateFlow<UiFeedback?>(null)
    val uiFeedback: StateFlow<UiFeedback?> = _uiFeedback.asStateFlow()

    val plants: StateFlow<List<PlantEntity>> = repository.allPlants
        .catch { e ->
            emitFeedbackError("Failed to load plants from database: ${e.localizedMessage ?: "Unknown error"}")
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lowStockPlants: StateFlow<List<PlantEntity>> = repository.lowStockPlants
        .catch { e ->
            emitFeedbackError("Failed to load low-stock alerts: ${e.localizedMessage ?: "Unknown error"}")
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customers: StateFlow<List<CustomerEntity>> = repository.allCustomers
        .catch { e ->
            emitFeedbackError("Failed to load customers: ${e.localizedMessage ?: "Unknown error"}")
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sales: StateFlow<List<SalesEntity>> = repository.allSales
        .catch { e ->
            emitFeedbackError("Failed to load sales records: ${e.localizedMessage ?: "Unknown error"}")
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenses: StateFlow<List<ExpenseEntity>> = repository.allExpenses
        .catch { e ->
            emitFeedbackError("Failed to load expenses: ${e.localizedMessage ?: "Unknown error"}")
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stockLogs: StateFlow<List<StockEntity>> = repository.allStockLogs
        .catch { e ->
            emitFeedbackError("Failed to load stock logs: ${e.localizedMessage ?: "Unknown error"}")
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun <T> executeDbOperation(
        operationName: String,
        action: suspend () -> DatabaseResult<T>,
        retryBlock: () -> Unit
    ) {
        viewModelScope.launch {
            _operationState.value = UiState.Loading
            try {
                when (val result = action()) {
                    is DatabaseResult.Success -> {
                        _operationState.value = UiState.Success(Unit)
                        result.message?.let { msg ->
                            _uiFeedback.value = UiFeedback.Success(message = msg)
                        }
                    }
                    is DatabaseResult.Error -> {
                        _operationState.value = UiState.Error(
                            message = result.userMessage,
                            cause = result.throwable,
                            actionLabel = "Retry",
                            onRetry = retryBlock
                        )
                        _uiFeedback.value = UiFeedback.Error(
                            message = result.userMessage,
                            cause = result.throwable,
                            actionLabel = "Retry",
                            retryAction = retryBlock
                        )
                    }
                }
            } catch (t: Throwable) {
                val errorMsg = "Unexpected error during $operationName: ${t.localizedMessage ?: "Database error"}"
                _operationState.value = UiState.Error(
                    message = errorMsg,
                    cause = t,
                    actionLabel = "Retry",
                    onRetry = retryBlock
                )
                _uiFeedback.value = UiFeedback.Error(
                    message = errorMsg,
                    cause = t,
                    actionLabel = "Retry",
                    retryAction = retryBlock
                )
            }
        }
    }

    fun addPlant(plant: PlantEntity) {
        executeDbOperation(
            operationName = "Add Plant",
            action = { repository.insertPlant(plant) },
            retryBlock = { addPlant(plant) }
        )
    }

    fun updatePlant(plant: PlantEntity) {
        executeDbOperation(
            operationName = "Update Plant",
            action = { repository.updatePlant(plant) },
            retryBlock = { updatePlant(plant) }
        )
    }

    fun deletePlant(plant: PlantEntity) {
        executeDbOperation(
            operationName = "Delete Plant",
            action = { repository.deletePlant(plant) },
            retryBlock = { deletePlant(plant) }
        )
    }

    fun addCustomer(customer: CustomerEntity) {
        executeDbOperation(
            operationName = "Add Customer",
            action = { repository.insertCustomer(customer) },
            retryBlock = { addCustomer(customer) }
        )
    }

    fun recordSale(sale: SalesEntity) {
        executeDbOperation(
            operationName = "Record Sale",
            action = { repository.recordSale(sale) },
            retryBlock = { recordSale(sale) }
        )
    }

    fun addExpense(expense: ExpenseEntity) {
        executeDbOperation(
            operationName = "Add Expense",
            action = { repository.insertExpense(expense) },
            retryBlock = { addExpense(expense) }
        )
    }

    fun deleteExpense(expense: ExpenseEntity) {
        executeDbOperation(
            operationName = "Delete Expense",
            action = { repository.deleteExpense(expense) },
            retryBlock = { deleteExpense(expense) }
        )
    }

    fun clearFeedback() {
        _uiFeedback.value = null
    }

    fun emitFeedbackError(message: String, retry: (() -> Unit)? = null) {
        _uiFeedback.value = UiFeedback.Error(
            message = message,
            actionLabel = if (retry != null) "Retry" else null,
            retryAction = retry
        )
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
