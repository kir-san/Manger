package com.san.kir.catalog.ui.addOnline

import com.san.kir.core.utils.viewModel.ScreenState
import kotlinx.collections.immutable.ImmutableList

internal data class AddOnlineState(
    val isCheckingUrl: Boolean,
    val validatesCatalogs: ImmutableList<String>,
    val isErrorAvailable: Boolean,
    val isEnableAdding: Boolean
) : ScreenState
