package com.wallpaper.wallpaper.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.wallpaper.wallpaper.data.category.Category
import com.wallpaper.wallpaper.data.category.CategoryDao
import com.wallpaper.wallpaper.data.picture.PictureDao
import com.wallpaper.wallpaper.data.picture.PictureEntity


@Database(entities = [PictureEntity::class, Category::class], version = 3)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pictureDao(): PictureDao
    abstract fun categoryDao(): CategoryDao
}
