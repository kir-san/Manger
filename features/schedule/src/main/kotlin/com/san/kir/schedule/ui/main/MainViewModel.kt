package com.san.kir.schedule.ui.main

import com.san.kir.core.utils.viewModel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

@HiltViewModel
internal class MainViewModel @Inject constructor(

) : BaseViewModel<MainEvent, MainState>() {
    override val tempState = flowOf(MainState())

    override val defaultState = MainState()

    override suspend fun onEvent(event: MainEvent) {}
}
