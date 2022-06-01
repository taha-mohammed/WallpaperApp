package com.wallpaper.wallpaper.data.category

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "category")
data class Category(
    @PrimaryKey val id: String,
    @ColumnInfo val name: String,
    @ColumnInfo val background: String
)