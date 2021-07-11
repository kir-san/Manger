package com.san.kir.manger.ui.utils

import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource

@Composable
fun MenuIcon(icon: ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    IconButton(onClick = onClick, modifier = modifier) {
        Icon(icon, "")
    }
}

@Composable
fun MenuIcon(icon: MutableState<ImageVector>, modifier: Modifier = Modifier, onClick: () -> Unit) {
    IconButton(onClick = onClick, modifier = modifier) {
        Icon(icon.value, "")
    }
}

@Composable
fun MenuText(text: String, onClick: () -> Unit) {
    DropdownMenuItem(onClick = onClick) {
        Text(text = text)
    }
}

@Composable
fun MenuText(id: Int, onClick: () -> Unit) = MenuText(stringResource(id = id), onClick)

