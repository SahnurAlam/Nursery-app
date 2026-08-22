package com.sahnurnursery.app.dao

import androidx.room.*
import com.sahnurnursery.app.entity.StockEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StockDao {
    @Query("SELECT * FROM stock_logs_nursery ORDER BY date DESC")
    fun getAllStockLogs(): Flow<List<StockEntity>>

    @Query("SELECT * FROM stock_logs_nursery WHERE plantId = :plantId ORDER BY date DESC")
    fun getLogsByPlant(plantId: Long): Flow<List<StockEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: StockEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(logs: List<StockEntity>)
}
