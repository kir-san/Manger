package com.san.kir.manger

import android.app.Application
import android.content.Context
import com.raizlabs.android.dbflow.config.FlowConfig
import com.raizlabs.android.dbflow.config.FlowManager
import com.san.kir.manger.components.Main.MainActivity
import com.san.kir.manger.dbflow.models.Category
import com.san.kir.manger.dbflow.wrapers.CategoryWrapper
import com.san.kir.manger.utils.categoryAll

class App : Application() {
    companion object {
        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()

        FlowManager.init(FlowConfig.Builder(this).build())
        context = applicationContext

        if (CategoryWrapper.getCategories().isEmpty())
            Category(categoryAll, 0).insert()

        MainActivity.checkNewVersion()

    }

    override fun onTerminate() {
        super.onTerminate()
        FlowManager.destroy()
    }
}
