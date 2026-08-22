package com.example.data.model

data class NurseryBackup(
    val version: Int = 1,
    val backupDate: Long = System.currentTimeMillis(),
    val nurseryName: String = "Sahnur Nursery",
    val plants: List<Plant> = emptyList(),
    val customers: List<Customer> = emptyList(),
    val sales: List<Sale> = emptyList(),
    val expenses: List<Expense> = emptyList(),
    val stockLogs: List<StockLog> = emptyList()
)
