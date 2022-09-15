package com.san.kir.features.shikimori.logic.repo

import com.san.kir.core.utils.coroutines.withIoContext
import com.san.kir.data.db.dao.ShikimoriDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class LibraryItemRepository @Inject constructor(
    private val shikimoriDao: ShikimoriDao,
) : ItemsRepository {
    override fun loadItems() = shikimoriDao.loadLibraryItems()

    override fun loadItemById(id: Long) = shikimoriDao.loadLibraryItemById(id)

    override suspend fun items() = withIoContext { loadItems().first() }

    override suspend fun itemById(id: Long) =
        withIoContext { shikimoriDao.loadLibraryItemById(id).firstOrNull() }
}
