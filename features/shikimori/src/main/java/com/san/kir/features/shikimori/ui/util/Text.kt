package com.san.kir.features.shikimori.ui.util

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.san.kir.core.compose.Dimensions
import com.san.kir.core.compose.Fonts
import com.san.kir.core.compose.Styles
import com.san.kir.core.compose.animation.FromBottomToBottomAnimContent
import com.san.kir.data.models.base.ShikimoriStatus
import com.san.kir.features.shikimori.R
import com.san.kir.features.shikimori.ui.accountItem.LoginState

@Composable
fun StatusText(currentStatus: ShikimoriStatus?) {
    if (currentStatus != null) {
        val statuses = LocalContext.current.resources.getStringArray(R.array.statuses)
        Text(
            stringResource(R.string.current_status, statuses[currentStatus.ordinal]),
            fontSize = Fonts.Size.less,
        )
    }
}

@Composable
internal fun TextLoginOrNot(state: LoginState) {
    FromBottomToBottomAnimContent(targetState = state) { targetState ->
        when (targetState) {
            is LoginState.LogInOk, is LoginState.LogInError, is LoginState.LogInCheck -> {
                Text(stringResource(R.string.login_text, targetState.nickName))
            }

            LoginState.LogOut                                                         -> {
                Text(stringResource(R.string.no_login_text), style = Styles.secondaryText)
            }

            LoginState.Error                                                          -> {
                Text(stringResource(R.string.error_try_again), style = Styles.secondaryText)
            }

            else                                                                      -> {}
        }
    }
}

@Composable
internal fun ItemHeader(id: Int) {
    Text(
        stringResource(id),
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dimensions.half),
        textAlign = TextAlign.Center
    )
}

// Отображение названий манги с установленым стилем
@Composable
internal fun MangaNames(
    name: String? = null,
    russianName: String? = null,
) {
    ProvideTextStyle(Fonts.Style.bigBoldCenter) {
        name?.let { name ->
            Text(
                name, modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Dimensions.default)
            )
        }

        russianName?.let { name ->
            if (name.isNotEmpty())
                Text(name, modifier = Modifier.fillMaxWidth())
        }
    }
}
