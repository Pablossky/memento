package com.pawel.memento.data.db

import android.content.Context
import androidx.room.*
import com.pawel.memento.data.model.Category
import com.pawel.memento.data.model.Memento

@Database(entities = [Memento::class, Category::class], version = 1, exportSchema = false)
abstract class MementoDatabase : RoomDatabase() {
    abstract fun mementoDao(): MementoDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile private var INSTANCE: MementoDatabase? = null
        fun getDatabase(context: Context): MementoDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(context.applicationContext, MementoDatabase::class.java, "memento_database")
                    .build().also { INSTANCE = it }
            }
    }
}
