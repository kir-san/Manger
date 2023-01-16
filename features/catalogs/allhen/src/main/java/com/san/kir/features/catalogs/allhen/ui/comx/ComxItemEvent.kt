package com.san.kir.features.catalogs.allhen.ui.comx

import com.san.kir.core.utils.viewModel.ScreenEvent

internal sealed interface ComxItemEvent : ScreenEvent {
    data object Update : ComxItemEvent
}
