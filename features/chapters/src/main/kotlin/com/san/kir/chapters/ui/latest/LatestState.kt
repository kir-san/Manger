package com.san.kir.chapters.ui.latest

import com.san.kir.core.utils.viewModel.ScreenState
import com.san.kir.data.models.main.SimplifiedChapter

internal data class LatestState(
    val newChapters: Int = 0,
    val hasBackgroundWork: Boolean = true,
    val itemsSize: Int = 0,
    val readSize: Int = 0,
    val downloadedSize: Int = 0,
) : ScreenState {
    val hasNewChapters = newChapters > 0
}

internal data class SelectionState(
    val selections: Set<Long> = emptySet()
) {
    val count = selections.size
    val enabled = count > 0

    fun hasItem(id: Long): Boolean = selections.contains(id)
    fun hasItems(ids: Collection<Long>): Boolean = selections.containsAll(ids)
}


internal data class MangaContainer(
    val manga: String,
    val chapters: List<SimplifiedChapter>
) {
    val itemsCount: Int = chapters.size
    val chaptersIds: List<Long> = chapters.map { it.id }
}


internal data class DateContainer(
    val date: String,
    val mangas: List<MangaContainer>
) {
    val chaptersCount: Int = mangas.sumOf { it.itemsCount }
    val chaptersIds: List<Long> = mangas.flatMap { it.chaptersIds }
}

