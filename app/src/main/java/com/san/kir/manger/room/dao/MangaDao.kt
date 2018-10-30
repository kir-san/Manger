package com.san.kir.manger.room.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import com.san.kir.manger.components.main.Main
import com.san.kir.manger.room.models.Category
import com.san.kir.manger.room.models.Manga
import com.san.kir.manger.room.models.MangaColumn
import com.san.kir.manger.room.models.SiteCatalogElement
import com.san.kir.manger.utils.CATEGORY_ALL
import com.san.kir.manger.utils.SortLibrary
import com.san.kir.manger.utils.SortLibraryUtil
import com.san.kir.manger.utils.getFullPath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

@Dao
interface MangaDao : BaseDao<Manga> {
    @Query("SELECT * FROM `${MangaColumn.tableName}`")
    fun loadAllManga(): List<Manga>

    @Query("SELECT * FROM `${MangaColumn.tableName}` WHERE `${MangaColumn.unic}` IS :unic")
    fun loadManga(unic: String): Manga

    @Query("SELECT * FROM `${MangaColumn.tableName}` WHERE `${MangaColumn.unic}` IS :unic")
    fun loadMangaOrNull(unic: String): Manga?

    @Query("SELECT * FROM `${MangaColumn.tableName}` WHERE `${MangaColumn.categories}` IS :category")
    fun loadMangaWhereCategoryNotAll(category: String): List<Manga>

    @Query("SELECT * FROM `${MangaColumn.tableName}` ORDER BY `${MangaColumn.id}` ASC")
    fun loadMangaAddTimeAsc(): LiveData<List<Manga>>

    @Query("SELECT * FROM `${MangaColumn.tableName}` ORDER BY `${MangaColumn.id}` DESC")
    fun loadMangaAddTimeDesc(): LiveData<List<Manga>>

    @Query("SELECT * FROM `${MangaColumn.tableName}` ORDER BY `${MangaColumn.name}` ASC")
    fun loadMangaAbcSortAsc(): LiveData<List<Manga>>

    @Query("SELECT * FROM `${MangaColumn.tableName}` ORDER BY `${MangaColumn.name}` DESC")
    fun loadMangaAbcSortDesc(): LiveData<List<Manga>>

    @Query("SELECT * FROM `${MangaColumn.tableName}` ORDER BY `${MangaColumn.populate}` DESC")
    fun loadMangaPopulateAsc(): LiveData<List<Manga>>

    @Query("SELECT * FROM `${MangaColumn.tableName}` ORDER BY `${MangaColumn.populate}` ASC")
    fun loadMangaPopulateDesc(): LiveData<List<Manga>>

    @Query("SELECT * FROM `${MangaColumn.tableName}` WHERE `${MangaColumn.categories}` IS :category ORDER BY `${MangaColumn.id}` ASC")
    fun loadMangaWithCategoryAddTimeAsc(category: String): LiveData<List<Manga>>

    @Query("SELECT * FROM `${MangaColumn.tableName}` WHERE `${MangaColumn.categories}` IS :category ORDER BY `${MangaColumn.id}` DESC")
    fun loadMangaWithCategoryAddTimeDesc(category: String): LiveData<List<Manga>>

    @Query("SELECT * FROM `${MangaColumn.tableName}` WHERE `${MangaColumn.categories}` IS :category ORDER BY `${MangaColumn.name}` ASC")
    fun loadMangaWithCategoryAbcSortAsc(category: String): LiveData<List<Manga>>

    @Query("SELECT * FROM `${MangaColumn.tableName}` WHERE `${MangaColumn.categories}` IS :category ORDER BY `${MangaColumn.name}` DESC")
    fun loadMangaWithCategoryAbcSortDesc(category: String): LiveData<List<Manga>>

    @Query("SELECT * FROM `${MangaColumn.tableName}` WHERE `${MangaColumn.categories}` IS :category ORDER BY `${MangaColumn.populate}` DESC")
    fun loadMangaWithCategoryPopulateAsc(category: String): LiveData<List<Manga>>

    @Query("SELECT * FROM `${MangaColumn.tableName}` WHERE `${MangaColumn.categories}` IS :category ORDER BY `${MangaColumn.populate}` ASC")
    fun loadMangaWithCategoryPopulateDesc(category: String): LiveData<List<Manga>>
}

fun MangaDao.getFromPath(shortPath: String) = getFromPath(getFullPath(shortPath))

fun MangaDao.getFromPath(file: File) =
    loadAllManga().firstOrNull { getFullPath(it.path) == file }

fun MangaDao.loadMangaWhereCategory(category: String) =
    if (category == CATEGORY_ALL)
        loadAllManga()
    else
        loadMangaWhereCategoryNotAll(category)

fun MangaDao.contain(item: SiteCatalogElement) =
    loadAllManga().any { it.site == item.link }

fun MangaDao.loadMangas(cat: Category, filter: MangaFilter): LiveData<List<Manga>> {
    return if (cat.name == CATEGORY_ALL) {
        when (filter) {
            MangaFilter.ADD_TIME_ASC -> loadMangaAddTimeAsc()
            MangaFilter.ADD_TIME_DESC -> loadMangaAddTimeDesc()
            MangaFilter.ABC_SORT_ASC -> loadMangaAbcSortAsc()
            MangaFilter.ABC_SORT_DESC -> loadMangaAbcSortDesc()
            MangaFilter.POPULATE_ASC -> loadMangaPopulateAsc()
            MangaFilter.POPULATE_DESC -> loadMangaPopulateDesc()
        }
    } else {
        when (filter) {
            MangaFilter.ADD_TIME_ASC -> loadMangaWithCategoryAddTimeAsc(cat.name)
            MangaFilter.ABC_SORT_ASC -> loadMangaWithCategoryAbcSortAsc(cat.name)
            MangaFilter.ADD_TIME_DESC -> loadMangaWithCategoryAddTimeDesc(cat.name)
            MangaFilter.ABC_SORT_DESC -> loadMangaWithCategoryAbcSortDesc(cat.name)
            MangaFilter.POPULATE_ASC -> loadMangaWithCategoryPopulateAsc(cat.name)
            MangaFilter.POPULATE_DESC -> loadMangaWithCategoryPopulateDesc(cat.name)
        }
    }
}

fun MangaDao.removeWithChapters(manga: Manga, withFiles: Boolean = false) =
    GlobalScope.launch(Dispatchers.Default) {
        delete(manga)

        Main.db.chapterDao.removeChapters(manga.unic)

        Main.db.latestChapterDao.removeChapters(manga.unic)

        if (withFiles) {
            getFullPath(manga.path).deleteRecursively()
        }
    }

fun Category.toFilter(): MangaFilter {
    return when (SortLibraryUtil.toType(typeSort)) {
        SortLibrary.AddTime ->
            if (isReverseSort) {
                MangaFilter.ADD_TIME_DESC
            } else {
                MangaFilter.ADD_TIME_ASC
            }
        SortLibrary.AbcSort ->
            if (isReverseSort) {
                MangaFilter.ABC_SORT_DESC
            } else {
                MangaFilter.ABC_SORT_ASC
            }
        SortLibrary.Populate ->
            if (isReverseSort) {
                MangaFilter.POPULATE_DESC
            } else {
                MangaFilter.POPULATE_ASC
            }
    }
}

enum class MangaFilter {
    ADD_TIME_ASC,
    ADD_TIME_DESC,
    ABC_SORT_ASC,
    ABC_SORT_DESC,
    POPULATE_ASC,
    POPULATE_DESC,
}
