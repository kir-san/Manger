package com.san.kir.manger.components.downloadManager

import android.arch.lifecycle.Observer
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.text.Html
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import com.san.kir.manger.R
import com.san.kir.manger.components.drawer.DrawerActivity
import com.san.kir.manger.components.main.Main
import com.san.kir.manger.extending.views.showAlways
import com.san.kir.manger.room.models.DownloadItem
import com.san.kir.manger.room.models.DownloadStatus
import com.san.kir.manger.utils.ID
import com.san.kir.manger.utils.PrefDownload
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch

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

//            downloadManager.pausedAllIfNotDownloading()
        }
    }
    private val titleObserver = Observer<List<DownloadItem>> {
        it?.let { downloads ->
            val loadingCount = async {
                downloads.filter {
                    it.status == DownloadStatus.queued ||
                            it.status == DownloadStatus.loading
                }.size
            }
            val stoppedCount = async {
                downloads.filter {
                    it.status == DownloadStatus.error ||
                            it.status == DownloadStatus.pause
                }.size
            }
            val completedCount = async {
                downloads.filter {
                    it.status == DownloadStatus.completed
                }.size
            }

            async(UI) {
                supportActionBar?.title =
                        getString(R.string.main_menu_downloader_count, loadingCount.await())
                supportActionBar?.subtitle =
                        Html.fromHtml(
                            "<font color='#FFFFFF'>${getString(
                                R.string.download_activity_subtitle,
                                stoppedCount.await(),
                                completedCount.await()
                            )}</font>"
                        )
            }
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
        dao.loadAllDownloads().observe(this, titleObserver)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (bound) {
            unbindService(connection)
            bound = false
        }
        downloadManager.removeListeners(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(
            OptionId.defaultGroupId,
            OptionId.parallel,
            0,
            R.string.download_activity_option_parallel
        )
            .isCheckable = true
        menu.add(
            OptionId.defaultGroupId,
            OptionId.isRetry,
            0,
            R.string.download_activity_option_error_retry
        )
            .isCheckable = true
        menu.add(
            OptionId.defaultGroupId,
            OptionId.start,
            0,
            R.string.download_activity_option_start
        )
            .showAlways()
            .setIcon(R.drawable.ic_start_white)
        menu.add(OptionId.defaultGroupId, OptionId.stop, 0, R.string.download_activity_option_stop)
            .showAlways()
            .setIcon(R.drawable.ic_stop_white)
        menu.add(
            OptionId.defaultGroupId,
            OptionId.retry,
            0,
            R.string.download_activity_option_retry
        )
            .showAlways()
            .setIcon(R.drawable.ic_update)
        menu.addSubMenu(R.string.download_activity_option_submenu_clean).apply {
            add(
                OptionId.clearGroupId,
                OptionId.clearAll,
                0,
                R.string.download_activity_option_submenu_clean_all
            )
            add(
                OptionId.clearGroupId,
                OptionId.clearCompleted,
                0,
                R.string.download_activity_option_submenu_clean_completed
            )
            add(
                OptionId.clearGroupId,
                OptionId.clearPaused,
                0,
                R.string.download_activity_option_submenu_clean_paused
            )
            add(
                OptionId.clearGroupId,
                OptionId.clearError,
                0,
                R.string.download_activity_option_submenu_clean_error
            )
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        getSharedPreferences(PrefDownload.mainName, MODE_PRIVATE).apply {
            val concurrent = getInt(PrefDownload.concurrent, 4)
            menu.findItem(OptionId.parallel).isChecked = concurrent == 4

            val isRetry = getBoolean(PrefDownload.isRetry, false)
            menu.findItem(OptionId.isRetry).isChecked = isRetry
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            OptionId.parallel -> {
                getSharedPreferences(PrefDownload.mainName, MODE_PRIVATE).apply {
                    val concurrent = getInt(PrefDownload.concurrent, 4)
                    val newConcurrent = if (concurrent == 4) 1 else 4
                    edit().putInt(PrefDownload.concurrent, newConcurrent).apply()
                    downloadManager.setConcurrentPages(newConcurrent)
                }
            }
            OptionId.isRetry -> {
                getSharedPreferences(PrefDownload.mainName, MODE_PRIVATE).apply {
                    val isRetry = getBoolean(PrefDownload.isRetry, false)
                    edit().putBoolean(PrefDownload.isRetry, isRetry.not()).apply()
                    downloadManager.setRetryOnError(isRetry.not())
                }
            }
            OptionId.stop -> downloadManager.pauseAll()
            OptionId.start -> downloadManager.startAll()
            OptionId.retry -> downloadManager.retryAll()
            OptionId.clearCompleted -> {
                launch {
                    dao.loadItems()
                        .filter { it.status == DownloadStatus.completed }
                        .forEach { dao.delete(it) }
                }
            }
            OptionId.clearPaused -> {
                launch {
                    dao.loadItems()
                        .filter { it.status == DownloadStatus.pause }
                        .forEach { dao.delete(it) }
                }
            }
            OptionId.clearError -> {
                launch {
                    dao.loadItems()
                        .filter { it.status == DownloadStatus.error }
                        .forEach { dao.delete(it) }
                }
            }
            OptionId.clearAll -> {
                launch {
                    dao.loadItems()
                        .filter {
                            it.status == DownloadStatus.completed
                                    || it.status == DownloadStatus.pause
                                    || it.status == DownloadStatus.error
                        }.forEach { dao.delete(it) }
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private object OptionId {
        val defaultGroupId = ID.generate()
        val clearGroupId = ID.generate()

        val parallel = ID.generate()
        val isRetry = ID.generate()
        val stop = ID.generate()
        val start = ID.generate()
        val retry = ID.generate()
        val clearCompleted = ID.generate()
        val clearPaused = ID.generate()
        val clearError = ID.generate()
        val clearAll = ID.generate()
    }
}

