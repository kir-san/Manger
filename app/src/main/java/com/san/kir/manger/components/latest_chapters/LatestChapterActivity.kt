package com.san.kir.manger.components.latest_chapters

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.work.WorkManager
import com.san.kir.ankofork.dip
import com.san.kir.ankofork.horizontalProgressBar
import com.san.kir.ankofork.matchParent
import com.san.kir.ankofork.recyclerview.recyclerView
import com.san.kir.ankofork.sdk28._LinearLayout
import com.san.kir.manger.R
import com.san.kir.manger.components.drawer.DrawerActivity
import com.san.kir.manger.utils.extensions.doOnApplyWindowInstets
import com.san.kir.manger.utils.extensions.gone
import com.san.kir.manger.utils.extensions.showNever
import com.san.kir.manger.utils.extensions.visible
import com.san.kir.manger.view_models.LatestChapterViewModel
import com.san.kir.manger.workmanager.AllLatestClearWorker
import com.san.kir.manger.workmanager.DownloadedLatestClearWorker
import com.san.kir.manger.workmanager.LatestClearWorker
import com.san.kir.manger.workmanager.ReadLatestClearWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class LatestChapterActivity : DrawerActivity() {
    private val _adapter = LatestChaptersRecyclerPresenter(this)
    private lateinit var action: ProgressBar

    val mViewModel by viewModels<LatestChapterViewModel>()
    override val _LinearLayout.customView: View
        get() = this.apply {
            action = horizontalProgressBar {
                isIndeterminate = true
                gone()
                progressDrawable = ContextCompat.getDrawable(
                    this@LatestChapterActivity,
                    R.drawable.storage_progressbar
                )
            }.lparams(width = matchParent, height = dip(10))
            recyclerView {
                lparams(width = matchParent, height = matchParent)
                layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
                _adapter.into(this)

                clipToPadding = false

                doOnApplyWindowInstets { view, insets, padding ->
                    view.updatePadding(bottom = padding.bottom + insets.systemWindowInsetBottom)
                    insets
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launchWhenCreated {
            mViewModel
                .getLatestItems()
                .collect { list ->
                    if (list.isNotEmpty())
                        title = getString(R.string.main_menu_latest_count, list.size)
                    else
                        setTitle(R.string.main_menu_latest)
                }
        }

        WorkManager
            .getInstance(this)
            .getWorkInfosByTagLiveData(LatestClearWorker.tag)
            .observe(this, Observer { works ->
                if (works.isNotEmpty())
                    if (works.all { it.state.isFinished }) {
                        action.gone()
                    } else {
                        action.visible()
                    } })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(0, 0, 0, R.string.latest_chapter_download_new).showNever()
        menu.add(1, 1, 1, R.string.latest_chapter_clean).showNever()
        menu.add(1, 2, 2, R.string.latest_chapter_clean_read).showNever()
        menu.add(1, 3, 3, R.string.latest_chapter_clean_download).showNever()
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        lifecycleScope.launch(Dispatchers.Main) {
            menu.getItem(0).isEnabled = _adapter.hasNewChapters()
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            0 -> {
                _adapter.downloadNewChapters()
                item.isEnabled = false
            }
            1 -> LatestClearWorker.addTask<AllLatestClearWorker>(this)
            2 -> LatestClearWorker.addTask<ReadLatestClearWorker>(this)
            3 -> LatestClearWorker.addTask<DownloadedLatestClearWorker>(this)
        }
        return true
    }
}
