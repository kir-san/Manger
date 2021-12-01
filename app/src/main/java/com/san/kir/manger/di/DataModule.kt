package com.san.kir.manger.di

import android.app.Application
import com.san.kir.manger.data.room.RoomDB
import com.san.kir.manger.data.room.dao.CategoryDao
import com.san.kir.manger.data.room.dao.ChapterDao
import com.san.kir.manger.data.room.dao.DownloadDao
import com.san.kir.manger.data.room.dao.MainMenuDao
import com.san.kir.manger.data.room.dao.MangaDao
import com.san.kir.manger.data.room.dao.PlannedDao
import com.san.kir.manger.data.room.dao.SiteDao
import com.san.kir.manger.data.room.dao.StatisticDao
import com.san.kir.manger.data.room.dao.StorageDao
import com.san.kir.manger.data.room.getDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideAppDatabase(application: Application): RoomDB {
        return getDatabase(application)
    }

    @Provides
    fun provideMainMenuDao(database: RoomDB): MainMenuDao {
        return database.mainMenuDao
    }

    @Provides
    fun provideMangaDao(database: RoomDB): MangaDao {
        return database.mangaDao
    }

    @Provides
    fun provideStorageDao(database: RoomDB): StorageDao {
        return database.storageDao
    }

    @Provides
    fun provideCategoryDao(database: RoomDB): CategoryDao {
        return database.categoryDao
    }

    @Provides
    fun provideSiteDao(database: RoomDB): SiteDao {
        return database.siteDao
    }

    @Provides
    fun provideDownloadDao(database: RoomDB): DownloadDao {
        return database.downloadDao
    }

    @Provides
    fun provideChapterDao(database: RoomDB): ChapterDao {
        return database.chapterDao
    }

    @Provides
    fun providePlannedDao(database: RoomDB): PlannedDao {
        return database.plannedDao
    }

    @Provides
    fun provideStatisticDao(database: RoomDB): StatisticDao {
        return database.statisticDao
    }
}
