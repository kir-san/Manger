package com.san.kir.manger.components.latest_chapters

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.san.kir.ankofork.Binder
import com.san.kir.ankofork.dip
import com.san.kir.ankofork.horizontalProgressBar
import com.san.kir.ankofork.matchParent
import com.san.kir.ankofork.negative
import com.san.kir.ankofork.positive
import com.san.kir.ankofork.recyclerview.recyclerView
import com.san.kir.ankofork.sdk28._LinearLayout
import com.san.kir.manger.R
import com.san.kir.manger.components.drawer.DrawerActivity
import com.san.kir.manger.utils.extensions.showNever
import com.san.kir.manger.utils.extensions.visibleOrGone
import com.san.kir.manger.view_models.LatestChapterViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LatestChapterActivity : DrawerActivity() {
    private val _adapter = LatestChaptersRecyclerPresenter(this)
    private val isAction = Binder(false)

    val mViewModel by viewModels<LatestChapterViewModel>()
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
                layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
                _adapter.into(this)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(0, 0, 0, R.string.latest_chapter_download_new).showNever()
        menu.add(1, 1, 1, R.string.latest_chapter_clean).showNever()
        menu.add(1, 2, 2, R.string.latest_chapter_clean_read).showNever()
        menu.add(1, 3, 3, R.string.latest_chapter_clean_download).showNever()
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        lifecycleScope.launch(Dispatchers.Main) {
            menu.getItem(0).isEnabled =_adapter.hasNewChapters()
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
