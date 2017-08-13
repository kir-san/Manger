package com.san.kir.manger.dbflow

import com.raizlabs.android.dbflow.annotation.Database

@Database(name = AppDatabase.NAME, version = AppDatabase.VERSION, generatedClassSeparator = "_")
class AppDatabase {
    companion object {
        const val NAME = "profile"
        const val VERSION = 8
    }
}
