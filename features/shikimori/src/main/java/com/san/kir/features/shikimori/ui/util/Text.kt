package com.san.kir.features.shikimori.ui.util

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.san.kir.core.compose_utils.Dimensions
import com.san.kir.data.models.base.ShikimoriAccount
import com.san.kir.features.shikimori.R

@Composable
fun StatusText(currentStatus: ShikimoriAccount.Status) {
    val statuses = LocalContext.current.resources.getStringArray(R.array.statuses)

    Text(stringResource(R.string.current_status, statuses[currentStatus.ordinal]))
}

@Composable
internal fun textLoginOrNot(isLogin: Boolean, nickname: String): String {
    return if (isLogin) {
        stringResource(R.string.login_text, nickname)
    } else {
        stringResource(R.string.no_auth_text)
    }
}

@Composable
internal fun ItemHeader(id: Int) {
    Text(
        stringResource(id),
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dimensions.small),
        textAlign = TextAlign.Center
    )
}
