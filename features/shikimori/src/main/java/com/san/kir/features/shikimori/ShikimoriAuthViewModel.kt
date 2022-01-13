package com.san.kir.features.shikimori

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.san.kir.core.utils.coroutines.defaultLaunch
import com.san.kir.data.store.TokenStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
internal class ShikimoriAuthViewModel @Inject constructor(
    private val manager: Repository,
    private val store: TokenStore,
) : ViewModel() {

    fun login(code: String) = viewModelScope.defaultLaunch {
        val token = manager.accessToken(code)
        store.updateToken(token)
        val whoami = manager.whoami(token)
        store.updateWhoami(whoami)
        store.setLogin(true)
    }

}
