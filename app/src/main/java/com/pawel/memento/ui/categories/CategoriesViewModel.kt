package com.pawel.memento.ui.categories

import android.app.Application
import androidx.lifecycle.*
import com.pawel.memento.MementoApp
import com.pawel.memento.data.model.Category
import kotlinx.coroutines.launch

class CategoriesViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = (application as MementoApp).repository
    val allCategories = repo.allCategories.asLiveData()
    fun insertCategory(c: Category) = viewModelScope.launch { repo.insertCategory(c) }
    fun updateCategory(c: Category) = viewModelScope.launch { repo.updateCategory(c) }
    fun deleteCategory(c: Category) = viewModelScope.launch { repo.deleteCategory(c) }
}
