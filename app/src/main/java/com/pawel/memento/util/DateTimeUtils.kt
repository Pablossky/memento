package com.pawel.memento.util

import java.text.SimpleDateFormat
import java.util.*

object DateTimeUtils {
    private val dateTimeFormat = SimpleDateFormat("d MMM yyyy, HH:mm", Locale.getDefault())
    fun formatDateTime(millis: Long): String = dateTimeFormat.format(Date(millis))
    fun isOverdue(dueDateTime: Long?): Boolean = dueDateTime != null && dueDateTime < System.currentTimeMillis()
    fun isToday(dueDateTime: Long?): Boolean {
        dueDateTime ?: return false
        val a = Calendar.getInstance().apply { timeInMillis = dueDateTime }
        val b = Calendar.getInstance()
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR) && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)
    }
}
