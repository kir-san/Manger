package com.san.kir.manger.ui.application_navigation.additional_manga_screens

import androidx.lifecycle.ViewModel
import com.san.kir.manger.components.parsing.ManageSites
import com.san.kir.manger.components.parsing.SiteCatalogAlternative
import com.san.kir.manger.room.dao.CategoryDao
import com.san.kir.manger.room.dao.MangaDao
import com.san.kir.manger.room.dao.StatisticDao
import com.san.kir.manger.room.entities.Category
import com.san.kir.manger.room.entities.MangaStatistic
import com.san.kir.manger.room.entities.SiteCatalogElement
import com.san.kir.manger.room.entities.toManga
import com.san.kir.manger.utils.enums.DIR
import com.san.kir.manger.utils.extensions.createDirs
import com.san.kir.manger.utils.extensions.getFullPath
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.regex.Pattern
import javax.inject.Inject

@HiltViewModel
class MangaAddViewModel @Inject constructor(
    private val categoryDao: CategoryDao,
    private val mangaDao: MangaDao,
    private val statisticDao: StatisticDao,
) : ViewModel() {
    suspend fun getCategories() =
        withContext(Dispatchers.IO) {
            categoryDao.getItems().map { it.name }
        }

    suspend fun hasCategory(category: String): Boolean {
        return getCategories().any { it.contains(category) }
    }

    suspend fun addCategory(category: String) {
        categoryDao.insert(
            Category(
                name = category,
                order = getCategories().size + 1
            )
        )
    }

    suspend fun updateSiteElement(
        item: SiteCatalogElement,
        category: String
    ) = withContext(Dispatchers.IO) {
        val updatedElement = ManageSites.getFullElement(item)
        val pat = Pattern.compile("[a-z/0-9]+-").matcher(updatedElement.shotLink)
        var shortPath = item.shotLink
        if (pat.find())
            shortPath = item.shotLink.removePrefix(pat.group()).removeSuffix(".html")
        val path = "${DIR.MANGA}/${item.catalogName}/$shortPath"

        val manga = updatedElement.toManga(category = category, path = path)

        manga.isAlternativeSite = ManageSites.getSite(item.link) is SiteCatalogAlternative

        mangaDao.insert(manga)
        statisticDao.insert(MangaStatistic(manga = manga.unic))

        path to manga
    }

    fun createDirs(path: String): Boolean {
        return (getFullPath(path)).createDirs()
    }
}
