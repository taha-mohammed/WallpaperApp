package com.wallpaper.wallpaper.data.picture

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "picture")
data class PictureEntity(
    @PrimaryKey val id: String,
    @ColumnInfo val cid: String,
    @ColumnInfo val name: String,
    @ColumnInfo(name = "is_favourite") val isFavourite: Boolean = false
)