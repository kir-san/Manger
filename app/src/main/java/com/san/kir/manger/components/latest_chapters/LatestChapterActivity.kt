package com.san.kir.manger.components.latest_chapters

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.work.WorkManager
import com.san.kir.ankofork.sdk28._LinearLayout
import com.san.kir.manger.R
import com.san.kir.manger.components.drawer.DrawerActivity
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
    private val mAdapter = LatestChaptersRecyclerPresenter(this)
    private val mView = LatestChapterView(mAdapter)

    val mViewModel by viewModels<LatestChapterViewModel>()

    override val _LinearLayout.customView: View
        get() = mView.view(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launchWhenCreated {
            mViewModel
                .items()
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
                        mView.action.gone()
                    } else {
                        mView.action.visible()
                    }
            })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(0, R.id.latests_menu_download_new, 0, R.string.latest_chapter_download_new)
            .showNever()
        menu.add(1, R.id.latests_menu_clean, 1, R.string.latest_chapter_clean)
            .showNever()
        menu.add(1, R.id.latests_menu_clean_read, 2, R.string.latest_chapter_clean_read)
            .showNever()
        menu.add(1, R.id.latests_menu_clean_download, 3, R.string.latest_chapter_clean_download)
            .showNever()
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        lifecycleScope.launch(Dispatchers.Main) {
            menu.findItem(R.id.latests_menu_download_new).isEnabled = mAdapter.hasNewChapters()
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.latests_menu_download_new -> {
                mAdapter.downloadNewChapters()
                item.isEnabled = false
            }
            R.id.latests_menu_clean ->
                LatestClearWorker.addTask<AllLatestClearWorker>(this)
            R.id.latests_menu_clean_read ->
                LatestClearWorker.addTask<ReadLatestClearWorker>(this)
            R.id.latests_menu_clean_download ->
                LatestClearWorker.addTask<DownloadedLatestClearWorker>(this)
        }
        return true
    }
}
