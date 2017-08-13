package com.san.kir.manger.dbflow.wrapers

import com.raizlabs.android.dbflow.kotlinextensions.from
import com.raizlabs.android.dbflow.kotlinextensions.list
import com.raizlabs.android.dbflow.kotlinextensions.select
import com.san.kir.manger.dbflow.models.Setting

object SettingsWrapper {
    fun getSettings(): MutableList<Setting> = (select from Setting::class).list
    fun addSetting(settings: Setting) = settings.insert()
}
