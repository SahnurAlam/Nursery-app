package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.Sale
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleDao {
    @Query("SELECT * FROM sales ORDER BY date DESC")
    fun getAllSales(): Flow<List<Sale>>

    @Query("SELECT * FROM sales WHERE id = :id")
    suspend fun getSaleById(id: Long): Sale?

    @Query("SELECT * FROM sales WHERE customerId = :customerId ORDER BY date DESC")
    fun getSalesByCustomer(customerId: Long): Flow<List<Sale>>

    @Query("SELECT * FROM sales WHERE plantId = :plantId ORDER BY date DESC")
    fun getSalesByPlant(plantId: Long): Flow<List<Sale>>

    @Query("SELECT * FROM sales WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC")
    fun getSalesByDateRange(startDate: Long, endDate: Long): Flow<List<Sale>>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM sales WHERE date >= :startDate AND date <= :endDate")
    fun getTotalSalesAmountByDateRange(startDate: Long, endDate: Long): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM sales")
    fun getTotalAllTimeSalesAmount(): Flow<Double>

    @Query("SELECT COALESCE(SUM(quantity), 0) FROM sales WHERE date >= :startDate AND date <= :endDate")
    fun getTotalSoldQuantityByDateRange(startDate: Long, endDate: Long): Flow<Int>

    @Query("SELECT * FROM sales WHERE customerName LIKE '%' || :query || '%' OR plantName LIKE '%' || :query || '%' OR paymentMethod LIKE '%' || :query || '%' ORDER BY date DESC")
    fun searchSales(query: String): Flow<List<Sale>>

    @Query("SELECT * FROM sales")
    suspend fun getAllSalesList(): List<Sale>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: Sale): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sales: List<Sale>)

    @Delete
    suspend fun deleteSale(sale: Sale)

    @Query("DELETE FROM sales WHERE id = :id")
    suspend fun deleteSaleById(id: Long)

    @Query("DELETE FROM sales")
    suspend fun clearAll()
}
