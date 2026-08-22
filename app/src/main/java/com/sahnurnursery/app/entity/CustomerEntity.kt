package com.sahnurnursery.app.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers_nursery")
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val mobile: String,
    val address: String = "",
    val notes: String = "",
    val createdDate: Long = System.currentTimeMillis()
)
