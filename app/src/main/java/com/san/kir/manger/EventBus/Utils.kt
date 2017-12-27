package com.san.kir.manger.EventBus

import kotlinx.coroutines.experimental.android.UI
import kotlin.coroutines.experimental.CoroutineContext

fun Binder<Boolean>.toogle(): Boolean {
    item = !item
    return item
}

fun Binder<Boolean>.negative() {
    item = false
}

fun Binder<Boolean>.positive() {
    item = true
}

fun Binder<Boolean>.onTrue(context: CoroutineContext = UI,
                           binding: suspend (item: Boolean) -> Unit) {
    bind(context) { if (it) binding(it) }
}

fun Binder<Boolean>.onFalse(context: CoroutineContext = UI,
                            binding: suspend (item: Boolean) -> Unit) {
    bind(context) { if (!it) binding(it) }
}

fun <T> Binder<T?>.bindIfNotNull(context: CoroutineContext = UI,
                                 binding: suspend (item: T) -> Unit) {
    bind(context) {
        it?.let {
            binding(it)
        }
    }
}

