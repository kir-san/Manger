package com.san.kir.manger.room.dao

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import com.san.kir.manger.R
import com.san.kir.manger.room.models.MainMenuItem

@Dao
interface MainMenuDao : BaseDao<MainMenuItem> {
    @Query("SELECT * FROM mainmenuitems ORDER BY `order`")
    fun loadItems(): List<MainMenuItem>
}

enum class MainMenuType {
    Library {
        override fun stringId() = R.string.main_menu_library
    },
    Storage {
        override fun stringId() = R.string.main_menu_storage
    },
    Category {
        override fun stringId() = R.string.main_menu_category
    },
    Catalogs {
        override fun stringId() = R.string.main_menu_catalogs
    },
    Downloader {
        override fun stringId() = R.string.main_menu_downloader
    },
    Latest {
        override fun stringId() = R.string.main_menu_latest
    },
    Settings {
        override fun stringId() = R.string.main_menu_settings
    },
    Schedule {
        override fun stringId() = R.string.main_menu_schedule
    },
    Statistic {
        override fun stringId() = R.string.main_menu_statistic
    },
    Default {
        override fun stringId() = R.string.main_menu_storage
    };

    abstract fun stringId(): Int
}


