package com.san.kir.ankofork.dialogs

import android.content.Context
import android.content.DialogInterface
import androidx.fragment.app.Fragment
import com.san.kir.ankofork.AnkoContext

typealias AlertBuilderFactory<D> = (Context) -> AlertBuilder<D>

inline fun <D : DialogInterface> AnkoContext<*>.alert(
    noinline factory: AlertBuilderFactory<D>,
    message: String,
    title: String? = null,
    noinline init: (AlertBuilder<D>.() -> Unit)? = null
) = ctx.alert(factory, message, title, init)

inline fun <D : DialogInterface> Fragment.alert(
    noinline factory: AlertBuilderFactory<D>,
    message: String,
    title: String? = null,
    noinline init: (AlertBuilder<D>.() -> Unit)? = null
) = requireActivity().alert(factory, message, title, init)

fun <D : DialogInterface> Context.alert(
    factory: AlertBuilderFactory<D>,
    message: String,
    title: String? = null,
    init: (AlertBuilder<D>.() -> Unit)? = null
): AlertBuilder<D> {
    return factory(this).apply {
        if (title != null) {
            this.title = title
        }
        this.message = message
        if (init != null) init()
    }
}

inline fun <D : DialogInterface> AnkoContext<*>.alert(
    noinline factory: AlertBuilderFactory<D>,
    message: Int,
    title: Int? = null,
    noinline init: (AlertBuilder<D>.() -> Unit)? = null
) = ctx.alert(factory, message, title, init)

inline fun <D : DialogInterface> Fragment.alert(
    noinline factory: AlertBuilderFactory<D>,
    message: Int,
    title: Int? = null,
    noinline init: (AlertBuilder<D>.() -> Unit)? = null
) = requireActivity().alert(factory, message, title, init)

fun <D : DialogInterface> Context.alert(
    factory: AlertBuilderFactory<D>,
    messageResource: Int,
    titleResource: Int? = null,
    init: (AlertBuilder<D>.() -> Unit)? = null
): AlertBuilder<D> {
    return factory(this).apply {
        if (titleResource != null) {
            this.titleResource = titleResource
        }
        this.messageResource = messageResource
        if (init != null) init()
    }
}

inline fun <D : DialogInterface> AnkoContext<*>.alert(
    noinline factory: AlertBuilderFactory<D>,
    noinline init: AlertBuilder<D>.() -> Unit
) = ctx.alert(factory, init)

inline fun <D : DialogInterface> Fragment.alert(
    noinline factory: AlertBuilderFactory<D>,
    noinline init: AlertBuilder<D>.() -> Unit
) = requireActivity().alert(factory, init)

fun <D : DialogInterface> Context.alert(
    factory: AlertBuilderFactory<D>,
    init: AlertBuilder<D>.() -> Unit
): AlertBuilder<D> = factory(this).apply { init() }
