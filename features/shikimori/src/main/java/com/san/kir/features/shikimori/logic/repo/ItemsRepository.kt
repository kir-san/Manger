package com.san.kir.features.shikimori.logic.repo

import com.san.kir.data.models.base.ShikimoriMangaItem
import kotlinx.coroutines.flow.Flow

interface ItemsRepository {
    fun loadItems(): Flow<List<ShikimoriMangaItem>>

    fun loadItemById(id: Long): Flow<ShikimoriMangaItem?>

    suspend fun items(): List<ShikimoriMangaItem>

    suspend fun itemById(id: Long): ShikimoriMangaItem?
}
