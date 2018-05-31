package com.san.kir.manger.eventBus

fun Binder<Boolean>.toggle(): Boolean {
    item = !item
    return item
}

fun Binder<Boolean>.negative() {
    item = false
}

fun Binder<Boolean>.positive() {
    item = true
}

