package com.mudita.habits.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CompletionDao {

    /** All completions for a specific date (used by the day view). */
    @Query("SELECT * FROM completions WHERE date = :date")
    fun observeForDate(date: String): Flow<List<CompletionEntity>>

    /** All completions, grouped by task, for statistics. */
    @Query("SELECT * FROM completions")
    fun observeAll(): Flow<List<CompletionEntity>>

    @Query("SELECT * FROM completions")
    suspend fun getAll(): List<CompletionEntity>

    @Query("SELECT * FROM completions WHERE taskId = :taskId AND date = :date LIMIT 1")
    suspend fun get(taskId: Long, date: String): CompletionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(completion: CompletionEntity)

    @Query("DELETE FROM completions WHERE taskId = :taskId AND date = :date")
    suspend fun delete(taskId: Long, date: String)

    @Query("DELETE FROM completions")
    suspend fun deleteAll()
}
