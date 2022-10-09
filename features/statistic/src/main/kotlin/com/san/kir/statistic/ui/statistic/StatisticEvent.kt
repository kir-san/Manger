package com.san.kir.statistic.ui.statistic

import com.san.kir.core.utils.viewModel.ScreenEvent

internal sealed interface StatisticEvent : ScreenEvent {
    data class Set(val itemId: Long) : StatisticEvent
}
