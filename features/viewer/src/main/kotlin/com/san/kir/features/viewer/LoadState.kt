package com.san.kir.features.viewer

import com.davemorrissey.labs.subscaleview.ImageSource


internal sealed class LoadState {
    class Ready(val image: ImageSource, val imageSize: Long = 0, val downloadTime: Long = 0) : LoadState()
    object Init : LoadState()
    class Error(val exception: Throwable) : LoadState()
    class Load(val percent: Float) : LoadState()
}
