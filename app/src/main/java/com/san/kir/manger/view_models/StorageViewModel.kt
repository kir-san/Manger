package com.san.kir.manger.view_models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.san.kir.manger.repositories.MangaRepository
import com.san.kir.manger.repositories.StorageRepository
import com.san.kir.manger.room.entities.Manga
import com.san.kir.manger.room.entities.Storage

class StorageViewModel(app: Application) : AndroidViewModel(app) {
    private val mStorageRepository = StorageRepository(app)
    private val mMangaRepository = MangaRepository(app)

    fun getStorageItems(): LiveData<List<Storage>> {
        return mStorageRepository.loadItems()
    }

    fun flowItems() = mStorageRepository.flowItems()
    fun flowAllSize() = mStorageRepository.flowAllSize()

    fun getMangaFromPath(path: String): Manga? {
        return mMangaRepository.getFromPath(path)
    }

    suspend fun allSize() = mStorageRepository.items().map { it.sizeFull }.sum()
    suspend fun storageDelete(item: Storage) = mStorageRepository.delete(item)
}
