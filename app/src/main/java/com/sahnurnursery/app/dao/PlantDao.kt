package com.sahnurnursery.app.dao

import androidx.room.*
import com.sahnurnursery.app.entity.PlantEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlantDao {
    @Query("SELECT * FROM plants_nursery ORDER BY plantName ASC")
    fun getAllPlants(): Flow<List<PlantEntity>>

    @Query("SELECT * FROM plants_nursery WHERE id = :id")
    fun getPlantById(id: Long): Flow<PlantEntity?>

    @Query("SELECT * FROM plants_nursery WHERE quantity <= lowStockThreshold ORDER BY quantity ASC")
    fun getLowStockPlants(): Flow<List<PlantEntity>>

    @Query("SELECT COUNT(*) FROM plants_nursery")
    fun getPlantCount(): Flow<Int>

    @Query("SELECT SUM(quantity) FROM plants_nursery")
    fun getTotalStockQuantity(): Flow<Int?>

    @Query("SELECT * FROM plants_nursery WHERE plantName LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%'")
    fun searchPlants(query: String): Flow<List<PlantEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlant(plant: PlantEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(plants: List<PlantEntity>)

    @Update
    suspend fun updatePlant(plant: PlantEntity)

    @Delete
    suspend fun deletePlant(plant: PlantEntity)

    @Query("UPDATE plants_nursery SET quantity = quantity + :delta WHERE id = :plantId")
    suspend fun updateStock(plantId: Long, delta: Int)
}
