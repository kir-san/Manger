package com.san.kir.features.shikimori.di

import com.san.kir.core.utils.coroutines.defaultDispatcher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal class CoroutineScopeModule {

    @Singleton
    @Provides
    @ShikiFeatureScope
    fun provideCoroutineScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + defaultDispatcher)
    }
}

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ShikiFeatureScope
