package com.san.kir.manger.EventBus

import com.san.kir.manger.utils.log
import rx.android.schedulers.AndroidSchedulers
import rx.subjects.PublishSubject
import rx.subjects.SerializedSubject
import rx.subjects.Subject
import rx.subscriptions.CompositeSubscription

class BusMain<T> {
    private val _bus: Subject<T, T> = SerializedSubject(PublishSubject.create())
    private val _sub: MutableMap<String, CompositeSubscription> = HashMap()

    fun post(event: T) {
        _bus.onNext(event)
    }

    fun onEvent(group: Int = 0, action: (T) -> Unit) {
        val key: String = group.toString()
        if (_sub.containsKey(key)) {
            _sub[key]!!.add(_bus.observeOn(AndroidSchedulers.mainThread()).subscribe { action(it) })
        } else {
            log = "Произошла ошибка"
        }
    }

    fun register(group: Int = 0) {
        _sub[group.toString()] = CompositeSubscription()
    }

    fun unregister(group: Int = 0) {
        val key: String = group.toString()
        if (_sub.containsKey(key)) {
            val subscription = _sub[key]
            if (!subscription!!.isUnsubscribed) {
                subscription.unsubscribe()
            }
        }
    }
}
