package com.pawel.memento.data.db

import androidx.room.*
import com.pawel.memento.data.model.Memento
import com.pawel.memento.data.model.MementoWithCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface MementoDao {
    @Transaction
    @Query("SELECT * FROM mementos WHERE isCompleted = 0 ORDER BY dueDateTime ASC, createdAt DESC")
    fun getAllActiveMementos(): Flow<List<MementoWithCategory>>

    @Transaction
    @Query("SELECT * FROM mementos WHERE isCompleted = 1 ORDER BY createdAt DESC")
    fun getCompletedMementos(): Flow<List<MementoWithCategory>>

    @Transaction
    @Query("SELECT * FROM mementos WHERE categoryId = :categoryId AND isCompleted = 0 ORDER BY dueDateTime ASC")
    fun getMementosByCategory(categoryId: Long): Flow<List<MementoWithCategory>>

    @Transaction
    @Query("SELECT * FROM mementos WHERE isCompleted = 0 AND dueDateTime IS NOT NULL AND dueDateTime >= :startOfDay AND dueDateTime < :endOfDay ORDER BY dueDateTime ASC")
    fun getMementosForToday(startOfDay: Long, endOfDay: Long): Flow<List<MementoWithCategory>>

    @Transaction
    @Query("SELECT * FROM mementos WHERE isCompleted = 0 AND dueDateTime IS NOT NULL AND dueDateTime >= :from ORDER BY dueDateTime ASC")
    fun getUpcomingMementos(from: Long): Flow<List<MementoWithCategory>>

    @Query("SELECT * FROM mementos WHERE id = :id")
    suspend fun getMementoById(id: Long): Memento?

    @Transaction
    @Query("SELECT * FROM mementos WHERE id = :id")
    suspend fun getMementoWithCategoryById(id: Long): MementoWithCategory?

    @Query("SELECT * FROM mementos WHERE isCompleted = 0 AND dueDateTime IS NOT NULL AND dueDateTime > :now ORDER BY dueDateTime ASC")
    suspend fun getPendingAlarms(now: Long): List<Memento>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(memento: Memento): Long

    @Update
    suspend fun update(memento: Memento)

    @Delete
    suspend fun delete(memento: Memento)

    @Query("UPDATE mementos SET isCompleted = :completed WHERE id = :id")
    suspend fun setCompleted(id: Long, completed: Boolean)

    @Transaction
    @Query("SELECT * FROM mementos WHERE isCompleted = 0 AND (title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%') ORDER BY dueDateTime ASC")
    fun searchMementos(query: String): Flow<List<MementoWithCategory>>
}
