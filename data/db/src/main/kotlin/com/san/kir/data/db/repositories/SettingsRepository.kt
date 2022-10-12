package com.san.kir.data.db.repositories

import com.san.kir.data.db.dao.SettingsDao
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.last

abstract class AbstractSettingsRepository(
    val settingsDao: SettingsDao
) {

    fun settings() = settingsDao.loadItems().filterNotNull()

    suspend fun currentSettings() = settings().last()

}
