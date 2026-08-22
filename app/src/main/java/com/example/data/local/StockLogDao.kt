package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.StockLog
import kotlinx.coroutines.flow.Flow

@Dao
interface StockLogDao {
    @Query("SELECT * FROM stock_logs ORDER BY date DESC")
    fun getAllLogs(): Flow<List<StockLog>>

    @Query("SELECT * FROM stock_logs WHERE plantId = :plantId ORDER BY date DESC")
    fun getLogsByPlant(plantId: Long): Flow<List<StockLog>>

    @Query("SELECT * FROM stock_logs WHERE type = :type ORDER BY date DESC")
    fun getLogsByType(type: String): Flow<List<StockLog>>

    @Query("SELECT * FROM stock_logs")
    suspend fun getAllStockLogsList(): List<StockLog>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: StockLog): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(logs: List<StockLog>)

    @Delete
    suspend fun deleteLog(log: StockLog)

    @Query("DELETE FROM stock_logs WHERE id = :id")
    suspend fun deleteLogById(id: Long)

    @Query("DELETE FROM stock_logs")
    suspend fun clearAll()
}
