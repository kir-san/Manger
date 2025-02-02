package com.san.kir.core.utils

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

public fun findInGoogle(name: String): String =
    "https://www.google.com/s2/favicons?domain=$name"

public fun String.asHttp(): String {
    val prepare = trim().removeSurrounding("\"", "\"").trim()
    return when {
        prepare.contains("https") -> prepare.replace("https", "http")
        prepare.contains("http") -> prepare
        else -> {
            prepare.removePrefix(":").removePrefix("/").removePrefix("/")
            "http://$prepare"
        }
    }
}

public fun String.asHttps(): String {
    val prepare = trim().removeSurrounding("\"", "\"").trim()
    return when {
        prepare.contains("https") -> prepare
        prepare.contains("http") -> prepare.replace("http", "https")
        else -> {
            prepare.removePrefix(":").removePrefix("/").removePrefix("/")
            "https://$prepare"
        }
    }
}

public fun Text.text(context: Context): CharSequence {
    return when (this) {
        is Text.Resource -> context.getString(id)
        is Text.Simple -> text
    }
}

@Composable
public fun Text.text(): CharSequence {
    val context = LocalContext.current
    return text(context)
}
