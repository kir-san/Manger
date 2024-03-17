package com.san.kir.core.utils

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

fun findInGoogle(name: String) =
    "https://www.google.com/s2/favicons?domain=$name"

fun Text.text(context: Context): CharSequence {
    return when (this) {
        is Text.Resource -> context.getString(id)
        is Text.Simple -> text
    }
}

@Composable
fun Text.text(): CharSequence {
    val context = LocalContext.current
    return text(context)
}
