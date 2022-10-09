package com.san.kir.statistic.ui.statistic

import com.san.kir.core.utils.viewModel.ScreenState
import com.san.kir.data.models.base.Statistic

internal data class StatisticState(
    val item: Statistic,
    val mangaName: String
) : ScreenState
