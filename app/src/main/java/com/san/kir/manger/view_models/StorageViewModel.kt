package com.san.kir.manger.view_models

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.paging.PagedList
import com.san.kir.manger.repositories.MangaRepository
import com.san.kir.manger.repositories.StorageRepository
import com.san.kir.manger.room.models.Manga
import com.san.kir.manger.room.models.Storage

class StorageViewModel(app: Application) : AndroidViewModel(app) {
    private val mStorageRepository = StorageRepository(app)
    private val mMangaRepository = MangaRepository(app)

    fun getStorageItems(): LiveData<List<Storage>> {
        return mStorageRepository.loadItems()
    }

    fun getStoragePagedItems(): LiveData<PagedList<Storage>> {
        return mStorageRepository.loadPagedItems()
    }

    fun getMangaFromPath(path: String): Manga? {
        return mMangaRepository.getFromPath(path)
    }

    fun getStorageAllSize(): LiveData<Double> {
        return mStorageRepository.loadAllSize()
    }

    fun storageDelete(item: Storage) {
        mStorageRepository.delete(item)
    }
}
