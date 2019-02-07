package com.san.kir.manger.repositories

import android.content.Context
import com.san.kir.manger.room.getDatabase
import com.san.kir.manger.room.models.MainMenuItem
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainMenuRepository(context: Context) {
    private val db = getDatabase(context)
    private val mMainMenuDao = db.mainMenuDao

    fun getItems(): List<MainMenuItem> {
        return mMainMenuDao.getItems()
    }

    fun insert(vararg menuItem: MainMenuItem) = GlobalScope.launch { mMainMenuDao.insert(*menuItem) }
    fun update(vararg menuItem: MainMenuItem) = GlobalScope.launch { mMainMenuDao.update(*menuItem) }
    fun delete(vararg menuItem: MainMenuItem) = GlobalScope.launch { mMainMenuDao.delete(*menuItem) }
}
