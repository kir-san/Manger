package com.san.kir.statistic.ui.statistic

import com.san.kir.core.utils.viewModel.BaseViewModel
import com.san.kir.data.models.base.Statistic
import com.san.kir.statistic.logic.repo.StatisticRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
internal class StatisticViewModel @Inject constructor(
    private val statisticRepository: StatisticRepository
) : BaseViewModel<StatisticEvent, StatisticState>() {
    private val statistic = MutableStateFlow(Statistic())
    private val mangaName = MutableStateFlow("")

    override val tempState = combine(statistic, mangaName, ::StatisticState)

    override val defaultState = StatisticState(Statistic(), "")

    override suspend fun onEvent(event: StatisticEvent) {
        when (event) {
            is StatisticEvent.Set -> set(event.itemId)
        }
    }

    private suspend fun set(itemId: Long) {
        val item = statisticRepository.item(itemId)
        Timber.v(item.toString())
        statistic.value = item
        mangaName.value = statisticRepository.mangaName(statistic.value.mangaId).name
    }
}
