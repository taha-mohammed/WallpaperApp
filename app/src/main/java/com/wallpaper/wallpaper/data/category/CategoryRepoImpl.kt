package com.wallpaper.wallpaper.data.category

import com.wallpaper.wallpaper.data.DriveApi
import com.wallpaper.wallpaper.util.toCategories
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEmpty
import javax.inject.Inject

class CategoryRepoImpl @Inject constructor(
    private val categoryDao: CategoryDao,
    private val api: DriveApi
) : CategoryRepo {

    override fun getAllCategories(): Flow<List<Category>> {
        return categoryDao.getAllCategories().onEmpty {
            refreshCategories()
        }
    }

    override suspend fun refreshCategories() {
        api.getCategories()
            .onSuccess {
                categoryDao.clear()
                categoryDao.insertCategories(it.toCategories())
            }
    }

}