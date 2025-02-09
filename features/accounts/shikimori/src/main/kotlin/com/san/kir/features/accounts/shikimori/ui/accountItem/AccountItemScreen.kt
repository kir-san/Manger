package com.san.kir.features.accounts.shikimori.ui.accountItem

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.san.kir.core.compose.Dimensions
import com.san.kir.core.compose.HalfSpacer
import com.san.kir.core.compose.animation.FromBottomToTopAnimContent
import com.san.kir.core.compose.animation.FromEndToEndAnimContent
import com.san.kir.core.compose.animation.SharedParams
import com.san.kir.core.compose.animation.rememberSharedParams
import com.san.kir.core.compose.animation.saveParams
import com.san.kir.core.compose.horizontalInsetsPadding
import com.san.kir.core.compose.rememberImage
import com.san.kir.core.utils.flow.collectAsStateWithLifecycle
import com.san.kir.core.utils.navigation.EmptyDialogData
import com.san.kir.core.utils.navigation.rememberDialogState
import com.san.kir.core.utils.navigation.show
import com.san.kir.core.utils.viewModel.OnGlobalEvent
import com.san.kir.core.utils.viewModel.rememberSendAction
import com.san.kir.core.utils.viewModel.rememberSendEvent
import com.san.kir.core.utils.viewModel.stateHolder
import com.san.kir.features.accounts.shikimori.R
import com.san.kir.features.accounts.shikimori.logic.api.ShikimoriData
import com.san.kir.features.accounts.shikimori.ui.util.LogOutDialog

@Composable
public fun ShikimoriListItem(navigateToManager: (Long, SharedParams) -> Unit) {
    val holder: AccountItemStateHolder = stateHolder(creator = ::AccountItemViewModel)
    val state by holder.state.collectAsStateWithLifecycle()
    val sendAction = holder.rememberSendAction()
    val sendEvent = rememberSendEvent()

    LoginOrNot(
        state = state.login,
        navigateToManager = {
            when (val curState = state.login) {
                LoginState.LogOut, LoginState.Error -> holder.sendAction(AccountItemAction.LogIn)
                is LoginState.Ok -> navigateToManager(curState.accountId, it)
                else -> sendAction(AccountItemAction.Update)
            }
        },
        login = { sendAction(AccountItemAction.LogIn) },
        logout = { sendEvent(AccountItemEvent.ShowLogoutDialog) }
    )
}

@Composable
public fun ShikimoriItemDialog() {
    val sendEvent = rememberSendEvent()
    val logOutDialog = rememberDialogState<EmptyDialogData>(
        onNeutral = { sendEvent.invoke(AccountItemEvent.LogOut(false)) },
        onSuccess = { sendEvent.invoke(AccountItemEvent.LogOut(true)) }
    )

    OnGlobalEvent { event ->
        if (event is AccountItemEvent.ShowLogoutDialog) {
            logOutDialog.show()
        }
    }

    LogOutDialog(logOutDialog)
}


@Composable
private fun LoginOrNot(
    state: LoginState,
    navigateToManager: (SharedParams) -> Unit,
    login: () -> Unit,
    logout: () -> Unit,
) {
    val params = rememberSharedParams()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .saveParams(params)
            .clickable(onClick = { navigateToManager(params) })
            .padding(vertical = Dimensions.quarter, horizontal = Dimensions.default)
            .horizontalInsetsPadding(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Image(
            rememberImage(ShikimoriData.ICON_URL),
            contentDescription = "Shikimori site icon",
            modifier = Modifier
                .padding(vertical = Dimensions.half)
                .padding(end = Dimensions.default)
                .size(Dimensions.Image.default)
        )

        Column(modifier = Modifier.weight(1f, true)) {
            Text(stringResource(R.string.site_name))
            FromBottomToTopAnimContent(state) {
                when (it) {
                    is LoginState.Ok, is LoginState.LogInCheck -> Row {
                        Text(stringResource(R.string.login_text, it.nickName))
                        HalfSpacer()
                        Image(rememberImage(it.logo), "", modifier = Modifier.size(Dimensions.Image.mini))
                    }

                    is LoginState.Error -> {
                        Text(stringResource(R.string.error_try_again))
                    }

                    is LoginState.LogInError -> {
                        Text(
                            stringResource(R.string.whoami_error),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }

                    is LoginState.LogOut -> {
                        Text(stringResource(R.string.no_login_text))
                    }

                    else -> Unit
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FromEndToEndAnimContent(targetState = state) { targetState ->
                when (targetState) {
                    is LoginState.LogInError, is LoginState.Ok -> IconButton(onClick = logout) {
                        Icon(
                            Icons.AutoMirrored.Filled.Logout, "",
                            modifier = Modifier.size(Dimensions.Image.small)
                        )
                    }

                    LoginState.LogOut -> IconButton(onClick = login) {
                        Icon(
                            Icons.AutoMirrored.Filled.Login, "",
                            modifier = Modifier.size(Dimensions.Image.small)
                        )
                    }

                    LoginState.Error -> Icon(Icons.Default.Error, "")

                    is LoginState.LogInCheck, LoginState.Loading -> CircularProgressIndicator(
                        modifier = Modifier.size(Dimensions.ProgressBar.big)
                    )
                }
            }
        }
    }
}
