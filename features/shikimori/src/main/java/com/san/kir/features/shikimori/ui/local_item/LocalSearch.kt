package com.san.kir.features.shikimori.ui.local_item

import com.san.kir.data.models.base.ShikiManga

sealed interface LocalSearch {
    class Sync(val manga: ShikiManga) : LocalSearch
    object NoSearch : LocalSearch
    object Searching : LocalSearch
    class Founds(val items: List<ShikiManga>) : LocalSearch
    class NotFounds(val name: String) : LocalSearch
}

sealed interface Dialog {
    object None : Dialog

    class DifferentChapterCount(
        val manga: ShikiManga,
        val local: Long,
        val online: Long,
    ) : Dialog

    class DifferentReadCount(
        val manga: ShikiManga,
        val local: Long,
        val online: Long,
    ) : Dialog

    object CancelSync : Dialog
}
