package com.san.kir.schedule.ui.updates

import com.san.kir.core.utils.viewModel.ScreenState
import com.san.kir.data.models.extend.MiniManga
import kotlinx.collections.immutable.ImmutableList


internal data class UpdatesState(
    val items: ImmutableList<MiniManga>
) : ScreenState
