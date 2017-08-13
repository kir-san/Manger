package com.san.kir.manger.utils

import android.support.v4.app.NotificationCompat
import com.san.kir.manger.App
import com.san.kir.manger.EventBus.BusMain
import com.san.kir.manger.R
import com.san.kir.manger.components.Parsing.ManageSites
import com.san.kir.manger.dbflow.models.LatestChapter
import com.san.kir.manger.dbflow.models.Manga
import com.san.kir.manger.dbflow.wrapers.ChapterWrapper
import org.jetbrains.anko.notificationManager
import rx.Observable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers


object MangaUpdater {
    private val _context = App.context
    private val _notifyManager = _context.notificationManager
    private val _notify = NotificationCompat.Builder(_context)
            .setContentTitle("Ищу новые главы")
            .setContentText(" ")
            .setSmallIcon(R.drawable.ic_notify_updater)
            .setOngoing(true) // Основной нотификатор

    private var _catalog = mutableListOf<Manga>()
    private var _currentItem: Manga? = null
    private var _isWorked = false
    private var _main_observable: Observable<Int>? = null
    private var _main_subscription: Subscription? = null

    private var _progress = 0 // Прогресс проверенных манг
    private var _error = 0 // Счетчик закончившихся с ошибкой
    private var _fullCountNew = 0 // Количество новых глав

    // Сообщение передаваемое всем подписчикам
    data class Message(var manga: Manga, var isFoundNew: Boolean, var countNew: Int)

    // Шина для передачи сообщений
    val bus = BusMain<Message>()

    private fun _setWorking(work: Boolean) {
        if (work) {
            if (_isWorked)
            else {
                // Если только началась работа, сбросить прогресс и счетчик глав
                _progress = 0
                _fullCountNew = 0
                _error = 0
                _isWorked = true
            }
        } else {
            _notifyManager.cancel(0)
            _isWorked = false
            val end = NotificationCompat.InboxStyle(
                    NotificationCompat.Builder(_context)
                            .setContentTitle("Обновление завершено")
                            .setDefaults(NotificationCompat.DEFAULT_VIBRATE)
                            .setSmallIcon(R.drawable.ic_notify_updater)
            )
                    .addLine("Проверенно манг было: $_progress")
                    .addLine("Найдено новых глав: $_fullCountNew")
                    .addLine("С ошибкой проверенно: $_error")
                    .build()

            _notifyManager.notify(1, end)
        }
    }

    private fun _startTask(task: Manga) {
        var _isFindNew = false
        var _countNew = 0

        _main_observable = Observable.defer {
            Observable.create<Int> { sub ->
                _currentItem = task
                val work = _notify.setContentText(task.name)
                        .setProgress(_catalog.size, _progress, false)
                        .build().apply {
                    flags = NotificationCompat.FLAG_NO_CLEAR
                }
                _notifyManager.notify(0, work)

                sub.onNext(0)
                val chapters = ChapterWrapper.getChapters(task.unic)
                val newChapters = ManageSites.getOnlineChapters(task)!!.filter {
                    if (chapters.isNotEmpty()) {
                        chapters.none { chapter -> chapter.site == it.site }
                    } else true
                }.toList().toBlocking().single()

                if (newChapters.isNotEmpty()) {
                    newChapters.reversed().forEach {
                        it.save()
                        LatestChapter(it).insert()
                    }
                    _isFindNew = true
                    _countNew = newChapters.size
                }
                sub.onCompleted()
            }
        }.subscribeOn(Schedulers.io())

        _main_subscription = _main_observable!!
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ },
                           {
                               bus.post(Message(_currentItem!!, false, -1))
                               _progress++
                               _error++
                               _next()
                           },
                           {
                               bus.post(Message(_currentItem!!, _isFindNew, _countNew))
                               _fullCountNew += _countNew
                               _progress++
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

    fun contains(manga: Manga): Boolean {
        _catalog.forEach {
            if (it.unic == manga.unic)
                return true
        }
        return false
    }

    fun addTask(task: Manga) {
        _catalog.add(task)
        if (!_isWorked) {
            _next()
        }
    }

    fun cancelTask(task: Manga) {
        if (_currentItem != null) {
            if (_currentItem!!.unic == task.unic) {
                _main_subscription!!.unsubscribe()
                if (_main_subscription!!.isUnsubscribed) {
                    _setWorking(false)
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
