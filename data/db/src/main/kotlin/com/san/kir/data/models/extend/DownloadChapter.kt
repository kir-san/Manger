package com.san.kir.data.models.extend

import android.content.Context
import androidx.compose.runtime.Stable
import androidx.room.ColumnInfo
import androidx.room.Ignore
import com.san.kir.core.support.DownloadState
import com.san.kir.core.utils.TimeFormat
import com.san.kir.core.utils.bytesToMb
import com.san.kir.core.utils.format

@Stable
data class DownloadChapter(
    @ColumnInfo(name = "id") val id: Long,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "manga") val manga: String,
    @ColumnInfo(name = "logo") val logo: String,
    @ColumnInfo(name = "status") val status: DownloadState,
    @ColumnInfo(name = "totalTime") private val totalTime: Long,
    @ColumnInfo(name = "downloadSize") private val downloadSize: Long,
    @ColumnInfo(name = "downloadPages") val downloadPages: Int,
    @ColumnInfo(name = "pages") val pages: List<String>,
) {
    @Ignore
    val progress =
        if (pages.isNotEmpty()) downloadPages.toFloat() / pages.size.toFloat() else 0F

    @Ignore
    val size = bytesToMb(downloadSize).format()

    fun time(ctx: Context) = TimeFormat(totalTime / 1000).toString(ctx)
}
