package com.pawel.memento.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.pawel.memento.MementoApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED && intent.action != "android.intent.action.LOCKED_BOOT_COMPLETED") return
        CoroutineScope(Dispatchers.IO).launch {
            (context.applicationContext as MementoApp).repository.getPendingAlarms().forEach {
                AlarmScheduler.schedule(context, it)
            }
        }
    }
}
