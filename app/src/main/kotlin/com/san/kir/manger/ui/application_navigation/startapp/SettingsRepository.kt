package com.san.kir.manger.ui.application_navigation.startapp

import com.san.kir.data.db.dao.SettingsDao
import com.san.kir.data.db.repositories.AbstractSettingsRepository
import javax.inject.Inject

class SettingsRepository @Inject constructor(
    settingsDao: SettingsDao
) : AbstractSettingsRepository(settingsDao) {

    suspend fun initFirstLaunch() {
        settingsDao.update(
            currentSettings().copy(
                isFirstLaunch = false
            )
        )
    }
}
