package com.san.kir.manger.EventBus

import android.os.Parcelable
import android.util.SparseArray
import collections.forEach
import com.san.kir.manger.Extending.BaseActivity
import com.san.kir.manger.utils.ID

abstract class BaseViewModel: Parcelable {
    private val bounds = SparseArray<Binder<Any>>()
    protected var act: BaseActivity? = null

    protected fun newBinder(binder: Binder<Any>) {
        bounds.put(ID.generate(), binder)
    }

    fun globalClose() {
        bounds.forEach { _, binder -> binder.close() }
    }

    fun setActivity(activity: BaseActivity) {
        act = activity
    }

    fun isSetActivity() = act != null
}
