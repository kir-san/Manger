package com.san.kir.features.catalogs.allhen.ui.allhen

import androidx.compose.runtime.Stable
import com.san.kir.core.utils.viewModel.ScreenState

@Stable
internal data class AllhenItemState(
    val login: LoginState = LoginState.Loading,
) : ScreenState

internal sealed interface LoginState {
    data object Loading : LoginState
    data object NonLogIn : LoginState
    data class LogIn(val nickName: String, val avatar: String) : LoginState
    data object Error : LoginState
}


