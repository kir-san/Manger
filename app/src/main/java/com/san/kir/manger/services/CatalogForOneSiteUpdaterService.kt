package com.san.kir.manger.services

import android.annotation.SuppressLint
import android.app.IntentService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import androidx.core.net.toUri
import com.san.kir.ankofork.intentFor
import com.san.kir.ankofork.sdk28.notificationManager
import com.san.kir.manger.R
import com.san.kir.manger.components.parsing.SiteCatalogsManager
import com.san.kir.manger.di.DefaultDispatcher
import com.san.kir.manger.repositories.SiteCatalogRepository
import com.san.kir.manger.room.dao.MangaDao
import com.san.kir.manger.room.dao.SiteDao
import com.san.kir.manger.room.entities.SiteCatalogElement
import com.san.kir.manger.ui.MainActivity
import com.san.kir.manger.ui.application_navigation.MainNavTarget
import com.san.kir.manger.utils.ID
import com.san.kir.manger.utils.extensions.log
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.Executors
import javax.inject.Inject

@AndroidEntryPoint
class CatalogForOneSiteUpdaterService : IntentService(TAG) {
    companion object {
        const val ACTION_CATALOG_UPDATER_SERVICE =
            "kir.san.manger.CatalogForOneSiteUpdaterService.UPDATE"
        const val ACTION_CANCEL_ALL = "kir.san.manger.CatalogForOneSiteUpdaterService.CANCEL_ALL"

        const val EXTRA_KEY_OUT = "EXTRA_OUT"

        private const val channelId = "CatalogUpdaterId"
        private const val TAG = "CatalogForOneSiteUpdaterService"
        private const val name = "CatalogForOneSiteUpdaterServiceName"
        private const val descriptions = "CatalogForOneSiteUpdaterServiceDescription"
        private var taskCounter = listOf<String>()
        fun isContain(catalogName: String) = taskCounter.contains(catalogName)
    }

    private var notificationId = ID.generate()

    private val actionGoToCatalogs by lazy {
        val deepLinkIntent = Intent(
            Intent.ACTION_VIEW,
            MainNavTarget.Catalogs.deepLink.toUri(),
            this,
            MainActivity::class.java
        )
        TaskStackBuilder.create(this).run {
            addNextIntentWithParentStack(deepLinkIntent)
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        }
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

    @Inject
    lateinit var manager: SiteCatalogsManager

    @DefaultDispatcher
    @Inject
    lateinit var default: CoroutineDispatcher

    @Inject
    lateinit var mangaDao: MangaDao

    @Inject
    lateinit var siteDao: SiteDao

    @SuppressLint("InlinedApi")
    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
            && notificationManager.getNotificationChannel(channelId) == null) {

            val importance = NotificationManager.IMPORTANCE_LOW

            NotificationChannel(channelId, name, importance).apply {
                description = descriptions
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
//            taskCounter = taskCounter + intent.getStringExtra("catalogName")
        }
        return super.onStartCommand(intent, flags, startId)
    }


    override fun onHandleIntent(intent: Intent?) = runBlocking(default) {
        job = launch {
            try {
                val site = manager.catalog
                    .first { it.catalogName == intent!!.getStringExtra("catalogName") }
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
                site.getCatalog()
                    .flowOn(Executors.newSingleThreadExecutor().asCoroutineDispatcher())
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

                        log("$it")
                    }
                    .map { el ->
                        el.isAdded = mangaDao.getItems().any { it.shortLink == el.shotLink }
                        el
                    }
                    .toList(tempList)

                log("update finish. elements getting ${tempList.size}")

                SiteCatalogRepository(
                    this@CatalogForOneSiteUpdaterService, site.catalogName, manager
                ).apply {
                    clearDb()
                    insert(*tempList.toTypedArray())
                    close()
                }

                siteDb?.oldVolume = counter
                siteDao.update(siteDb)

                sendPositiveBroadcast(site.catalogName)

                taskCounter = taskCounter - site.catalogName
            } catch (e: Exception) {
                e.printStackTrace()
                isError = true
            } finally { //
            }
        }
        job.join()
    }


    override fun onDestroy() {
        super.onDestroy()

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
        val responseIntent = Intent()
        responseIntent.putExtra(EXTRA_KEY_OUT, catalogName)
        responseIntent.action = ACTION_CATALOG_UPDATER_SERVICE
        sendBroadcast(responseIntent)
    }

    private fun sendNeutralBroadcast() {
        val responseIntent = Intent()
        responseIntent.putExtra(EXTRA_KEY_OUT, "destroy")
        responseIntent.action = ACTION_CATALOG_UPDATER_SERVICE
        sendBroadcast(responseIntent)
    }

    private fun sendNegativeBroadcast() {
        val responseIntent = Intent()
        responseIntent.putExtra(EXTRA_KEY_OUT, "error")
        responseIntent.action = ACTION_CATALOG_UPDATER_SERVICE
        sendBroadcast(responseIntent)
    }
}
