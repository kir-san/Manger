package com.san.kir.features.catalogs.allhen.ui.accountItem

import androidx.compose.runtime.Stable
import com.san.kir.core.utils.viewModel.ScreenState

@Stable
internal data class AccountItemState(
    val login: LoginState = LoginState.Loading,
) : ScreenState

internal sealed interface LoginState {
    data object Loading : LoginState
    data object NonLogIn : LoginState
    data class LogIn(val nickName: String, val avatar: String) : LoginState
    data object Error : LoginState
}


