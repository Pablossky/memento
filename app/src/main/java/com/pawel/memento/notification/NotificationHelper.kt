package com.pawel.memento.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import com.pawel.memento.MainActivity
import com.pawel.memento.R

object NotificationHelper {
    const val CHANNEL_ID_REMINDERS = "memento_reminders"
    const val CHANNEL_ID_ALARMS    = "memento_alarms"

    fun createNotificationChannel(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(NotificationChannel(CHANNEL_ID_REMINDERS,
            context.getString(R.string.channel_reminders_name), NotificationManager.IMPORTANCE_HIGH).apply {
            description = context.getString(R.string.channel_reminders_desc); enableVibration(true)
        })
        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        manager.createNotificationChannel(NotificationChannel(CHANNEL_ID_ALARMS,
            context.getString(R.string.channel_alarms_name), NotificationManager.IMPORTANCE_HIGH).apply {
            description = context.getString(R.string.channel_alarms_desc)
            setSound(alarmSound, AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build())
            enableVibration(true)
        })
    }

    fun showReminderNotification(context: Context, id: Int, title: String, description: String,
                                  isAlarm: Boolean, soundEnabled: Boolean, vibrationEnabled: Boolean) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val pi = PendingIntent.getActivity(context, id,
            Intent(context, MainActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val channelId = if (isAlarm) CHANNEL_ID_ALARMS else CHANNEL_ID_REMINDERS
        val sound = if (soundEnabled) RingtoneManager.getDefaultUri(if (isAlarm) RingtoneManager.TYPE_ALARM else RingtoneManager.TYPE_NOTIFICATION) else null
        val notif = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(description.ifBlank { context.getString(R.string.reminder_tap_to_open) })
            .setStyle(NotificationCompat.BigTextStyle().bigText(description))
            .setContentIntent(pi).setAutoCancel(true).setPriority(NotificationCompat.PRIORITY_HIGH)
            .apply {
                if (sound != null) setSound(sound)
                if (vibrationEnabled) setVibrate(longArrayOf(0, 500, 200, 500))
                if (isAlarm) setCategory(NotificationCompat.CATEGORY_ALARM) else setCategory(NotificationCompat.CATEGORY_REMINDER)
            }.build()
        manager.notify(id, notif)
    }
}
