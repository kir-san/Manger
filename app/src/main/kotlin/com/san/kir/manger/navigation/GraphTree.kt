package com.san.kir.manger.navigation

open class Nested(private val base: String) {
    operator fun invoke() = base
    fun screen(name: String) = "$base/$name"
}

/*
* Обозначения в дереве навигации
* const - конечный экран
* operator - вложенная навигация
* */
object GraphTree {

    object Library : Nested("library") {
        val main = screen("main")
        val item = screen("chapters")
        val addOnline = screen("add_online")
        val about = screen("about")
    }

    object Storage : Nested("storage") {
        val main = screen("main")
        val item = screen("storage_item")
    }

    object Categories : Nested("categories") {
        val main = screen("main")
        val item = screen("category_item")
    }

    object Statistic : Nested("statistic") {
        val main = screen("main")
        val item = screen("statistic_item")
    }

    object Schedule : Nested("schedule") {
        val main = screen("main")
        val item = screen("schedule_item")
    }

    object Catalogs : Nested("catalogs") {
        val main = screen("main")
        val item = screen("catalog")
        val itemInfo = screen("info")
        val itemAdd = screen("add")
        val search = screen("global_search")
    }

    object Accounts : Nested("accounts") {
        val main = screen("main")

        object Catalogs : Nested("accounts/catalogs") {
            val allhen = screen("allhen")
        }

        object Shikimori : Nested("accounts/shikimori") {
            val main = screen("main")
            val localItems = screen("local_items")
            val localItem = screen("local_item")
            val search = screen("shiki_search")
            val shikiItem = screen("shiki_search_item")
        }
    }

    const val downloader = "downloader"
    const val latest = "latest"
    const val settings = "settings"
}
