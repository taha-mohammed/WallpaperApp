package com.wallpaper.wallpaper.data.category

import kotlinx.coroutines.flow.Flow

interface CategoryRepo {
    fun getAllCategories(): Flow<List<Category>>
    suspend fun refreshCategories()
}