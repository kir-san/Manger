package com.san.kir.manger.components.DownloadManager

import android.content.Context
import android.content.Intent
import com.san.kir.manger.components.Main.Main
import com.san.kir.manger.room.DAO.insert
import com.san.kir.manger.room.models.DownloadItem
import com.san.kir.manger.utils.log
import java.util.*
import java.util.concurrent.Executors

class DownloadManager(private val context: Context) : Thread() {
    companion object {
        val action = "DownloadChapters"
    }

    private val taskQueue: Queue<DownloadTask> = object : LinkedList<DownloadTask>() {
        override fun poll(): DownloadTask {
            var task: DownloadTask? = super.poll()
            while (task == null) {
                try {
                    Thread.sleep(1000)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
                task = super.poll()
            }

            return task
        }
    }
    private var downloadingTasks: List<DownloadTask> = ArrayList()
    private val executor = Executors.newFixedThreadPool(4)

    var isRunning = false
        private set

    private fun startManage() {
        isRunning = true
        if (!isAlive) {
            start()
        }
        if (isInterrupted) {

        }
    }

    fun finish() {
        pauseAllTask()
        isRunning = false
        if (isAlive) {
            interrupt()
        }
    }

    override fun run() {
        while (isRunning) {
            val task = taskQueue.poll()
            task.executeOnExecutor(executor)
        }
    }

    fun addTask(item: DownloadItem) {
        var newItem = Main.db.downloadDao.loadItem(item.link)
        if (newItem == null) {
            Main.db.downloadDao.insert(item)

            newItem = Main.db.downloadDao.loadItem(item.link)
            while (newItem == null) {
                newItem = Main.db.downloadDao.loadItem(item.link)
            }
        }

        addTask(newDownloadTask(newItem))
    }

    private fun addTask(task: DownloadTask) {
        broadcastAddTask(task.downloadItem)
        downloadingTasks += task
        taskQueue.offer(task)
        if (!isAlive)
            startManage()
    }

    private fun broadcastAddTask(item: DownloadItem, isInterrupt: Boolean = false) {
        val intent = Intent(action)
        intent.putExtra(IndexConstants.TYPE,
                        IndexConstants.OperationIndex.ADD)
        intent.putExtra(IndexConstants.ITEM, item)
        intent.putExtra(IndexConstants.IS_PAUSED, isInterrupt)
        context.sendBroadcast(intent)
    }

    fun hasTask(item: DownloadItem): Boolean {
        return downloadingTasks.any { it.downloadItem.link == item.link }
    }

    fun pauseAllTask() {
        downloadingTasks.forEach { pauseTask(it) }
    }

    fun pauseTask(item: DownloadItem) {
        downloadingTasks.filter { it.downloadItem.link == item.link }.forEach { pauseTask(it) }
    }

    @Synchronized
    private fun pauseTask(task: DownloadTask) {
        task.cancel(false)
        try {
            downloadingTasks -= task
            taskQueue.remove(task)

            val intent = Intent(action)
            intent.putExtra(IndexConstants.TYPE,
                            IndexConstants.OperationIndex.PAUSE)
            intent.putExtra(IndexConstants.ITEM, task.downloadItem)
            context.sendBroadcast(intent)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun completeTask(task: DownloadTask) {
        if (downloadingTasks.contains(task)) {
            downloadingTasks -= task

            val intent = Intent(action)
            intent.putExtra(IndexConstants.TYPE,
                            IndexConstants.OperationIndex.COMPLETE)
            intent.putExtra(IndexConstants.ITEM, task.downloadItem)
            context.sendBroadcast(intent)
        }
    }

    private fun completeErrorTask(task: DownloadTask) {
        if (downloadingTasks.contains(task)) {
            downloadingTasks -= task

            val updateIntent = Intent(action)
            updateIntent.putExtra(IndexConstants.TYPE,
                                  IndexConstants.OperationIndex.ERROR)
            updateIntent.putExtra(IndexConstants.ITEM, task.downloadItem)
            context.sendBroadcast(updateIntent)
        }
    }


    private fun newDownloadTask(item: DownloadItem): DownloadTask {
        val taskListener = object : DownloadTaskListener {
            override fun preDownload(task: DownloadTask) {
                log("preDownload...${task.downloadItem}")
                // Сохранене в базе данных
                val updateIntent = Intent(action)
                updateIntent.putExtra(IndexConstants.TYPE,
                                      IndexConstants.OperationIndex.START)
                updateIntent.putExtra(IndexConstants.ITEM, task.downloadItem)
                context.sendBroadcast(updateIntent)
            }

            override fun updateProcess(task: DownloadTask) {
                val updateIntent = Intent(action)

                updateIntent.putExtra(IndexConstants.TYPE,
                                      IndexConstants.OperationIndex.UPDATE)
                updateIntent.putExtra(IndexConstants.ITEM, task.downloadItem)

                context.sendBroadcast(updateIntent)
            }

            override fun finishDownload(task: DownloadTask) {
                completeTask(task)
            }

            override fun errorDownload(task: DownloadTask, error: Throwable?) {
                completeErrorTask(task)
            }
        }

        return DownloadTask(item, taskListener)
    }


    fun getTotalTaskCount() = downloadingTasks.size
}
