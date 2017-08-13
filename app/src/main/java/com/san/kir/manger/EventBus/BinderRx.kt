package com.san.kir.manger.EventBus

import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.subjects.BehaviorSubject
import rx.subjects.SerializedSubject
import rx.subjects.Subject

class BinderRx<T>(initValue: T) {
    private val _bus: Subject<T, T> = SerializedSubject(BehaviorSubject.create())
    private val _bound: MutableMap<Int, Subscription> = HashMap()
    private var _item: T = initValue

    var item: T
        get() = _item
        set(value) {
            _item = value
            _bus.onNext(value)
        }

    fun bind(id: Int, binding: (T) -> Unit) {
        _bound.put(id, _bus.observeOn(AndroidSchedulers.mainThread()).onBackpressureBuffer().subscribe { binding(it) })
    }

    fun unBind(id: Int) {
        val sub = _bound[id]
        sub?.let {
            if (!it.isUnsubscribed) {
                it.unsubscribe()
                _bound.remove(id)
            }
        }
    }

    fun unBindAll() {
        _bound.values.forEach {
            if (!it.isUnsubscribed) {
                it.unsubscribe()
            }
        }
        _bound.clear()
    }
}
