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
    fun getItems(): List<Manga>

    @Query("SELECT * FROM `${MangaColumn.tableName}` WHERE `${MangaColumn.unic}` IS :unic")
    fun getItem(unic: String): Manga

    @Query("SELECT * FROM `${MangaColumn.tableName}` WHERE `${MangaColumn.unic}` IS :unic")
    fun getItemOrNull(unic: String): Manga?

    @Query("SELECT * FROM `${MangaColumn.tableName}` WHERE `${MangaColumn.categories}` IS :category")
    fun getItems(category: String): List<Manga>

    @Query("SELECT * FROM `${MangaColumn.tableName}` ORDER BY :order ASC")
    fun loadItemsAscBy(order: String): LiveData<List<Manga>>

    @Query("SELECT * FROM `${MangaColumn.tableName}` ORDER BY :order DESC")
    fun loadItemsDescBy(order: String): LiveData<List<Manga>>

    @Query("SELECT * FROM `${MangaColumn.tableName}`  WHERE `${MangaColumn.categories}` IS :category ORDER BY :order ASC")
    fun loadItemsAscBy(order: String, category: String): LiveData<List<Manga>>

    @Query("SELECT * FROM `${MangaColumn.tableName}`  WHERE `${MangaColumn.categories}` IS :category ORDER BY :order DESC")
    fun loadItemsDescBy(order: String, category: String): LiveData<List<Manga>>
}

fun MangaDao.getFromPath(shortPath: String) = getFromPath(getFullPath(shortPath))

fun MangaDao.getFromPath(file: File) =
    getItems().firstOrNull { getFullPath(it.path) == file }

fun MangaDao.getItemsWhere(category: String) =
    if (category == CATEGORY_ALL)
        getItems()
    else
        getItems(category)

fun MangaDao.contain(item: SiteCatalogElement) =
    getItems().any { it.site == item.link }

fun MangaDao.loadItems(cat: Category, filter: MangaFilter): LiveData<List<Manga>> {
    return if (cat.name == CATEGORY_ALL) {
        when (filter) {
            MangaFilter.ADD_TIME_ASC -> loadItemsAscBy(MangaColumn.id)
            MangaFilter.ADD_TIME_DESC -> loadItemsDescBy(MangaColumn.id)
            MangaFilter.ABC_SORT_ASC -> loadItemsAscBy(MangaColumn.name)
            MangaFilter.ABC_SORT_DESC -> loadItemsDescBy(MangaColumn.name)
            MangaFilter.POPULATE_ASC -> loadItemsAscBy(MangaColumn.populate)
            MangaFilter.POPULATE_DESC -> loadItemsDescBy(MangaColumn.populate)
        }
    } else {
        when (filter) {
            MangaFilter.ADD_TIME_ASC -> loadItemsAscBy(MangaColumn.id, cat.name)
            MangaFilter.ADD_TIME_DESC -> loadItemsDescBy(MangaColumn.id, cat.name)
            MangaFilter.ABC_SORT_ASC -> loadItemsAscBy(MangaColumn.name, cat.name)
            MangaFilter.ABC_SORT_DESC -> loadItemsDescBy(MangaColumn.name, cat.name)
            MangaFilter.POPULATE_ASC -> loadItemsAscBy(MangaColumn.populate, cat.name)
            MangaFilter.POPULATE_DESC -> loadItemsDescBy(MangaColumn.populate, cat.name)
        }
    }
}

fun MangaDao.removeWithChapters(manga: Manga, withFiles: Boolean = false) =
    GlobalScope.launch(Dispatchers.Default) {
        delete(manga)

        Main.db.chapterDao.deleteItems(manga.unic)

        Main.db.latestChapterDao.deleteItems(manga.unic)

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
