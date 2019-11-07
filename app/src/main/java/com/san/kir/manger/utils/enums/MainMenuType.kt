package com.san.kir.manger.utils.enums

import com.san.kir.manger.R

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
