package com.san.kir.chapters.logic.repo

import com.san.kir.core.support.ChapterFilter
import com.san.kir.core.utils.coroutines.withIoContext
import com.san.kir.data.db.dao.SettingsDao
import com.san.kir.data.db.repositories.AbstractSettingsRepository
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SettingsRepository @Inject constructor(
    settingsDao: SettingsDao
) : AbstractSettingsRepository(settingsDao) {

    suspend fun currentChapters() = currentSettings().chapters

    val wifi = settings().map { it.download.wifi }
    val showTitle = settings().map { it.chapters.isTitle }
    val isIndividual = settings().map { it.chapters.isIndividual }

    suspend fun update(newFilter: ChapterFilter) = withIoContext {
        settingsDao.update(
            currentSettings().copy(chapters = currentChapters().copy(filterStatus = newFilter))
        )
    }
}
