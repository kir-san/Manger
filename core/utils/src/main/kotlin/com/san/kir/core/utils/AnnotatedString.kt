package com.san.kir.core.utils

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle

fun AnnotatedString.Builder.append(text: String, textStyle: TextStyle) {
    append(AnnotatedString(text, textStyle.toSpanStyle(), textStyle.toParagraphStyle()))
}
