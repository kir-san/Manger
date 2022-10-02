package com.san.kir.categories.ui.categories

import com.san.kir.categories.logic.repo.CategoryRepository
import com.san.kir.core.utils.viewModel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
) : BaseViewModel<CategoriesEvent, CategoriesState>() {

    override val tempState = categoryRepository.items.map { CategoriesState(it.toPersistentList()) }

    override val defaultState = CategoriesState(persistentListOf())

    override suspend fun onEvent(event: CategoriesEvent) {
        when (event) {
            is CategoriesEvent.Reorder -> categoryRepository.swap(event.from, event.to)
            is CategoriesEvent.ChangeVisibility -> {
                categoryRepository.update(event.item.copy(isVisible = event.newState))
            }
        }
    }
}
