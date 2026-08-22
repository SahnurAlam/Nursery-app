package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "expenses",
    indices = [
        Index("category"),
        Index("date")
    ]
)
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val category: String, // Labour, Courier, Packaging, Fertilizer, Plant Purchase, Marketing, Miscellaneous
    val amount: Double = 0.0,
    val description: String = "",
    val paymentMethod: String = "Cash",
    val date: Long = System.currentTimeMillis()
)

object ExpenseCategories {
    val ALL = listOf(
        "Labour",
        "Courier",
        "Packaging",
        "Fertilizer",
        "Plant Purchase",
        "Pots & Soil",
        "Marketing",
        "Electricity & Water",
        "Miscellaneous"
    )
}
