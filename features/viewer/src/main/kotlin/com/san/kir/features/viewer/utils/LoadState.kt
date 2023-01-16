package com.san.kir.features.viewer.utils

import com.davemorrissey.labs.subscaleview.ImageSource


internal sealed interface LoadState {
    data class Ready(
        val image: ImageSource,
        val imageSize: Long = 0,
        val downloadTime: Long = 0,
    ) : LoadState

    data object Init : LoadState
    data class Error(val exception: Throwable) : LoadState
    data class Load(val percent: Float) : LoadState
}
