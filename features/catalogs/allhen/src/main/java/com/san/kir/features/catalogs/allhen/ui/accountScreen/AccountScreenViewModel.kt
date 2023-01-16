package com.san.kir.features.catalogs.allhen.ui.accountScreen

import com.san.kir.core.utils.viewModel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

@HiltViewModel
internal class AccountScreenViewModel @Inject constructor(

) : BaseViewModel<AccountScreenEvent, AccountScreenState>() {
    override val tempState = flowOf(AccountScreenState())

    override val defaultState = AccountScreenState()

    override suspend fun onEvent(event: AccountScreenEvent) {
//        when(event) {
//
//        }
    }
}
