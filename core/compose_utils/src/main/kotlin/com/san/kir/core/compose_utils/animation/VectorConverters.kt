package com.san.kir.core.compose_utils.animation

import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.TwoWayConverter

val Double.Companion.VectorConverter: TwoWayConverter<Double, AnimationVector1D>
    get() = DoubleToVector

private val DoubleToVector: TwoWayConverter<Double, AnimationVector1D> =
    TwoWayConverter({ AnimationVector1D(it.toFloat()) }, { it.value.toDouble() })
