package com.san.kir.manger.repositories

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.san.kir.manger.components.parsing.ManageSites
import com.san.kir.manger.components.parsing.SiteCatalogAlternative
import com.san.kir.manger.room.entities.Category
import com.san.kir.manger.room.entities.Manga
import com.san.kir.manger.room.entities.MangaStatistic
import com.san.kir.manger.room.entities.SiteCatalogElement
import com.san.kir.manger.room.entities.toManga
import com.san.kir.manger.room.getDatabase
import com.san.kir.manger.utils.CATEGORY_ALL
import com.san.kir.manger.utils.enums.DIR
import com.san.kir.manger.utils.enums.MangaFilter
import com.san.kir.manger.utils.extensions.createDirs
import com.san.kir.manger.utils.extensions.getFullPath
import java.io.File
import java.util.regex.Pattern

class MangaRepository(context: Context) {
    private val db = getDatabase(context)
    private val mMangaDao = db.mangaDao

    fun getItems(): List<Manga> {
        return mMangaDao.getItems()
    }

    suspend fun getItem(mangaUnic: String) = mMangaDao.getItem(mangaUnic)

    suspend fun getItemOrNull(unic: String) = mMangaDao.getItemOrNull(unic)

    suspend fun insert(vararg manga: Manga) = mMangaDao.insert(*manga)
    suspend fun update(vararg manga: Manga) = mMangaDao.update(*manga)
    suspend fun delete(vararg manga: Manga) = mMangaDao.delete(*manga)

    fun getFromPath(file: File): Manga? {
        return getItems().firstOrNull { getFullPath(it.path) == file }
    }

    fun getFromLogoUrl(url: String): Manga? {
        return getItems().firstOrNull { it.logo == url }
    }

    fun getItemsWhere(category: String): List<Manga> {
        return if (category == CATEGORY_ALL)
            getItems()
        else
            mMangaDao.loadMangaWhereCategoryNotAll(category)
    }

    fun contain(item: SiteCatalogElement): Boolean {
        return getItems().any { it.shortLink == item.shotLink }
    }

    fun loadItems(): LiveData<List<Manga>> {
        return mMangaDao.loadItems()
    }

    fun flowItems() = mMangaDao.flowItems()

}

