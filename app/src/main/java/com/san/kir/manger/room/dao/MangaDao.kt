package com.san.kir.manger.room.dao

import android.arch.lifecycle.LiveData
import android.arch.paging.DataSource
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import com.san.kir.manger.room.models.Manga
import com.san.kir.manger.room.models.MangaColumn

@Dao
interface MangaDao : BaseDao<Manga> {
    @Query("SELECT * FROM `${MangaColumn.tableName}`")
    fun getItems(): List<Manga>

    @Query("SELECT * FROM `${MangaColumn.tableName}` WHERE `${MangaColumn.unic}` IS :unic")
    fun getItem(unic: String): Manga

    @Query("SELECT * FROM `${MangaColumn.tableName}` WHERE `${MangaColumn.unic}` IS :unic")
    fun getItemOrNull(unic: String): Manga?

    @Query("SELECT * FROM `${MangaColumn.tableName}` WHERE `${MangaColumn.categories}` IS :category")
    fun loadMangaWhereCategoryNotAll(category: String): List<Manga>

    @Query("SELECT * FROM `${MangaColumn.tableName}`")
    fun loadItems(): LiveData<List<Manga>>

    @Query("SELECT * FROM `${MangaColumn.tableName}` ORDER BY `${MangaColumn.id}` ASC")
    fun loadMangaAddTimeAsc(): DataSource.Factory<Int, Manga>

    @Query("SELECT * FROM `${MangaColumn.tableName}` ORDER BY `${MangaColumn.id}` DESC")
    fun loadMangaAddTimeDesc(): DataSource.Factory<Int, Manga>

    @Query("SELECT * FROM `${MangaColumn.tableName}` ORDER BY `${MangaColumn.name}` ASC")
    fun loadMangaAbcSortAsc(): DataSource.Factory<Int, Manga>

    @Query("SELECT * FROM `${MangaColumn.tableName}` ORDER BY `${MangaColumn.name}` DESC")
    fun loadMangaAbcSortDesc(): DataSource.Factory<Int, Manga>

    @Query("SELECT * FROM `${MangaColumn.tableName}` ORDER BY `${MangaColumn.populate}` DESC")
    fun loadMangaPopulateAsc(): DataSource.Factory<Int, Manga>

    @Query("SELECT * FROM `${MangaColumn.tableName}` ORDER BY `${MangaColumn.populate}` ASC")
    fun loadMangaPopulateDesc(): DataSource.Factory<Int, Manga>

    @Query("SELECT * FROM `${MangaColumn.tableName}` WHERE `${MangaColumn.categories}` IS :category ORDER BY `${MangaColumn.id}` ASC")
    fun loadMangaWithCategoryAddTimeAsc(category: String): DataSource.Factory<Int, Manga>

    @Query("SELECT * FROM `${MangaColumn.tableName}` WHERE `${MangaColumn.categories}` IS :category ORDER BY `${MangaColumn.id}` DESC")
    fun loadMangaWithCategoryAddTimeDesc(category: String): DataSource.Factory<Int, Manga>

    @Query("SELECT * FROM `${MangaColumn.tableName}` WHERE `${MangaColumn.categories}` IS :category ORDER BY `${MangaColumn.name}` ASC")
    fun loadMangaWithCategoryAbcSortAsc(category: String): DataSource.Factory<Int, Manga>

    @Query("SELECT * FROM `${MangaColumn.tableName}` WHERE `${MangaColumn.categories}` IS :category ORDER BY `${MangaColumn.name}` DESC")
    fun loadMangaWithCategoryAbcSortDesc(category: String): DataSource.Factory<Int, Manga>

    @Query("SELECT * FROM `${MangaColumn.tableName}` WHERE `${MangaColumn.categories}` IS :category ORDER BY `${MangaColumn.populate}` DESC")
    fun loadMangaWithCategoryPopulateAsc(category: String): DataSource.Factory<Int, Manga>

    @Query("SELECT * FROM `${MangaColumn.tableName}` WHERE `${MangaColumn.categories}` IS :category ORDER BY `${MangaColumn.populate}` ASC")
    fun loadMangaWithCategoryPopulateDesc(category: String): DataSource.Factory<Int, Manga>
}
