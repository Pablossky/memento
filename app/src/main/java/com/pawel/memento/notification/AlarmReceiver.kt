package com.pawel.memento.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id          = intent.getIntExtra(EXTRA_MEMENTO_ID, 0)
        val title       = intent.getStringExtra(EXTRA_TITLE) ?: return
        val description = intent.getStringExtra(EXTRA_DESCRIPTION) ?: ""
        val isAlarm     = intent.getBooleanExtra(EXTRA_IS_ALARM, false)
        val sound       = intent.getBooleanExtra(EXTRA_SOUND, true)
        val vibration   = intent.getBooleanExtra(EXTRA_VIBRATION, true)
        NotificationHelper.showReminderNotification(context, id, title, description, isAlarm, sound, vibration)
    }
    companion object {
        const val EXTRA_MEMENTO_ID  = "memento_id"
        const val EXTRA_TITLE       = "title"
        const val EXTRA_DESCRIPTION = "description"
        const val EXTRA_IS_ALARM    = "is_alarm"
        const val EXTRA_SOUND       = "sound_enabled"
        const val EXTRA_VIBRATION   = "vibration_enabled"
    }
}
