package com.san.kir.manger.di

import android.app.Application
import com.san.kir.data.db.RoomDB
import com.san.kir.data.db.dao.CategoryDao
import com.san.kir.data.db.dao.ChapterDao
import com.san.kir.data.db.dao.DownloadDao
import com.san.kir.data.db.dao.MainMenuDao
import com.san.kir.data.db.dao.MangaDao
import com.san.kir.data.db.dao.PlannedDao
import com.san.kir.data.db.dao.SiteDao
import com.san.kir.data.db.dao.StatisticDao
import com.san.kir.data.db.dao.StorageDao
import com.san.kir.data.db.getDatabase
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
    fun provideAppDatabase(application: Application): com.san.kir.data.db.RoomDB {
        return com.san.kir.data.db.getDatabase(application)
    }

    @Provides
    fun provideMainMenuDao(database: com.san.kir.data.db.RoomDB): com.san.kir.data.db.dao.MainMenuDao {
        return database.mainMenuDao
    }

    @Provides
    fun provideMangaDao(database: com.san.kir.data.db.RoomDB): com.san.kir.data.db.dao.MangaDao {
        return database.mangaDao
    }

    @Provides
    fun provideStorageDao(database: com.san.kir.data.db.RoomDB): com.san.kir.data.db.dao.StorageDao {
        return database.storageDao
    }

    @Provides
    fun provideCategoryDao(database: com.san.kir.data.db.RoomDB): com.san.kir.data.db.dao.CategoryDao {
        return database.categoryDao
    }

    @Provides
    fun provideSiteDao(database: com.san.kir.data.db.RoomDB): com.san.kir.data.db.dao.SiteDao {
        return database.siteDao
    }

    @Provides
    fun provideDownloadDao(database: com.san.kir.data.db.RoomDB): com.san.kir.data.db.dao.DownloadDao {
        return database.downloadDao
    }

    @Provides
    fun provideChapterDao(database: com.san.kir.data.db.RoomDB): com.san.kir.data.db.dao.ChapterDao {
        return database.chapterDao
    }

    @Provides
    fun providePlannedDao(database: com.san.kir.data.db.RoomDB): com.san.kir.data.db.dao.PlannedDao {
        return database.plannedDao
    }

    @Provides
    fun provideStatisticDao(database: com.san.kir.data.db.RoomDB): com.san.kir.data.db.dao.StatisticDao {
        return database.statisticDao
    }
}
