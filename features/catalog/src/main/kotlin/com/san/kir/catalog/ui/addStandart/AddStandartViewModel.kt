package com.san.kir.catalog.ui.addStandart

import com.san.kir.background.logic.UpdateMangaManager
import com.san.kir.catalog.logic.repo.CatalogRepository
import com.san.kir.core.utils.DIR
import com.san.kir.core.utils.viewModel.BaseViewModel
import com.san.kir.data.models.base.Category
import com.san.kir.data.models.base.Statistic
import com.san.kir.data.models.base.toManga
import com.san.kir.data.parsing.SiteCatalogAlternative
import com.san.kir.data.parsing.SiteCatalogsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import timber.log.Timber
import java.util.regex.Pattern
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
internal class AddStandartViewModel @Inject constructor(
    private val catalogRepository: CatalogRepository,
    private val manager: SiteCatalogsManager,
    private val updateManager: UpdateMangaManager,
) : BaseViewModel<AddStandartEvent, AddStandartState>() {
    private var url = ""
    private val categoryName = MutableStateFlow("")
    private val processState = MutableStateFlow<ProcessState>(ProcessState.None)
    private val progress = MutableStateFlow(0)

    override val tempState = combine(
        categoryName, catalogRepository.categoryNames, processState, progress,
    ) { category, categories, process, progress ->

        val filteredCategories = categories.filter { category in it }.toPersistentList()

        AddStandartState(
            categoryName = category,
            hasAllow = category.length >= 3,
            availableCategories = filteredCategories,
            createNewCategory = filteredCategories.size != 1 || filteredCategories.first() != category,
            processState = process,
            progress = progress
        )
    }

    override val defaultState = AddStandartState()

    override suspend fun onEvent(event: AddStandartEvent) {
        when (event) {
            is AddStandartEvent.Set -> url = event.url
            is AddStandartEvent.UpdateText -> categoryName.update { event.text }
            AddStandartEvent.StartProcess -> startProcess()
        }
    }

    private suspend fun startProcess()  {
        kotlin.runCatching {
            processState.update { ProcessState.Load }

            if (state.value.createNewCategory) {
                catalogRepository.insert(Category(name = categoryName.value))
                delay(1.seconds)
            }

            progress.update { ProcessStatus.categoryChanged }

            progress.update { ProcessStatus.prevAndUpdateManga }

            val element = manager.elementByUrl(url) ?: throw NullPointerException()
            val matcher = Pattern.compile("[a-z/0-9]+-").matcher(element.shortLink)
            var shortPath = element.shortLink
            if (matcher.find())
                shortPath = element.shortLink.removePrefix(matcher.group()).removeSuffix(".html")

            val path = "${DIR.MANGA}/${element.catalogName}/$shortPath"
            val mangaId = catalogRepository.insert(
                element.toManga(
                    categoryId = catalogRepository.categoryId(categoryName.value),
                    path = path
                ).copy(
                    isAlternativeSite = manager.getSite(element.link) is SiteCatalogAlternative
                )
            ).ifEmpty { throw ArrayIndexOutOfBoundsException() }.first()

            catalogRepository.insert(Statistic(mangaId = mangaId))

            progress.update { ProcessStatus.prevAndCreatedFolder }

            delay(1.seconds)

            progress.update { ProcessStatus.prevAndSearchChapters }

            updateManager.addTask(mangaId)
            delay(1.seconds)

            progress.update { ProcessStatus.allComplete }

        }.onFailure {
            processState.update { ProcessState.Error }
            Timber.e(it)
        }.onSuccess {
            processState.update { ProcessState.Complete }
        }
    }

}
