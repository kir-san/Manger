package com.san.kir.core.download

import com.san.kir.data.db.dao.SettingsDao
import com.san.kir.data.db.repositories.AbstractSettingsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.mapLatest
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsRepository @Inject constructor(
    settingsDao: SettingsDao
) : AbstractSettingsRepository(settingsDao) {

    fun download() = settings().mapLatest { it.download }
}
