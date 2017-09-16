package com.san.kir.manger

import android.support.v7.app.AppCompatDelegate
import com.raizlabs.android.dbflow.config.FlowConfig
import com.raizlabs.android.dbflow.config.FlowManager
import com.san.kir.manger.dbflow.models.Category
import com.san.kir.manger.dbflow.wrapers.CategoryWrapper
import com.san.kir.manger.utils.categoryAll
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import java.io.File

class App : DaggerApplication() {

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerAppComponent.builder().create(this)
    }

    companion object {
        lateinit var exCacheDir: File
        lateinit var context: App
    }

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

        context = this
        exCacheDir = externalCacheDir

        FlowManager.init(FlowConfig.Builder(this).build())

        if (CategoryWrapper.getCategories().isEmpty())
            Category(categoryAll, 0).insert()

    }

    override fun onTerminate() {
        super.onTerminate()
        FlowManager.destroy()
    }
}
