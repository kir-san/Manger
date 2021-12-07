package com.san.kir.manger.utils.compose

import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LayoutModifier
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.unit.Constraints

fun Modifier.squareMaxSize() = this.then(SquareMaxSizeModifier())

class SquareMaxSizeModifier : LayoutModifier {
    override fun MeasureScope.measure(
        measurable: Measurable,
        constraints: Constraints
    ): MeasureResult {
        val minWidth: Int = constraints.minWidth
        val maxWidth: Int = constraints.maxWidth
        val minHeight: Int = constraints.minHeight
        val maxHeight: Int = constraints.maxHeight

        val placeable = measurable.measure(
            Constraints(minWidth, maxWidth, minWidth, maxWidth)
        )

        return layout(placeable.width, placeable.height) {
            placeable.placeRelative(0, 0)
        }
    }

}
