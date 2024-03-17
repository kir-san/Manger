package com.san.kir.core.utils.navigation

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import com.san.kir.core.utils.viewModel.LocalComponentContext

@Composable
fun <T : Parcelable> rememberDialogState(
    dismissOnBackPressed: Boolean = true,
    onDismiss: () -> Unit = {},
    onNeutral: (T) -> Unit = {},
    onSuccess: (T) -> Unit = {},
): DialogState<T> {

    val componentContext = LocalComponentContext.current

    return rememberSaveable(
        saver = DialogState.Saver(
            componentContext,
            dismissOnBackPressed,
            onSuccess,
            onDismiss,
            onNeutral
        )
    ) {
        DialogState(
            componentContext,
            dismissOnBackPressed,
            onSuccess,
            onDismiss,
            onNeutral,
            null
        )
    }
}

@Composable
fun <T : Parcelable> DialogBase(
    dialogState: DialogState<T>,
    content: @Composable (T?) -> Unit
) {
    content(dialogState.state)
}
