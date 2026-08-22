package com.sahnurnursery.app.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "plants_nursery")
data class PlantEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val plantName: String,
    val category: String,
    val variety: String = "",
    val quantity: Int,
    val purchasePrice: Double,
    val sellingPrice: Double,
    val notes: String = "",
    val lowStockThreshold: Int = 5,
    val createdDate: Long = System.currentTimeMillis()
)
