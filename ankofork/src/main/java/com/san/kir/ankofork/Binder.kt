package com.san.kir.ankofork

import android.util.SparseArray
import androidx.core.util.forEach
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.CoroutineContext

data class Action<in T>(
    val context: CoroutineContext = Dispatchers.Main,
    val action: suspend (T) -> Unit
)

class Binder<T>(initValue: T) {

    private val _channel = Channel<T>(1)
    private var _bound = SparseArray<Action<T>>()
    private var _item: T = initValue
    private val lock = Mutex()

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

    init {
        GlobalScope.launch {
            for (new in _channel) {
                lock.withLock {
                    _bound.forEach { _, it ->
                        launch(it.context) {
                            it.action(new)
                        }
                    }
                }
            }
        }
    }

    fun bind(action: Action<T>) {
        GlobalScope.launch(action.context) {
            lock.withLock {
                _bound.append(ID.generate(), action)
            }
            action.action.invoke(_item)
        }
    }

    fun bind(
        context: CoroutineContext = Dispatchers.Main,
        binding: suspend (item: T) -> Unit
    ) {
        bind(Action(context, binding))
    }

    @Suppress("unused")
    fun unBind(action: Action<T>) {
        val value = _bound.indexOfValue(action)
        if (value != -1)
            _bound.removeAt(value)
    }

    fun close() {
        _bound.clear()
        _channel.close()
    }

    private object ID {
        private var count = 1

        fun generate(): Int {
            return count++
        }
    }
}

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
