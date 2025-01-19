package com.san.kir.core.compose.animation

import androidx.compose.animation.VectorConverter
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationResult
import androidx.compose.animation.core.AnimationVector
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.AnimationVector4D
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.TwoWayConverter
import androidx.compose.animation.core.VectorConverter
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


private const val FastValueAnimationDuration = 500
private const val ValueAnimationDuration = 1200

private val DoubleToVector: TwoWayConverter<Double, AnimationVector1D> = TwoWayConverter(
    convertToVector = { AnimationVector1D(it.toFloat()) },
    convertFromVector = { it.value.toDouble() }
)

private val DoubleSaver: Saver<Animatable<Double, AnimationVector1D>, *> = Saver(
    save = { it.value },
    restore = { Animatable(it, DoubleToVector) }
)

private val FloatSaver: Saver<Animatable<Float, AnimationVector1D>, *> = Saver(
    save = { it.value },
    restore = { Animatable(it, Float.VectorConverter) }
)

private val LongToVector: TwoWayConverter<Long, AnimationVector1D> = TwoWayConverter(
    convertToVector = { AnimationVector1D(it.toFloat()) },
    convertFromVector = { it.value.toLong() }
)

private val LongSaver: Saver<Animatable<Long, AnimationVector1D>, *> = Saver(
    save = { it.value },
    restore = { Animatable(it, LongToVector) }
)

private val IntSaver: Saver<Animatable<Int, AnimationVector1D>, *> = Saver(
    save = { it.value },
    restore = { Animatable(it, Int.VectorConverter) }
)

private val DpSaver: Saver<Animatable<Dp, AnimationVector1D>, *> = Saver(
    save = { it.value.value },
    restore = { Animatable(it.dp, Dp.VectorConverter) }
)

private val ColorSaver: Saver<Animatable<Color, AnimationVector4D>, *> = Saver(
    save = { it.value },
    restore = { Animatable(it, Color.VectorConverter(it.colorSpace)) }
)

private val SizeSaver: Saver<Animatable<Size, AnimationVector2D>, *> = Saver(
    save = { it.value },
    restore = { Animatable(it, Size.VectorConverter) }
)

public suspend fun <Value, Vector : AnimationVector> Animatable<Value, Vector>.animateToDelayed(
    value: Value,
    delay: Int = 0,
    duration: Int = ValueAnimationDuration,
): AnimationResult<Value, Vector> =
    animateTo(value, TweenSpec(duration, delay, LinearEasing))

public suspend fun <Value, Vector : AnimationVector> Animatable<Value, Vector>.fastAnimateTo(
    value: Value
): AnimationResult<Value, Vector> =
    animateTo(value, TweenSpec(FastValueAnimationDuration, 0, LinearEasing))

@Composable
public fun rememberDoubleAnimatable(initialValue: Double = 0.0): Animatable<Double, AnimationVector1D> =
    rememberSaveable(saver = DoubleSaver) { Animatable(initialValue, DoubleToVector) }

@Composable
public fun rememberFloatAnimatable(initialValue: Float = 0f): Animatable<Float, AnimationVector1D> =
    rememberSaveable(saver = FloatSaver) { Animatable(initialValue, Float.VectorConverter) }

@Composable
public fun rememberIntAnimatable(initialValue: Int = 0): Animatable<Int, AnimationVector1D> =
    rememberSaveable(saver = IntSaver) { Animatable(initialValue, Int.VectorConverter) }

@Composable
public fun rememberLongAnimatable(initialValue: Long = 0): Animatable<Long, AnimationVector1D> =
    rememberSaveable(saver = LongSaver) { Animatable(initialValue, LongToVector) }

@Composable
public fun rememberDpAnimatable(initialValue: Dp = 0.dp): Animatable<Dp, AnimationVector1D> =
    rememberSaveable(saver = DpSaver) { Animatable(initialValue, Dp.VectorConverter) }

@Composable
public fun rememberColorAnimatable(initialValue: Color = Color.Unspecified): Animatable<Color, AnimationVector4D> =
    rememberSaveable(saver = ColorSaver) {
        Animatable(initialValue, Color.VectorConverter(initialValue.colorSpace))
    }

@Composable
public fun rememberSizeAnimatable(initialValue: Size = Size.Unspecified): Animatable<Size, AnimationVector2D> =
    rememberSaveable(saver = SizeSaver) { Animatable(initialValue, Size.VectorConverter) }
