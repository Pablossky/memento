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
    val repeatType: RepeatType = RepeatType.NONE
)

enum class Priority { LOW, MEDIUM, HIGH, URGENT }
enum class ReminderType { NONE, NOTIFICATION, ALARM }
enum class RepeatType { NONE, DAILY, WEEKLY, MONTHLY }
