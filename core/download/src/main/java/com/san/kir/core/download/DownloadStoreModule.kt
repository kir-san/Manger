package com.san.kir.core.download

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
    fun provideDownloadListener(listenerProvider: ListenerProvider): DownloadListener {
        return listenerProvider.mainListener
    }

    @Provides
    @Singleton
    fun provideJobContext(): JobContext {
        return JobContext(Executors.newSingleThreadExecutor())
    }
}
