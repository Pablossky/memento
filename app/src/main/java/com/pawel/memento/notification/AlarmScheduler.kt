package com.pawel.memento.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.pawel.memento.data.model.Memento
import com.pawel.memento.data.model.ReminderType

object AlarmScheduler {
    fun schedule(context: Context, memento: Memento) {
        val due = memento.dueDateTime ?: return
        if (memento.reminderType == ReminderType.NONE || due <= System.currentTimeMillis()) return
        scheduleByParams(context, memento.id, memento.title, due, memento.reminderType, memento.soundEnabled, memento.vibrationEnabled, memento.description)
    }

    fun scheduleByParams(context: Context, mementoId: Long, title: String, dueDateTime: Long?,
                          reminderType: ReminderType, soundEnabled: Boolean, vibrationEnabled: Boolean, description: String = "") {
        dueDateTime ?: return
        if (reminderType == ReminderType.NONE || dueDateTime <= System.currentTimeMillis()) return
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_MEMENTO_ID, mementoId.toInt())
            putExtra(AlarmReceiver.EXTRA_TITLE, title)
            putExtra(AlarmReceiver.EXTRA_DESCRIPTION, description)
            putExtra(AlarmReceiver.EXTRA_IS_ALARM, reminderType == ReminderType.ALARM)
            putExtra(AlarmReceiver.EXTRA_SOUND, soundEnabled)
            putExtra(AlarmReceiver.EXTRA_VIBRATION, vibrationEnabled)
        }
        val pi = PendingIntent.getBroadcast(context, mementoId.toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        if (am.canScheduleExactAlarms()) am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, dueDateTime, pi)
        else am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, dueDateTime, pi)
    }

    fun cancel(context: Context, mementoId: Int) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = PendingIntent.getBroadcast(context, mementoId, Intent(context, AlarmReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        am.cancel(pi)
    }
}
