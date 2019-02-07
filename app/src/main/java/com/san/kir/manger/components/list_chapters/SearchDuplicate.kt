package com.san.kir.manger.components.list_chapters

import android.content.Context
import com.san.kir.manger.repositories.ChapterRepository
import com.san.kir.manger.repositories.LatestChapterRepository
import com.san.kir.manger.room.models.Chapter
import com.san.kir.manger.room.models.Manga

class SearchDuplicate(context: Context) {
    private val mChapterRepository = ChapterRepository(context)
    private val mLatestChapterRepository = LatestChapterRepository(context)

    fun silentRemoveDuplicate(manga: Manga) {
        removeDuplicates(searchDuplicate(manga))
    }

    private fun searchDuplicate(manga: Manga): MutableList<List<Chapter>> {
        var list = mChapterRepository.getItems(manga.unic)
        val iterator = list.iterator()

        val allDuplicateList: MutableList<List<Chapter>> = mutableListOf()

        while (iterator.hasNext()) {
            var duplicateList: List<Chapter> = listOf()
            val chapter = iterator.next()
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

    private fun removeDuplicates(allDuplicateList: MutableList<List<Chapter>>) =
        allDuplicateList.forEach { chapterDuplicates ->
            val first = chapterDuplicates.first()
            val last = chapterDuplicates.last()
            first.isRead = last.isRead
            first.progress = last.progress

            val removesChapters = chapterDuplicates - first
            removesChapters.forEach { chapter ->
                val latests = mLatestChapterRepository.getItemsWhereLink(chapter.site)
                if (latests.isNotEmpty()) {
                    mLatestChapterRepository.delete(*latests.toTypedArray())
                }
            }
            mChapterRepository.delete(*removesChapters.toTypedArray())

            val latests = mLatestChapterRepository.getItemsWhereLink(first.site)
            if (latests.size > 1) {
                val deleting = latests - latests.first()
                mLatestChapterRepository.delete(*deleting.toTypedArray())
            }
            mChapterRepository.update(first)
        }
}
