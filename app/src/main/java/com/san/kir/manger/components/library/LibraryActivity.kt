package com.san.kir.manger.components.library

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.ViewPager
import androidx.work.WorkManager
import com.san.kir.ankofork.dialogs.longToast
import com.san.kir.ankofork.horizontalProgressBar
import com.san.kir.ankofork.include
import com.san.kir.ankofork.matchParent
import com.san.kir.ankofork.sdk28._LinearLayout
import com.san.kir.ankofork.startService
import com.san.kir.ankofork.support.onPageChangeListener
import com.san.kir.ankofork.support.viewPager
import com.san.kir.ankofork.wrapContent
import com.san.kir.manger.R
import com.san.kir.manger.components.drawer.DrawerActivity
import com.san.kir.manger.extending.dialogs.AddMangaOnlineDialog
import com.san.kir.manger.room.entities.MangaColumn
import com.san.kir.manger.services.AppUpdateService
import com.san.kir.manger.services.MangaUpdaterService
import com.san.kir.manger.utils.extensions.gone
import com.san.kir.manger.utils.extensions.showAlways
import com.san.kir.manger.utils.extensions.startForegroundService
import com.san.kir.manger.utils.extensions.visible
import com.san.kir.manger.view_models.LibraryViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LibraryActivity : DrawerActivity() {

    private lateinit var viewPager: ViewPager
    private lateinit var action: ProgressBar

    private var currentAdapter: LibraryItemsRecyclerPresenter? = null
    private val pagerAdapter by lazy { LibraryPageAdapter(this) }

    val mViewModel by viewModels<LibraryViewModel>()

    override val _LinearLayout.customView: View
        get() = this.apply {
            action = horizontalProgressBar {
                isIndeterminate = true
                visible()
            }.lparams(width = matchParent, height = wrapContent)

            viewPager = viewPager {
                id = R.id.library_viewpager
                include<androidx.viewpager.widget.PagerTabStrip>(R.layout.page_tab_strip)
                adapter = pagerAdapter
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = getString(R.string.main_menu_library)
        action.visible()
        pagerAdapter.init.invokeOnCompletion {
            lifecycleScope.launchWhenResumed {
                try {
                    currentAdapter = pagerAdapter.adapters[0]
                    invalidateOptionsMenu()

                    val count = currentAdapter?.itemCount

                    title = if (count != null && count > 0) {
                        getString(R.string.main_menu_library_count, count)
                    } else {
                        delay(1300L)
                        getString(R.string.main_menu_library_count, currentAdapter?.itemCount)
                    }
                } catch (ex: IndexOutOfBoundsException) {
                    longToast(R.string.library_error_hide_categories)
                } finally {
                    action.gone()
                }
            }
        }

        viewPager.onPageChangeListener {
            onPageSelected {
                currentAdapter = pagerAdapter.adapters[it]
                invalidateOptionsMenu()
                title = getString(
                    R.string.main_menu_library_count,
                    currentAdapter?.itemCount
                )
            }
        }

        WorkManager
            .getInstance(this)
            .getWorkInfosByTagLiveData("mangaDelete")
            .observe(this, Observer { works ->
                if (works.isNotEmpty())
                    if (works.all { it.state.isFinished }) {
                        action.gone()
                    } else {
                        action.visible()
                    }
            })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(0, 0, 0, R.string.library_menu_reload)
        menu.add(1, 1, 1, R.string.library_menu_reload_all)
        menu.add(3, 3, 4, R.string.library_menu_update)
        menu.add(4, 4, 5, R.string.library_menu_add_manga).setIcon(R.drawable.ic_add_white)
            .showAlways()
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            0 -> updateCurrent()
            1 -> updateAll()
            3 -> startForegroundService<AppUpdateService>()
            4 -> addMangaOnline()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun addMangaOnline() = AddMangaOnlineDialog(this)

    private fun updateCurrent(): Unit? {
        return currentAdapter?.catalog?.forEach {
            startService<MangaUpdaterService>(MangaColumn.tableName to it)
        }
    }

    private fun updateAll(): Job {
        return lifecycleScope.launch(Dispatchers.Default) {
            mViewModel.getMangas().forEach {
                startService<MangaUpdaterService>(MangaColumn.tableName to it)
            }
        }
    }
}

