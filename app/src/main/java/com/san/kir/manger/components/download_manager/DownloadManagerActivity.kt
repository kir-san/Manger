package com.san.kir.manger.components.download_manager

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.san.kir.ankofork.Binder
import com.san.kir.ankofork.sdk28._LinearLayout
import com.san.kir.manger.R
import com.san.kir.manger.components.drawer.DrawerActivity
import com.san.kir.manger.room.entities.DownloadItem
import com.san.kir.manger.utils.enums.DownloadStatus
import com.san.kir.manger.view_models.DownloadManagerViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DownloadManagerActivity : DrawerActivity() {
    val mViewModel by viewModels<DownloadManagerViewModel>()
    val updateNetwork = Binder(false)

    private val titleObserver = Observer<List<DownloadItem>> { item ->
        item?.let { downloads ->
            lifecycleScope.launch(Dispatchers.Default) {
                val loadingCount = downloads.filter {
                    it.status == DownloadStatus.queued ||
                            it.status == DownloadStatus.loading
                }.size
                val stoppedCount = downloads.filter {
                    it.status == DownloadStatus.pause
                }.size
                val completedCount = downloads.filter {
                    it.status == DownloadStatus.completed
                }.size

                withContext(Dispatchers.Main) {
                    supportActionBar?.title =
                        getString(R.string.main_menu_downloader_count, loadingCount)
                    supportActionBar?.subtitle = getString(
                        R.string.download_activity_subtitle, stoppedCount, completedCount
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

