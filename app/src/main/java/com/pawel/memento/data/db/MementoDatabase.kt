package com.pawel.memento.data.db

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import com.pawel.memento.data.model.Category
import com.pawel.memento.data.model.Memento

@Database(entities = [Memento::class, Category::class], version = 2, exportSchema = false)
abstract class MementoDatabase : RoomDatabase() {
    abstract fun mementoDao(): MementoDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile private var INSTANCE: MementoDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE mementos ADD COLUMN dailyCount INTEGER NOT NULL DEFAULT 1")
                database.execSQL("ALTER TABLE mementos ADD COLUMN completionMask INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE mementos ADD COLUMN lastCompletedDate INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getDatabase(context: Context): MementoDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(context.applicationContext, MementoDatabase::class.java, "memento_database")
                    .addMigrations(MIGRATION_1_2)
                    .build().also { INSTANCE = it }
            }
    }
}
