package com.san.kir.features.shikimori.ui.catalog_item

import com.san.kir.data.models.base.Manga
import com.san.kir.data.models.base.ShikiManga
import com.san.kir.data.models.extend.SimplefiedMangaWithChapterCounts

sealed interface LocalSearch {
    class Sync(val manga: Manga) : LocalSearch
    object NoSearch : LocalSearch
    object Searching : LocalSearch
    class Founds(val items: List<SimplefiedMangaWithChapterCounts>) : LocalSearch
    class NotFounds(val name: String) : LocalSearch
}

sealed interface Dialog {
    object None : Dialog

    class DifferentChapterCount(
        val manga: SimplefiedMangaWithChapterCounts,
        val local: Long,
        val online: Long,
    ) : Dialog

    class DifferentReadCount(
        val manga: SimplefiedMangaWithChapterCounts,
        val local: Long,
        val online: Long,
    ) : Dialog

    object CancelSync : Dialog
}
