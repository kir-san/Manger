package com.san.kir.features.shikimori.ui.accountItem

import android.app.Application
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.viewModelScope
import com.san.kir.core.utils.coroutines.defaultLaunch
import com.san.kir.core.utils.viewModel.BaseViewModel
import com.san.kir.features.shikimori.AuthActivity
import com.san.kir.features.shikimori.CODE
import com.san.kir.features.shikimori.authCodeStore
import com.san.kir.features.shikimori.logic.useCases.AuthUseCase
import com.san.kir.features.shikimori.ui.util.DialogState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
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
    private var hasCheck = false

    init {
        authUseCase.authData
            .catch { loginState.update { LoginState.Error } }
            .onEach { auth ->
                loginState.value =
                    if (auth.isLogin.not()) LoginState.LogOut
                    else if (hasCheck) LoginState.LogInOk(auth.nickName)
                    else {
                        checkAccountAccess(auth.nickName)
                        LoginState.LogInCheck(auth.nickName)
                    }
            }.launchIn(viewModelScope)

        // ОЖидание получения кода, для старта авторизации
        app.authCodeStore.data
            .mapNotNull { it[CODE] }
            .onEach {
                hasCheck = true
                authUseCase.login(it)
                // После использования, избавляемся от кода
                app.authCodeStore.edit { settings -> settings.clear() }
            }
            .launchIn(viewModelScope)
    }

    override val tempState = combine(loginState, dialogState, ::AccountItemState)
    override val defaultState = AccountItemState()

    override suspend fun onEvent(event: AccountItemEvent) {
        when (event) {
            AccountItemEvent.LogIn        -> {
                loginState.update { LoginState.Loading }
                AuthActivity.start(app)
            }

            AccountItemEvent.LogOut       -> when (dialogState.value) {
                DialogState.Hide -> dialogState.update { DialogState.Show }

                DialogState.Show -> {
                    dialogState.update { DialogState.Hide }
                    loginState.update { LoginState.Loading }
                    authUseCase.logout()
                }
            }

            AccountItemEvent.CancelLogOut -> when (dialogState.value) {
                DialogState.Hide -> {}
                DialogState.Show -> dialogState.update { DialogState.Hide }
            }
        }
    }

    private fun checkAccountAccess(nickname: String) = viewModelScope.defaultLaunch {
        hasCheck = true
        val whoami = authUseCase.whoami()
        loginState.value =
            if (whoami == null) LoginState.LogInError(nickname)
            else LoginState.LogInOk(whoami.nickname)
    }
}
