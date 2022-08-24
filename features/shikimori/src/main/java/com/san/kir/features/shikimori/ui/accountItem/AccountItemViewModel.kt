package com.san.kir.features.shikimori.ui.accountItem

import android.app.Application
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.viewModelScope
import com.san.kir.core.utils.flow.Result
import com.san.kir.core.utils.flow.asResult
import com.san.kir.core.utils.viewModel.BaseViewModel
import com.san.kir.features.shikimori.AuthActivity
import com.san.kir.features.shikimori.CODE
import com.san.kir.features.shikimori.authCodeStore
import com.san.kir.features.shikimori.ui.util.DialogState
import com.san.kir.features.shikimori.useCases.AuthUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
internal class AccountItemViewModel @Inject internal constructor(
    private val app: Application,
    private val authUseCase: AuthUseCase,
) : BaseViewModel<AccountItemEvent, AccountItemState>() {
    private val loginState = MutableStateFlow<LoginState>(LoginState.Loading)
    private val dialogState = MutableStateFlow<DialogState>(DialogState.Hide)

    init {
        authUseCase.authData.asResult()
            .map { auth ->
                when (auth) {
                    is Result.Error -> LoginState.Error
                    Result.Loading -> LoginState.Loading
                    is Result.Success -> {
                        if (auth.data.isLogin) {
                            LoginState.LogIn(auth.data.nickName)
                        } else {
                            LoginState.LogOut
                        }
                    }
                }
            }
            .onEach { state -> loginState.update { state } }
            .launchIn(viewModelScope)

        // ОЖидание получения кода, для старта авторизации
        app.authCodeStore.data
            .mapNotNull { it[CODE] }
            .onEach {
                authUseCase.login(it)
                // После использования, избавляемся от кода
                app.authCodeStore.edit { settings -> settings.clear() }
            }
            .launchIn(viewModelScope)
    }

    override val tempState =
        combine(loginState, dialogState) { login, dialog ->
            AccountItemState(login, dialog)
        }

    override val defaultState =
        AccountItemState(
            login = LoginState.Loading,
            dialog = DialogState.Hide
        )

    override suspend fun onEvent(event: AccountItemEvent) {
        when (event) {
            AccountItemEvent.LogIn -> {
                loginState.update { LoginState.Loading }
                AuthActivity.start(app)
            }
            AccountItemEvent.LogOut -> {
                when (dialogState.value) {
                    DialogState.Hide -> {
                        dialogState.update { DialogState.Show }
                    }
                    DialogState.Show -> {
                        dialogState.update { DialogState.Hide }
                        loginState.update { LoginState.Loading }
                        authUseCase.logout()
                    }
                }
            }
            AccountItemEvent.CancelLogOut -> {
                when (dialogState.value) {
                    DialogState.Hide -> {}
                    DialogState.Show -> {
                        dialogState.update { DialogState.Hide }
                    }
                }
            }
        }
    }
}




