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
    private val mStatisticDao = db.statisticDao

    fun getItems(): List<Manga> {
        return mMangaDao.getItems()
    }

    fun getItem(mangaUnic: String): Manga {
        return mMangaDao.getItem(mangaUnic)
    }

    fun getItemOrNull(unic: String): Manga? {
        return mMangaDao.getItemOrNull(unic)
    }

    suspend fun insert(vararg manga: Manga) =  mMangaDao.insert(*manga)
    suspend fun update(vararg manga: Manga) =  mMangaDao.update(*manga)
    suspend fun delete(vararg manga: Manga) =  mMangaDao.delete(*manga)

    fun loadMangas(cat: Category, filter: MangaFilter): LiveData<PagedList<Manga>> {
        val source = if (cat.name == CATEGORY_ALL) {
            when (filter) {
                MangaFilter.ADD_TIME_ASC -> mMangaDao.loadMangaAddTimeAsc()
                MangaFilter.ADD_TIME_DESC -> mMangaDao.loadMangaAddTimeDesc()
                MangaFilter.ABC_SORT_ASC -> mMangaDao.loadMangaAbcSortAsc()
                MangaFilter.ABC_SORT_DESC -> mMangaDao.loadMangaAbcSortDesc()
                MangaFilter.POPULATE_ASC -> mMangaDao.loadMangaPopulateAsc()
                MangaFilter.POPULATE_DESC -> mMangaDao.loadMangaPopulateDesc()
            }
        } else {
            when (filter) {
                MangaFilter.ADD_TIME_ASC -> mMangaDao.loadMangaWithCategoryAddTimeAsc(cat.name)
                MangaFilter.ABC_SORT_ASC -> mMangaDao.loadMangaWithCategoryAbcSortAsc(cat.name)
                MangaFilter.ADD_TIME_DESC -> mMangaDao.loadMangaWithCategoryAddTimeDesc(cat.name)
                MangaFilter.ABC_SORT_DESC -> mMangaDao.loadMangaWithCategoryAbcSortDesc(cat.name)
                MangaFilter.POPULATE_ASC -> mMangaDao.loadMangaWithCategoryPopulateAsc(cat.name)
                MangaFilter.POPULATE_DESC -> mMangaDao.loadMangaWithCategoryPopulateDesc(cat.name)
            }
        }
        return LivePagedListBuilder(source, 50).build()
    }

    fun getFromPath(shortPath: String): Manga? {
        return getFromPath(getFullPath(shortPath))
    }

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

    suspend fun addMangaToDb(
        element: SiteCatalogElement,
        category: String
    ): Manga {
        val updatingElement = ManageSites.getFullElement(element)

        val pat = Pattern.compile("[a-z/0-9]+-").matcher(updatingElement.shotLink)
        var shortPath = element.shotLink
        if (pat.find())
            shortPath = element.shotLink.removePrefix(pat.group()).removeSuffix(".html")
        val path = "${DIR.MANGA}/${element.catalogName}/$shortPath"
        (getFullPath(path)).createDirs()

        val manga = updatingElement.toManga(category = category, path = path)

        manga.isAlternativeSite = ManageSites.getSite(element.link) is SiteCatalogAlternative

        mMangaDao.insert(manga)
        mStatisticDao.insert(MangaStatistic(manga = manga.unic))
        return manga
    }
}

