package com.san.kir.manger.components.DownloadManager

import android.arch.lifecycle.Observer
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.LinearLayout
import com.san.kir.manger.R
import com.san.kir.manger.components.Drawer.DrawerActivity
import com.san.kir.manger.components.Main.Main

class DownloadManagerActivity : DrawerActivity() {
    private val dao = Main.db.downloadDao
    private var bound = false
    private val connection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            bound = false
        }

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            downloadManager =
                    (service as DownloadService.LocalBinder).chapterLoader
            bound = true

            downloadManager.pausedAllIfNotDownloading()
        }
    }
    lateinit var downloadManager: ChapterLoader

    override val LinearLayout.customView: View
        get() = DownloadManagerView(this@DownloadManagerActivity).view(this@customView)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = Intent(this, DownloadService::class.java)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)
        setTitle(R.string.main_menu_downloader)
        dao.loadLoadingDownloads().observe(this, Observer {
            title = getString(R.string.main_menu_downloader_count, it?.size)
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        if (bound) {
            unbindService(connection)
            bound = false
        }
        downloadManager.removeListeners(this)
    }
}

