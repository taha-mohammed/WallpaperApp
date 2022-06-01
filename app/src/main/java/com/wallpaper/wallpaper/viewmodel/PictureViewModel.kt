package com.wallpaper.wallpaper.viewmodel

import android.app.DownloadManager
import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wallpaper.wallpaper.BuildConfig
import com.wallpaper.wallpaper.R
import com.wallpaper.wallpaper.data.picture.Picture
import com.wallpaper.wallpaper.data.picture.PictureRepo
import com.wallpaper.wallpaper.util.PictureEvent
import com.wallpaper.wallpaper.util.toPictures
import com.wallpaper.wallpaper.util.toUri
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class PictureViewModel @Inject constructor(
    private val pictureRepo: PictureRepo,
    stateHandle: SavedStateHandle
) : ViewModel() {

    val initState = stateHandle.get<String>("picture_id").orEmpty()
    private val categoryId: String = stateHandle.get<String>("category_id").orEmpty()
    private val categoryName: String = stateHandle.get<String>("category_name") ?: "Favourite"

    private val _pictures = mutableStateOf(emptyList<Picture>())
    val pictures: State<List<Picture>> = _pictures

    private val _state = mutableStateOf(0)
    val state: State<Int> = _state

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        viewModelScope.launch {
            if (categoryName == "Favourite") {
                pictureRepo.getAllFavourites().collectLatest {
                    _pictures.value = it.toPictures(_pictures.value)
                }
                return@launch
            }
            pictureRepo.getPictures(categoryId).collectLatest {
                _pictures.value = it.toPictures(_pictures.value)
            }
        }
    }

    fun onEvent(event: PictureEvent) {
        when (event) {
            is PictureEvent.SharePicture -> {
                viewModelScope.launch {
                    sharePicture(event.context)
                }
            }
            is PictureEvent.DownloadPicture -> {
                viewModelScope.launch{
                    downloadPicture(event.dm)
                }
            }
            is PictureEvent.SetAsWallpaper -> {
                viewModelScope.launch {
                    setWallpaper(event)
                }
            }
            is PictureEvent.CropWallpaper -> {
                viewModelScope.launch {
                    cropWallpaper(event.context)
                }
            }
            is PictureEvent.ToggleFavourite -> {
                viewModelScope.launch {
                    toggleFavourite()
                }
            }
            is PictureEvent.ChangeState -> {
                _state.value = event.page
            }
        }
    }

    private suspend fun toggleFavourite() {
        val isFavourite = pictures.value[state.value].isFavourite
        if (state.value >= pictures.value.size - 1 && isFavourite) {
            if (state.value == 0) {
                _eventFlow.emit(UiEvent.Back)
            } else {
                _eventFlow.emit(UiEvent.ChangePage(pictures.value.size - 2))
            }
        }
        pictureRepo.toggleFavourite(pictures.value[state.value].id)
        if (isFavourite) {
            _eventFlow.emit(UiEvent.ShowToast(R.string.remove_favourite))
            return
        }
        _eventFlow.emit(UiEvent.ShowToast(R.string.add_favourite))
    }

    private suspend fun sharePicture(context: Context) {
        val uri = getUri(pictures.value[state.value].id, context)
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.putExtra(
            Intent.EXTRA_TEXT, context.getString(R.string.sharing_message) +"\n"+
                    "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID
        )
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
        shareIntent.type = "image/*"

        _eventFlow.emit(
            UiEvent.StartIntent(
                Intent.createChooser(shareIntent, context.getString(R.string.share_chooser_title))
            )
        )
    }
    
    private suspend fun setWallpaper(event: PictureEvent.SetAsWallpaper) {
        try {
            if (event.flagSystem != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                event.wallpaperManager.setBitmap(
                    getBitmap(pictures.value[state.value].id),
                    null,
                    true,
                    event.flagSystem
                )
                _eventFlow.emit(UiEvent.ShowToast(R.string.set_wallpaper))
                return
            }
            event.wallpaperManager.setBitmap(
                getBitmap(pictures.value[state.value].id)
            )
            _eventFlow.emit(UiEvent.ShowToast(R.string.set_wallpaper))
        } catch (e: Exception) {
            _eventFlow.emit(UiEvent.ShowToast(R.string.network_error))
        }
    }

    private suspend fun cropWallpaper(context: Context) {
        try {
            val uri = getUri(pictures.value[state.value].id, context)
            print(uri)
            val wallpaperIntent = WallpaperManager
                .getInstance(context)
                .getCropAndSetWallpaperIntent(uri)
                .setDataAndType(uri, "image/*")
                .putExtra("mimeType", "image/*")

            _eventFlow.emit(UiEvent.StartIntent(wallpaperIntent))
        } catch (e: IllegalArgumentException) {
            _eventFlow.emit(UiEvent.ShowToast(R.string.crop_error))
        }
    }

    private suspend fun downloadPicture(downloadManager: DownloadManager) {
        val time: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            .format(System.currentTimeMillis())
        val uri: Uri =
            Uri.parse("https://drive.google.com/uc?id=${pictures.value[state.value].id}&export=download")
        val request = DownloadManager.Request(uri)
        request.setTitle("$time.jpg")
        request.setMimeType("image/*")
        request.setAllowedOverMetered(true)
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalPublicDir(
            Environment.DIRECTORY_DOWNLOADS,
            "$time.jpg"
        )

        downloadManager.enqueue(request)
        _eventFlow.emit(UiEvent.ShowToast(R.string.start_download))
    }

    private suspend fun getUri(id: String, context: Context): Uri? {
        _pictures.value.find { it.id == id }?.let { picture ->
            return picture.uri ?: getBitmap(id)?.toUri(context)
                .also { configurePictures(id, uri = it) }
        }
        return null
    }

    private suspend fun getBitmap(id: String): Bitmap? {
        pictures.value.find { it.id == id }?.let { picture ->
            return picture.bitmap ?: pictureRepo.getBitmap(id)
                .also { configurePictures(id, bitmap = it) }
        }
        return null
    }

    private fun configurePictures(id: String, bitmap: Bitmap? = null, uri: Uri? = null) {
        _pictures.value = _pictures.value.map { map ->
            if (map.id == id) {
                bitmap?.let { map.bitmap = it }
                uri?.let { map.uri = it }
            }
            map
        }
    }

    sealed class UiEvent {
        data class ShowToast(val resId: Int) : UiEvent()
        data class StartIntent(val intent: Intent) : UiEvent()
        data class ChangePage(val page: Int) : UiEvent()
        object Back : UiEvent()
    }

}