package com.san.kir.manger.ui.init

sealed interface InitState {
    data object Memory : InitState
    data object Notification : InitState
    data object Init : InitState
}
