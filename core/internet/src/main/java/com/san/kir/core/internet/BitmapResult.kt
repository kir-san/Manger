package com.san.kir.core.internet

import android.graphics.Bitmap

data class BitmapResult(
    val bitmap: Bitmap,
    val size: Long,
    val time: Long,
)

data class DownloadResult(
    val source: ByteArray,
    val contentLength: Long,
    val time: Long,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DownloadResult

        if (!source.contentEquals(other.source)) return false
        if (contentLength != other.contentLength) return false
        if (time != other.time) return false

        return true
    }

    override fun hashCode(): Int {
        var result = source.contentHashCode()
        result = 31 * result + contentLength.hashCode()
        result = 31 * result + time.hashCode()
        return result
    }
}
