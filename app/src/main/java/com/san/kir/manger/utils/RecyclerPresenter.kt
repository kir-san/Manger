package com.san.kir.manger.utils

import android.support.v7.widget.RecyclerView

open class RecyclerPresenter {
    lateinit var recycler: RecyclerView
    open fun into(recyclerView: RecyclerView) {
        recycler = recyclerView
    }
}
