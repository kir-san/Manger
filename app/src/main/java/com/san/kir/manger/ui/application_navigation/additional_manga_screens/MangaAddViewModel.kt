package com.san.kir.manger.ui.application_navigation.additional_manga_screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.san.kir.data.parsing.SiteCatalogAlternative
import com.san.kir.data.parsing.SiteCatalogsManager
import com.san.kir.manger.data.room.entities.Category
import com.san.kir.manger.data.room.entities.MangaStatistic
import com.san.kir.manger.data.room.entities.SiteCatalogElement
import com.san.kir.manger.data.room.entities.toManga
import com.san.kir.core.utils.coroutines.withMainContext
import com.san.kir.core.support.DIR
import com.san.kir.manger.utils.extensions.createDirs
import com.san.kir.manger.utils.extensions.getFullPath
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.regex.Pattern
import javax.inject.Inject

@HiltViewModel
class MangaAddViewModel @Inject constructor(
    private val categoryDao: com.san.kir.data.db.dao.CategoryDao,
    private val mangaDao: com.san.kir.data.db.dao.MangaDao,
    private val statisticDao: com.san.kir.data.db.dao.StatisticDao,
    private val manager: com.san.kir.data.parsing.SiteCatalogsManager,
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
                com.san.kir.core.utils.coroutines.withMainContext {
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
        category: String,
    ) = withContext(Dispatchers.IO) {
        manager.getElementOnline(url)?.let { element ->
            val pat = Pattern.compile("[a-z/0-9]+-").matcher(element.shotLink)
            var shortPath = element.shotLink
            if (pat.find())
                shortPath = element.shotLink.removePrefix(pat.group()).removeSuffix(".html")
            val path = "${com.san.kir.core.support.DIR.MANGA}/${element.catalogName}/$shortPath"

            val manga = element.toManga(category = category, path = path)

            manga.isAlternativeSite = manager.getSite(element.link) is com.san.kir.data.parsing.SiteCatalogAlternative
            mangaDao.insert(manga)
            statisticDao.insert(MangaStatistic(manga = manga.name))
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
