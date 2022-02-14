package com.san.kir.features.shikimori.ui.syncItem

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.san.kir.core.compose_utils.Dimensions
import com.san.kir.core.compose_utils.Styles
import com.san.kir.core.compose_utils.rememberImage
import com.san.kir.features.shikimori.AuthActivity
import com.san.kir.features.shikimori.R
import com.san.kir.features.shikimori.ShikimoriData
import com.san.kir.features.shikimori.ui.main.ShikimoriViewModel

@Composable
fun ShikimoriItem(
    viewModel: ShikimoriViewModel,
    navigateToManager: () -> Unit,
) {
    val authData by viewModel.auth.collectAsState()

    val ctx = LocalContext.current

    LoginOrNot(
        authData.isLogin,
        authData.whoami.nickname,
        navigateToManager,
        { AuthActivity.start(ctx) },
        viewModel::logout
    )
}

@Composable
internal fun LoginOrNot(
    isLogin: Boolean,
    nickname: String,
    navigateToManager: () -> Unit,
    login: () -> Unit,
    logout: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = navigateToManager)
    ) {
        Image(
            rememberImage(ShikimoriData.iconUrl),
            contentDescription = "Shikimori site icon",
            modifier = Modifier
                .padding(vertical = Dimensions.small)
                .padding(end = Dimensions.small)
                .size(Dimensions.imageSize)
        )
        Column(modifier = Modifier
            .weight(1f, true)
            .align(Alignment.CenterVertically)) {
            Text(stringResource(R.string.site_name))
            if (isLogin)
                Text(stringResource(R.string.login_text, nickname))
            else
                Text(stringResource(R.string.no_login_text), style = Styles.secondaryText)
        }

//        IconLoginOrNot(isLogin, login, logout)
    }
}

@Preview(showSystemUi = true, group = "item")
@Composable
internal fun ContentPreview() {
    MaterialTheme {
        Column {
            LoginOrNot(
                isLogin = false,
                nickname = "",
                navigateToManager = {}, {}, {}
            )

            LoginOrNot(
                isLogin = true,
                nickname = "Kir-san",
                navigateToManager = {}, {}, {}
            )
        }
    }
}

