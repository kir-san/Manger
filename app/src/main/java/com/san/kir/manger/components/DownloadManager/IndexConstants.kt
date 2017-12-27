package com.san.kir.manger.components.DownloadManager

object IndexConstants {
    object OperationIndex {
        val UPDATE = 0
        val COMPLETE = 1
        val START = 2
        val PAUSE = 3
        val DELETE = 4
        val RESUME = 5
        val ADD = 6
        val DELETE_ALL = 7
        val RESUME_ALL = 8
        val ERROR = 10
    }

    val TYPE = "type"
    val TOTAL_SIZE = "total_size"
    val DOWNLOAD_SIZE = "download_size"
    val URL = "url"
    val IS_PAUSED = "is_paused"
    val ITEM = "item"
    val ITEMS = "items"
}
