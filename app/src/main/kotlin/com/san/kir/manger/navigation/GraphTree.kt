package com.san.kir.manger.navigation

/*
* Обозначения в дереве навигации
* const - конечный экран
* operator - вложенная навигация
* */
object GraphTree {

    object Library {
        operator fun invoke() = "library"

        const val main = "main"
        const val item = "chapters"
        const val addOnline = "add_online"
        const val about = "about"
    }

    object Storage {
        operator fun invoke(): String = "storage"

        const val main = "main"
        const val item = "storage_item"
    }

    object Categories {
        operator fun invoke(): String = "categories"

        const val main = "main"
        const val item = "category_item"
    }

    object Statistic {
        operator fun invoke(): String = "statistic"

        const val main = "main"
        const val item = "statistic_item"
    }

    object Schedule {
        operator fun invoke(): String = "schedule"

        const val main = "main"
        const val item = "schedule_item"
    }

    object Catalogs {
        operator fun invoke(): String = "catalogs"

        const val main = "main"
        const val item = "catalog"
        const val itemInfo = "info"
        const val itemAdd = "add"
        const val search = "global_search"
    }

    object Accounts {
        operator fun invoke(): String = "accounts"

        const val main = "main"
        const val shikimori = "shikimori"
        const val localItems = "local_items"
        const val localItem = "local_item"
        const val search = "shiki_search"
        const val shikiItem = "shiki_search_item"
    }

    const val downloader = "downloader"
    const val latest = "latest"
    const val settings = "settings"
}
