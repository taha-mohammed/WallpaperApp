package com.wallpaper.wallpaper.util

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.core.content.FileProvider
import androidx.core.text.layoutDirection
import com.wallpaper.wallpaper.BuildConfig
import com.wallpaper.wallpaper.data.category.Category
import com.wallpaper.wallpaper.data.picture.Picture
import com.wallpaper.wallpaper.data.picture.PictureEntity
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import com.google.api.services.drive.model.File as DriveFile

fun Bitmap.toUri(context: Context): Uri? {
    try {
        val file = File(context.externalCacheDir, System.currentTimeMillis().toString() + ".jpg")
        this.compress(Bitmap.CompressFormat.JPEG, 90, FileOutputStream(file))
        return FileProvider.getUriForFile(
            context,
            BuildConfig.APPLICATION_ID + ".provider", //(use your app signature + ".provider" )
            file
        )
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return null
}

//merge gif image with folder
fun List<DriveFile>.toCategories(): List<Category> {
    return this.groupBy { it.name.split(".")[0] }
        .map { map ->
            Category(
                map.value.find { it.mimeType == "application/vnd.google-apps.folder" }?.id.orEmpty(),
                map.key,
                map.value.find { it.mimeType != "application/vnd.google-apps.folder" }?.id.orEmpty()
            )
        }
}

fun List<DriveFile>.toPicturesEntity(categoryId: String): List<PictureEntity> {
    return this.map {
        PictureEntity(
            id = it.id,
            cid = categoryId
        )
    }
}

fun List<PictureEntity>.toPictures(pictures: List<Picture>): List<Picture> {
    return this.map { map ->
        val picture = pictures.find { it.id == map.id }
        Picture(
            id = map.id,
            isFavourite = map.isFavourite,
            bitmap = picture?.bitmap,
            uri = picture?.uri
        )
    }
}

fun Modifier.mirror(): Modifier {
    if (Locale.getDefault().layoutDirection == android.util.LayoutDirection.RTL)
        return this.scale(scaleX = -1f, scaleY = 1f)
    return this
}