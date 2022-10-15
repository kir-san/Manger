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
    private val chapters = settings().map { it.chapters }

    val showTitle = chapters.map { it.isTitle }
    val isIndividual = chapters.map { it.isIndividual }

    suspend fun update(newFilter: ChapterFilter) = withIoContext {
        settingsDao.update(
            currentSettings().copy(chapters = currentChapters().copy(filterStatus = newFilter))
        )
    }
}
