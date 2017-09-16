package com.san.kir.manger.dbflow.wrapers

import com.raizlabs.android.dbflow.kotlinextensions.`is`
import com.raizlabs.android.dbflow.kotlinextensions.from
import com.raizlabs.android.dbflow.kotlinextensions.list
import com.raizlabs.android.dbflow.kotlinextensions.select
import com.raizlabs.android.dbflow.kotlinextensions.where
import com.raizlabs.android.dbflow.sql.language.From
import com.san.kir.manger.dbflow.models.Manga
import com.san.kir.manger.dbflow.models.Manga_Table
import com.san.kir.manger.dbflow.models.Manga_Table.categories
import com.san.kir.manger.dbflow.models.Manga_Table.unic
import com.san.kir.manger.dbflow.models.SiteCatalogElement
import com.san.kir.manger.utils.categoryAll
import com.san.kir.manger.utils.getFullPath

object MangaWrapper {
    private fun getAll() = (select from Manga::class)
    suspend private fun asyncGetAll(): From<Manga> {
        return select from Manga::class
    }

    fun getAllManga(): List<Manga> = getAll().list
    suspend fun asyncGetAllManga(): List<Manga> {
        return asyncGetAll().list
    }

    fun get(unicName: String) = (getAll() where (unic `is` unicName)).querySingle()
    suspend fun asyncGet(unic: String): Manga? {
        return (asyncGetAll() where (Manga_Table.unic `is` unic)).querySingle()
    }

    fun getFromPath(shortPath: String) = getAllManga().firstOrNull {
        getFullPath(it.path) == getFullPath(shortPath)
    }

    /*fun getAllWithCategories(category: String): MutableList<Manga> {
        if (category == categoryAll)
            return getAllManga().toMutableList()
        return (getAll() where (categories `is` category)).list
    }*/

    suspend fun asyncGetAllWithCategories(category: String): MutableList<Manga> {
        return if (category == categoryAll)
            asyncGetAllManga().toMutableList()
        else
            (asyncGetAll() where (categories `is` category)).list
    }

    fun getAllPath(): List<String> {
        val pathList = mutableListOf<String>()
        getAll().list.forEach { pathList.add(it.path) }
        return pathList
    }

    suspend fun asyncContain(item: SiteCatalogElement): Boolean {
        asyncGetAllManga().forEach {
            if (it.host == item.host && it.unic == item.name)
                return true
        }
        return false
    }

    fun contain(catalog: List<Manga>, host: String, unic: String): Boolean {
        catalog.forEach {
            if (it.host == host && it.unic == unic)
                return true
        }
        return false
    }
}
