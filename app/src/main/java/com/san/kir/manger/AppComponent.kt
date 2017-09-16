package com.san.kir.manger

import com.san.kir.manger.components.Main.MainActivity
import com.san.kir.manger.components.Main.MainModule
import com.san.kir.manger.components.Main.MainSecondModule
import com.san.kir.manger.utils.ActivityScope
import dagger.Component
import dagger.Module
import dagger.Provides
import dagger.android.AndroidInjector
import dagger.android.ContributesAndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Component(modules = arrayOf(AppModule::class,
                             AppSecondModule::class,
                             AndroidSupportInjectionModule::class))
@Singleton
interface AppComponent : AndroidInjector<App> {
    @Component.Builder
    abstract class Builder : AndroidInjector.Builder<App>()
}

@Module
interface AppModule {
    @ActivityScope
    @ContributesAndroidInjector(modules = arrayOf(MainSecondModule::class, MainModule::class))
    fun mainActivityInjector(): MainActivity
}

@Module
class AppSecondModule {
    @Provides
    @Singleton
    fun context(app: App) = app.applicationContext

}

