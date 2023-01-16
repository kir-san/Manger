package com.san.kir.catalog.ui.addOnline

import com.san.kir.core.utils.viewModel.ScreenState
import kotlinx.collections.immutable.ImmutableList

internal data class AddOnlineState(
    val isCheckingUrl: Boolean = false,
    val validatesCatalogs: ImmutableList<String>,
    val isErrorAvailable: Boolean = false,
    val isEnableAdding: Boolean = false,
) : ScreenState
