package com.san.kir.manger.components.CatalogForOneSite

import android.app.IntentService
import android.app.PendingIntent
import android.content.Intent
import android.support.v4.app.NotificationCompat
import com.san.kir.manger.R
import com.san.kir.manger.components.Main.Main
import com.san.kir.manger.components.Parsing.ManageSites
import com.san.kir.manger.components.SitesCatalog.SiteCatalogActivity
import com.san.kir.manger.room.DAO.contain
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
        val ACTION_CATALOGUPDATERSERVICE = "kir.san.manger.CatalogForOneSiteUpdaterService.UPDATE"
        val ACTION_CANCELALL = "kir.san.manger.CatalogForOneSiteUpdaterService.CANCELLALL"

        val EXTRA_KEY_OUT = "EXTRA_OUT"

        private val channelId = "CatalogUpdaterId"
        private val TAG = "CatalogForOneSiteUpdaterService"
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
        val intent = intentFor<CatalogForOneSiteUpdaterService>().setAction(ACTION_CANCELALL)
        val cancelAll = PendingIntent.getService(this, 0, intent, 0)
        NotificationCompat
                .Action
                .Builder(R.drawable.ic_cancel, "Отменить все", cancelAll)
                .build()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (intent.action == ACTION_CANCELALL) {
            stopSelf()
        }
        taskCounter += intent.getIntExtra("id", -1)
        return super.onStartCommand(intent, flags, startId)
    }


    override fun onHandleIntent(intent: Intent) = runBlocking(CommonPool) {
        val site = ManageSites.CATALOG_SITES[intent.getIntExtra("id", -1)]
        val siteDb = Main.db.siteDao.loadSite(site.name)

        with(NotificationCompat.Builder(this@CatalogForOneSiteUpdaterService, channelId)) {
            setSmallIcon(R.drawable.ic_notify_updater)
            setContentTitle("Обновление каталогов: ${taskCounter.size} шт.")
            setContentText("Подготовка каталога ${site.name} к загрузке")
            notificationManager.notify(notificationId, build())
        }

        val viewModel = SiteCatalogElementViewModel.setSiteId(site.ID)
        viewModel.clearDb()
        var counter = 0

        site.init()


        val loadContext = newFixedThreadPoolContext(2, "LoadContext")
        catalog = site.getCatalog(loadContext)
        catalog?.consumeEach { newElement ->
            newElement.isAdded = Main.db.mangaDao.contain(newElement)
            viewModel.insert(newElement)
            counter++
            with(NotificationCompat.Builder(this@CatalogForOneSiteUpdaterService, channelId)) {
                setSmallIcon(R.drawable.ic_notify_updater)
                setContentTitle("Обновление ${taskCounter.size} шт")
                setContentText("${siteDb?.name}  ${((counter.toFloat() / site.volume.toFloat()) * 100).toInt()}%")
                setProgress(site.volume, counter, false)
                addAction(actionCancelAll)
                notificationManager.notify(notificationId, build())
            }
        }

        taskCounter -= site.ID

        val responseIntent = Intent()
        responseIntent.action = ACTION_CATALOGUPDATERSERVICE
        responseIntent.addCategory(Intent.CATEGORY_DEFAULT)
        sendBroadcast(responseIntent)
    }

    override fun onDestroy() {
        super.onDestroy()
        catalog?.cancel()
        taskCounter = emptyList()
        with(NotificationCompat.Builder(this@CatalogForOneSiteUpdaterService, channelId)) {
            setSmallIcon(R.drawable.ic_notify_updater)
            setContentTitle("Обновление каталогов завершенно")
            setContentText("")
            setContentIntent(actionGoToCatalogs)
            notificationManager.notify(notificationId, build())
        }

        val responseIntent = Intent()
        responseIntent.action = ACTION_CATALOGUPDATERSERVICE
        responseIntent.addCategory(Intent.CATEGORY_DEFAULT)
        sendBroadcast(responseIntent)
    }
}
