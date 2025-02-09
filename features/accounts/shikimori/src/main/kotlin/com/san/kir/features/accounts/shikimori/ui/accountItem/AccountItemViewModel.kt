package com.san.kir.features.accounts.shikimori.ui.accountItem

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.san.kir.core.utils.ManualDI
import com.san.kir.core.utils.coroutines.defaultLaunch
import com.san.kir.core.utils.viewModel.Action
import com.san.kir.core.utils.viewModel.EventBus
import com.san.kir.core.utils.viewModel.UpdateEvent
import com.san.kir.core.utils.viewModel.ViewModel
import com.san.kir.data.accountsRepository
import com.san.kir.data.db.main.repo.AccountRepository
import com.san.kir.data.models.utils.AccountType
import com.san.kir.features.accounts.shikimori.AuthActivity
import com.san.kir.features.accounts.shikimori.CODE
import com.san.kir.features.accounts.shikimori.authCodeStore
import com.san.kir.features.accounts.shikimori.logic.di.authRepository
import com.san.kir.features.accounts.shikimori.logic.models.Auth
import com.san.kir.features.accounts.shikimori.logic.repo.AuthRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.transformLatest
import timber.log.Timber

@OptIn(ExperimentalCoroutinesApi::class)
internal class AccountItemViewModel(
    private val eventBus: EventBus,
    private val context: Context = ManualDI.application,
    private val accountsRepository: AccountRepository = ManualDI.accountsRepository(),
    private val authRepository: AuthRepository = ManualDI.authRepository(),
) : ViewModel<AccountItemState>(), AccountItemStateHolder {

    companion object {
        private const val TAG: String = "AccountItemViewModel"
        private const val LOGIN_TRIES: Int = 3
    }

    private var accountId: Long = -1L
    private val loginState = MutableStateFlow<LoginState>(LoginState.Loading)
    private var auth = Auth()
    private var hasCheck = false

    init {
        subscribeOnGlobalEvents(eventBus)

        accountsRepository.items
            .distinctUntilChanged()
            .transformLatest { accounts ->
                val account = accounts.firstOrNull { it.type == AccountType.Shikimori }
                accountId = account?.id ?: -1L
                if (account != null) {
                    val flow = accountsRepository.loadItem(account.id)
                        .map { acc -> ManualDI.stringToJson<Auth>(acc?.data ?: "") }
                    emitAll(flow)
                } else {
                    emit(null)
                }
            }
            .catch { loginState.value = LoginState.Error }
            .onEach { auth ->
                this.auth = auth ?: Auth()
                loginState.value = when {
                    auth == null -> LoginState.LogOut
                    auth.isLogin.not() -> LoginState.LogOut

                    hasCheck -> {
                        val accountId = this.accountId
                        requireNotNull(accountId)
                        LoginState.Ok(accountId, auth.user.nickname, auth.user.avatar)
                    }

                    else -> {
                        checkAccountAccess(auth.user.nickname)
                        LoginState.LogInCheck(auth.user.nickname, auth.user.avatar)
                    }
                }
            }
            .launch()

        // ОЖидание получения кода, для старта авторизации
        context.authCodeStore.data
            .mapNotNull { it[CODE] }
            .onEach { code ->
                Timber.tag(TAG).i("Code from activity $code")
                hasCheck = true

                var success = 0
                var currentTry = 0
                while (currentTry < LOGIN_TRIES && success == 0) {
                    Timber.tag(TAG).i("try login ${currentTry + 1}")
                    val loginResult = authRepository.login(accountId, code)
                    if (loginResult) success = 1
                    currentTry++
                }

                if (success == 0) {
                    loginState.value = LoginState.Error
                }

                // После использования, избавляемся от кода
                context.authCodeStore.edit { settings -> settings.clear() }
            }
            .launch()
    }

    override val tempState = loginState.map(::AccountItemState)
    override val defaultState = AccountItemState()

    override suspend fun onAction(action: Action) {
        when (action) {
            AccountItemAction.LogIn -> {
                loginState.value = LoginState.Loading
                AuthActivity.start(context)
            }

            is AccountItemAction.LogOut -> {
                loginState.value = LoginState.Loading
                authRepository.logout(accountId, action.full)
            }

            AccountItemAction.Update -> {
                if (loginState.value.nickName.isNotEmpty()) {
                    checkAccountAccess(loginState.value.nickName)
                }
            }
        }
    }

    private fun checkAccountAccess(nickname: String) = defaultLaunch {
        hasCheck = true
        auth = authRepository.whoami(auth, accountId) ?: return@defaultLaunch
        loginState.value = if (auth.hasUser) {
            LoginState.Ok(accountId, auth.user.nickname, auth.user.avatar)
        } else {
            LoginState.LogInError(nickname)
        }
    }

    private fun subscribeOnGlobalEvents(eventBus: EventBus) {
        eventBus.events
            .onEach { event ->
                Timber.tag(TAG).d("EVENT -> $event")
                when (event) {
                    UpdateEvent -> {
                        if (loginState.value.nickName.isEmpty()) {
                            checkAccountAccess(loginState.value.nickName)
                        }
                    }

                    is AccountItemEvent.LogOut -> sendAction(AccountItemAction.LogOut(event.full))
                }
            }
            .launch()
    }
}
