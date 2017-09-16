package com.san.kir.manger.components.Storage

import android.arch.lifecycle.ViewModel
import com.san.kir.manger.dbflow.models.Manga
import com.san.kir.manger.dbflow.wrapers.MangaWrapper
import com.san.kir.manger.utils.getFullPath
import com.san.kir.manger.utils.getShortPath
import java.io.File

private object Storage {
    // данные о занимаемом месте всей мангой что есть в папках приложения
    val allSize by lazy { StorageAllSizeLiveData() }

    // данные о занимаемом месте выбранной папки
    val dirSizeMap = hashMapOf<File, StorageDirSizeLiveData>()

    // данные о занимаемом месте прочитанных глав конкретной манги
    val readSizeMap = hashMapOf<String, StorageReadSizeLiveData?>()
}

class StorageViewModel : ViewModel() {
    val allSize get() = Storage.allSize

    fun dirSize(manga: Manga) = dirSize(manga.path)
    fun dirSize(shortPath: String) = dirSize(getFullPath(shortPath))
    fun dirSize(file: File): StorageDirSizeLiveData {
        if (!Storage.dirSizeMap.contains(file))
            Storage.dirSizeMap[file] = StorageDirSizeLiveData(file)
        return Storage.dirSizeMap[file] as StorageDirSizeLiveData
    }

    fun readSize(manga: Manga) = readSize(manga.unic)
    fun readSize(mangaName: String): StorageReadSizeLiveData {
        if (!Storage.readSizeMap.contains(mangaName))
            Storage.readSizeMap[mangaName] = StorageReadSizeLiveData(
                    mangaName)
        return Storage.readSizeMap[mangaName] as StorageReadSizeLiveData
    }

    fun readSize(file: File): StorageReadSizeLiveData? {
        MangaWrapper.getFromPath(getShortPath(file))?.let {
            return readSize(it)
        } ?: return null
    }
}
