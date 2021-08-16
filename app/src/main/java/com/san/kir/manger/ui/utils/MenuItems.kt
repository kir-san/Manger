package com.san.kir.manger.ui.utils

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Checkbox
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun MenuIcon(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick, modifier = modifier, enabled = enabled) {
        Icon(icon, "")
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

@Composable
fun CheckedMenuText(
    id: Int,
    checked: Boolean,
    onClick: () -> Unit
) {
    DropdownMenuItem(onClick) {
        Checkbox(checked, { onClick() }, modifier = Modifier.padding(end = 16.dp))
        Text(stringResource(id))
    }
}
