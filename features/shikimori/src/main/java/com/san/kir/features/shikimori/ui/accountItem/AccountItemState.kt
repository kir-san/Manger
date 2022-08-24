package com.san.kir.features.shikimori.ui.accountItem

import androidx.compose.runtime.Stable
import com.san.kir.core.utils.viewModel.ScreenState
import com.san.kir.features.shikimori.ui.util.DialogState

@Stable
internal data class AccountItemState(
    val login: LoginState,
    val dialog: DialogState,
) : ScreenState

internal sealed interface LoginState {
    object Loading : LoginState
    data class LogIn(val nickName: String) : LoginState
    object LogOut : LoginState
    object Error : LoginState
}


