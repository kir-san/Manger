package com.san.kir.features.catalogs.allhen.ui.accountItem

import androidx.lifecycle.viewModelScope
import com.san.kir.core.internet.ConnectManager
import com.san.kir.core.utils.viewModel.BaseViewModel
import com.san.kir.data.parsing.sites.Allhentai
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class AccountItemViewModel @Inject internal constructor(
    private val manager: ConnectManager,
) : BaseViewModel<AccountItemEvent, AccountItemState>() {
    private val loginState = MutableStateFlow<LoginState>(LoginState.Loading)

    override val tempState = loginState.map { AccountItemState(it) }
    override val defaultState = AccountItemState()

    override suspend fun onEvent(event: AccountItemEvent) {
        when (event) {
            AccountItemEvent.Update -> update()
        }
    }

    private fun update() = viewModelScope.launch {
        loginState.value = LoginState.Loading
        runCatching {
            val document = manager.getDocument(Allhentai.HOST_NAME).select(".account-menu")
            val name = document.select("#accountMenu span.strong").first()?.text()
            val avatar = document.select(".user-profile-settings-link img").attr("src")

            if (name == null) loginState.value = LoginState.NonLogIn
            else loginState.value = LoginState.LogIn(name, avatar)
        }.onFailure {
            loginState.value = LoginState.Error
        }
    }
}




