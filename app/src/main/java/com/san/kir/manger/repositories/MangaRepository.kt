package com.san.kir.manger.repositories

import android.content.Context
import com.san.kir.manger.room.entities.Manga
import com.san.kir.manger.room.getDatabase
import com.san.kir.manger.utils.CATEGORY_ALL
import com.san.kir.manger.utils.extensions.getFullPath
import java.io.File

class MangaRepository(context: Context) {
    private val db = getDatabase(context)
    private val mMangaDao = db.mangaDao

    suspend fun getItems(): List<Manga> {
        return mMangaDao.getItems()
    }

    suspend fun getItem(mangaUnic: String) = mMangaDao.getItem(mangaUnic)

    suspend fun insert(vararg manga: Manga) = mMangaDao.insert(*manga)
    suspend fun update(vararg manga: Manga) = mMangaDao.update(*manga)
    suspend fun delete(vararg manga: Manga) = mMangaDao.delete(*manga)

    suspend fun getFromPath(file: File): Manga? {
        return getItems().firstOrNull { getFullPath(it.path) == file }
    }

    suspend fun getItemsWhere(category: String): List<Manga> {
        return if (category == CATEGORY_ALL)
            getItems()
        else
            mMangaDao.getMangaWhereCategoryNotAll(category)
    }

}

