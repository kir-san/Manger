package com.san.kir.features.shikimori.repositories

import com.san.kir.data.db.dao.ShikimoriDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class LibraryItemRepository @Inject constructor(
    private val shikimoriDao: ShikimoriDao,
) : ItemsRepository {
    override fun loadItems() = shikimoriDao.loadLibraryItems()

    override fun loadItemById(id: Long) = shikimoriDao.loadLibraryItemById(id)

    override suspend fun items() = loadItems().first()

    override suspend fun itemById(id: Long) = shikimoriDao.loadLibraryItemById(id).firstOrNull()
}
