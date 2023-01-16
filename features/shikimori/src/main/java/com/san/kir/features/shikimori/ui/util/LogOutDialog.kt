package com.san.kir.features.shikimori.ui.util

import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.san.kir.features.shikimori.R

internal sealed interface DialogState {
   data object Show : DialogState
   data object Hide : DialogState
}

@Composable
internal fun LogOutDialog(state: DialogState, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    when (state) {
        DialogState.Hide -> {}
        DialogState.Show -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                text = { Text(stringResource(R.string.logout_dialog_text)) },
                title = { Text(stringResource(R.string.logout_dialog_title)) },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.logout_dialog_cancel))
                    }
                },
                confirmButton = {
                    TextButton(onClick = onConfirm) {
                        Text(stringResource(R.string.logout_dialog_ok))
                    }
                }
            )
        }
    }
}
