package com.san.kir.features.shikimori.ui.util

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.runtime.Composable

@Composable
internal fun IconLoginOrNot(
    isLogin: Boolean,
    login: () -> Unit,
    logout: () -> Unit,
) {
    if (isLogin)
        IconButton(onClick = logout) {
            Icon(Icons.Default.Logout, "")
        }
    else
        IconButton(onClick = login) {
            Icon(Icons.Default.Login, "")
        }
}
