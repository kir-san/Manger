package com.san.kir.features.viewer.utils

import android.os.Parcelable
import com.san.kir.data.models.base.Chapter
import kotlinx.parcelize.Parcelize

sealed class Page {
    object NonePrev : Page()
    object Prev : Page()
    @Parcelize
    data class Current(val pagelink: String, val chapter: Chapter = Chapter()) : Page(), Parcelable
    object Next : Page()
    object NoneNext : Page()
}
