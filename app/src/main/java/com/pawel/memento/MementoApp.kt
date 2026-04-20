package com.pawel.memento

import android.app.Application
import com.pawel.memento.data.db.MementoDatabase
import com.pawel.memento.data.repository.MementoRepository
import com.pawel.memento.notification.NotificationHelper

class MementoApp : Application() {
    val database by lazy { MementoDatabase.getDatabase(this) }
    val repository by lazy { MementoRepository(database.mementoDao(), database.categoryDao()) }
    override fun onCreate() { super.onCreate(); NotificationHelper.createNotificationChannel(this) }
}
