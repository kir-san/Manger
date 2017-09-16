package com.san.kir.manger.EventBus

import android.arch.lifecycle.MutableLiveData

class BinderLive<T>(initValue: T) {
    private val _bound: MutableMap<Int, (T) -> Unit> = HashMap()
    private val _liveData = MutableLiveData<T>()

    init {
        _liveData.postValue(initValue)
    }

    var item: T?
        get() = _liveData.value
        set(value) {
            _liveData.postValue(value)
        }

    fun bind(id: Int, binding: (T) -> Unit) {
        _bound.put(id, binding)
        _liveData.observeForever { it?.let { it1 -> binding(it1) } }
    }

    fun unBind(id: Int) {
        _liveData.removeObserver { _bound[id] }
        _bound.remove(id)
    }

//    fun unBindAll() {
//        _bound.values.forEach {
//            if (!it.isUnsubscribed) {
//                it.unsubscribe()
//            }
//        }
//        _bound.clear()
//    }
}
