package com.san.kir.core.compose

import android.graphics.ComposeShader
import android.graphics.PorterDuff
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.util.lerp

@Stable
public class ColorPickerState internal constructor(private val initialColor: Color) {
    public val currentColor: Color by derivedStateOf { Color.hsv(hValue, sValue, vValue) }

    internal val hBar by derivedStateOf {
        val bitmap = ImageBitmap(hSize.width, hSize.height)
        if (hSize.width == 0 || hSize.height == 0) return@derivedStateOf bitmap

        val hueCanvas = Canvas(bitmap)
        var hue = 0.0f
        val paint = Paint()
        paint.strokeWidth = 0.0f

        if (0 <= hSize.width) {
            var index = 0
            while (true) {
                val color = Color.hsv(hue.coerceIn(0.0f, 360.0f), MAX_SATURATION, MAX_VALUE)
                paint.color = color
                val hue2 = hue + (360.0f / hSize.width)
                hueCanvas.drawLine(
                    Offset(index.toFloat(), 0.0f),
                    Offset(index.toFloat(), hSize.height.toFloat()),
                    paint
                )
                if (index == hSize.width) break
                index++
                hue = hue2
            }
        }
        if (!hasGesture) {
            hPosition = (hSize.width / 360.0f) * hValue
        }
        bitmap
    }

    internal val svBar by derivedStateOf {
        val bitmap = ImageBitmap(svSize.width.toInt(), svSize.height.toInt())

        if (svSize.width != 0.0f && svSize.height != 0.0f) {
            val canvas = Canvas(bitmap)
            val satShader = LinearGradientShader(
                from = Offset.Zero,
                to = Offset(svSize.width, 0.0f),
                colors = listOf(
                    Color.hsv(hValue, MIN_SATURATION, MAX_VALUE),
                    Color.hsv(hValue, MAX_SATURATION, MAX_VALUE)
                ),
            )
            val valShader = LinearGradientShader(
                from = Offset.Zero,
                to = Offset(0.0f, svSize.height),
                colors = listOf(
                    Color.hsv(hValue, MIN_SATURATION, MIN_VALUE),
                    Color.hsv(hValue, MIN_SATURATION, MAX_VALUE)
                )
            )
            val paint = Paint()
            paint.shader = ComposeShader(valShader, satShader, PorterDuff.Mode.MULTIPLY)
            canvas.drawRect(0.0f, 0.0f, svSize.width, svSize.height, paint)
            if (!hasGesture) {
                svPosition = Offset(
                    svSize.width * convertFromRangeToFraction(sValue, MIN_SATURATION, MAX_SATURATION),
                    svSize.height * convertFromRangeToFraction(vValue, MIN_VALUE, MAX_VALUE)
                )
            }
        }
        bitmap
    }
    internal var hPosition by mutableFloatStateOf(0.0f)
    internal var hSize by mutableStateOf(IntSize.Zero)

    private var hasGesture = false
    internal var svPosition by mutableStateOf(Offset.Zero)
    internal var svSize by mutableStateOf(Size.Zero)
    private var hValue: Float by mutableFloatStateOf(0.0f)
    private var sValue: Float by mutableFloatStateOf(0.0f)
    private var vValue: Float by mutableFloatStateOf(0.0f)

    init {
        initHSV(this.initialColor)
    }

    public fun reset() {
        initHSV(this.initialColor)
        hPosition = hSize.width / 360.0f * hValue
        svPosition = Offset(
            svSize.width * convertFromRangeToFraction(sValue, MIN_SATURATION, MAX_SATURATION),
            svSize.height * convertFromRangeToFraction(vValue, MIN_VALUE, MAX_VALUE)
        )
    }

    public fun updateHPosition(x: Float, width: Int) {
        hasGesture = true
        hPosition = x.coerceIn(0f, width.toFloat())
        hValue = ((hPosition * 360.0f) / width).coerceIn(0.0f, 360.0f)
    }

    public fun updateSVPosition(position: Offset, size: IntSize) {
        hasGesture = true

        svPosition = position.copy(
            position.x.coerceIn(0.0f, size.width.toFloat()),
            position.y.coerceIn(0.0f, size.height.toFloat())
        )
        sValue = lerp(MIN_SATURATION, MAX_SATURATION, (1.0f / size.width) * position.x)
            .coerceIn(MIN_SATURATION, MAX_SATURATION)

        vValue = lerp(MIN_VALUE, MAX_VALUE, (1.0f / size.height) * position.y)
            .coerceIn(MIN_VALUE, MAX_VALUE)
    }

    private fun initHSV(color: Color) {
        val min = minOf(color.red, color.green, color.blue)
        val max = maxOf(color.red, color.green, color.blue)

        vValue = max.coerceIn(MIN_VALUE, MAX_VALUE)
        val delta = max - min
        if (delta < 1.0E-5) {
            sValue = MIN_SATURATION
            hValue = 0.0f
        } else if (max > 0.0f) {
            sValue = (delta / max).coerceIn(MIN_SATURATION, MAX_SATURATION)
            val blue = if (color.red >= max) {
                (color.green - color.blue) / delta
            } else {
                if (color.green >= max) {
                    2 + (color.blue - color.red) / delta
                } else {
                    4 + (color.red - color.green) / delta
                }
            }
            var h = blue * 60.0f
            if (h < 0.0f) {
                h += 360f
            }
            hValue = h.coerceIn(0.0f, 360.0f)
        } else {
            sValue = MIN_SATURATION
            hValue = 0.0f
        }
    }

    private fun convertFromRangeToFraction(value: Float, min: Float, max: Float): Float {
        return (value - min) / (max - min)
    }

    internal companion object {
        private const val MAX_SATURATION = 1f
        private const val MAX_VALUE = 1f
        private const val MIN_SATURATION = 0.15f
        private const val MIN_VALUE = 0.27f
        internal val Saver: Saver<ColorPickerState, Int> = Saver(
            { it.currentColor.toArgb() },
            { ColorPickerState(Color(it)) }
        )
    }
}
