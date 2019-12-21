package com.san.kir.manger.components.catalog_for_one_site

import android.annotation.SuppressLint
import android.app.IntentService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.san.kir.ankofork.intentFor
import com.san.kir.ankofork.sdk28.notificationManager
import com.san.kir.manger.R
import com.san.kir.manger.components.parsing.ManageSites
import com.san.kir.manger.components.sites_catalog.SiteCatalogActivity
import com.san.kir.manger.repositories.MangaRepository
import com.san.kir.manger.repositories.SiteCatalogRepository
import com.san.kir.manger.repositories.SiteRepository
import com.san.kir.manger.room.entities.SiteCatalogElement
import com.san.kir.manger.utils.ID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

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
        private var taskCounter = listOf<String>()
        fun isContain(catalogName: String) = taskCounter.contains(catalogName)
    }

    private var notificationId = ID.generate()
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
    private var job: Job = Job()
    private var isError = false
    private var isManualStop = false

    @SuppressLint("InlinedApi")
    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
            && notificationManager.getNotificationChannel(channelId) == null) {

            val importance = NotificationManager.IMPORTANCE_LOW

            NotificationChannel(channelId, name, importance).apply {
                description = CatalogForOneSiteUpdaterService.description
                notificationManager.createNotificationChannel(this)
            }
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (intent.action == ACTION_CANCEL_ALL) {
            job.cancel()
            stopSelf()
            isManualStop = true
        } else {
            taskCounter = taskCounter + intent.getStringExtra("catalogName")
        }
        return super.onStartCommand(intent, flags, startId)
    }


    override fun onHandleIntent(intent: Intent) {
        runBlocking(Dispatchers.Default) {
            job = launch {
                try {
                    val siteRepository = SiteRepository(this@CatalogForOneSiteUpdaterService)
                    val mangaRepository = MangaRepository(this@CatalogForOneSiteUpdaterService)
                    val site = ManageSites.CATALOG_SITES
                        .first { it.catalogName == intent.getStringExtra("catalogName") }
                    val siteDb = siteRepository.getItem(site.name)

                    with(
                        NotificationCompat.Builder(
                            this@CatalogForOneSiteUpdaterService, channelId
                        )
                    ) {
                        setSmallIcon(R.drawable.ic_notification_update)
                        setContentTitle(
                            getString(R.string.catalog_fos_service_notify_title, taskCounter.size)
                        )
                        setContentText(
                            getString(
                                R.string.catalog_fos_service_notify_text,
                                site.name
                            )
                        )
                        startForeground(notificationId, build())
                    }


                    var counter = 0
                    val tempList = mutableListOf<SiteCatalogElement>()

                    site.init()
                    site.getCatalog()
                        .onEach {
                            counter++
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
                                setContentText("${siteDb?.name}  ${((counter.toFloat() / site.volume.toFloat()) * 100).toInt()}%")
                                setProgress(site.volume, counter, false)
                                addAction(actionCancelAll)
                                startForeground(notificationId, build())
                            }
                        }
                        .map { el ->
                            el.isAdded = mangaRepository.contain(el)
                            el
                        }
                        .toList(tempList)

                    SiteCatalogRepository(
                        this@CatalogForOneSiteUpdaterService, site.catalogName
                    ).apply {
                        clearDb()
                        insert(*tempList.toTypedArray())
                        close()
                    }

                    siteDb?.oldVolume = counter
                    siteRepository.update(siteDb)

                    val responseIntent = Intent()
                    responseIntent.putExtra(EXTRA_KEY_OUT, site.catalogName)
                    responseIntent.action = ACTION_CATALOG_UPDATER_SERVICE
                    sendBroadcast(responseIntent)

                    taskCounter = taskCounter - site.catalogName
                } catch (e: Exception) {
                    e.printStackTrace()
                    isError = true
                } finally { //
                }
            }
            job.join()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        job.cancel()
        stopForeground(false)

        val responseIntent = Intent()
        responseIntent.putExtra(EXTRA_KEY_OUT, "destroy")
        responseIntent.action = ACTION_CATALOG_UPDATER_SERVICE

        sendBroadcast(responseIntent)

        with(NotificationCompat.Builder(this@CatalogForOneSiteUpdaterService, channelId)) {
            setSmallIcon(R.drawable.ic_notification_update)
            setContentTitle(getString(R.string.catalog_fos_service_notify_complete))
            if (isError && !isManualStop) {
                setContentTitle(getString(R.string.catalog_fos_service_notify_error_title))
                setContentText(getString(R.string.catalog_fos_service_notify_error_text))
            } else if (isManualStop) {
                setContentText(getString(R.string.catalog_fos_service_notify_manual_stop_text))
                setContentTitle(getString(R.string.catalog_fos_service_notify_manual_stop_title))
            }
            setContentIntent(actionGoToCatalogs)

            stopForeground(false)
            notificationManager.cancel(notificationId)
            notificationManager.notify(notificationId, build())
        }

        notificationId = ID.generate()

        taskCounter = emptyList()
    }
}
