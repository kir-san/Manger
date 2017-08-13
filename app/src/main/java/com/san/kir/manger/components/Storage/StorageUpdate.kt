package com.san.kir.manger.components.Storage

import com.san.kir.manger.dbflow.models.Manga
import com.san.kir.manger.dbflow.wrapers.ChapterWrapper
import com.san.kir.manger.dbflow.wrapers.MangaWrapper
import com.san.kir.manger.utils.DIR
import com.san.kir.manger.utils.getFullPath
import com.san.kir.manger.utils.lengthMb
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch


object StorageUpdate {

    private var allSize: Long = 0
    private val mangaSizes = mutableMapOf<String, Long>()
    private val mangaReadSizes = mutableMapOf<String, Long>()


    fun onAllSize(listener: (Long) -> Unit) = launch(CommonPool) {
        listener.invoke(allSize)
        val newSize = async(CommonPool) { getFullPath(DIR.MANGA).lengthMb }.await()
        if (newSize != allSize) {
            allSize = newSize
            listener.invoke(allSize)
        }
    }


    fun onMangaSize(manga: Manga, listener: (Long) -> Unit) = launch(CommonPool) {
        listener.invoke(mangaSizes[manga.unic] ?: 0)
        val newSize = async(CommonPool) { getFullPath(manga.path).lengthMb }.await()
        if (newSize != mangaSizes[manga.unic]) {
            mangaSizes[manga.unic] = newSize
            listener.invoke(newSize)
        }
    }

    fun onMangaSize(manga: String, listener: (Long) -> Unit) {
        MangaWrapper.get(manga)?.let {
            onMangaSize(it, listener)
        }
    }


    fun onReadSize(manga: Manga, listener: (Long) -> Unit) {
        onReadSize(manga.unic, listener)
    }

    fun onReadSize(manga: String, listener: (Long) -> Unit) = launch(CommonPool) {
        listener.invoke(mangaReadSizes[manga] ?: 0)
        val newSize = async(CommonPool) {
            ChapterWrapper.getChapters(manga)
                    .filter { it.isRead }
                    .map { getFullPath(it.path) }
                    .map { it.lengthMb }
                    .sum()
        }.await()
        if (newSize != mangaReadSizes[manga]) {
            mangaReadSizes[manga] = newSize
            listener.invoke(newSize)
        }
    }

}
