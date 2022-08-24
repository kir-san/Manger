package com.san.kir.features.shikimori.ui.accountScreen

import androidx.compose.runtime.Stable
import com.san.kir.core.utils.viewModel.ScreenState
import com.san.kir.data.models.base.ShikiDbManga
import com.san.kir.features.shikimori.BackgroundTasks
import com.san.kir.features.shikimori.ui.accountItem.LoginState
import com.san.kir.features.shikimori.ui.util.DialogState
import com.san.kir.features.shikimori.useCases.BindStatus

@Stable
internal data class AccountScreenState(
    val login: LoginState,
    val dialog: DialogState,
    val action: BackgroundTasks,
    val items: ScreenItems
) : ScreenState

@Stable
internal data class ScreenItems(
    val bind: List<ShikiDbManga>,
    val unBind:  List<BindStatus<ShikiDbManga>>
)
