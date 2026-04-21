package com.pawel.memento.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "mementos",
    foreignKeys = [ForeignKey(entity = Category::class, parentColumns = ["id"], childColumns = ["categoryId"], onDelete = ForeignKey.SET_NULL)],
    indices = [Index("categoryId")]
)
data class Memento(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val dueDateTime: Long? = null,
    val categoryId: Long? = null,
    val priority: Priority = Priority.MEDIUM,
    val colorTagIndex: Int = 0,
    val reminderType: ReminderType = ReminderType.NOTIFICATION,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val repeatType: RepeatType = RepeatType.NONE,
    /** How many times per day this memento should be completed (1..5) */
    val dailyCount: Int = 1,
    /** Bitmask of completed occurrences for the current day (bit 0 = 1st, bit 1 = 2nd, …) */
    val completionMask: Int = 0,
    /** Start-of-day timestamp when completionMask was last written (used to detect day change) */
    val lastCompletedDate: Long = 0
)

enum class Priority { LOW, MEDIUM, HIGH, URGENT }
enum class ReminderType { NONE, NOTIFICATION, ALARM }
enum class RepeatType { NONE, DAILY, WEEKLY, MONTHLY }
