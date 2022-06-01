package com.wallpaper.wallpaper.data.picture

import androidx.room.*
import kotlinx.coroutines.flow.Flow


@Dao
interface PictureDao {

    @Query("SELECT * FROM picture WHERE picture.cid = :categoryId")
    fun getPictures(categoryId: String): Flow<List<PictureEntity>>

    @Query("SELECT * FROM picture WHERE picture.id = :id")
    suspend fun getPicture(id: String): PictureEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPictures(pictures: List<PictureEntity>)

    @Update
    suspend fun updatePictures(pictures: List<PictureEntity>)

    @Query("DELETE FROM picture WHERE picture.cid = :categoryId")
    suspend fun clear(categoryId: String)

    @Query("SELECT * FROM picture WHERE picture.is_favourite")
    fun getAllFavourites(): Flow<List<PictureEntity>>

    @Query("SELECT * FROM picture WHERE picture.is_favourite")
    suspend fun getFavourites(): List<PictureEntity>

}