package com.sahnurnursery.app.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses_nursery")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val category: String,
    val amount: Double,
    val description: String = "",
    val paymentMethod: String = "Cash",
    val date: Long = System.currentTimeMillis()
)
