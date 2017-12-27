package com.san.kir.manger.components.DownloadManager

interface DownloadTaskListener {

    fun updateProcess(task: DownloadTask)

    fun finishDownload(task: DownloadTask)

    fun preDownload(task: DownloadTask)

    fun errorDownload(task: DownloadTask, error: Throwable?)
}
