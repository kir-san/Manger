package com.san.kir.statistic.ui.statistics

import com.san.kir.core.utils.viewModel.ScreenState
import com.san.kir.data.models.extend.SimplifiedStatistic
import kotlinx.collections.immutable.ImmutableList

data class StatisticsState(
    val items: ImmutableList<SimplifiedStatistic>,
    val allTime: Long
) : ScreenState
