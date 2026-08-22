package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Plant
import kotlinx.coroutines.flow.Flow

@Dao
interface PlantDao {
    @Query("SELECT * FROM plants ORDER BY plantName ASC")
    fun getAllPlants(): Flow<List<Plant>>

    @Query("SELECT * FROM plants WHERE id = :id")
    suspend fun getPlantById(id: Long): Plant?

    @Query("SELECT * FROM plants WHERE id = :id")
    fun getPlantFlowById(id: Long): Flow<Plant?>

    @Query("SELECT * FROM plants WHERE plantName LIKE '%' || :query || '%' OR variety LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%' ORDER BY plantName ASC")
    fun searchPlants(query: String): Flow<List<Plant>>

    @Query("SELECT * FROM plants WHERE category = :category ORDER BY plantName ASC")
    fun getPlantsByCategory(category: String): Flow<List<Plant>>

    @Query("SELECT * FROM plants WHERE quantity <= lowStockThreshold ORDER BY quantity ASC")
    fun getLowStockPlants(): Flow<List<Plant>>

    @Query("SELECT COUNT(*) FROM plants")
    fun getTotalPlantCount(): Flow<Int>

    @Query("SELECT COALESCE(SUM(quantity), 0) FROM plants")
    fun getTotalQuantityInStock(): Flow<Int>

    @Query("SELECT COALESCE(SUM(quantity * purchasePrice), 0.0) FROM plants")
    fun getTotalInventoryCost(): Flow<Double>

    @Query("SELECT COALESCE(SUM(quantity * sellingPrice), 0.0) FROM plants")
    fun getTotalInventoryRetailValue(): Flow<Double>

    @Query("SELECT * FROM plants")
    suspend fun getAllPlantsList(): List<Plant>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlant(plant: Plant): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(plants: List<Plant>)

    @Update
    suspend fun updatePlant(plant: Plant)

    @Query("UPDATE plants SET quantity = :newQuantity WHERE id = :id")
    suspend fun updatePlantQuantity(id: Long, newQuantity: Int)

    @Delete
    suspend fun deletePlant(plant: Plant)

    @Query("DELETE FROM plants WHERE id = :id")
    suspend fun deletePlantById(id: Long)

    @Query("DELETE FROM plants")
    suspend fun clearAll()
}
