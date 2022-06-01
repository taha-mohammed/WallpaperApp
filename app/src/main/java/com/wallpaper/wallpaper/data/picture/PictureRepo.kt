package com.wallpaper.wallpaper.data.picture

import android.graphics.Bitmap
import kotlinx.coroutines.flow.Flow

interface PictureRepo {
    fun getPictures(categoryId: String): Flow<List<PictureEntity>>
    suspend fun refreshPictures(categoryId: String)
    fun getAllFavourites(): Flow<List<PictureEntity>>
    suspend fun toggleFavourite(id: String)
    suspend fun getBitmap(imageId: String): Bitmap?
}