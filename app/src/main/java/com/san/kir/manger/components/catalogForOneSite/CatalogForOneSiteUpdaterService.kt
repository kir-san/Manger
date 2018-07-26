package com.san.kir.manger.components.catalogForOneSite

import android.annotation.SuppressLint
import android.app.IntentService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.support.v4.app.NotificationCompat
import com.san.kir.manger.R
import com.san.kir.manger.components.main.Main
import com.san.kir.manger.components.parsing.ManageSites
import com.san.kir.manger.components.sitesCatalog.SiteCatalogActivity
import com.san.kir.manger.room.dao.contain
import com.san.kir.manger.room.dao.updateAsync
import com.san.kir.manger.room.models.SiteCatalogElement
import com.san.kir.manger.utils.ID
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.channels.ProducerJob
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.newFixedThreadPoolContext
import kotlinx.coroutines.experimental.runBlocking
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.notificationManager

class CatalogForOneSiteUpdaterService : IntentService(TAG) {
    companion object {
        const val ACTION_CATALOG_UPDATER_SERVICE =
            "kir.san.manger.CatalogForOneSiteUpdaterService.UPDATE"
        const val ACTION_CANCEL_ALL = "kir.san.manger.CatalogForOneSiteUpdaterService.CANCEL_ALL"

        const val EXTRA_KEY_OUT = "EXTRA_OUT"

        private const val channelId = "CatalogUpdaterId"
        private const val TAG = "CatalogForOneSiteUpdaterService"
        private const val name = "CatalogForOneSiteUpdaterServiceName"
        private const val description = "CatalogForOneSiteUpdaterServiceDescription"
        private var taskCounter = listOf<Int>()
        fun isContain(id: Int) = taskCounter.contains(id)
    }

    private var catalog: ProducerJob<SiteCatalogElement>? = null
    private val notificationId = ID.generate()
    private val actionGoToCatalogs by lazy {
        val intent = intentFor<SiteCatalogActivity>()
        PendingIntent.getActivity(this, 0, intent, 0)
    }
    private val actionCancelAll by lazy {
        val intent = intentFor<CatalogForOneSiteUpdaterService>().setAction(ACTION_CANCEL_ALL)
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
    private var isError = false

    @SuppressLint("InlinedApi")
    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager.getNotificationChannel(channelId) == null) {
                val importance = NotificationManager.IMPORTANCE_DEFAULT

                NotificationChannel(channelId, name, importance).apply {
                    description = CatalogForOneSiteUpdaterService.description
                    notificationManager.createNotificationChannel(this)
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (intent.action == ACTION_CANCEL_ALL) {
            stopSelf()
        }
        taskCounter += intent.getIntExtra("id", -1)
        return super.onStartCommand(intent, flags, startId)
    }


    override fun onHandleIntent(intent: Intent) {
        runBlocking(CommonPool) {
            try {
                val site = ManageSites.CATALOG_SITES[intent.getIntExtra("id", -1)]
                val siteDb = Main.db.siteDao.loadSite(site.name)

                with(NotificationCompat.Builder(this@CatalogForOneSiteUpdaterService, channelId)) {
                    setSmallIcon(R.drawable.ic_notification_update)
                    setContentTitle(
                        getString(R.string.catalog_fos_service_notify_title, taskCounter.size)
                    )
                    setContentText(getString(R.string.catalog_fos_service_notify_text, site.name))
                    startForeground(notificationId, build())
                }

                val viewModel = SiteCatalogElementViewModel.setSiteId(site.id)
                viewModel.clearDb()
                var counter = 0

                site.init()

                val loadContext = newFixedThreadPoolContext(2, "LoadContext")
                catalog = site.getCatalog(loadContext)
                catalog?.consumeEach { newElement ->
                    newElement.isAdded = Main.db.mangaDao.contain(newElement)
                    viewModel.insert(newElement)
                    counter++
                    with(
                        NotificationCompat.Builder(
                            this@CatalogForOneSiteUpdaterService,
                            channelId
                        )
                    ) {
                        setSmallIcon(R.drawable.ic_notification_update)
                        setContentTitle(
                            getString(R.string.catalog_fos_service_notify_title_2, taskCounter.size)
                        )
                        setContentText("${siteDb?.name}  ${((counter.toFloat() / site.volume.toFloat()) * 100).toInt()}%")
                        setProgress(site.volume, counter, false)
                        addAction(actionCancelAll)
                        startForeground(notificationId, build())
                    }
                }

                siteDb?.oldVolume = counter
                Main.db.siteDao.updateAsync(siteDb)

                catalog?.isActive?.let {
                    if (it) {
                        val responseIntent = Intent()
                        responseIntent.putExtra(EXTRA_KEY_OUT, site.id)
                        responseIntent.action = ACTION_CATALOG_UPDATER_SERVICE
                        sendBroadcast(responseIntent)

                        taskCounter -= site.id
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                isError = true
                stopSelf()
            } finally {

            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        stopForeground(false)

        catalog?.invokeOnCompletion {
            val responseIntent = Intent()
            responseIntent.putExtra(EXTRA_KEY_OUT, -2)
            responseIntent.action = ACTION_CATALOG_UPDATER_SERVICE

            sendBroadcast(responseIntent)

            with(NotificationCompat.Builder(this@CatalogForOneSiteUpdaterService, channelId)) {
                setSmallIcon(R.drawable.ic_notification_update)
                setContentTitle(getString(R.string.catalog_fos_service_notify_complete))
                setContentText("")
                if (isError) {
                    setContentTitle(getString(R.string.catalog_fos_service_notify_error_title))
                    setContentText(getString(R.string.catalog_fos_service_notify_error_text))
                }
                setContentIntent(actionGoToCatalogs)
                notificationManager.notify(notificationId, build())
            }
        }

        catalog?.cancel()
        taskCounter = emptyList()
        stopSelf()
    }
}
