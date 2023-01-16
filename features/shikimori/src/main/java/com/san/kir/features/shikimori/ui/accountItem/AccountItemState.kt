package com.san.kir.features.shikimori.ui.accountItem

import androidx.compose.runtime.Stable
import com.san.kir.core.utils.viewModel.ScreenState
import com.san.kir.features.shikimori.ui.util.DialogState

@Stable
internal data class AccountItemState(
    val login: LoginState = LoginState.Loading,
    val dialog: DialogState = DialogState.Hide,
) : ScreenState

internal sealed class LoginState(val nickName: String) {
    data object Loading : LoginState("")
    class LogInOk(nickName: String) : LoginState(nickName)
    class LogInCheck(nickName: String) : LoginState(nickName)
    class LogInError(nickName: String) : LoginState(nickName)
    data object LogOut : LoginState("")
    data object Error : LoginState("")
}
