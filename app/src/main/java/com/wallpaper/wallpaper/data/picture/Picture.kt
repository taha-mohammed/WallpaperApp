package com.wallpaper.wallpaper.data.picture

import android.graphics.Bitmap
import android.net.Uri

data class Picture(
    val id: String,
    var isFavourite: Boolean = false,
    var bitmap: Bitmap? = null,
    var uri: Uri? = null
)