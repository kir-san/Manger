package com.san.kir.manger.di

import android.app.Application
import com.san.kir.manger.room.RoomDB
import com.san.kir.manger.room.dao.CategoryDao
import com.san.kir.manger.room.dao.ChapterDao
import com.san.kir.manger.room.dao.DownloadDao
import com.san.kir.manger.room.dao.MainMenuDao
import com.san.kir.manger.room.dao.MangaDao
import com.san.kir.manger.room.dao.PlannedDao
import com.san.kir.manger.room.dao.SiteDao
import com.san.kir.manger.room.dao.StorageDao
import com.san.kir.manger.room.getDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Singleton

@Module
@InstallIn(ViewModelComponent::class)
object DataModule {

    @Provides
    @ViewModelScoped
    fun provideAppDatabase(application: Application): RoomDB {
        return getDatabase(application)
    }

    @Provides
    @ViewModelScoped
    fun provideMainMenuDao(database: RoomDB): MainMenuDao {
        return database.mainMenuDao
    }

    @Provides
    @ViewModelScoped
    fun provideMangaDao(database: RoomDB): MangaDao {
        return database.mangaDao
    }
    @Provides
    @ViewModelScoped
    fun provideStorageDao(database: RoomDB): StorageDao {
        return database.storageDao
    }
    @Provides
    @ViewModelScoped
    fun provideCategoryDao(database: RoomDB): CategoryDao {
        return database.categoryDao
    }
    @Provides
    @ViewModelScoped
    fun provideSiteDao(database: RoomDB): SiteDao {
        return database.siteDao
    }
    @Provides
    @ViewModelScoped
    fun provideDownloadDao(database: RoomDB): DownloadDao {
        return database.downloadDao
    }
    @Provides
    @ViewModelScoped
    fun provideChapterDao(database: RoomDB): ChapterDao {
        return database.chapterDao
    }
    @Provides
    @ViewModelScoped
    fun providePlannedDao(database: RoomDB): PlannedDao {
        return database.plannedDao
    }
}
