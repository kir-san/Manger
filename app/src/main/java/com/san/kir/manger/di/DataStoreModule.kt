package com.san.kir.manger.di

import android.app.Application
import androidx.datastore.core.DataStore
import com.san.kir.manger.Main
import com.san.kir.manger.data.datastore.mainStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class DataStoreModule {
    @Provides
    fun provideMainDataStore(application: Application): DataStore<Main> {
        return application.mainStore
    }
}
