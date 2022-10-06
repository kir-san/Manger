package com.san.kir.storage.ui.storage

import androidx.compose.runtime.Stable
import com.san.kir.core.utils.viewModel.ScreenState
import com.san.kir.data.models.base.Storage

data class StorageState(
    val background: BackgroundState,
    val mangaName: String,
    val item: Storage,
    val size: Double,
) : ScreenState

@Stable
sealed interface BackgroundState {
    object None : BackgroundState
    object Load : BackgroundState
    object Deleting : BackgroundState
}
