package com.san.kir.schedule.ui.updates

import com.san.kir.core.utils.viewModel.BaseViewModel
import com.san.kir.schedule.logic.repo.UpdatesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
internal class UpdatesViewModel @Inject constructor(
    private val updatesRepository: UpdatesRepository,
) : BaseViewModel<UpdatesEvent, UpdatesState>() {
    override val tempState = updatesRepository.items.map { UpdatesState(it.toPersistentList()) }

    override val defaultState = UpdatesState(persistentListOf())

    override suspend fun onEvent(event: UpdatesEvent) {
        when (event) {
            is UpdatesEvent.Update -> updatesRepository.update(event.itemId, event.updateState)
        }
    }
}
