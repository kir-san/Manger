package com.san.kir.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.san.kir.data.models.Site
import kotlinx.coroutines.flow.Flow

@Dao
interface SiteDao : BaseDao<Site> {

    @Query("SELECT * FROM sites")
    suspend fun getItems(): List<Site>

    @Query("SELECT * FROM sites WHERE name is :name")
    fun getItem(name: String): Site?

    @Query("SELECT * FROM sites")
    fun loadItems(): Flow<List<Site>>
}
