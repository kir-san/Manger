package com.san.kir.manger.components.DownloadManager

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.san.kir.manger.room.models.DownloadItem

class DownloadControl {
    private var listeners: List<DownloadListener> = ArrayList()
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            if (intent.action == DownloadManager.action) {
                val type = intent.getIntExtra(IndexConstants.TYPE, -1)
                when (type) {
                    IndexConstants.OperationIndex.UPDATE -> {
                        actionForItem(intent) { onUpdate(it) }
                    }
                    IndexConstants.OperationIndex.COMPLETE -> {
                        actionForItem(intent) { onComplete(it) }
                    }
                    IndexConstants.OperationIndex.START -> {
                        actionForItem(intent) { onStart(it) }
                    }
                    IndexConstants.OperationIndex.PAUSE -> {
                        actionForItem(intent) { onPause(it) }
                    }
                    IndexConstants.OperationIndex.DELETE -> {
                        actionForItem(intent) { onDelete(it) }
                    }
                    IndexConstants.OperationIndex.ADD -> {
                        actionForItem(intent) { onAdd(it) }
                    }
                    IndexConstants.OperationIndex.DELETE_ALL -> {
                        listeners.forEach { it.onDeleteAll() }
                    }
                    IndexConstants.OperationIndex.RESUME_ALL -> {
                        actionForItems(intent) { onResumeAll(it) }
                    }
                    IndexConstants.OperationIndex.ERROR -> {
                        actionForItem(intent) { onError(it) }
                    }
                }
            }
        }

        private fun actionForItem(intent: Intent, action: DownloadListener.(DownloadItem) -> Unit) {
            val item = intent.getParcelableExtra<DownloadItem>(IndexConstants.ITEM)
            listeners.forEach { it.action(item) }
        }

        private fun actionForItems(intent: Intent,
                                   action: DownloadListener.(ArrayList<DownloadItem>) -> Unit) {
            val items = intent.getParcelableArrayListExtra<DownloadItem>(
                    IndexConstants.ITEMS)
            listeners.forEach { it.action(items) }
        }
    }

    lateinit var downloadManager: DownloadManager

    fun addListener(listener: DownloadListener) {
        listeners += listener
    }

    fun removeListener(listener: DownloadListener) {
        listeners -= listener
    }

    fun removeListeners() {
        listeners = ArrayList()
    }

    fun register(context: Context) {
        val intentFilter = IntentFilter().apply { addAction("DownloadChapters") }
        context.registerReceiver(receiver, intentFilter)
    }

    fun unregister(context: Context) {
        context.unregisterReceiver(receiver)
    }
}
