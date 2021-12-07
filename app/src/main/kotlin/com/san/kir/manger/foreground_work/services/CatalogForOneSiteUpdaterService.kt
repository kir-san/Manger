package com.san.kir.manger.foreground_work.services

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import android.os.Message
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import androidx.core.net.toUri
import com.san.kir.ankofork.sdk28.notificationManager
import com.san.kir.core.utils.log
import com.san.kir.data.db.CatalogDb.Factory
import com.san.kir.data.db.dao.MangaDao
import com.san.kir.data.db.dao.SiteDao
import com.san.kir.data.models.SiteCatalogElement
import com.san.kir.manger.R
import com.san.kir.data.parsing.SiteCatalogsManager
import com.san.kir.manger.ui.MainActivity
import com.san.kir.manger.ui.application_navigation.catalog.CatalogsNavTarget
import com.san.kir.manger.utils.ID
import com.san.kir.manger.utils.extensions.intentFor
import com.san.kir.manger.utils.extensions.startService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class CatalogForOneSiteUpdaterService : Service() {
    companion object {
        const val ACTION_CATALOG_UPDATER_SERVICE =
            "kir.san.manger.CatalogForOneSiteUpdaterService.UPDATE"
        const val ACTION_CANCEL_ALL = "kir.san.manger.CatalogForOneSiteUpdaterService.CANCEL_ALL"

        const val EXTRA_KEY_OUT = "EXTRA_OUT"

        const val INTENT_DATA = "siteName"

        private const val channelId = "CatalogUpdaterId"
        private const val TAG = "CatalogForOneSiteUpdaterService"
        private const val name = "CatalogForOneSiteUpdaterServiceName"
        private const val descriptions = "CatalogForOneSiteUpdaterServiceDescription"

        private var taskCounter = listOf<String>()
        fun isContain(name: String) = taskCounter.contains(name)
        fun add(ctx: Context, name: String) {
           startService<CatalogForOneSiteUpdaterService>(ctx, INTENT_DATA to name)
        }
        fun addIfNotContain(ctx: Context, name: String) {
            if (isContain(name).not()) {
                add(ctx, name)
            }
        }
    }

    @Volatile
    private lateinit var mServiceLopper: Looper

    @Volatile
    private lateinit var mServiceHandler: ServiceHandler

    private var notificationId = ID.generate()

    private val actionGoToCatalogs by lazy {
        val deepLinkIntent = Intent(
            Intent.ACTION_VIEW,
            CatalogsNavTarget.Main.deepLink.toUri(),
            this,
            MainActivity::class.java
        )
        TaskStackBuilder.create(this).run {
            addNextIntentWithParentStack(deepLinkIntent)
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        }
    }

    private val actionCancelAll by lazy {
        val intent = intentFor<CatalogForOneSiteUpdaterService>(this).setAction(ACTION_CANCEL_ALL)
        val cancelAll = PendingIntent.getService(this, 0, intent, 0)
        NotificationCompat
            .Action
            .Builder(
                R.drawable.ic_notification_cancel,
                getString(R.string.catalog_fos_service_action_cancel_all),
                cancelAll
            )
            .build()
    }
    private var job: Job = Job()
    private var isError = false
    private var isManualStop = false

    @Inject
    lateinit var manager: SiteCatalogsManager

    private val default = Dispatchers.Default

    @Inject
    lateinit var mangaDao: MangaDao

    @Inject
    lateinit var siteDao: SiteDao

    @Inject
    lateinit var dbFactory: Factory

    override fun onBind(intent: Intent?): IBinder? = null

    @SuppressLint("InlinedApi")
    override fun onCreate() {

        val thread = HandlerThread(TAG)
        thread.start()

        mServiceLopper = thread.looper
        mServiceHandler = ServiceHandler(mServiceLopper, this)

        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }

        setForeground()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val chan = NotificationChannel(channelId, TAG, NotificationManager.IMPORTANCE_LOW)
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE

        notificationManager.createNotificationChannel(chan)
    }

    private fun setForeground() {
        with(NotificationCompat.Builder(this, channelId)) {
            setSmallIcon(R.drawable.ic_notification_update)
            setContentTitle(getString(R.string.catalog_fos_service_title))
            setContentText(getString(R.string.catalog_fos_service_message))
            setContentIntent(actionGoToCatalogs)
            priority = NotificationCompat.PRIORITY_MIN
            setAutoCancel(true)
            startForeground(ID.generate(), build())
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (intent.action == ACTION_CANCEL_ALL) {
            job.cancel()
            stopSelf()
            isManualStop = true
        } else {
            intent.getStringExtra(INTENT_DATA)?.let { task ->
                taskCounter = taskCounter + task
                val msg = mServiceHandler.obtainMessage()
                msg.arg1 = startId
                msg.obj = task
                mServiceHandler.sendMessage(msg)
            }

        }
        setForeground()
        return super.onStartCommand(intent, flags, startId)
    }


    fun onHandleIntent(siteName: String) = runBlocking(default) {
        launch {
            try {
                val site = manager.catalog.first { it.name == siteName }
                val siteDb = siteDao.getItem(site.name)

                with(NotificationCompat.Builder(this@CatalogForOneSiteUpdaterService, channelId)) {
                    setSmallIcon(R.drawable.ic_notification_update)
                    setContentTitle(
                        getString(R.string.catalog_fos_service_notify_title, taskCounter.size)
                    )
                    setContentText(
                        getString(R.string.catalog_fos_service_notify_text, site.name)
                    )
                    startForeground(notificationId, build())
                }


                var counter = 0
                var percent = 0
                val tempList = mutableListOf<SiteCatalogElement>()

                site.init()
                var retry = 3
                while (retry != 0) {
                    retry--
                    counter = 0
                    tempList.clear()

                    site.getCatalog()
                        .onEach {
                            counter++
                            val new = ((counter.toFloat() / site.volume.toFloat()) * 100).toInt()
                            if (new != percent) {
                                percent = new
                                with(
                                    NotificationCompat.Builder(
                                        this@CatalogForOneSiteUpdaterService, channelId
                                    )
                                ) {
                                    setSmallIcon(R.drawable.ic_notification_update)
                                    setContentTitle(
                                        getString(
                                            R.string.catalog_fos_service_notify_title_2,
                                            taskCounter.size
                                        )
                                    )
                                    setContentText("${siteDb?.name}  ${percent}%")
                                    setProgress(site.volume, counter, false)
                                    addAction(actionCancelAll)
                                    startForeground(notificationId, build())
                                }
                            }

                            log("$counter / ${site.volume}")
                        }
                        .map { el ->
                            el.isAdded = mangaDao.getItems().any { it.shortLink == el.shotLink }
                            el
                        }
                        .toList(tempList)
                    if (tempList.size >= site.volume - 10) break
                }

                log("update finish. elements getting ${tempList.size}")

                dbFactory.create(site.name).apply {
                    dao.deleteAll()
                    dao.insert(*tempList.toTypedArray())
                    close()
                }

                log("save items in db")

                siteDb?.oldVolume = counter
                siteDao.update(siteDb)

                log("save counter in db")

                sendPositiveBroadcast(site.name)

                taskCounter = taskCounter - site.name
            } catch (e: Exception) {
                log("error")
                e.printStackTrace()
                isError = true
            } finally { //
                log("finally")
            }
        }.join()
    }


    override fun onDestroy() {
        super.onDestroy()

        log("onDestroy")

        job.cancel()
        stopForeground(false)

        with(NotificationCompat.Builder(this@CatalogForOneSiteUpdaterService, channelId)) {
            setSmallIcon(R.drawable.ic_notification_update)
            setContentTitle(getString(R.string.catalog_fos_service_notify_complete))
            if (isError && !isManualStop) {
                setContentTitle(getString(R.string.catalog_fos_service_notify_error_title))
                setContentText(getString(R.string.catalog_fos_service_notify_error_text))
                sendNegativeBroadcast()
            } else if (isManualStop) {
                setContentText(getString(R.string.catalog_fos_service_notify_manual_stop_text))
                setContentTitle(getString(R.string.catalog_fos_service_notify_manual_stop_title))
                sendNeutralBroadcast()
            }
            setContentIntent(actionGoToCatalogs)

            stopForeground(false)
            notificationManager.cancel(notificationId)
            notificationManager.notify(notificationId, build())
        }

        notificationId = ID.generate()

        taskCounter = emptyList()
    }

    private fun sendPositiveBroadcast(catalogName: String) {
        Intent().apply {
            action = ACTION_CATALOG_UPDATER_SERVICE
            putExtra(EXTRA_KEY_OUT, catalogName)

            sendBroadcast(this)
        }
    }

    private fun sendNeutralBroadcast() {
        Intent().apply {
            action = ACTION_CATALOG_UPDATER_SERVICE
            putExtra(EXTRA_KEY_OUT, "destroy")

            sendBroadcast(this)
        }
    }

    private fun sendNegativeBroadcast() {
        Intent().apply {
            action = ACTION_CATALOG_UPDATER_SERVICE
            putExtra(EXTRA_KEY_OUT, "error")

            sendBroadcast(this)
        }
    }

    private open class ServiceHandler(
        looper: Looper,
        val service: CatalogForOneSiteUpdaterService,
    ) : Handler(looper) {
        override fun handleMessage(msg: Message) {
            service.onHandleIntent(msg.obj as String)
            service.stopSelf(msg.arg1)
        }
    }
}
