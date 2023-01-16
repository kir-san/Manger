package com.san.kir.schedule.logic.repo

import com.san.kir.core.utils.coroutines.withIoContext
import com.san.kir.data.db.dao.CategoryDao
import com.san.kir.data.db.dao.MangaDao
import com.san.kir.data.db.dao.PlannedDao
import com.san.kir.data.models.base.PlannedTask
import com.san.kir.data.parsing.SiteCatalogsManager
import javax.inject.Inject

class TasksRepository @Inject constructor(
    private val plannedDao: PlannedDao,
    categoryDao: CategoryDao,
    mangaDao: MangaDao,
    manager: SiteCatalogsManager,
) {
    val items = plannedDao.loadSimpleItems()
    val categories = categoryDao.loadNamesAndIds()
    val mangas = mangaDao.loadNamesAndIds()
    val catalogs = manager.catalog.map { it.name }

    suspend fun item(itemId: Long) = withIoContext { plannedDao.itemById(itemId) }
    suspend fun update(id: Long, enable: Boolean) = withIoContext { plannedDao.update(id, enable) }
    suspend fun save(task: PlannedTask, isNew: Boolean) =
        withIoContext { if (isNew) plannedDao.insert(task) else plannedDao.update(task) }
}
