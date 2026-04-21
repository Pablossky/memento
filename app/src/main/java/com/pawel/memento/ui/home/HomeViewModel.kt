package com.pawel.memento.ui.home

import android.app.Application
import androidx.lifecycle.*
import com.pawel.memento.MementoApp
import com.pawel.memento.data.model.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class FilterTab { ALL, TODAY, UPCOMING, COMPLETED }

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = (application as MementoApp).repository
    val allCategories = repo.allCategories.asLiveData()

    private val _tab   = MutableStateFlow(FilterTab.ALL)
    private val _cat   = MutableStateFlow<Long?>(null)
    private val _sort  = MutableStateFlow(SortOrder.BY_DATE_ASC)
    private val _query = MutableStateFlow("")

    val filterTab:        StateFlow<FilterTab> = _tab
    val selectedCategory: StateFlow<Long?>     = _cat
    val sortOrder:        StateFlow<SortOrder> = _sort

    val mementos: LiveData<List<MementoWithCategory>> = combine(_tab, _cat, _sort, _query) { t, c, s, q ->
        Quad(t, c, s, q)
    }.flatMapLatest { (tab, cat, sort, q) ->
        val base: Flow<List<MementoWithCategory>> = when {
            q.isNotBlank()             -> repo.searchMementos(q)
            cat != null                -> repo.getMementosByCategory(cat)
            tab == FilterTab.TODAY     -> repo.getMementosForToday()
            tab == FilterTab.UPCOMING  -> repo.getUpcomingMementos()
            tab == FilterTab.COMPLETED -> repo.completedMementos
            else                       -> repo.allActiveMementos
        }
        base.map { list -> sorted(list, sort) }
    }.asLiveData()

    private fun sorted(list: List<MementoWithCategory>, order: SortOrder) = when (order) {
        SortOrder.BY_DATE_ASC      -> list.sortedWith(compareBy(nullsLast()) { it.memento.dueDateTime })
        SortOrder.BY_DATE_DESC     -> list.sortedWith(compareByDescending(nullsFirst()) { it.memento.dueDateTime })
        SortOrder.BY_PRIORITY_DESC -> list.sortedByDescending { it.memento.priority.ordinal }
        SortOrder.BY_TITLE_ASC     -> list.sortedBy { it.memento.title.lowercase() }
        SortOrder.BY_CREATED_DESC  -> list.sortedByDescending { it.memento.createdAt }
    }

    fun setFilterTab(tab: FilterTab)   { _tab.value = tab }
    fun setSelectedCategory(id: Long?) { _cat.value = id }
    fun setSortOrder(o: SortOrder)     { _sort.value = o }
    fun setSearchQuery(q: String)      { _query.value = q }

    fun toggleCompleted(m: Memento) = viewModelScope.launch { repo.setCompleted(m.id, !m.isCompleted) }
    fun deleteMemento(m: Memento)   = viewModelScope.launch { repo.deleteMemento(m) }
    fun toggleOccurrence(m: Memento, occurrenceIndex: Int, checked: Boolean) =
        viewModelScope.launch { repo.toggleOccurrence(m, occurrenceIndex, checked) }

    private data class Quad<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
}
