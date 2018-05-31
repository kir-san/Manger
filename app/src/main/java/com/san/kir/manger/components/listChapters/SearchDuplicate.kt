package com.san.kir.manger.components.listChapters

import com.san.kir.manger.components.main.Main
import com.san.kir.manger.room.models.Chapter
import com.san.kir.manger.room.models.Manga
import com.san.kir.manger.utils.log
import kotlinx.coroutines.experimental.async

object SearchDuplicate {
    fun silentRemoveDuplicate(manga: Manga) = async {
        removeDuplicates(searchDuplicate(manga))
    }

    private fun searchDuplicate(manga: Manga): MutableList<List<Chapter>> {
        var list = Main.db.chapterDao.loadChapters(manga.unic)
        val iterator = list.iterator()

        val allDuplicateList: MutableList<List<Chapter>> = mutableListOf()

        while (iterator.hasNext()) {
            var duplicateList: List<Chapter> = listOf()
            val chapter = iterator.next()
            var hasDuplicate = false
            list -= chapter
            list.forEach { current ->
                if (current.name == chapter.name) {
                    log("chapter ${chapter.name} is duplicate name")
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
                val latests = Main.db.latestChapterDao.loadChaptersWhereLink(chapter.site)
                if (latests.isNotEmpty()) {
                    Main.db.latestChapterDao.delete(*latests.toTypedArray())
                }
            }
            Main.db.chapterDao.delete(*removesChapters.toTypedArray())

            val latests = Main.db.latestChapterDao.loadChaptersWhereLink(first.site)
            if (latests.size > 1) {
                val deleting = latests - latests.first()
                Main.db.latestChapterDao.delete(*deleting.toTypedArray())
            }
            Main.db.chapterDao.update(first)
        }
}
