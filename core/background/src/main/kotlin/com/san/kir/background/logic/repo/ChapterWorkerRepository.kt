package com.san.kir.background.logic.repo

import com.san.kir.core.utils.coroutines.withIoContext
import com.san.kir.data.db.WorkersDb
import com.san.kir.data.models.base.ChapterTask
import javax.inject.Inject

class ChapterWorkerRepository @Inject constructor(
    private val db: WorkersDb.Instance,
) : BaseWorkerRepository<ChapterTask> {

    override val catalog = db.chapters.loadItems()

    override suspend fun remove(item: ChapterTask) {
        withIoContext { db.chapters.removeById(item.id) }
    }

    override suspend fun clear() = withIoContext { db.chapters.clear() }

    fun loadTask(chapterId: Long) = db.chapters.loadItemByChapterId(chapterId)
    suspend fun task(chapterId: Long) = withIoContext { db.chapters.itemByChapterId(chapterId) }
    suspend fun add(item: ChapterTask) = withIoContext { db.chapters.insert(item) }
    suspend fun update(item: ChapterTask) = withIoContext { db.chapters.update(item) }
    suspend fun remove(items: List<ChapterTask>) =
        withIoContext { db.chapters.removeByIds(items.map { it.id }) }
}
