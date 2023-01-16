package com.san.kir.features.catalogs.allhen.ui.allhen

import com.san.kir.core.utils.viewModel.ScreenEvent

internal sealed interface AllhenItemEvent : ScreenEvent {
    data object Update : AllhenItemEvent
}
