package com.san.kir.features.catalogs.allhen.ui.comx

import androidx.compose.runtime.Stable
import com.san.kir.core.utils.viewModel.ScreenState
import com.san.kir.features.catalogs.allhen.ui.allhen.LoginState

@Stable
internal data class ComxItemState(
    val login: LoginState = LoginState.Loading,
) : ScreenState
