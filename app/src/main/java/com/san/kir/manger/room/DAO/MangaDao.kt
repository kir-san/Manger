package com.san.kir.manger.room.DAO

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import com.san.kir.manger.room.models.Manga
import com.san.kir.manger.room.models.SiteCatalogElement
import com.san.kir.manger.utils.CATEGORY_ALL
import com.san.kir.manger.utils.getFullPath
import java.io.File

@Dao
interface MangaDao: BaseDao<Manga> {
    @Query("SELECT * FROM manga")
    fun loadAllManga(): List<Manga>

    @Query("SELECT * FROM manga WHERE unic IS :arg0")
    fun loadManga(unic: String): Manga

    @Query("SELECT * FROM manga WHERE categories IS :arg0")
    fun loadMangaWhereCategoryNotAll(category: String): List<Manga>
}

fun MangaDao.getFromPath(shortPath: String) =
        loadAllManga().firstOrNull { getFullPath(it.path) == getFullPath(shortPath) }

fun MangaDao.getFromPath(file: File) =
        loadAllManga().firstOrNull { getFullPath(it.path) == file }

fun MangaDao.loadMangaWhereCategory(category: String) =
        if (category == CATEGORY_ALL)
            loadAllManga()
        else
            loadMangaWhereCategoryNotAll(category)

fun MangaDao.contain(item: SiteCatalogElement) =
        loadAllManga().any { it.host == item.host && it.unic == item.name }

fun MangaDao.contain(catalog: List<Manga>, host: String, unic: String) =
        catalog.any { it.host == host && it.unic == unic }
