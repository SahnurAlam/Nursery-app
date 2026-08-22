package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.SearchHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchHistoryDao {
    @Query("SELECT * FROM search_history ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentSearches(limit: Int = 20): Flow<List<SearchHistory>>

    @Query("SELECT * FROM search_history WHERE searchType = :type OR searchType = 'GLOBAL' ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentSearchesByType(type: String, limit: Int = 15): Flow<List<SearchHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearch(search: SearchHistory): Long

    @Query("DELETE FROM search_history WHERE id = :id")
    suspend fun deleteSearchById(id: Long)

    @Query("DELETE FROM search_history WHERE query = :query AND searchType = :type")
    suspend fun deleteSearchByQuery(query: String, type: String)

    @Query("DELETE FROM search_history WHERE searchType = :type")
    suspend fun clearHistoryByType(type: String)

    @Query("DELETE FROM search_history")
    suspend fun clearAllHistory()
}
