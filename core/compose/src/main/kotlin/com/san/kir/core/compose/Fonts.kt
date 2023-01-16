package com.san.kir.core.compose

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

object Fonts {
    object Size {
        val less = 13.sp
        val default = 14.sp
        val bigger = 16.sp
    }

    object Style {
        val bigBoldCenter = TextStyle(
            fontWeight = FontWeight.Bold,
            fontSize = Size.bigger,
            textAlign = TextAlign.Center
        )
    }

    object Annotated {
        fun bold(end: Int) = listOf(AnnotatedString.Range(SpanStyle(fontWeight = FontWeight.Bold),
            start = 0,
            end = end))

    }
}
