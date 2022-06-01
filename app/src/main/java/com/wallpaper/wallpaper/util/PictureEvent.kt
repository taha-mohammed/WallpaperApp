package com.wallpaper.wallpaper.util

import android.app.DownloadManager
import android.app.WallpaperManager
import android.content.Context

sealed class PictureEvent {
    data class SharePicture(val context: Context): PictureEvent()
    data class DownloadPicture(val dm: DownloadManager): PictureEvent()
    data class SetAsWallpaper(val wallpaperManager: WallpaperManager, val flagSystem: Int? = null): PictureEvent()
    data class CropWallpaper(val context: Context): PictureEvent()
    object ToggleFavourite: PictureEvent()
    data class ChangeState(val page: Int): PictureEvent()
}