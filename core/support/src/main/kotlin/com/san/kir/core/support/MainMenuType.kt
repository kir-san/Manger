package com.san.kir.core.support

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.GetApp
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.ui.graphics.vector.ImageVector

enum class MainMenuType {
    Library {
        override fun stringId() = R.string.main_menu_library
        override val icon: ImageVector
            get() = Icons.Default.LocalLibrary
    },
    Storage {
        override fun stringId() = R.string.main_menu_storage
        override val icon: ImageVector
            get() = Icons.Default.LocalLibrary
    },
    Category {
        override fun stringId() = R.string.main_menu_category
        override val icon: ImageVector
            get() = Icons.Default.LocalLibrary
    },
    Catalogs {
        override fun stringId() = R.string.main_menu_catalogs
        override val icon: ImageVector
            get() = Icons.Default.Storage
    },
    Downloader {
        override fun stringId() = R.string.main_menu_downloader
        override val icon: ImageVector
            get() = Icons.Default.Category
    },
    Latest {
        override fun stringId() = R.string.main_menu_latest
        override val icon: ImageVector
            get() = Icons.Default.FormatListBulleted
    },
    Settings {
        override fun stringId() = R.string.main_menu_settings
        override val icon: ImageVector
            get() = Icons.Default.GetApp
    },
    Schedule {
        override fun stringId() = R.string.main_menu_schedule
        override val icon: ImageVector
            get() = Icons.Default.History
    },
    Statistic {
        override fun stringId() = R.string.main_menu_statistic
        override val icon: ImageVector
            get() = Icons.Default.Settings
    },
    Accounts {
        override fun stringId() =R.string.main_menu_accounts
        override val icon: ImageVector
            get() = Icons.Default.Note
    },
    Default {
        override fun stringId() = R.string.main_menu_storage
        override val icon: ImageVector
            get() = Icons.Default.Schedule
        override val added = false
    };

    abstract fun stringId(): Int
    abstract val icon: ImageVector
    open val added: Boolean = true
}
