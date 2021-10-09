package com.san.kir.manger.di

import android.app.Application
import androidx.datastore.core.DataStore
import com.san.kir.manger.Chapters
import com.san.kir.manger.Download
import com.san.kir.manger.FirstLaunch
import com.san.kir.manger.Main
import com.san.kir.manger.Viewer
import com.san.kir.manger.data.datastore.chaptersStore
import com.san.kir.manger.data.datastore.downloadStore
import com.san.kir.manger.data.datastore.firstLaunchStore
import com.san.kir.manger.data.datastore.mainStore
import com.san.kir.manger.data.datastore.viewerStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {
    @Provides
    fun provideMainDataStore(application: Application): DataStore<Main> {
        return application.mainStore
    }

    @Provides
    fun provideChaptersDataStore(application: Application): DataStore<Chapters> {
        return application.chaptersStore
    }

    @Provides
    fun provideFirstLaunchDataStore(application: Application): DataStore<FirstLaunch> {
        return application.firstLaunchStore
    }

    @Provides
    fun provideDownloadDataStore(application: Application): DataStore<Download> {
        return application.downloadStore
    }

    @Provides
    fun provideViewerDataStore(application: Application): DataStore<Viewer> {
        return application.viewerStore
    }
}
