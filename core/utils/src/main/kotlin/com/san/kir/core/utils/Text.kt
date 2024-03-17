package com.san.kir.core.utils

import androidx.annotation.StringRes

sealed interface Text {
    data class Resource(@StringRes val id: Int) : Text
    data class Simple(val text: String) : Text
}
