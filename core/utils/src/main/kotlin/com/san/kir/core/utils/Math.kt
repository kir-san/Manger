package com.san.kir.core.utils

fun lerp(a: Float, b: Float, fraction: Float): Float {
    return ((b - a) * fraction) + a
}
