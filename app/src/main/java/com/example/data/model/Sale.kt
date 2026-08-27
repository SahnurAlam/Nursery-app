package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import org.json.JSONArray

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
    val discountPercent: Double = 0.0,
    val discount: Double = 0.0,
    val amount: Double = 0.0,
    val paymentMethod: String = "Cash", // Cash, UPI / GPay, Card, Bank Transfer, Credit / Due
    val notes: String = "",
    val date: Long = System.currentTimeMillis(),
    val itemsJson: String = ""
) {
    fun getSaleItems(): List<SaleItem> {
        if (itemsJson.isNotBlank()) {
            try {
                val array = JSONArray(itemsJson)
                val list = mutableListOf<SaleItem>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(SaleItem.fromJson(obj))
                }
                if (list.isNotEmpty()) return list
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        // Fallback for legacy single-item sales
        if (plantName.isNotBlank() || plantId > 0 || quantity > 0) {
            val calcLineTotal = if (amount > 0) amount else (quantity * unitPrice - discount).coerceAtLeast(0.0)
            val calcDiscountPercent = if (discountPercent > 0) discountPercent else if (quantity * unitPrice > 0 && discount > 0) (discount / (quantity * unitPrice)) * 100.0 else 0.0
            return listOf(
                SaleItem(
                    plantId = plantId,
                    plantName = plantName.ifBlank { "Plant Item" },
                    quantity = quantity.coerceAtLeast(1),
                    unitPrice = unitPrice,
                    discountPercent = calcDiscountPercent,
                    discount = discount,
                    lineTotal = calcLineTotal
                )
            )
        }
        return emptyList()
    }

    fun getItemsSummary(): String {
        val items = getSaleItems()
        return when {
            items.isEmpty() -> plantName.ifBlank { "Plant Sale" }
            items.size == 1 -> items[0].plantName
            items.size == 2 -> "${items[0].plantName}, ${items[1].plantName}"
            items.size == 3 -> "${items[0].plantName}, ${items[1].plantName}, ${items[2].plantName}"
            else -> "${items[0].plantName}, ${items[1].plantName} + ${items.size - 2} more"
        }
    }
}

object PaymentMethods {
    const val CASH = "Cash"
    val ALL = listOf(CASH, "UPI / GPay", "Card", "Bank Transfer", "Credit / Due")
}
