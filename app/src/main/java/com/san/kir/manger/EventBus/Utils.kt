package com.san.kir.manger.EventBus

fun BinderRx<Boolean>.toogle() {
    item = !item
}

fun Binder<Boolean>.toogle() {
    item = !item
}
