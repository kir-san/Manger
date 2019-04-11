package com.san.kir.manger.repositories

import android.arch.lifecycle.LiveData
import android.arch.paging.LivePagedListBuilder
import android.arch.paging.PagedList
import android.content.Context
import com.san.kir.manger.components.parsing.ManageSites
import com.san.kir.manger.room.getDatabase
import com.san.kir.manger.room.models.Category
import com.san.kir.manger.room.models.Manga
import com.san.kir.manger.room.models.MangaStatistic
import com.san.kir.manger.room.models.SiteCatalogElement
import com.san.kir.manger.room.models.toManga
import com.san.kir.manger.utils.CATEGORY_ALL
import com.san.kir.manger.utils.createDirs
import com.san.kir.manger.utils.enums.DIR
import com.san.kir.manger.utils.enums.MangaFilter
import com.san.kir.manger.utils.getFullPath
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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

    fun insert(vararg manga: Manga) = GlobalScope.launch { mMangaDao.insert(*manga) }
    fun update(vararg manga: Manga) = GlobalScope.launch { mMangaDao.update(*manga) }
    fun delete(vararg manga: Manga) = GlobalScope.launch { mMangaDao.delete(*manga) }

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
        return getItems().any { it.site == item.link }
    }

    fun loadItems(): LiveData<List<Manga>> {
        return mMangaDao.loadItems()
    }

    suspend fun addMangaToDb(
        element: SiteCatalogElement,
        category: String
    ): Manga {
        val pat = Pattern.compile("[a-z/0-9]+-").matcher(element.shotLink)
        if (pat.find())
            element.shotLink = element.shotLink
                .removePrefix(pat.group()).removeSuffix(".html")
        val path = "${DIR.MANGA}/${element.catalogName}/${element.shotLink}"
        createDirs(getFullPath(path))

        val updatingElement = ManageSites.getFullElement(element).await()

        val manga = updatingElement.toManga(category = category, path = path)

        manga.isAlternativeSite = ManageSites.getSite(element.link) is SiteCatalogAlternative

        mMangaDao.insert(manga)
        mStatisticDao.insert(MangaStatistic(manga = manga.unic))
        return manga


    }
}

