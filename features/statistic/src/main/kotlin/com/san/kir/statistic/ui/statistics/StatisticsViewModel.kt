package com.san.kir.statistic.ui.statistics

import com.san.kir.core.utils.viewModel.BaseViewModel
import com.san.kir.statistic.logic.repo.StatisticRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val statisticRepository: StatisticRepository
) : BaseViewModel<StatisticsEvent, StatisticsState>() {

    override val tempState =
        combine(statisticRepository.items, statisticRepository.allTime) { items, time ->
            StatisticsState(items.toPersistentList(), time)
        }

    override val defaultState = StatisticsState(persistentListOf(), 0)

    override suspend fun onEvent(event: StatisticsEvent) {
        when (event) {
            is StatisticsEvent.Delete -> statisticRepository.delete(event.itemId)
        }
    }
}
