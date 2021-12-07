package com.san.kir.ui.viewer

import com.davemorrissey.labs.subscaleview.ImageSource


internal sealed class LoadState {
    class Ready(val image: ImageSource) : LoadState()
    object Init : LoadState()
    class Error(val exception: Throwable) : LoadState()
    class Load(val percent: Float) : LoadState()
}
