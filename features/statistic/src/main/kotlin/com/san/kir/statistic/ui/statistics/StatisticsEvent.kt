package com.san.kir.statistic.ui.statistics

import com.san.kir.core.utils.viewModel.ScreenEvent

sealed interface StatisticsEvent : ScreenEvent {
    data class Delete(val itemId: Long) : StatisticsEvent
}
