package com.san.kir.manger.ui.startapp

sealed interface InitState {
    data object Memory : InitState
    data object Notification : InitState
    data object Init : InitState
}
