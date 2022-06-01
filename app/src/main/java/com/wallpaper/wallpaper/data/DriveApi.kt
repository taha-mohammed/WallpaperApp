package com.wallpaper.wallpaper.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.wallpaper.wallpaper.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.IOException
import javax.inject.Inject

class DriveApi @Inject constructor(
    private var service: Drive
) {

    suspend fun getCategories(): Result<List<File>> = withContext(Dispatchers.IO) {
        val files = mutableListOf<File>()
        var pageToken: String? = null
        do {
            try {
                val result = service.files().list()
                    .setKey(BuildConfig.API_KEY)
                    .setQ("'${BuildConfig.PARENT_ID}' in parents")
                    .setFields("nextPageToken, files(id, name, mimeType)")
                    .setPageToken(pageToken)
                    .execute()
                files.addAll(result.files)
                pageToken = result.nextPageToken;
            } catch (e: IOException) {
                return@withContext Result.failure<List<File>>(e)
            }
        } while (pageToken != null)
        Result.success(files)
    }

    suspend fun getFiles(parentId: String): Result<List<File>> = withContext(Dispatchers.IO) {
        val files = mutableListOf<File>()
        var pageToken: String? = null
        do {
            try {
                val result = service.files().list()
                    .setKey(BuildConfig.API_KEY)
                    .setQ("'$parentId' in parents")
                    .setFields("nextPageToken, files(id)")
                    .setPageToken(pageToken)
                    .execute()
                files.addAll(result.files)
                pageToken = result.nextPageToken;
            } catch (e: IOException) {
                return@withContext Result.failure<List<File>>(e)
            }
        } while (pageToken != null)
        Result.success(files)
    }

    suspend fun getBitmapImage(imageId: String): Bitmap? = withContext(Dispatchers.IO) {
        val outputStream = ByteArrayOutputStream()
        try {
            service.files().get(imageId).setKey(BuildConfig.API_KEY).executeMediaAndDownloadTo(outputStream)
            val bitmap: Bitmap = BitmapFactory
                .decodeByteArray(outputStream.toByteArray(), 0, outputStream.size())
            bitmap
        } catch (e: IOException) {
            e.printStackTrace()
            null
        } finally {
            outputStream.close()
        }
    }
}