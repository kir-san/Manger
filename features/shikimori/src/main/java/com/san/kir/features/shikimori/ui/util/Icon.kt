package com.san.kir.features.shikimori.ui.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.runtime.Composable
import com.san.kir.core.compose_utils.MenuIcon

@Composable
internal fun IconLoginOrNot(
    isLogin: Boolean,
    login: () -> Unit,
    logout: () -> Unit,
) {
    if (isLogin)
        MenuIcon(icon = Icons.Default.Logout, onClick = logout)
    else
        MenuIcon(icon = Icons.Default.Login, onClick = login)
}
