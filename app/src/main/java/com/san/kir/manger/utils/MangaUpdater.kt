package com.san.kir.manger.utils

import android.support.v4.app.NotificationCompat
import com.san.kir.manger.App
import com.san.kir.manger.EventBus.BusMain
import com.san.kir.manger.R
import com.san.kir.manger.components.Main.Main
import com.san.kir.manger.components.Parsing.ManageSites
import com.san.kir.manger.room.DAO.insert
import com.san.kir.manger.room.models.LatestChapter
import com.san.kir.manger.room.models.Manga
import org.jetbrains.anko.notificationManager
import rx.Observable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers


object MangaUpdater {
    private val chapters = Main.db.chapterDao
    private val latestChapters = Main.db.latestChapterDao
    private val context = App.context
    private val notifyManager = context.notificationManager
    private val notify = NotificationCompat.Builder(context, "mangaUpdate")
            .setContentTitle("Ищу новые главы")
            .setContentText(" ")
            .setSmallIcon(R.drawable.ic_notify_updater)
            .setOngoing(true) // Основной нотификатор

    private var catalog = mutableListOf<Manga>()
    private var currentItem: Manga? = null
    private var isWorked = false
    private var mainObservable: Observable<Int>? = null
    private var mainSubscription: Subscription? = null

    private var progress = 0 // Прогресс проверенных манг
    private var error = 0 // Счетчик закончившихся с ошибкой
    private var fullCountNew = 0 // Количество новых глав

    // Сообщение передаваемое всем подписчикам
    data class Message(var manga: Manga, var isFoundNew: Boolean, var countNew: Int)

    // Шина для передачи сообщений
    val bus = BusMain<Message>()

    private fun setWorking(work: Boolean) {
        if (work) {
            if (isWorked)
            else {
                // Если только началась работа, сбросить прогресс и счетчик глав
                progress = 0
                fullCountNew = 0
                error = 0
                isWorked = true
            }
        } else {
            notifyManager.cancel(0)
            isWorked = false
            val end = NotificationCompat.InboxStyle(
                    NotificationCompat.Builder(context, "mangaUpdate")
                            .setContentTitle("Обновление завершено")
                            .setDefaults(NotificationCompat.DEFAULT_VIBRATE)
                            .setSmallIcon(R.drawable.ic_notify_updater)
            )
                    .addLine("Проверенно манг было: $progress")
                    .addLine("Найдено новых глав: $fullCountNew")
                    .addLine("С ошибкой проверенно: $error")
                    .build()

            notifyManager.notify(1, end)
        }
    }

    private fun startTask(task: Manga) {
        var isFindNew = false
        var countNew = 0

        mainObservable = Observable.defer {
            Observable.create<Int> { sub ->
                currentItem = task
                val work = notify.setContentText(task.name)
                        .setProgress(catalog.size, progress, false)
                        .build().apply {
                    flags = NotificationCompat.FLAG_NO_CLEAR
                }
                notifyManager.notify(0, work)

                sub.onNext(0)
                val oldChapters = chapters.loadChapters(task.unic)
                val newChapters = ManageSites.getOnlineChapters(task)!!.filter {
                    if (oldChapters.isNotEmpty()) {
                        oldChapters.none { chapter -> chapter.site == it.site }
                    } else true
                }.toList().toBlocking().single()

                if (newChapters.isNotEmpty()) {
                    newChapters.reversed().forEach {
                        chapters.insert(it)
                        latestChapters.insert(LatestChapter(it))
                    }
                    isFindNew = true
                    countNew = newChapters.size
                }
                sub.onCompleted()
            }
        }.subscribeOn(Schedulers.io())

        mainSubscription = mainObservable!!
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ },
                           {
                               bus.post(Message(currentItem!!, false, -1))
                               progress++
                               error++
                               next()
                           },
                           {
                               bus.post(Message(currentItem!!, isFindNew, countNew))
                               fullCountNew += countNew
                               progress++
                               next()
                           })
    }

    private fun next() {
        setWorking(true)
        if (currentItem == null)
            if (catalog.isNotEmpty())
                startTask(catalog.first())
            else
                setWorking(false)
        else
            if (catalog.remove(currentItem!!)) {
                currentItem = null
                next()
            } else
                log = "Произошли непредвиденные внезапности"
    }

    fun contains(manga: Manga): Boolean {
        catalog.forEach {
            if (it.unic == manga.unic)
                return true
        }
        return false
    }

    fun addTask(task: Manga) {
        catalog.add(task)
        if (!isWorked) {
            next()
        }
    }

    fun cancelTask(task: Manga) {
        if (currentItem != null) {
            if (currentItem!!.unic == task.unic) {
                mainSubscription!!.unsubscribe()
                if (mainSubscription!!.isUnsubscribed) {
                    setWorking(false)
                    next()
                }
            } else {
                if (catalog.contains(task))
                    catalog.remove(task)
            }
        }
    }

    fun cancelAll() {
        if (mainSubscription != null) {
            mainSubscription!!.unsubscribe()
            catalog.clear()
            setWorking(false)
        }
    }

}
