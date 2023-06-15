package com.wallpaper.wallpaper.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wallpaper.wallpaper.data.picture.Picture
import com.wallpaper.wallpaper.data.picture.PictureRepo
import com.wallpaper.wallpaper.util.toPictures
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WallpaperState(
    val wallpapers: List<Picture> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class WallpaperViewModel @Inject constructor(
    private val pictureRepo: PictureRepo,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = mutableStateOf(WallpaperState(isLoading = true))
    val state: State<WallpaperState> = _state

    val categoryId: String = savedStateHandle.get<String>("category_id").orEmpty()
    val categoryName: String = savedStateHandle.get<String>("category_name") ?: "Favourite"

    init {
        viewModelScope.launch {
            if (categoryName == "Favourite") {
                pictureRepo.getAllFavourites().collectLatest {
                    _state.value = WallpaperState(it.toPictures(emptyList()))
                }
                return@launch
            }
            refreshData()
            pictureRepo.getPictures(categoryId).collectLatest {
                _state.value = WallpaperState(it.toPictures(emptyList()))
            }
        }
    }

    suspend fun refreshData() = viewModelScope.launch {
        if (categoryName == "Favourite")
            return@launch
        _state.value = state.value.copy(isLoading = true)
        pictureRepo.refreshPictures(categoryId)
        _state.value = state.value.copy(isLoading = false)
    }
}