package com.pawel.memento.data.repository

import com.pawel.memento.data.db.CategoryDao
import com.pawel.memento.data.db.MementoDao
import com.pawel.memento.data.model.*
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class MementoRepository(private val mementoDao: MementoDao, private val categoryDao: CategoryDao) {
    val allActiveMementos: Flow<List<MementoWithCategory>> = mementoDao.getAllActiveMementos()
    val completedMementos: Flow<List<MementoWithCategory>> = mementoDao.getCompletedMementos()
    val allCategories: Flow<List<Category>> = categoryDao.getAllCategories()

    fun getMementosByCategory(categoryId: Long) = mementoDao.getMementosByCategory(categoryId)

    fun getMementosForToday(): Flow<List<MementoWithCategory>> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59); cal.set(Calendar.SECOND, 59)
        return mementoDao.getMementosForToday(start, cal.timeInMillis)
    }

    fun getUpcomingMementos() = mementoDao.getUpcomingMementos(System.currentTimeMillis())
    fun searchMementos(query: String) = mementoDao.searchMementos(query)
    suspend fun getMementoById(id: Long) = mementoDao.getMementoById(id)
    suspend fun getMementoWithCategoryById(id: Long) = mementoDao.getMementoWithCategoryById(id)
    suspend fun insertMemento(memento: Memento): Long = mementoDao.insert(memento)
    suspend fun updateMemento(memento: Memento) = mementoDao.update(memento)
    suspend fun deleteMemento(memento: Memento) = mementoDao.delete(memento)
    suspend fun setCompleted(id: Long, completed: Boolean) = mementoDao.setCompleted(id, completed)
    suspend fun getPendingAlarms() = mementoDao.getPendingAlarms(System.currentTimeMillis())
    suspend fun insertCategory(category: Category): Long = categoryDao.insert(category)
    suspend fun updateCategory(category: Category) = categoryDao.update(category)
    suspend fun deleteCategory(category: Category) = categoryDao.delete(category)
    suspend fun getCategoryById(id: Long) = categoryDao.getCategoryById(id)
    suspend fun getMementoCountForCategory(categoryId: Long) = categoryDao.getMementoCountForCategory(categoryId)
}
