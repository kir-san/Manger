package com.san.kir.ankofork.dialogs

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import androidx.fragment.app.Fragment
import com.san.kir.ankofork.AnkoContext

inline fun AnkoContext<*>.alert(
        message: CharSequence,
        title: CharSequence? = null,
        noinline init: (AlertBuilder<DialogInterface>.() -> Unit)? = null
) = ctx.alert(message, title, init)

inline fun Fragment.alert(
        message: CharSequence,
        title: CharSequence? = null,
        noinline init: (AlertBuilder<DialogInterface>.() -> Unit)? = null
) = requireActivity().alert(message, title, init)

fun Context.alert(
        message: CharSequence,
        title: CharSequence? = null,
        init: (AlertBuilder<DialogInterface>.() -> Unit)? = null
): AlertBuilder<AlertDialog> {
    return AndroidAlertBuilder(this).apply {
        if (title != null) {
            this.title = title
        }
        this.message = message
        if (init != null) init()
    }
}

inline fun AnkoContext<*>.alert(
        message: Int,
        title: Int? = null,
        noinline init: (AlertBuilder<DialogInterface>.() -> Unit)? = null
) = ctx.alert(message, title, init)

inline fun Fragment.alert(
        message: Int,
        title: Int? = null,
        noinline init: (AlertBuilder<DialogInterface>.() -> Unit)? = null
) = requireActivity().alert(message, title, init)

fun Context.alert(
        messageResource: Int,
        titleResource: Int? = null,
        init: (AlertBuilder<DialogInterface>.() -> Unit)? = null
): AlertBuilder<DialogInterface> {
    return AndroidAlertBuilder(this).apply {
        if (titleResource != null) {
            this.titleResource = titleResource
        }
        this.messageResource = messageResource
        if (init != null) init()
    }
}


inline fun AnkoContext<*>.alert(noinline init: AlertBuilder<DialogInterface>.() -> Unit) = ctx.alert(init)
inline fun Fragment.alert(noinline init: AlertBuilder<DialogInterface>.() -> Unit) = requireActivity().alert(init)

fun Context.alert(init: AlertBuilder<DialogInterface>.() -> Unit): AlertBuilder<DialogInterface> =
        AndroidAlertBuilder(this).apply { init() }
