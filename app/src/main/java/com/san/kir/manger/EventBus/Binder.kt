package com.san.kir.manger.EventBus

import android.util.SparseArray
import collections.forEach
import com.san.kir.manger.utils.ID
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch
import kotlin.coroutines.experimental.CoroutineContext

/*
* Придумал Lewis Rhine
* https://medium.com/lewisrhine/data-binding-in-anko-77cd11408cf9#.mvv349hoh
*
* Я переработал для использования с корутинами
* */
data class Action<in T>(val context: CoroutineContext,
                        val action: suspend (T) -> Unit)

class Binder<T>(initValue: T) {

    constructor(initValue: T, context: CoroutineContext = UI,
                binding: suspend (item: T) -> Unit) : this(initValue) {
        bind(context, binding)
    }

    private val _channel = Channel<T>(1)
    private var _bound = SparseArray<Action<T>>()
    private var _item: T = initValue

    var item: T
        get() = _item
        set(value) {
                _item = value
                launch(CommonPool) {
                    _channel.send(_item)
                }
        }

    var asyncItem: Deferred<T>
        get() = async { item }
        set(value) {
            launch {
                item = value.await()
            }
        }

    init {
        launch {
            _channel.consumeEach { new ->
                _bound.forEach { _, it ->
                    launch(it.context) {
                        it.action(new)
                    }
                }
            }
        }
    }

    fun bind(action: Action<T>) {
        _bound.append(ID.generate(), action)
        launch(action.context) { action.action.invoke(_item) }
    }

    fun bind(context: CoroutineContext = UI,
             binding: suspend (item: T) -> Unit) {
        bind(Action(context, binding))
    }

    fun unBind(action: Action<T>) {
        val value = _bound.indexOfValue(action)
        if (value != -1)
            _bound.removeAt(value)
    }

    fun unBind(context: CoroutineContext = UI, binding: suspend (item: T) -> Unit) {
        unBind(Action(context, binding))
    }

    fun close() {
        _channel.close()
        _bound.clear()
    }
}
