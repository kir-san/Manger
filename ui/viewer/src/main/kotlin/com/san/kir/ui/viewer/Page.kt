package com.san.kir.ui.viewer

import android.os.Parcelable
import com.san.kir.data.models.Chapter
import kotlinx.parcelize.Parcelize

sealed class Page {
    object NonePrev : Page()
    object Prev : Page()
    @Parcelize
    data class Current(val pagelink: String, val chapter: Chapter = Chapter()) : Page(), Parcelable
    object Next : Page()
    object NoneNext : Page()
}
