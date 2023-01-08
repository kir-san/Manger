package com.san.kir.storage.ui.storage

import androidx.compose.runtime.Stable
import com.san.kir.core.utils.viewModel.ScreenState
import com.san.kir.data.models.base.Storage

internal data class StorageState(
    val background: BackgroundState = BackgroundState.None,
    val mangaName: String = "",
    val item: Storage = Storage(),
    val size: Double = 0.0,
) : ScreenState

@Stable
internal sealed interface BackgroundState {
    object None : BackgroundState
    object Load : BackgroundState
    object Deleting : BackgroundState
}
