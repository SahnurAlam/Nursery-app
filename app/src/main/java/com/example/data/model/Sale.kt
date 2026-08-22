package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sales",
    indices = [
        Index("customerId"),
        Index("plantId"),
        Index("date")
    ]
)
data class Sale(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val customerId: Long = 0,
    val customerName: String = "Walk-in Customer",
    val plantId: Long = 0,
    val plantName: String = "",
    val quantity: Int = 1,
    val unitPrice: Double = 0.0,
    val discount: Double = 0.0,
    val amount: Double = 0.0,
    val paymentMethod: String = "Cash", // Cash, UPI, Card, Bank Transfer, Due
    val notes: String = "",
    val date: Long = System.currentTimeMillis()
)

object PaymentMethods {
    const val CASH = "Cash"
    val ALL = listOf(CASH, "UPI / GPay", "Card", "Bank Transfer", "Credit / Due")
}
