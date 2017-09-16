package com.san.kir.manger.components.Main

import android.support.v7.app.ActionBar
import com.san.kir.manger.components.Library.LibraryFragment
import com.san.kir.manger.components.Library.LibraryModule
import com.san.kir.manger.components.Parsing.ManageSites
import com.san.kir.manger.components.Storage.StorageItemFragment
import com.san.kir.manger.components.Storage.StorageMainDirFragment
import com.san.kir.manger.components.Storage.StorageMangaDirFragment
import com.san.kir.manger.components.Storage.StorageModule
import com.san.kir.manger.utils.ActivityScope
import com.san.kir.manger.utils.FragmentScope
import com.san.kir.manger.utils.MainRouter
import com.san.kir.manger.utils.MainRouterImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector

@Module
class MainModule {
    @ActivityScope
    @Provides
    fun view(activity: MainActivity) = MainView(activity)

    @ActivityScope
    @Provides
    fun updateApp(act: MainActivity) = ManageSites.UpdateApp(act)

    @ActivityScope
    @Provides
    fun supportActionBar(activity: MainActivity): ActionBar {
        return activity.supportActionBar!!
    }
}

@Module
interface MainSecondModule {
    @ActivityScope
    @Binds
    fun router(routerImpl: MainRouterImpl): MainRouter

    @FragmentScope
    @ContributesAndroidInjector(modules = arrayOf(LibraryModule::class))
    fun library(): LibraryFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = arrayOf(StorageModule::class))
    fun mainStorage(): StorageMainDirFragment

    @FragmentScope
    @ContributesAndroidInjector(modules = arrayOf(StorageModule::class))
    fun mangaStorage(): StorageMangaDirFragment

@FragmentScope
    @ContributesAndroidInjector(modules = arrayOf(StorageModule::class))
    fun itemStorage(): StorageItemFragment

}
