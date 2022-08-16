package com.san.kir.features.shikimori.ui.accountItem

import com.san.kir.core.utils.viewModel.State
import com.san.kir.features.shikimori.ui.util.DialogState

internal data class ScreenState(
    val login: LoginState,
    val dialog: DialogState,
) : State

internal sealed interface LoginState {
    object Loading : LoginState
    data class LogIn(val nickName: String) : LoginState
    object LogOut : LoginState
    object Error : LoginState
}


