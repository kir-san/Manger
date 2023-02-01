package com.san.kir.background.logic.repo

import com.san.kir.core.utils.coroutines.withIoContext
import com.san.kir.data.db.WorkersDb
import com.san.kir.data.models.base.MangaTask
import javax.inject.Inject

class MangaWorkerRepository @Inject constructor(
    private val db: WorkersDb.Instance,
) : BaseWorkerRepository<MangaTask> {

    override val catalog = db.mangas.loadItems()

    override suspend fun remove(item: MangaTask) {
        withIoContext { db.mangas.removeById(item.id) }
    }

    suspend fun remove(ids: List<Long>) = withIoContext { db.mangas.removeByIds(ids) }

    override suspend fun clear() = withIoContext { db.mangas.clear() }

    fun loadTask(mangaId: Long) = db.mangas.loadItemByMangaId(mangaId)
    suspend fun task(mangaId: Long) = withIoContext { db.mangas.itemByMangaId(mangaId) }
    suspend fun add(item: MangaTask) = withIoContext { db.mangas.insert(item) }
    suspend fun update(item: MangaTask) = withIoContext { db.mangas.update(item) }
}
