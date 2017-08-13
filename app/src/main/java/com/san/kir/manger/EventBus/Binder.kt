package com.san.kir.manger.EventBus

import kotlin.properties.Delegates

/*
* Придумал Lewis Rhine
* https://medium.com/lewisrhine/data-binding-in-anko-77cd11408cf9#.mvv349hoh
* */

class Binder<T>(initValue: T) {
    private val bound: MutableMap<Int, (item: T) -> Unit> = HashMap()
    var item: T by Delegates.observable(initValue) { _, old, new ->
        if (old != new)
            bound.values.forEach { it(new) }
    }

    fun bind(id: Int, binding: (item: T) -> Unit) {
        bound.put(id, binding)
        binding(item)
    }

    fun unBind(id: Int) {
        bound.remove(id)
    }
}
