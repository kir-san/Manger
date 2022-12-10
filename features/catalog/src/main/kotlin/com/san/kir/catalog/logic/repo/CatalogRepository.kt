package com.san.kir.catalog.logic.repo

import com.san.kir.core.utils.coroutines.withIoContext
import com.san.kir.data.db.CatalogDb
import com.san.kir.data.db.dao.CategoryDao
import com.san.kir.data.db.dao.MangaDao
import com.san.kir.data.db.dao.StatisticDao
import com.san.kir.data.models.base.Category
import com.san.kir.data.models.base.Manga
import com.san.kir.data.models.base.SiteCatalogElement
import com.san.kir.data.models.base.Statistic
import com.san.kir.data.models.extend.MiniCatalogItem
import com.san.kir.data.parsing.SiteCatalogsManager
import javax.inject.Inject

internal class CatalogRepository @Inject constructor(
    private val manager: SiteCatalogsManager,
    private val catalogFactory: CatalogDb.Factory,
    private val mangaDao: MangaDao,
    private val categoryDao: CategoryDao,
    private val statisticDao: StatisticDao,
) {
    private var links = listOf<String>()
    val items = manager.catalog

    suspend fun volume(catalogName: String) = kotlin.runCatching {
        val db = catalogFactory.create(manager.catalogName(catalogName))
        db.dao.itemsCount().apply {
            db.close()
        }
    }.onFailure { it.printStackTrace() }

    suspend fun items(name: String): List<MiniCatalogItem> = withIoContext {
        val db = catalogFactory.create(manager.catalogName(name))
        db.dao.miniItems()
            .onEach {
                it.state =
                    if (checkContains(it.shotLink)) MiniCatalogItem.State.Update
                    else MiniCatalogItem.State.Added
            }
            .apply { db.close() }
    }

    suspend fun item(name: String, id: Long): SiteCatalogElement = withIoContext {
        val db = catalogFactory.create(manager.catalogName(name))
        db.dao.itemById(id).apply { db.close() }
    }

    suspend fun checkContains(shortLink: String): Boolean {
        if (links.isEmpty()) links = withIoContext { mangaDao.links() }
        return links.any { it.contains(shortLink) }
    }

    suspend fun updateMangaBy(item: MiniCatalogItem) = withIoContext {
        val oldManga = mangaDao.items().first { it.shortLink.contains(item.shotLink) }
        val updatedItem = fullElement(item(item.catalogName, item.id))
        mangaDao.update(
            oldManga.copy(
                authorsList = updatedItem.authors,
                logo = updatedItem.logo,
                about = updatedItem.about,
                genresList = updatedItem.genres,
                host = updatedItem.host,
                shortLink = updatedItem.shortLink,
                status = updatedItem.statusEdition,
            )
        )
    }

    private suspend fun fullElement(element: SiteCatalogElement) = manager.getFullElement(element)
    suspend fun insert(item: Category) = withIoContext { categoryDao.insert(item) }
    suspend fun insert(item: Manga) = withIoContext { mangaDao.insert(item) }
    suspend fun insert(item: Statistic) = withIoContext { statisticDao.insert(item) }

    val categoryNames = categoryDao.loadNames()
    suspend fun categoryId(name: String) = withIoContext { categoryDao.itemByName(name).id }
}
