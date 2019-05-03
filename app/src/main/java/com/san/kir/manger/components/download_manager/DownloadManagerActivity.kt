package com.san.kir.manger.components.download_manager

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.text.Html
import android.view.View
import com.san.kir.manger.R
import com.san.kir.manger.components.drawer.DrawerActivity
import com.san.kir.manger.eventBus.Binder
import com.san.kir.manger.room.models.DownloadItem
import com.san.kir.manger.room.models.DownloadStatus
import com.san.kir.manger.view_models.DownloadManagerViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko._LinearLayout

class DownloadManagerActivity : DrawerActivity() {
    val mViewModel by lazy {
        ViewModelProviders.of(this).get(DownloadManagerViewModel::class.java)
    }
    val updateNetwork = Binder(false)

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

    override val _LinearLayout.customView: View
        get() = DownloadManagerView(this@DownloadManagerActivity).view(this@customView)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle(R.string.main_menu_downloader)
        mViewModel.getDownloadItems().observe(this, titleObserver)

        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(receiver, filter)

    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }
}

