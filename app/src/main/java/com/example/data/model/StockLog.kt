package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "stock_logs",
    indices = [
        Index("plantId"),
        Index("date")
    ]
)
data class StockLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val plantId: Long,
    val plantName: String,
    val type: String, // STOCK_IN, STOCK_OUT, SALE, ADJUSTMENT, DAMAGE
    val quantityChanged: Int,
    val remainingStock: Int,
    val reason: String = "",
    val date: Long = System.currentTimeMillis()
)

object StockLogType {
    const val STOCK_IN = "STOCK_IN"
    const val STOCK_OUT = "STOCK_OUT"
    const val SALE = "SALE"
    const val ADJUSTMENT = "ADJUSTMENT"
    const val DAMAGE = "DAMAGE"
}
