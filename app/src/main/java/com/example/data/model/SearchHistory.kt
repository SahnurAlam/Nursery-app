package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "search_history")
data class SearchHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val query: String,
    val searchType: String = "GLOBAL", // "GLOBAL", "PLANTS", "SALES", "CUSTOMERS", "EXPENSES"
    val timestamp: Long = System.currentTimeMillis()
)
