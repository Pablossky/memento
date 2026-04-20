package com.pawel.memento.ui.editor

import android.app.Application
import androidx.lifecycle.*
import com.pawel.memento.MementoApp
import com.pawel.memento.data.model.*
import kotlinx.coroutines.launch

class MementoEditorViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = (application as MementoApp).repository
    val allCategories = repo.allCategories.asLiveData()
    private val _memento = MutableLiveData<Memento?>()
    val memento: LiveData<Memento?> = _memento
    private val _savedId = MutableLiveData<Long?>()
    val savedId: LiveData<Long?> = _savedId

    fun loadMemento(id: Long) = viewModelScope.launch { _memento.value = repo.getMementoById(id) }

    fun saveMemento(id: Long, title: String, description: String, dueDateTime: Long?, categoryId: Long?,
                    priority: Priority, colorTagIndex: Int, reminderType: ReminderType,
                    soundEnabled: Boolean, vibrationEnabled: Boolean, repeatType: RepeatType) = viewModelScope.launch {
        val existing = _memento.value
        val m = Memento(id = if (id == 0L) 0L else id, title = title, description = description,
            dueDateTime = dueDateTime, categoryId = categoryId, priority = priority,
            colorTagIndex = colorTagIndex, reminderType = reminderType, soundEnabled = soundEnabled,
            vibrationEnabled = vibrationEnabled, isCompleted = existing?.isCompleted ?: false,
            createdAt = existing?.createdAt ?: System.currentTimeMillis(), repeatType = repeatType)
        _savedId.value = if (id == 0L) repo.insertMemento(m) else { repo.updateMemento(m); id }
    }
}
