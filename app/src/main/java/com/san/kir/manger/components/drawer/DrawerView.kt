package com.san.kir.manger.components.drawer

import android.content.Intent
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.san.kir.ankofork.AnkoContextImpl
import com.san.kir.ankofork.appcompat.toolbar
import com.san.kir.ankofork.design.navigationView
import com.san.kir.ankofork.design.themedAppBarLayout
import com.san.kir.ankofork.dip
import com.san.kir.ankofork.leftPadding
import com.san.kir.ankofork.matchParent
import com.san.kir.ankofork.padding
import com.san.kir.ankofork.recyclerview.recyclerView
import com.san.kir.ankofork.rightPadding
import com.san.kir.ankofork.sdk28._LinearLayout
import com.san.kir.ankofork.sdk28.backgroundResource
import com.san.kir.ankofork.sdk28.imageView
import com.san.kir.ankofork.sdk28.linearLayout
import com.san.kir.ankofork.sdk28.textView
import com.san.kir.ankofork.support.drawerLayout
import com.san.kir.ankofork.topPadding
import com.san.kir.ankofork.verticalLayout
import com.san.kir.ankofork.wrapContent
import com.san.kir.manger.BuildConfig
import com.san.kir.manger.R
import com.san.kir.manger.components.category.CategoryActivity
import com.san.kir.manger.components.download_manager.DownloadManagerActivity
import com.san.kir.manger.components.latest_chapters.LatestChapterActivity
import com.san.kir.manger.components.library.LibraryActivity
import com.san.kir.manger.components.schedule.ScheduleActivity
import com.san.kir.manger.components.settings.SettingActivity
import com.san.kir.manger.components.sites_catalog.SiteCatalogActivity
import com.san.kir.manger.components.statistics.StatisticActivity
import com.san.kir.manger.components.storage.StorageActivity
import com.san.kir.manger.utils.RecyclerViewAdapterFactory
import com.san.kir.manger.utils.SimpleItemTouchHelperCallback
import com.san.kir.manger.utils.enums.MainMenuType
import com.san.kir.manger.utils.extensions.BaseActivity
import com.san.kir.manger.utils.extensions.doOnApplyWindowInstets
import com.san.kir.manger.utils.extensions.onClick
import com.san.kir.manger.view_models.DrawerViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class DrawerView(private val act: DrawerActivity) {
    private val mViewModel by act.viewModels<DrawerViewModel>()
    private val mAdapter = RecyclerViewAdapterFactory
        .createDraggable(
            { MainMenuItemView(act, mViewModel) },
            { fromPosition: Int, toPosition: Int ->
                Collections.swap(items, fromPosition, toPosition)
                notifyItemMoved(fromPosition, toPosition)
                items.forEachIndexed { index, item ->
                    item.order = index
                }
                mViewModel.mainMenuUpdate(*items.toTypedArray())
            })

    private val mItemTouchHelper by lazy {
        ItemTouchHelper(SimpleItemTouchHelperCallback(mAdapter))
    }

    lateinit var drawerLayout: DrawerLayout
    private lateinit var toolbar: Toolbar

    fun init() {
        act.lifecycleScope.launch(Dispatchers.Main) {
            mAdapter.items = withContext(Dispatchers.Default) {
                mViewModel.getMainMenuItems()
            }
            mAdapter.notifyDataSetChanged()
        }
    }

    fun createView(act: BaseActivity, otherView: _LinearLayout.() -> View): View {
        return with(AnkoContextImpl(act, act, true)) {
            drawerLayout {
                lparams(width = matchParent, height = matchParent)
                systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
                verticalLayout {
                    lparams(width = matchParent, height = matchParent)

                    doOnApplyWindowInstets { v, insets, _ ->
                        v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                            // Получаем размер выреза, если есть
                            val cutoutRight = insets.displayCutout?.safeInsetRight ?: 0
                            val cutoutLeft = insets.displayCutout?.safeInsetLeft ?: 0
                            // Вычитаем из WindowInsets размер выреза, для fullscreen
                            rightMargin = insets.systemWindowInsetRight - cutoutRight
                            leftMargin = insets.systemWindowInsetLeft - cutoutLeft
                        }
                        insets
                    }

                    themedAppBarLayout(R.style.ThemeOverlay_AppCompat_DayNight_ActionBar) {

                        doOnApplyWindowInstets { v, insets, _ ->
                            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                                topMargin = insets.systemWindowInsetTop
                            }
                            insets
                        }

                        toolbar = toolbar {
                            lparams(width = matchParent, height = wrapContent)
                            act.setSupportActionBar(this)
                        }
                    }

                    otherView()
                }

                navigationView {
                    doOnApplyWindowInstets { v, insets, padding ->
                        v.updatePadding(top = padding.top + insets.systemWindowInsetTop)
                        if (insets.systemWindowInsetRight > insets.systemWindowInsetLeft) {
                            rightPadding = insets.systemWindowInsetRight
                            leftPadding = 0
                        } else {
                            rightPadding = 0
                            leftPadding = insets.systemWindowInsetLeft
                        }
                        insets
                    }
                    verticalLayout {
                        linearLayout {
                            lparams(width = matchParent, height = wrapContent)
                            padding = dip(6)

                            // Иконка приложения
                            imageView {
                                backgroundResource = R.mipmap.ic_launcher_foreground
                            }

                            verticalLayout {
                                lparams {
                                    gravity = Gravity.CENTER
                                }

                                // Название приложения
                                textView {
                                    text = context.getString(
                                        R.string.app_name_version,
                                        BuildConfig.VERSION_NAME
                                    )
                                    leftPadding = dip(10)
                                    rightPadding = dip(10)
                                    textSize = 16f
                                }.lparams(width = wrapContent, height = matchParent)

                                // Имя автора, точнее никнейм
                                textView(text = R.string.name) {
                                    leftPadding = dip(10)
                                    rightPadding = dip(10)
                                    topPadding = dip(9)
                                }
                            }
                        }

                        recyclerView {
                            setHasFixedSize(true)
                            layoutManager =
                                LinearLayoutManager(context)
                            mItemTouchHelper.attachToRecyclerView(this)
                            adapter = mAdapter
                            overScrollMode = View.OVER_SCROLL_NEVER
                            onClick { _, position ->
                                showScreen(position)
                                this@drawerLayout.closeDrawer(GravityCompat.START)
                            }
                            lparams(width = matchParent, height = wrapContent)
                        }
                    }

                }.lparams(width = matchParent, height = matchParent) {
                    gravity = GravityCompat.START
                }

                ActionBarDrawerToggle(
                    act, this, toolbar,
                    R.string.navigation_drawer_open,
                    R.string.navigation_drawer_close
                ).apply {
                    addDrawerListener(this)
//                    drawerArrowDrawable = drawerArrowDrawable.apply { color = Color.WHITE }
                    syncState()
                }
                drawerLayout = this
            }
        }
    }

    private fun showScreen(position: Int) {

        val activity = when (mAdapter.items[position].type) {
            MainMenuType.Library -> LibraryActivity::class.java
            MainMenuType.Storage -> StorageActivity::class.java
            MainMenuType.Category -> CategoryActivity::class.java
            MainMenuType.Catalogs -> SiteCatalogActivity::class.java
            MainMenuType.Downloader -> DownloadManagerActivity::class.java
            MainMenuType.Latest -> LatestChapterActivity::class.java
            MainMenuType.Settings -> SettingActivity::class.java
            MainMenuType.Schedule -> ScheduleActivity::class.java
            MainMenuType.Statistic -> StatisticActivity::class.java
            MainMenuType.Default -> LibraryActivity::class.java
        }

        val intent = Intent(act, activity)
        act.startActivity(intent)
        act.finish()
    }

}
