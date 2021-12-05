package com.san.kir.ui.viewer

import com.san.kir.data.models.Chapter

sealed class Page {
    object NonePrev : Page()
    object Prev : Page()
    data class Current(val pagelink: String, val chapter: Chapter = Chapter()) : Page()
    object Next : Page()
    object NoneNext : Page()
}
