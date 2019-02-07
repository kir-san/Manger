package com.san.kir.manger.eventBus

import android.util.SparseArray
import com.san.kir.manger.utils.ID
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import org.jetbrains.anko.collections.forEach
import kotlin.coroutines.CoroutineContext

/*
* Придумал Lewis Rhine
* https://medium.com/lewisrhine/data-binding-in-anko-77cd11408cf9#.mvv349hoh
*
* Я переработал для использования с корутинами
* */
data class Action<in T>(
    val context: CoroutineContext,
    val action: suspend (T) -> Unit
)

class Binder<T>(initValue: T) {

    constructor(
        initValue: T, context: CoroutineContext = Dispatchers.Main,
        binding: suspend (item: T) -> Unit
    ) : this(initValue) {
        bind(context, binding)
    }

    private val _channel = Channel<T>(1)
    private var _bound = SparseArray<Action<T>>()
    private var _item: T = initValue

    var item: T
        get() = _item
        set(value) {
            _item = value
            GlobalScope.launch(Dispatchers.Default) {
                _channel.send(_item)
            }
        }

    var unicItem: T
        get() = item
        set(value) {
            if (value != _item) {
                item = value
            }
        }

    var asyncItem: Deferred<T>
        get() = GlobalScope.async { _channel.receive() }
        set(value) {
            GlobalScope.launch {
                item = value.await()
            }
        }

    init {
        GlobalScope.launch {
            for (new in _channel) {
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
        GlobalScope.launch(action.context) { action.action.invoke(_item) }
    }

    fun bind(
        context: CoroutineContext = Dispatchers.Main,
        binding: suspend (item: T) -> Unit
    ) {
        bind(Action(context, binding))
    }

    fun unBind(action: Action<T>) {
        val value = _bound.indexOfValue(action)
        if (value != -1)
            _bound.removeAt(value)
    }

    fun unBind(context: CoroutineContext = Dispatchers.Main, binding: suspend (item: T) -> Unit) {
        unBind(Action(context, binding))
    }

    fun close() {
        _bound.clear()
        _channel.close()
    }
}
