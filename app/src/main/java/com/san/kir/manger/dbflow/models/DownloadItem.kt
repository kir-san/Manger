package com.san.kir.manger.dbflow.models

import com.san.kir.manger.EventBus.Binder
import com.san.kir.manger.utils.getFullPath
import java.io.File

class DownloadItem(val name: String,
                   val link: String,
                   path: String) {
    val path: File = getFullPath(path)
    var max: Binder<Int> = Binder(1)
    var progress: Binder<Int> = Binder(0)
}
