package com.san.kir.manger.components.ChaptersDownloader

import android.app.PendingIntent
import android.content.Intent
import android.support.v4.app.NotificationCompat
import com.san.kir.manger.App
import com.san.kir.manger.EventBus.BusMain
import com.san.kir.manger.R
import com.san.kir.manger.components.Main.MainActivity
import com.san.kir.manger.components.Parsing.ManageSites
import com.san.kir.manger.dbflow.models.Chapter
import com.san.kir.manger.dbflow.models.DownloadItem
import com.san.kir.manger.dbflow.models.LatestChapter
import com.san.kir.manger.utils.downloadTo
import com.san.kir.manger.utils.log
import org.jetbrains.anko.notificationManager
import rx.Observable
import rx.Subscription
import rx.schedulers.Schedulers
import java.io.File
import java.util.regex.Pattern

object ChaptersDownloader {
    private val _context get() = App.context
    private val _notifyManager = _context.notificationManager
    private val intent = Intent(_context, MainActivity::class.java).apply {
        putExtra("launch", "download")
    }
    private val _notify: NotificationCompat.Builder = NotificationCompat.Builder(_context)
            .setContentTitle("Скачиваю главы")
            .setContentText(" ")
            .setSmallIcon(R.drawable.ic_action_download_green)
            .setContentIntent(PendingIntent.getActivities(_context, 0, arrayOf(intent), 0))

    private var _catalog = mutableListOf<DownloadItem>()
    private var _currentItem: DownloadItem? = null
    private var _isWorked = false
    private var _main_observable: Observable<DownloadItem>? = null
    private var _main_subscription: Subscription? = null

    private var _progress = 0 // Количество скачаных манг
    private var _error = 0 // Счетчик закончившихся с ошибкой
    private var _fullCount = 0 // Общее количество глав

    val bus = BusMain<DownloadItem>()

    val catalog: List<DownloadItem>
        get() = _catalog

    private fun _setWorking(work: Boolean) {
        if (work) {
            if (_isWorked)
            else {
                // Если только началась работа, сбросить прогресс и счетчик глав
                _progress = 0
                _fullCount = 0
                _error = 0
                _isWorked = true
            }
        } else {
            _currentItem = null
            _notifyManager.cancel(2)
            _isWorked = false
            val end = NotificationCompat.InboxStyle(
                    NotificationCompat.Builder(_context)
                            .setContentTitle("Главы скачаны")
                            .setDefaults(NotificationCompat.DEFAULT_VIBRATE)
                            .setSmallIcon(R.drawable.ic_action_download_green)
            )
                    .addLine("Глав отправленно на загрузку: $_fullCount")
                    .addLine("Из них скачано: $_progress")
                    .addLine("Из них с ошибками: $_error")
                    .build()
            _notifyManager.notify(3, end)
        }
    }

    private fun _startTask(task: DownloadItem) {
        _main_observable = ManageSites.getPageList(task, Observable.just(task)
                .doOnNext {
                    _currentItem = it
                })
                .doOnNext { _currentItem!!.max.item = it.size }
                .doOnNext {
                    val work = _notify.setContentText(task.name)
                            .setProgress(it.size, 0, false)
                            .build().apply {
                        flags = NotificationCompat.FLAG_NO_CLEAR
                    }
                    _notifyManager.notify(2, work)
                }
                .flatMap {
                    Observable.from(it)
                            .subscribeOn(Schedulers.computation())
                            .map { it.removeSurrounding("\"", "\"") }
                            .map {
                                val pat = Pattern.compile("[a-z0-9._-]+\\.[a-z]{3,4}").matcher(
                                        it) // из ссылки получаю имя для файла
                                var name: String = ""
                                while (pat.find())
                                    name = pat.group()

                                File(_currentItem!!.path, name) to it
                            }
                            .observeOn(Schedulers.io())
                            .map {
                                val (img: File, url: String) = it
                                ManageSites.openLink(url).downloadTo(img)
                                _currentItem
                            }
                }


        _main_subscription = _main_observable!!
                .subscribe({
                               _currentItem!!.progress.item += 1
                               val work = _notify.setContentText(task.name)
                                       .setProgress(_currentItem!!.max.item,
                                                    _currentItem!!.progress.item,
                                                    false)
                                       .build().apply {
                                   flags = NotificationCompat.FLAG_NO_CLEAR
                               }
                               _notifyManager.notify(2, work)

                           }, {
                               it.printStackTrace()
                               bus.post(_currentItem!!)
                               _error++
                               _fullCount++
                               _next()
                           }, {
                               bus.post(_currentItem!!)
                               _progress++
                               _fullCount++
                               _next()
                           })
    }

    private fun _next() {
        _setWorking(true)
        if (_currentItem == null)
            if (_catalog.isNotEmpty())
                _startTask(_catalog.first())
            else
                _setWorking(false)
        else
            if (_catalog.remove(_currentItem!!)) {
                _currentItem = null
                _next()
            } else
                log = "Произошли непредвиденные внезапности"
    }

    private fun contains(chapter: Chapter): Boolean {
        val item = getTask(chapter)
        _catalog.forEach {
            if (it.link == item.link)
                return true
        }
        return false
    }

    fun getTask(chapter: Chapter): DownloadItem {
        _catalog.forEach { item ->
            if (item.link == chapter.site)
                return item
        }
        return DownloadItem("${chapter.manga} ${chapter.name}", chapter.site, chapter.path)
    }

    fun getTask(chapter: LatestChapter): DownloadItem {
        _catalog.forEach { item ->
            if (item.link == chapter.site)
                return item
        }
        return DownloadItem("${chapter.manga} ${chapter.name}", chapter.site, chapter.path)
    }

    fun addTask(task: DownloadItem) {
        _catalog.add(task)
        if (!_isWorked) {
            _next()
        }
    }

    fun addTask(chapter: Chapter) {
        addTask(getTask(chapter))
    }

    fun addTask(chapter: LatestChapter) {
        addTask(getTask(chapter))
    }

    fun cancelTask(task: DownloadItem) {
        if (_currentItem != null) {
            if (_currentItem!!.link == task.link) {
                _main_subscription!!.unsubscribe()
                if (_main_subscription!!.isUnsubscribed) {
                    _setWorking(false)
                    if (_catalog.contains(task))
                        _catalog.remove(task)
                    _next()
                }
            } else {
                if (_catalog.contains(task))
                    _catalog.remove(task)
            }
        }
    }

    fun cancelAll() {
        if (_main_subscription != null) {
            _main_subscription!!.unsubscribe()
            _catalog.clear()
            _setWorking(false)
        }
    }
}
