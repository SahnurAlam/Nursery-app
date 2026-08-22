package com.sahnurnursery.app.model

data class NurserySummary(
    val totalPlants: Int = 0,
    val totalSpecies: Int = 0,
    val lowStockCount: Int = 0,
    val totalRevenue: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val netProfit: Double = 0.0
)
