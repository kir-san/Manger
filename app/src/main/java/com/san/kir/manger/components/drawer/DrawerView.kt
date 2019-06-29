package com.san.kir.manger.components.drawer

import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.graphics.Color
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.Gravity
import android.view.View
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
import com.san.kir.manger.extending.BaseActivity
import com.san.kir.manger.extending.anko_extend.onClick
import com.san.kir.manger.utils.RecyclerViewAdapterFactory
import com.san.kir.manger.utils.SimpleItemTouchHelperCallback
import com.san.kir.manger.utils.getDrawableCompat
import com.san.kir.manger.view_models.DrawerViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.AnkoContextImpl
import org.jetbrains.anko._LinearLayout
import org.jetbrains.anko.appcompat.v7.toolbar
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.design.appBarLayout
import org.jetbrains.anko.design.navigationView
import org.jetbrains.anko.dip
import org.jetbrains.anko.imageView
import org.jetbrains.anko.leftPadding
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.padding
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.rightPadding
import org.jetbrains.anko.support.v4.drawerLayout
import org.jetbrains.anko.textView
import org.jetbrains.anko.themedLinearLayout
import org.jetbrains.anko.topPadding
import org.jetbrains.anko.verticalLayout
import org.jetbrains.anko.wrapContent
import java.util.*

class DrawerView(private val act: DrawerActivity) {
    private val mViewModel by lazy {
        ViewModelProviders.of(act).get(DrawerViewModel::class.java)
    }
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
        .apply {
            act.launch(act.coroutineContext) {
                setHasStableIds(true)
                items = mViewModel.getMainMenuItems()

                withContext(Dispatchers.Main) {
                    notifyDataSetChanged()
                }
            }
        }

    private val mItemTouchHelper by lazy {
        ItemTouchHelper(SimpleItemTouchHelperCallback(mAdapter))
    }

    lateinit var drawerLayout: DrawerLayout
    lateinit var toolbar: Toolbar

    fun createView(act: BaseActivity, otherView: _LinearLayout.() -> View): View {
        return with(AnkoContextImpl(act, act, true)) {
            drawerLayout {
                lparams(width = matchParent, height = matchParent)
                fitsSystemWindows = true

                verticalLayout {
                    lparams(width = matchParent, height = matchParent)

                    appBarLayout {

                        toolbar = toolbar {
                            lparams(width = matchParent, height = wrapContent)
                            backgroundColor =
                                    Color.parseColor("#ff212121") // material_grey_900
                            setTitleTextColor(Color.WHITE)
                            overflowIcon = getDrawableCompat(R.drawable.dots_vertical)
                            act.setSupportActionBar(this)
                        }
                    }

                    otherView()
                }

                navigationView {
                    verticalLayout {
                        themedLinearLayout(R.style.ThemeOverlay_AppCompat_Dark) {
                            lparams(width = matchParent, height = wrapContent)
                            backgroundColor = Color.parseColor("#ff212121") // material_grey_900
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
                            layoutManager = LinearLayoutManager(context)
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

                }.lparams(width = wrapContent, height = matchParent) {
                    gravity = GravityCompat.START
                }

                ActionBarDrawerToggle(
                    act, this, toolbar,
                    R.string.navigation_drawer_open,
                    R.string.navigation_drawer_close
                ).apply {
                    addDrawerListener(this)
                    drawerArrowDrawable = drawerArrowDrawable.apply { color = Color.WHITE }
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
