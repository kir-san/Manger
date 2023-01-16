package com.san.kir.statistic.ui.statistics

import com.san.kir.core.utils.viewModel.ScreenEvent

internal sealed interface StatisticsEvent : ScreenEvent {
    data class Delete(val itemId: Long) : StatisticsEvent
}
