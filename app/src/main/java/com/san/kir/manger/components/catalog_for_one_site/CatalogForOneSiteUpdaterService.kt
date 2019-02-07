package com.san.kir.manger.components.catalog_for_one_site

import android.annotation.SuppressLint
import android.app.IntentService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.support.v4.app.NotificationCompat
import com.san.kir.manger.R
import com.san.kir.manger.components.parsing.ManageSites
import com.san.kir.manger.components.sites_catalog.SiteCatalogActivity
import com.san.kir.manger.extending.launchCtx
import com.san.kir.manger.repositories.MangaRepository
import com.san.kir.manger.repositories.SiteCatalogRepository
import com.san.kir.manger.repositories.SiteRepository
import com.san.kir.manger.room.models.SiteCatalogElement
import com.san.kir.manger.utils.ID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.runBlocking
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.notificationManager
import java.util.concurrent.Executors

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

    private var catalog: ReceiveChannel<SiteCatalogElement>? = null
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
    private var job = Job()
    private var isError = false
    private var isManualStop = false
    private lateinit var mSiteCatalogRepository: SiteCatalogRepository

    @SuppressLint("InlinedApi")
    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager.getNotificationChannel(channelId) == null) {
                val importance = NotificationManager.IMPORTANCE_LOW

                NotificationChannel(channelId, name, importance).apply {
                    description = CatalogForOneSiteUpdaterService.description
                    notificationManager.createNotificationChannel(this)
                }
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
            job = launchCtx {
                try {
                    val siteRepository = SiteRepository(this@CatalogForOneSiteUpdaterService)
                    val mangaRepository = MangaRepository(this@CatalogForOneSiteUpdaterService)
                    val site = ManageSites.CATALOG_SITES
                        .first { it.catalogName == intent.getStringExtra("catalogName") }
                    val siteDb = siteRepository.getItem(site.name)

                    with(NotificationCompat.Builder(this@CatalogForOneSiteUpdaterService, channelId)) {
                        setSmallIcon(R.drawable.ic_notification_update)
                        setContentTitle(
                            getString(R.string.catalog_fos_service_notify_title, taskCounter.size)
                        )
                        setContentText(getString(R.string.catalog_fos_service_notify_text, site.name))
                        startForeground(notificationId, build())
                    }

                    mSiteCatalogRepository =
                        SiteCatalogRepository(this@CatalogForOneSiteUpdaterService, site.catalogName)
                    mSiteCatalogRepository.clearDb()
                    var counter = 0

                    site.init()

                    val loadContext = Executors.newFixedThreadPool(2).asCoroutineDispatcher()
                    catalog = site.getCatalog(loadContext)
                    catalog?.consumeEach { newElement ->
                        newElement.isAdded = mangaRepository.contain(newElement)
                        mSiteCatalogRepository.insert(newElement)
                        counter++
                        with(
                            NotificationCompat.Builder(this@CatalogForOneSiteUpdaterService, channelId)
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
                    siteRepository.update(siteDb)

                    val responseIntent = Intent()
                    responseIntent.putExtra(EXTRA_KEY_OUT, site.catalogName)
                    responseIntent.action = ACTION_CATALOG_UPDATER_SERVICE
                    sendBroadcast(responseIntent)

                    taskCounter = taskCounter - site.catalogName


                } catch (e: Exception) {
                    e.printStackTrace()
                    isError = true
                } finally {
                    if (::mSiteCatalogRepository.isInitialized) {
                        mSiteCatalogRepository.close()
                    }
                }
            }
            job.join()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        stopForeground(false)

        val responseIntent = Intent()
        responseIntent.putExtra(EXTRA_KEY_OUT, "destroy")
        responseIntent.action = ACTION_CATALOG_UPDATER_SERVICE

        sendBroadcast(responseIntent)

        with(NotificationCompat.Builder(this@CatalogForOneSiteUpdaterService, channelId)) {
            setSmallIcon(R.drawable.ic_notification_update)
            setContentTitle(getString(R.string.catalog_fos_service_notify_complete))
            setContentText("")
            if (isError && !isManualStop) {
                setContentTitle(getString(R.string.catalog_fos_service_notify_error_title))
                setContentText(getString(R.string.catalog_fos_service_notify_error_text))
            } else if(isManualStop) {
                setContentText(getString(R.string.catalog_fos_service_notify_manual_stop_text))
                setContentTitle(getString(R.string.catalog_fos_service_notify_manual_stop_title))
            }
            setContentIntent(actionGoToCatalogs)

            stopForeground(false)
            notificationManager.cancel(notificationId)
            notificationManager.notify(notificationId, build())
        }

        notificationId = ID.generate()

        catalog?.cancel()
        taskCounter = emptyList()
    }
}
