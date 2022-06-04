package com.wallpaper.wallpaper.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wallpaper.wallpaper.data.category.Category
import com.wallpaper.wallpaper.data.category.CategoryRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class CategoryState(
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val categoryRepo: CategoryRepo
) : ViewModel() {

    private val _state = mutableStateOf(CategoryState(isLoading = true))
    val state: State<CategoryState> = _state

    init {
        viewModelScope.launch {
            refreshDate()
            categoryRepo.getAllCategories().collectLatest {
                _state.value = CategoryState(it)
            }
        }
    }

    suspend fun refreshDate() = withContext(viewModelScope.coroutineContext) {
        _state.value = state.value.copy(isLoading = true)
        categoryRepo.refreshCategories()
        _state.value = state.value.copy(isLoading = false)
    }
}