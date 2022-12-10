package com.san.kir.catalog.ui.catalogs

import androidx.compose.runtime.Stable
import com.san.kir.core.utils.viewModel.ScreenState
import kotlinx.collections.immutable.ImmutableList


internal data class CatalogsState(
    val items: ImmutableList<CheckableSite>,
    val background: Boolean,
) : ScreenState

@Stable
internal data class CheckableSite(
    val name: String,
    val host: String,
    val volume: VolumeState,
    val state: SiteState,
)

internal sealed interface VolumeState {
    object Load : VolumeState
    object Error : VolumeState
    data class Ok(val volume: Int, val diff: Int) : VolumeState
}

internal sealed interface SiteState {
    object Load : SiteState
    object Error : SiteState
    object Ok : SiteState
}
