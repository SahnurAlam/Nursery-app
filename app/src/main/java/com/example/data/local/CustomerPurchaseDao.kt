package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CustomerPurchase
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerPurchaseDao {

    @Query("SELECT * FROM customer_purchases WHERE customerId = :customerId ORDER BY purchaseDate DESC, id DESC")
    fun getPurchasesByCustomerId(customerId: Long): Flow<List<CustomerPurchase>>

    @Query("SELECT * FROM customer_purchases WHERE customerId = :customerId ORDER BY purchaseDate DESC, id DESC")
    suspend fun getPurchasesByCustomerIdList(customerId: Long): List<CustomerPurchase>

    @Query("SELECT * FROM customer_purchases ORDER BY purchaseDate DESC, id DESC")
    fun getAllPurchases(): Flow<List<CustomerPurchase>>

    @Query("SELECT * FROM customer_purchases ORDER BY purchaseDate DESC, id DESC")
    suspend fun getAllPurchasesList(): List<CustomerPurchase>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchase(purchase: CustomerPurchase): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchases(purchases: List<CustomerPurchase>)

    @Update
    suspend fun updatePurchase(purchase: CustomerPurchase)

    @Delete
    suspend fun deletePurchase(purchase: CustomerPurchase)

    @Query("DELETE FROM customer_purchases WHERE id = :id")
    suspend fun deletePurchaseById(id: Long)

    @Query("DELETE FROM customer_purchases WHERE customerId = :customerId")
    suspend fun deletePurchasesByCustomerId(customerId: Long)

    @Query("DELETE FROM customer_purchases")
    suspend fun clearAll()
}
