package com.san.kir.background.util

import com.san.kir.data.db.dao.ChapterDao
import com.san.kir.data.models.base.Chapter
import com.san.kir.data.models.base.Manga
import javax.inject.Inject

class SearchDuplicate @Inject constructor(
    private val chapterDao: ChapterDao,
) {
    suspend fun silentRemoveDuplicate(manga: Manga) {
        removeDuplicates(searchDuplicate(manga))
    }

    private suspend fun searchDuplicate(manga: Manga): MutableList<List<Chapter>> {
        val basicList = chapterDao.itemsByMangaId(manga.id)
        val list = basicList.toMutableList()

        val allDuplicateList: MutableList<List<Chapter>> = mutableListOf()

        basicList.forEach { chapter ->
            val duplicateList: MutableList<Chapter> = mutableListOf()
            var hasDuplicate = false

            list -= chapter
            list.forEach { current ->
                if (current.name == chapter.name) {
                    duplicateList += current
                    hasDuplicate = true
                }
            }

            if (hasDuplicate) {
                duplicateList += chapter
                allDuplicateList.add(duplicateList)
            }
        }

        return allDuplicateList
    }

    private suspend fun removeDuplicates(allDuplicateList: MutableList<List<Chapter>>) =
        allDuplicateList.forEach { chapterDuplicates ->
            val last = chapterDuplicates.last()
            val first = chapterDuplicates.first().copy(
                isRead = last.isRead, progress = last.progress
            )

            val removesChapters = chapterDuplicates - first
            chapterDao.delete(*removesChapters.toTypedArray())
            chapterDao.update(first)
        }
}
