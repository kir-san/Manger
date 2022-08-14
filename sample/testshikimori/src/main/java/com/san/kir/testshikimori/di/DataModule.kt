package com.san.kir.testshikimori.di

import android.app.Application
import com.san.kir.data.db.RoomDB
import com.san.kir.data.db.dao.ChapterDao
import com.san.kir.data.db.dao.MangaDao
import com.san.kir.data.db.dao.SettingsDao
import com.san.kir.data.db.dao.ShikimoriDao
import com.san.kir.data.db.dao.StatisticDao
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
        return RoomDB.getDefaultDatabase(application)
    }

    @Provides
    fun provideMangaDao(database: RoomDB): MangaDao {
        return database.mangaDao()
    }

    @Provides
    fun provideChapterDao(database: RoomDB): ChapterDao {
        return database.chapterDao()
    }

    @Provides
    fun provideStatisticDao(database: RoomDB): StatisticDao {
        return database.statisticDao()
    }

    @Provides
    fun provideShikimoriDao(database: RoomDB): ShikimoriDao {
        return database.shikimoriDao()
    }

    @Provides
    fun provideSettingsDao(database: RoomDB): SettingsDao {
        return database.settingsDao()
    }
}
