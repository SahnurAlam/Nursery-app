package com.sahnurnursery.app.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stock_logs_nursery")
data class StockEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val plantId: Long,
    val plantName: String,
    val type: String, // "STOCK_IN", "SALE", "DAMAGE", "ADJUSTMENT"
    val quantityChanged: Int,
    val remainingStock: Int,
    val reason: String = "",
    val date: Long = System.currentTimeMillis()
)
