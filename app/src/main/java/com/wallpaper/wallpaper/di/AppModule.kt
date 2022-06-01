package com.wallpaper.wallpaper.di

import android.content.Context
import androidx.room.Room
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import com.wallpaper.wallpaper.data.AppDatabase
import com.wallpaper.wallpaper.data.category.CategoryDao
import com.wallpaper.wallpaper.data.category.CategoryRepo
import com.wallpaper.wallpaper.data.category.CategoryRepoImpl
import com.wallpaper.wallpaper.data.picture.PictureDao
import com.wallpaper.wallpaper.data.picture.PictureRepo
import com.wallpaper.wallpaper.data.picture.PictureRepoImpl
import com.wallpaper.wallpaper.util.DATABASE_NAME
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object AppModule {
    @Provides
    @Singleton
    fun provideDriveService(): Drive {
        return Drive.Builder(
            NetHttpTransport(),
            JacksonFactory.getDefaultInstance(),
            null
        )
            .setApplicationName("Wallpaper")
            .build()
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext appContext: Context): AppDatabase {
        return Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            DATABASE_NAME
        ).fallbackToDestructiveMigration().build()
    }

    @Singleton
    @Provides
    fun providePictureDao(appDatabase: AppDatabase): PictureDao {
        return appDatabase.pictureDao()
    }

    @Singleton
    @Provides
    fun provideCategoryDao(appDatabase: AppDatabase): CategoryDao {
        return appDatabase.categoryDao()
    }

}

@InstallIn(SingletonComponent::class)
@Module
abstract class RepositoryModule {

    @Singleton
    @Binds
    abstract fun bindsFavouriteRepository(pictureRepoImpl: PictureRepoImpl): PictureRepo

    @Singleton
    @Binds
    abstract fun bindsCategoryRepository(categoryRepoImpl: CategoryRepoImpl): CategoryRepo
}