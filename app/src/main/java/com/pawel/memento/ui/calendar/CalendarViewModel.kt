package com.pawel.memento.ui.calendar

import android.app.Application
import androidx.lifecycle.*
import com.pawel.memento.MementoApp
import com.pawel.memento.data.model.Memento
import com.pawel.memento.data.model.MementoWithCategory
import com.pawel.memento.data.model.RepeatType
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

    val dayMementos: LiveData<List<MementoWithCategory>> =
        MediatorLiveData<List<MementoWithCategory>>().apply {
            fun update() {
                val dayTs = _selectedDay.value ?: run { value = emptyList(); return }
                val all   = allActiveMementos.value ?: return
                val sel   = Calendar.getInstance().apply { timeInMillis = dayTs }
                val y   = sel.get(Calendar.YEAR)
                val mon = sel.get(Calendar.MONTH)
                val d   = sel.get(Calendar.DAY_OF_MONTH)
                val dow = sel.get(Calendar.DAY_OF_WEEK)
                value = all.filter { mwc ->
                    val mem = mwc.memento
                    when {
                        mem.repeatType == RepeatType.DAILY -> true
                        mem.dueDateTime == null -> false
                        else -> {
                            val c = Calendar.getInstance().apply { timeInMillis = mem.dueDateTime }
                            when (mem.repeatType) {
                                RepeatType.NONE    -> c.get(Calendar.YEAR) == y &&
                                        c.get(Calendar.MONTH) == mon &&
                                        c.get(Calendar.DAY_OF_MONTH) == d
                                RepeatType.DAILY   -> true
                                RepeatType.WEEKLY  -> c.get(Calendar.DAY_OF_WEEK) == dow
                                RepeatType.MONTHLY -> c.get(Calendar.DAY_OF_MONTH) == d
                            }
                        }
                    }
                }
            }
            addSource(_selectedDay)      { update() }
            addSource(allActiveMementos) { update() }
        }

    fun prevMonth() {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR,  _currentYear.value  ?: return)
            set(Calendar.MONTH, _currentMonth.value ?: return)
            add(Calendar.MONTH, -1)
        }
        _currentYear.value  = cal.get(Calendar.YEAR)
        _currentMonth.value = cal.get(Calendar.MONTH)
        _selectedDay.value  = null
    }

    fun nextMonth() {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR,  _currentYear.value  ?: return)
            set(Calendar.MONTH, _currentMonth.value ?: return)
            add(Calendar.MONTH, 1)
        }
        _currentYear.value  = cal.get(Calendar.YEAR)
        _currentMonth.value = cal.get(Calendar.MONTH)
        _selectedDay.value  = null
    }

    fun selectDay(timestamp: Long) { _selectedDay.value = timestamp }
    fun toggleCompleted(m: Memento) = viewModelScope.launch { repo.setCompleted(m.id, !m.isCompleted) }
    fun toggleOccurrence(m: Memento, occurrenceIndex: Int, checked: Boolean) =
        viewModelScope.launch { repo.toggleOccurrence(m, occurrenceIndex, checked) }
    fun deleteMemento(m: Memento) = viewModelScope.launch { repo.deleteMemento(m) }
}
