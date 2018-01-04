package com.san.kir.manger.room.DAO

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import com.san.kir.manger.components.Main.Main
import com.san.kir.manger.room.models.Category
import com.san.kir.manger.room.models.Manga
import com.san.kir.manger.room.models.SiteCatalogElement
import com.san.kir.manger.utils.CATEGORY_ALL
import com.san.kir.manger.utils.SortLibrary
import com.san.kir.manger.utils.SortLibraryUtil
import com.san.kir.manger.utils.getFullPath
import kotlinx.coroutines.experimental.async
import java.io.File

@Dao
interface MangaDao : BaseDao<Manga> {
    @Query("SELECT * FROM manga")
    fun loadAllManga(): List<Manga>

    @Query("SELECT * FROM manga WHERE unic IS :arg0")
    fun loadManga(unic: String): Manga

    @Query("SELECT * FROM manga WHERE categories IS :arg0")
    fun loadMangaWhereCategoryNotAll(category: String): List<Manga>


    @Query("SELECT * FROM manga ORDER BY id ASC")
    fun loadMangaAddTimeAsc(): LiveData<List<Manga>>

    @Query("SELECT * FROM manga ORDER BY id DESC")
    fun loadMangaAddTimeDesc(): LiveData<List<Manga>>

    @Query("SELECT * FROM manga ORDER BY name ASC")
    fun loadMangaAbcSortAsc(): LiveData<List<Manga>>

    @Query("SELECT * FROM manga ORDER BY name DESC")
    fun loadMangaAbcSortDesc(): LiveData<List<Manga>>

    @Query("SELECT * FROM manga WHERE categories IS :arg0 ORDER BY id ASC")
    fun loadMangaWithCategoryAddTimeAsc(category: String): LiveData<List<Manga>>

    @Query("SELECT * FROM manga WHERE categories IS :arg0 ORDER BY id DESC")
    fun loadMangaWithCategoryAddTimeDesc(category: String): LiveData<List<Manga>>

    @Query("SELECT * FROM manga WHERE categories IS :arg0 ORDER BY name ASC")
    fun loadMangaWithCategoryAbcSortAsc(category: String): LiveData<List<Manga>>

    @Query("SELECT * FROM manga WHERE categories IS :arg0 ORDER BY name DESC")
    fun loadMangaWithCategoryAbcSortDesc(category: String): LiveData<List<Manga>>

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

fun MangaDao.loadMangas(cat: Category, filter: MangaFilter): LiveData<List<Manga>> {
    return if (cat.name == CATEGORY_ALL) {
        when (filter) {
            MangaFilter.ADD_TIME_ASC -> loadMangaAddTimeAsc()
            MangaFilter.ABC_SORT_ASC -> loadMangaAddTimeDesc()
            MangaFilter.ADD_TIME_DESC -> loadMangaAbcSortAsc()
            MangaFilter.ABC_SORT_DESC -> loadMangaAbcSortDesc()
        }
    } else {
        when (filter) {
            MangaFilter.ADD_TIME_ASC -> loadMangaWithCategoryAddTimeAsc(cat.name)
            MangaFilter.ABC_SORT_ASC -> loadMangaWithCategoryAbcSortAsc(cat.name)
            MangaFilter.ADD_TIME_DESC -> loadMangaWithCategoryAddTimeDesc(cat.name)
            MangaFilter.ABC_SORT_DESC -> loadMangaWithCategoryAbcSortDesc(cat.name)
        }
    }
}

fun MangaDao.removeWithChapters(manga: Manga, withFiles: Boolean = false) = async {
    delete(manga)

    Main.db.chapterDao.removeChapters(manga.unic)

    if (withFiles) {
        getFullPath(manga.path).deleteRecursively()
    }
}

fun Category.toFilter(): MangaFilter {
    val type = SortLibraryUtil.toType(typeSort)
    return when {
        type == SortLibrary.AddTime && !isReverseSort -> MangaFilter.ADD_TIME_ASC
        type == SortLibrary.AddTime && isReverseSort -> MangaFilter.ADD_TIME_DESC
        type == SortLibrary.AbcSort && !isReverseSort -> MangaFilter.ABC_SORT_ASC
        type == SortLibrary.AbcSort && isReverseSort -> MangaFilter.ABC_SORT_DESC
        else -> MangaFilter.ADD_TIME_ASC
    }
}

fun Category.updateFilter(filter: MangaFilter): Category {
    when(filter) {
        MangaFilter.ADD_TIME_ASC -> {
            typeSort = SortLibraryUtil.toString(SortLibrary.AddTime)
            isReverseSort = false
        }
        MangaFilter.ABC_SORT_ASC -> {
            typeSort = SortLibraryUtil.toString(SortLibrary.AbcSort)
            isReverseSort = false
        }
        MangaFilter.ADD_TIME_DESC -> {
            typeSort = SortLibraryUtil.toString(SortLibrary.AddTime)
            isReverseSort = true
        }
        MangaFilter.ABC_SORT_DESC -> {
            typeSort = SortLibraryUtil.toString(SortLibrary.AbcSort)
            isReverseSort = true
        }
    }
    return this
}

enum class MangaFilter {
    ADD_TIME_ASC {
        override fun inverse() = ADD_TIME_DESC
    },
    ABC_SORT_ASC {
        override fun inverse() = ABC_SORT_DESC
    },
    ADD_TIME_DESC {
        override fun inverse() = ADD_TIME_ASC
    },
    ABC_SORT_DESC {
        override fun inverse() = ABC_SORT_ASC
    };

    abstract fun inverse(): MangaFilter
}
