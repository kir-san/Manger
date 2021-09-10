package com.san.kir.manger.utils

import android.content.Context
import com.san.kir.manger.repositories.ChapterRepository
import com.san.kir.manger.room.entities.Chapter
import com.san.kir.manger.room.entities.Manga

class SearchDuplicate(context: Context) {
    private val mChapterRepository = ChapterRepository(context)

    suspend fun silentRemoveDuplicate(manga: Manga) {
        removeDuplicates(searchDuplicate(manga))
    }

    private suspend fun searchDuplicate(manga: Manga): MutableList<List<Chapter>> {
        val basicList = mChapterRepository.getItems(manga.unic)
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
            val first = chapterDuplicates.first()
            val last = chapterDuplicates.last()
            first.isRead = last.isRead
            first.progress = last.progress

            val removesChapters = chapterDuplicates - first
            mChapterRepository.delete(*removesChapters.toTypedArray())
            mChapterRepository.update(first)
        }
}
