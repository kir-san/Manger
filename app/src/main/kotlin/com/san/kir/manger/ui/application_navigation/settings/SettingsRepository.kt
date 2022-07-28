package com.san.kir.manger.ui.application_navigation.settings

import com.san.kir.data.db.dao.SettingsDao
import com.san.kir.data.db.repositories.AbstractSettingsRepository
import com.san.kir.data.models.base.Settings
import javax.inject.Inject

class SettingsRepository @Inject constructor(
    settingsDao: SettingsDao
) : AbstractSettingsRepository(settingsDao) {

    suspend fun currentChapters() = currentSettings().chapters
    suspend fun currentViewer() = currentSettings().viewer
    suspend fun currentDownload() = currentSettings().download
    suspend fun currentMain() = currentSettings().main

    suspend fun setTitle(titleVisibility: Boolean) {
        settingsDao.update(
            currentSettings().copy(
                chapters = currentChapters().copy(
                    isTitle = titleVisibility
                )
            )
        )
    }

    suspend fun setFilter(filter: Boolean) {
        settingsDao.update(
            currentSettings().copy(
                chapters = currentChapters().copy(
                    isIndividual = filter
                )
            )
        )
    }

    suspend fun setTheme(theme: Boolean) {
        settingsDao.update(
            currentSettings().copy(
                main = currentMain().copy(
                    theme = theme
                )
            )
        )
    }

    suspend fun setShowCategory(show: Boolean) {
        settingsDao.update(
            currentSettings().copy(
                main = currentMain().copy(
                    isShowCategory = show
                )
            )
        )
    }

    suspend fun setEditMenu(edit: Boolean) {
        settingsDao.update(
            currentSettings().copy(
                main = currentMain().copy(
                    editMenu = edit
                )
            )
        )
    }

    suspend fun setConcurrent(concurrent: Boolean) {
        settingsDao.update(
            currentSettings().copy(
                download = currentDownload().copy(
                    concurrent = concurrent
                )
            )
        )
    }

    suspend fun setRetry(retry: Boolean) {
        settingsDao.update(
            currentSettings().copy(
                download = currentDownload().copy(
                    retry = retry
                )
            )
        )
    }

    suspend fun setWifi(wifi: Boolean) {
        settingsDao.update(
            currentSettings().copy(
                download = currentDownload().copy(
                    wifi = wifi
                )
            )
        )
    }

    suspend fun setOrientation(orientation: Settings.Viewer.Orientation) {
        settingsDao.update(
            currentSettings().copy(
                viewer = currentViewer().copy(
                    orientation = orientation
                )
            )
        )
    }

    suspend fun setCutOut(cutout: Boolean) {
        settingsDao.update(
            currentSettings().copy(
                viewer = currentViewer().copy(
                    cutOut = cutout
                )
            )
        )
    }

    suspend fun setWithoutSaveFiles(without: Boolean) {
        settingsDao.update(
            currentSettings().copy(
                viewer = currentViewer().copy(
                    withoutSaveFiles = without
                )
            )
        )
    }

    suspend fun setControl(taps: Boolean, swipes: Boolean, keys: Boolean) {
        settingsDao.update(
            currentSettings().copy(
                viewer = currentViewer().copy(
                    control = Settings.Viewer.Control(taps, swipes, keys)
                )
            )
        )
    }
}
