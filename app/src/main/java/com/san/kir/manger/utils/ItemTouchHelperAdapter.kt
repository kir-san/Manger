package com.san.kir.manger.utils

interface ItemTouchHelperAdapter {
    fun onItemMove(fromPosition: Int, toPosition: Int): Boolean
    /*fun onItemDismiss(position: Int)*/
}
