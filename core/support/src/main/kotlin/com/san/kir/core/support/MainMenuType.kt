package com.san.kir.core.support

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
    Accounts {
        override fun stringId() =R.string.main_menu_accounts
    },
    Default {
        override fun stringId() = R.string.main_menu_storage
        override val added = false
    };

    abstract fun stringId(): Int
    open val added: Boolean = true
}
