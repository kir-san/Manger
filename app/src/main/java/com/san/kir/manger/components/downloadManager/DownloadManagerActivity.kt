package com.san.kir.manger.components.downloadManager

import android.arch.lifecycle.Observer
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.IBinder
import android.text.Html
import android.view.View
import android.widget.LinearLayout
import com.san.kir.manger.R
import com.san.kir.manger.components.drawer.DrawerActivity
import com.san.kir.manger.components.main.Main
import com.san.kir.manger.eventBus.Binder
import com.san.kir.manger.room.models.DownloadItem
import com.san.kir.manger.room.models.DownloadStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DownloadManagerActivity : DrawerActivity() {
    val dao = Main.db.downloadDao
    val updateNetwork = Binder(false)

    lateinit var downloadManager: ChapterLoader

    private var bound = false

    private val connection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            bound = false
        }

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            downloadManager =
                    (service as DownloadService.LocalBinderC).chapterLoader
            bound = true
        }
    }
    private val titleObserver = Observer<List<DownloadItem>> { item ->
        item?.let { downloads ->
            launch(coroutineContext) {
                val loadingCount = downloads.filter {
                    it.status == DownloadStatus.queued ||
                            it.status == DownloadStatus.loading
                }.size
                val stoppedCount = downloads.filter {
                    it.status == DownloadStatus.error ||
                            it.status == DownloadStatus.pause
                }.size
                val completedCount = downloads.filter {
                    it.status == DownloadStatus.completed
                }.size

                withContext(Dispatchers.Main) {
                    supportActionBar?.title =
                            getString(R.string.main_menu_downloader_count, loadingCount)
                    supportActionBar?.subtitle =
                            Html.fromHtml(
                                "<font color='#FFFFFF'>${getString(
                                    R.string.download_activity_subtitle,
                                    stoppedCount,
                                    completedCount
                                )}</font>"
                            )
                }
            }
        }
    }
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            updateNetwork.item = !updateNetwork.item
        }
    }

    override val LinearLayout.customView: View
        get() = DownloadManagerView(this@DownloadManagerActivity).view(this@customView)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = Intent(this, DownloadService::class.java)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)
        setTitle(R.string.main_menu_downloader)
        dao.loadItems().observe(this, titleObserver)

        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(receiver, filter)

    }

    override fun onDestroy() {
        super.onDestroy()
        if (bound) {
            unbindService(connection)
            bound = false
        }
        unregisterReceiver(receiver)
    }
}

