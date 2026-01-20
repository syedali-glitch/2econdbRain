package com.secondbrain.lite.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ThoughtDao {
    
    @Query("SELECT * FROM thoughts ORDER BY isPinned DESC, date DESC")
    fun getAllThoughts(): Flow<List<Thought>>
    
    @Query("SELECT * FROM thoughts WHERE id = :id")
    suspend fun getThoughtById(id: Long): Thought?
    
    @Query("SELECT * FROM thoughts WHERE text LIKE '%' || :query || '%' OR title LIKE '%' || :query || '%' ORDER BY isPinned DESC, date DESC")
    fun searchThoughts(query: String): Flow<List<Thought>>
    
    @Query("SELECT * FROM thoughts WHERE category = :category ORDER BY isPinned DESC, date DESC")
    fun getThoughtsByCategory(category: String): Flow<List<Thought>>
    
    @Query("SELECT COUNT(*) FROM thoughts WHERE isPinned = 1")
    suspend fun getPinnedCount(): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(thought: Thought): Long
    
    @Update
    suspend fun update(thought: Thought)
    
    @Delete
    suspend fun delete(thought: Thought)
    
    @Query("DELETE FROM thoughts WHERE id = :id")
    suspend fun deleteById(id: Long)
}
