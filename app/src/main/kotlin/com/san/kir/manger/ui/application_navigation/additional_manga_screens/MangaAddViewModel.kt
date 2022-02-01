package com.san.kir.manger.ui.application_navigation.additional_manga_screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.san.kir.core.utils.coroutines.withMainContext
import com.san.kir.core.utils.createDirs
import com.san.kir.core.utils.getFullPath
import com.san.kir.data.db.dao.CategoryDao
import com.san.kir.data.db.dao.MangaDao
import com.san.kir.data.db.dao.StatisticDao
import com.san.kir.data.models.base.Category
import com.san.kir.data.models.base.Statistic
import com.san.kir.data.models.base.SiteCatalogElement
import com.san.kir.data.models.base.toManga
import com.san.kir.data.parsing.SiteCatalogAlternative
import com.san.kir.data.parsing.SiteCatalogsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import java.util.regex.Pattern
import javax.inject.Inject

@HiltViewModel
class MangaAddViewModel @Inject constructor(
    private val categoryDao: CategoryDao,
    private val mangaDao: MangaDao,
    private val statisticDao: StatisticDao,
    private val manager: SiteCatalogsManager,
) : ViewModel() {

    var state by mutableStateOf(
        ViewState(
            inputText = "",
            activateContinue = false,
            newChapter = false,
            categories = listOf(),
            validateCategories = listOf(),
            catalogElement = SiteCatalogElement(),
        )
    )
        private set

    init {
        categoryDao
            .loadItems()
            .distinctUntilChanged()
            .onEach { list ->
                withMainContext {
                    state = state.copy(
                        categories = list.map { cat -> cat.name },
                        validateCategories = list.map { cat -> cat.name }
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun changeText(newText: String) {
        state = state.copy(inputText = newText)
        val temp = validate(newText, state.categories)
        state = state.copy(validateCategories = temp)
    }

    fun hasCategory(category: String): Boolean {
        return state.categories.any { it.contains(category) }
    }

    suspend fun addCategory(category: String) {
        categoryDao.insert(
            Category(
                name = category,
                order = state.categories.size + 1
            )
        )
    }

    suspend fun updateSiteElement(
        url: String,
        categoryName: String,
    ) = withContext(Dispatchers.IO) {
        manager.getElementOnline(url)?.let { element ->
            val pat = Pattern.compile("[a-z/0-9]+-").matcher(element.shotLink)
            var shortPath = element.shotLink
            if (pat.find())
                shortPath = element.shotLink.removePrefix(pat.group()).removeSuffix(".html")
            val path = "${com.san.kir.core.support.DIR.MANGA}/${element.catalogName}/$shortPath"

            val category = categoryDao.itemByName(categoryName)

            val manga = element.toManga(categoryId = category.id, path = path)

            manga.isAlternativeSite = manager.getSite(element.link) is SiteCatalogAlternative
            mangaDao.insert(manga)
            statisticDao.insert(Statistic(manga = manga.name))
            return@withContext path to manga
        }
    }

    fun createDirs(path: String): Boolean {
        return (getFullPath(path)).createDirs()
    }

    private fun validate(
        text: String,
        categories: List<String>,
    ): List<String> {
        state = state.copy(activateContinue = text.length >= 3)

        return if (text.isNotBlank()) {
            // список категорий подходящих под введенное
            val temp = categories.filter { it.contains(text) }
            state = state.copy(newChapter = !(temp.size == 1 && temp.first() == text))
            temp
        } else {
            // Если нет текста, то отображается список
            // доступных сайтов
            state = state.copy(newChapter = true)
            categories
        }
    }

    data class ViewState(
        val inputText: String,
        val activateContinue: Boolean,
        val newChapter: Boolean,
        val categories: List<String>,
        val validateCategories: List<String>,
        val catalogElement: SiteCatalogElement,
    )
}
