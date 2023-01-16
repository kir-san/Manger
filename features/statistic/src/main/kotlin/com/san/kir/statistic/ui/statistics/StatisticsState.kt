package com.san.kir.statistic.ui.statistics

import com.san.kir.core.utils.viewModel.ScreenState
import com.san.kir.data.models.extend.SimplifiedStatistic
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

internal data class StatisticsState(
    val items: ImmutableList<SimplifiedStatistic> = persistentListOf(),
    val allTime: Long = 0,
) : ScreenState
