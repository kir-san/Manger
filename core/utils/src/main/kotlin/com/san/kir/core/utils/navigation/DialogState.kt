package com.san.kir.core.utils.navigation

import android.os.Parcelable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.setValue
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.backhandler.BackCallback

class DialogState<T : Parcelable>(
    private val componentContext: ComponentContext,
    private val dismissOnBackPressed: Boolean,
    private val onSuccess: (T) -> Unit,
    private val onDismiss: () -> Unit,
    private val onNeutral: (T) -> Unit,
    initialValue: T? = null
) {
    private val backHandler = BackCallback(onBack = ::dismiss)
    internal var state by mutableStateOf<T?>(initialValue)

    val isShow: Boolean
        get() = state != null

    fun show(t: T) {
        state = t
        if (this.dismissOnBackPressed) {
            componentContext.backHandler.register(this.backHandler)
        }
    }

    fun success() {
        state?.let(onSuccess)
        dismiss()
    }

    fun neutral() {
        state?.let(onNeutral)
        dismiss()
    }

    fun dismiss() {
        state = null
        runCatching { componentContext.backHandler.unregister(backHandler) }
        onDismiss.invoke()
    }

    companion object {
        fun <T : Parcelable> Saver(
            componentContext: ComponentContext,
            dismissOnBackPressed: Boolean,
            onSuccess: Function1<T, Unit>,
            onDismiss: Function0<Unit>,
            onNeutral: Function1<T, Unit>,
        ) = Saver(
            save = { original: DialogState<T> -> original.state },
            restore = { saveable ->
                DialogState(
                    componentContext,
                    dismissOnBackPressed,
                    onSuccess,
                    onDismiss,
                    onNeutral,
                    saveable
                )
            }
        )
    }
}
