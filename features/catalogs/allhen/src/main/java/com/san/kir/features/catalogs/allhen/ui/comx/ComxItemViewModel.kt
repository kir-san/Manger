package com.san.kir.features.catalogs.allhen.ui.comx

import com.san.kir.core.internet.ConnectManager
import com.san.kir.core.utils.viewModel.BaseViewModel
import com.san.kir.data.parsing.sites.Allhentai
import com.san.kir.features.catalogs.allhen.ui.allhen.LoginState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
internal class ComxItemViewModel @Inject internal constructor(
    private val manager: ConnectManager,
) : BaseViewModel<ComxItemEvent, ComxItemState>() {
    private val loginState = MutableStateFlow<LoginState>(LoginState.Loading)

    override val tempState = loginState.map { ComxItemState(it) }
    override val defaultState = ComxItemState()

    override suspend fun onEvent(event: ComxItemEvent) {
        when (event) {
            ComxItemEvent.Update -> update()
        }
    }

    private suspend fun update() {
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




