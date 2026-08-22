package com.sahnurnursery.app.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sales_nursery")
data class SalesEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val customerId: Long = 0,
    val customerName: String,
    val plantId: Long,
    val plantName: String,
    val quantity: Int,
    val unitPrice: Double,
    val discount: Double = 0.0,
    val amount: Double,
    val paymentMethod: String = "Cash",
    val notes: String = "",
    val date: Long = System.currentTimeMillis()
)
