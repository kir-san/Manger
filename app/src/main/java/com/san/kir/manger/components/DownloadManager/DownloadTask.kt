package com.san.kir.manger.components.DownloadManager

import android.accounts.NetworkErrorException
import android.os.AsyncTask
import android.util.Log
import com.san.kir.manger.components.Parsing.ManageSites
import com.san.kir.manger.room.models.DownloadItem
import com.san.kir.manger.room.models.DownloadStatus
import com.san.kir.manger.utils.getFullPath
import okio.Okio
import java.io.File
import java.io.IOException
import java.net.SocketException
import java.net.URL
import java.util.regex.Pattern

class DownloadTask(val downloadItem: DownloadItem,
                   val listener: DownloadTaskListener) : AsyncTask<Void, Int, Long>() {

    private var previousTime: Long = 0
    private var error: Throwable? = null
    var isInterrupted = false

    override fun onPreExecute() {
        Log.i(TAG, "onPreExecute")
        previousTime = System.currentTimeMillis()
        downloadItem.status = DownloadStatus.loading
        listener.preDownload(this)
    }

    override fun doInBackground(vararg params: Void): Long? {
        Log.i(TAG, "doInBackground")
        var result: Long = -1
        try {
            result = download()
        } catch (e: NetworkErrorException) {
            error = e
        } catch (e: IOException) {
            error = e
        } catch (e: SocketException) {
            error = e
        }
        return result
    }

    override fun onPostExecute(result: Long?) {
        downloadItem.totalTime = System.currentTimeMillis() - previousTime
        listener.updateProcess(this)
        if (result == -1L || isInterrupted || error != null) {
            downloadItem.status = DownloadStatus.error
            listener.errorDownload(this, error)
            return
        }
        // finish download
        downloadItem.status = DownloadStatus.completed
        listener.finishDownload(this)
    }

    private fun download(): Long {
        Log.i(TAG, "download")

        if (isCancelled)
            return downloadItem.downloadSize

        val downloadPath = getFullPath(downloadItem.path)
        downloadItem.downloadSize = 0
        downloadItem.downloadPages = 0
        listener.updateProcess(this)

        if (isCancelled)
            return downloadItem.downloadSize

        val pages = ManageSites.getPages(downloadItem)
        downloadItem.totalPages = pages.size

        if (isCancelled)
            return downloadItem.downloadSize

        listener.updateProcess(this)

        pages.forEach { _url ->
            val url = _url.removePrefix("\"").removeSuffix("\"")

            if (isCancelled)
                return@forEach

            // из ссылки получаю имя для файла
            val pat = Pattern.compile("[a-z0-9._-]+\\.[a-z]{3,4}")
                    .matcher(url.removeSurrounding("\"", "\""))
            var name = ""
            while (pat.find())
                name = pat.group()
            val file = File(downloadPath, name)

            if (isCancelled)
                return@forEach

            val urEl = URL(url)
            val urlConnection = urEl.openConnection()
            urlConnection.connect()
            val fileSize = urlConnection.contentLength.toLong()

            if (fileSize != -1L && file.exists() && file.length() == fileSize) {
                if (isCancelled)
                    return@forEach

                downloadItem.downloadPages += 1
                downloadItem.downloadSize += fileSize
                listener.updateProcess(this)
            } else {
                val body = ManageSites.openLink(url).body()
                val contentLength = body!!.contentLength()
//                downloadItem.totalSize += contentLength

                if (isCancelled)
                    return@forEach

                listener.updateProcess(this)

                val sink = Okio.buffer(Okio.sink(file))
                sink.writeAll(body.source())
                sink.close()

                if (isCancelled)
                    return@forEach

                downloadItem.downloadSize += contentLength
                downloadItem.downloadPages += 1
                listener.updateProcess(this)
            }
        }

        return downloadItem.downloadSize

    }

    override fun toString(): String {
        return ("DownloadTask [item=$downloadItem]")
    }

    companion object {
        private val TAG = DownloadTask::class.java.name
    }

}
