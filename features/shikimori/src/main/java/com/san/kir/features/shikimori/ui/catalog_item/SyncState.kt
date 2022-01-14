package com.san.kir.features.shikimori.ui.catalog_item

import com.san.kir.data.models.base.Manga
import com.san.kir.data.models.base.ShikimoriAccount

// Состояние связывания
sealed interface SyncState {
    class Ok(val manga: Manga) : SyncState
    object NoFind : SyncState
    object Find : SyncState
    class Founds(val items: List<ShikimoriAccount.AbstractMangaItem>) : SyncState
    class NotFounds(val name: String) : SyncState
}

// Запросы при выполнении связывания
sealed interface AskState {
    object None : AskState

    class DifferentChapterCount(
        val manga: ShikimoriAccount.AbstractMangaItem,
        val local: Long,
        val online: Long,
    ) : AskState

    class DifferentReadCount(
        val manga: ShikimoriAccount.AbstractMangaItem,
        val local: Long,
        val online: Long,
    ) : AskState

    object CancelSync : AskState
}
