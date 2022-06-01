package com.wallpaper.wallpaper.data.picture

import android.graphics.Bitmap
import com.wallpaper.wallpaper.data.DriveApi
import com.wallpaper.wallpaper.util.toPicturesEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEmpty
import javax.inject.Inject

class PictureRepoImpl @Inject constructor(
    private val driveApi: DriveApi,
    private val pictureDao: PictureDao
) : PictureRepo {
    override fun getPictures(categoryId: String): Flow<List<PictureEntity>> {
        return pictureDao.getPictures(categoryId).onEmpty {
            refreshPictures(categoryId)
        }
    }

    override suspend fun refreshPictures(categoryId: String) {
        driveApi.getFiles(categoryId).onSuccess { result ->
            val favourites = pictureDao.getFavourites().map { it.id }
            result.toPicturesEntity(categoryId)
                .map {
                    if (favourites.contains(it.id))
                        return@map it.copy(isFavourite = true)
                    it
                }.let {
                    pictureDao.clear(categoryId)
                    pictureDao.insertPictures(it)
                }
        }
    }

    override fun getAllFavourites(): Flow<List<PictureEntity>> =
        pictureDao.getAllFavourites()

    override suspend fun toggleFavourite(id: String) {
        pictureDao.getPicture(id).let {
            pictureDao.updatePictures(listOf(it.copy(isFavourite = !it.isFavourite)))
        }
    }

    override suspend fun getBitmap(imageId: String): Bitmap? =
        driveApi.getBitmapImage(imageId)
}