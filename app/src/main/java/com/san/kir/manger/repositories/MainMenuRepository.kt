package com.san.kir.manger.repositories

import android.content.Context
import com.san.kir.manger.room.entities.MainMenuItem
import com.san.kir.manger.room.getDatabase

class MainMenuRepository(context: Context) {
    private val db = getDatabase(context)
    private val mMainMenuDao = db.mainMenuDao

    suspend fun getItems() = mMainMenuDao.getItems()
    fun loadItems() = mMainMenuDao.loadItems()

    suspend fun insert(vararg menuItem: MainMenuItem) = mMainMenuDao.insert(*menuItem)
    suspend fun update(vararg menuItem: MainMenuItem) = mMainMenuDao.update(*menuItem)
    suspend fun delete(vararg menuItem: MainMenuItem) = mMainMenuDao.delete(*menuItem)
}
