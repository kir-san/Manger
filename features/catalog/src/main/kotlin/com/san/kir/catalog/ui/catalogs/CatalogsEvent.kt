package com.san.kir.catalog.ui.catalogs

import com.san.kir.core.utils.viewModel.ScreenEvent


internal sealed interface CatalogsEvent : ScreenEvent {
    object UpdateData : CatalogsEvent
    object UpdateContent : CatalogsEvent
}
