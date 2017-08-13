package com.san.kir.manger.dbflow.wrapers

import com.raizlabs.android.dbflow.kotlinextensions.`is`
import com.raizlabs.android.dbflow.kotlinextensions.from
import com.raizlabs.android.dbflow.kotlinextensions.list
import com.raizlabs.android.dbflow.kotlinextensions.select
import com.raizlabs.android.dbflow.kotlinextensions.where
import com.san.kir.manger.dbflow.models.Chapter
import com.san.kir.manger.dbflow.models.Chapter_Table.manga
import com.san.kir.manger.dbflow.models.LatestChapter
import kotlinx.coroutines.experimental.runBlocking

object ChapterWrapper {
    fun getChapters(unic: String): List<Chapter> {
        return (select from Chapter::class where (manga `is` unic)).list
    }

    fun asyncGetChapters(unic: String) = runBlocking {
        (select from Chapter::class where (manga `is` unic)).list
    }

    fun getLatestChapters(): List<LatestChapter> {
        return (select from LatestChapter::class).list
    }

    fun count(manga: String) = getChapters(manga).size

    fun countNotRead(manga: String) = getChapters(manga).filter { !it.isRead }.size

    fun asyncDelChapters(manga: String) = runBlocking {
        val list = asyncGetChapters(manga)
        list.forEach { it.delete() }
    }
}
