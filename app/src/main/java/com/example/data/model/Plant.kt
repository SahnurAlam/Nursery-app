package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "plants")
data class Plant(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val plantName: String,
    val category: String, // Fruit Plants, Flowering Plants, Indoor & Ornamental, Medicinal, Bonsai & Succulents, Timber, Vegetable, Other
    val variety: String = "",
    val quantity: Int = 0,
    val purchasePrice: Double = 0.0,
    val sellingPrice: Double = 0.0,
    val imagePath: String = "",
    val notes: String = "",
    val lowStockThreshold: Int = 10,
    val createdDate: Long = System.currentTimeMillis()
) {
    val isLowStock: Boolean
        get() = quantity <= lowStockThreshold && quantity > 0

    val isOutOfStock: Boolean
        get() = quantity <= 0

    val profitMargin: Double
        get() = if (purchasePrice > 0) ((sellingPrice - purchasePrice) / purchasePrice) * 100 else 0.0
}

object PlantCategories {
    val ALL = listOf(
        "Fruit Plants",
        "Flowering Plants",
        "Indoor & Ornamental",
        "Medicinal & Herbal",
        "Bonsai & Succulents",
        "Timber & Forestry",
        "Vegetable & Seeds",
        "Palms & Cycads",
        "Other"
    )
}
