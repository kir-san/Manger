package com.san.kir.manger.utils

import androidx.recyclerview.widget.RecyclerView

open class RecyclerPresenter {
    lateinit var recycler: RecyclerView
    open fun into(recyclerView: RecyclerView) {
        recycler = recyclerView
    }
}
