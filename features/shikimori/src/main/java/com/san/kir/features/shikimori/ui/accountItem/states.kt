package com.san.kir.features.shikimori.ui.accountItem

import com.san.kir.core.utils.viewModel.State

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

internal sealed interface DialogState {
    object Show : DialogState
    object Hide : DialogState
}
