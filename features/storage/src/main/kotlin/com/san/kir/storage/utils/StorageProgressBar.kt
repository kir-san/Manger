package com.san.kir.storage.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.san.kir.core.compose.Dimensions

@Composable
internal fun StorageProgressBar(
    max: Double,
    full: Double,
    read: Double,
    modifier: Modifier = Modifier,
) {

    val fullPercent = if (max == 0.0) 0F else (full / max).toFloat()
    val readPercent = if (max == 0.0) 0F else (read / max).toFloat()

    Box(
        modifier = modifier
            .background(color = Color.LightGray, shape = RoundedCornerShape(Dimensions.smaller))
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(fullPercent)
                .background(Color(0xFFFF4081), shape = RoundedCornerShape(Dimensions.smaller))
        )
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(readPercent)
                .background(Color(0xFF222e7a), shape = RoundedCornerShape(Dimensions.smaller))
        )
    }
}
