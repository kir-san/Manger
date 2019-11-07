package com.san.kir.manger.utils

object ID {
    private var count = 1

    fun generate(): Int {
        return count++
    }
}
