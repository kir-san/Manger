package com.san.kir.manger.di

import com.san.kir.manger.components.download_manager.DownloadListener
import com.san.kir.manger.components.download_manager.ListenerProvider
import com.san.kir.manger.utils.JobContext
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.Executors
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DownloadStoreModule {
    @Provides
    @Singleton
    fun provideListenerProvider(): ListenerProvider {
        return ListenerProvider()
    }

    @Provides
    fun provideDownloadListener(listenerProvider: ListenerProvider): DownloadListener {
        return listenerProvider.mainListener
    }

    @Provides
    @Singleton
    fun provideJobContext(): JobContext {
        return JobContext(Executors.newSingleThreadExecutor())
    }
}
