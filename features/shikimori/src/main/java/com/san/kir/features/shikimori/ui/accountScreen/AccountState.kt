package com.san.kir.features.shikimori.ui.accountScreen

import androidx.compose.runtime.Stable
import com.san.kir.core.utils.viewModel.ScreenState
import com.san.kir.data.models.base.ShikiDbManga
import com.san.kir.features.shikimori.logic.BackgroundTasks
import com.san.kir.features.shikimori.logic.useCases.BindStatus
import com.san.kir.features.shikimori.ui.accountItem.LoginState
import com.san.kir.features.shikimori.ui.util.DialogState

@Stable
internal data class AccountScreenState(
    val login: LoginState = LoginState.Loading,
    val dialog: DialogState = DialogState.Hide,
    val action: BackgroundTasks = BackgroundTasks(),
    val items: ScreenItems = ScreenItems(emptyList(), emptyList()),
) : ScreenState {
    override fun toString(): String {
        return "AccountScreenState(login=$login, dialog=$dialog, action=$action, items=ScreenItems(bindSize=${items.bind.count()}, unBindSize=${items.unBind.count()}))"
    }
}

@Stable
internal data class ScreenItems(
    val bind: List<ShikiDbManga>,
    val unBind: List<BindStatus<ShikiDbManga>>,
)
