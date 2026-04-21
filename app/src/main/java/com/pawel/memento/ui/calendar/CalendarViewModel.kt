package com.pawel.memento.ui.calendar

import android.app.Application
import androidx.lifecycle.*
import com.pawel.memento.MementoApp
import com.pawel.memento.data.model.Memento
import com.pawel.memento.data.model.MementoWithCategory
import kotlinx.coroutines.launch
import java.util.*

class CalendarViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = (application as MementoApp).repository

    private val _currentYear  = MutableLiveData(Calendar.getInstance().get(Calendar.YEAR))
    private val _currentMonth = MutableLiveData(Calendar.getInstance().get(Calendar.MONTH))
    val currentYear:  LiveData<Int> = _currentYear
    val currentMonth: LiveData<Int> = _currentMonth

    private val _selectedDay = MutableLiveData<Long?>(null)
    val selectedDay: LiveData<Long?> = _selectedDay

    val allActiveMementos: LiveData<List<MementoWithCategory>> =
        repo.allActiveMementos.asLiveData()

    val dayMementos: LiveData<List<MementoWithCategory>> = MediatorLiveData<List<MementoWithCategory>>().apply {
        fun update() {
            val day = _selectedDay.value ?: run { value = emptyList(); return }
            val all = allActiveMementos.value ?: return
            val cal = Calendar.getInstance().apply { timeInMillis = day }
            val y = cal.get(Calendar.YEAR)
            val m = cal.get(Calendar.MONTH)
            val d = cal.get(Calendar.DAY_OF_MONTH)
            value = all.filter { mwc ->
                val dt = mwc.memento.dueDateTime ?: return@filter false
                val c = Calendar.getInstance().apply { timeInMillis = dt }
                c.get(Calendar.YEAR) == y && c.get(Calendar.MONTH) == m && c.get(Calendar.DAY_OF_MONTH) == d
            }
        }
        addSource(_selectedDay) { update() }
        addSource(allActiveMementos) { update() }
    }

    fun prevMonth() {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, _currentYear.value ?: return)
            set(Calendar.MONTH, _currentMonth.value ?: return)
            add(Calendar.MONTH, -1)
        }
        _currentYear.value  = cal.get(Calendar.YEAR)
        _currentMonth.value = cal.get(Calendar.MONTH)
        _selectedDay.value  = null
    }

    fun nextMonth() {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, _currentYear.value ?: return)
            set(Calendar.MONTH, _currentMonth.value ?: return)
            add(Calendar.MONTH, 1)
        }
        _currentYear.value  = cal.get(Calendar.YEAR)
        _currentMonth.value = cal.get(Calendar.MONTH)
        _selectedDay.value  = null
    }

    fun selectDay(timestamp: Long) { _selectedDay.value = timestamp }
    fun toggleCompleted(m: Memento) = viewModelScope.launch { repo.setCompleted(m.id, !m.isCompleted) }
    fun deleteMemento(m: Memento)   = viewModelScope.launch { repo.deleteMemento(m) }
}
