package com.san.kir.manger.components.latest_chapters

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.san.kir.manger.R
import com.san.kir.manger.components.download_manager.ChapterLoader
import com.san.kir.manger.components.download_manager.DownloadService
import com.san.kir.manger.components.drawer.DrawerActivity
import com.san.kir.manger.eventBus.Binder
import com.san.kir.manger.eventBus.negative
import com.san.kir.manger.eventBus.positive
import com.san.kir.manger.extending.ankoExtend.visibleOrGone
import com.san.kir.manger.extending.views.showNever
import com.san.kir.manger.view_models.LatestChapterViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.anko._LinearLayout
import org.jetbrains.anko.dip
import org.jetbrains.anko.horizontalProgressBar
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.recyclerview.v7.recyclerView

class LatestChapterActivity : DrawerActivity() {
    private val _adapter = LatestChaptersRecyclerPresenter(this)
    private val isAction = Binder(false)

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

    val mViewModel by lazy {
        ViewModelProviders.of(this).get(LatestChapterViewModel::class.java)
    }
    lateinit var downloadManager: ChapterLoader

    override val _LinearLayout.customView: View
        get() = this.apply {
            horizontalProgressBar {
                isIndeterminate = true
                visibleOrGone(isAction)
                progressDrawable = ContextCompat.getDrawable(
                    this@LatestChapterActivity,
                    R.drawable.storage_progressbar
                )
            }.lparams(width = matchParent, height = dip(10))
            recyclerView {
                lparams(width = matchParent, height = matchParent)
                layoutManager = LinearLayoutManager(context)
                _adapter.into(this)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = Intent(this, DownloadService::class.java)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)

        mViewModel
            .getLatestItems()
            .observe(this, Observer { list ->
                list?.let {
                    if (it.isNotEmpty())
                        title = getString(R.string.main_menu_latest_count, it.size)
                    else
                        setTitle(R.string.main_menu_latest)
                }
            })
    }

    override fun onDestroy() {
        super.onDestroy()
        if (bound) {
            unbindService(connection)
            bound = false
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(0, 0, 0, R.string.latest_chapter_download_new).showNever()
        menu.add(1, 1, 1, R.string.latest_chapter_clean).showNever()
        menu.add(1, 2, 2, R.string.latest_chapter_clean_read).showNever()
        menu.add(1, 3, 3, R.string.latest_chapter_clean_download).showNever()
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        launch(Dispatchers.Main) {
            menu.getItem(0).isEnabled =_adapter.hasNewChapters().await()
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            0 -> {
                _adapter.downloadNewChapters()
                item.isEnabled = false
            }
            1 -> {
                isAction.positive()
                _adapter.clearHistory().invokeOnCompletion {
                    isAction.negative()
                }
            }
            2 -> {
                isAction.positive()
                _adapter.clearHistoryRead().invokeOnCompletion {
                    isAction.negative()
                }
            }
            3 -> {
                isAction.positive()
                _adapter.clearHistoryDownload().invokeOnCompletion {
                    isAction.negative()
                }
            }
        }
        return true
    }
}
