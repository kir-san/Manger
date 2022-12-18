package com.san.kir.core.internet

import android.graphics.Bitmap

data class BitmapResult(
    val bitmap: Bitmap,
    val size: Long,
    val time: Long,
)

data class DownloadResult(
    val size: Long,
    val time: Long,
)
