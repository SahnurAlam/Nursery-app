package com.sahnurnursery.app.dao

import androidx.room.*
import com.sahnurnursery.app.entity.SalesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SalesDao {
    @Query("SELECT * FROM sales_nursery ORDER BY date DESC")
    fun getAllSales(): Flow<List<SalesEntity>>

    @Query("SELECT * FROM sales_nursery WHERE id = :id")
    fun getSaleById(id: Long): Flow<SalesEntity?>

    @Query("SELECT * FROM sales_nursery WHERE customerId = :customerId ORDER BY date DESC")
    fun getSalesByCustomer(customerId: Long): Flow<List<SalesEntity>>

    @Query("SELECT SUM(amount) FROM sales_nursery")
    fun getTotalRevenue(): Flow<Double?>

    @Query("SELECT SUM(quantity) FROM sales_nursery")
    fun getTotalPlantsSold(): Flow<Int?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: SalesEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sales: List<SalesEntity>)

    @Delete
    suspend fun deleteSale(sale: SalesEntity)
}
