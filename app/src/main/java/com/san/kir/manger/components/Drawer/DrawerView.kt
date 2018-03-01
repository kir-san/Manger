package com.san.kir.manger.components.Drawer

import android.graphics.Color
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import com.san.kir.manger.BuildConfig
import com.san.kir.manger.Extending.AnkoExtend.onClick
import com.san.kir.manger.Extending.BaseActivity
import com.san.kir.manger.R
import com.san.kir.manger.components.Category.CategoryActivity
import com.san.kir.manger.components.DownloadManager.DownloadManagerActivity
import com.san.kir.manger.components.LatestChapters.LatestChapterActivity
import com.san.kir.manger.components.Library.LibraryActivity
import com.san.kir.manger.components.Main.Main
import com.san.kir.manger.components.Settings.SettingActivity
import com.san.kir.manger.components.SitesCatalog.SiteCatalogActivity
import com.san.kir.manger.components.Storage.StorageActivity
import com.san.kir.manger.room.DAO.MainMenuType
import com.san.kir.manger.room.DAO.update
import com.san.kir.manger.utils.RecyclerViewAdapterFactory
import com.san.kir.manger.utils.SimpleItemTouchHelperCallback
import com.san.kir.manger.utils.getDrawableCompat
import com.san.kir.manger.utils.log
import org.jetbrains.anko.AnkoContextImpl
import org.jetbrains.anko.appcompat.v7.toolbar
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.design.appBarLayout
import org.jetbrains.anko.design.navigationView
import org.jetbrains.anko.dip
import org.jetbrains.anko.imageView
import org.jetbrains.anko.leftPadding
import org.jetbrains.anko.linearLayout
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.padding
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.rightPadding
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.support.v4.drawerLayout
import org.jetbrains.anko.textView
import org.jetbrains.anko.topPadding
import org.jetbrains.anko.verticalLayout
import org.jetbrains.anko.wrapContent
import java.util.*

class DrawerView(private val act: BaseActivity) {
    private val mAdapter = RecyclerViewAdapterFactory
            .createDragable({ MainMenuItemView(act) },
                            { fromPosition, toPosition ->
                                Collections.swap(items,
                                                 fromPosition,
                                                 toPosition)
                                notifyItemMoved(
                                        fromPosition,
                                        toPosition)
                                items.forEachIndexed { index, item ->
                                    item.order = index
                                }
                                Main.db.mainMenuDao.update(
                                        *items.toTypedArray())
                            })
            .apply {
                items = Main.db.mainMenuDao.loadItems()
                notifyDataSetChanged()
            }

    private val mItemTouchHelper by lazy {
        ItemTouchHelper(SimpleItemTouchHelperCallback(mAdapter))
    }

    lateinit var drawerLayout: DrawerLayout
    lateinit var toolbar: Toolbar

    fun createView(act: BaseActivity, otherView: LinearLayout.() -> View): View {
        return with(AnkoContextImpl(act, act, true)) {
            drawerLayout {
                lparams(width = matchParent, height = matchParent)
                fitsSystemWindows = true

                verticalLayout {
                    lparams(width = matchParent, height = matchParent)

                    appBarLayout {
                        lparams(width = matchParent, height = wrapContent)
                        toolbar = toolbar {
                            lparams(width = matchParent, height = wrapContent)
                            backgroundColor = Color.parseColor("#ff212121") // material_grey_900
                            setTitleTextColor(Color.WHITE)
                            overflowIcon = getDrawableCompat(R.drawable.dots_vertical)
                            act.setSupportActionBar(this)
                        }
                    }

                    otherView()
                }

                navigationView {
                    verticalLayout {
                        verticalLayout(R.style.ThemeOverlay_AppCompat_Dark) {
                            lparams(width = matchParent, height = wrapContent)
                            backgroundColor = Color.parseColor("#ff212121") // material_grey_900
                            padding = dip(16)

                            linearLayout {
                                // Иконка приложения
                                imageView { backgroundResource = R.mipmap.ic_launcher }

                                // Название приложения
                                textView(text = R.string.app_name) {
                                    gravity = Gravity.CENTER_VERTICAL
                                    leftPadding = dip(10)
                                    rightPadding = dip(10)
                                    textSize = 16f
                                }.lparams(width = wrapContent, height = matchParent)

                                // Текущая версия приложения
                                textView(text = BuildConfig.VERSION_NAME) {
                                    gravity = Gravity.CENTER_VERTICAL
                                }.lparams(width = wrapContent, height = matchParent) {
                                    topMargin = dip(5)
                                }
                            }

                            // Имя автора, точнее никнейм
                            textView(text = R.string.name) { topPadding = dip(10) }

                            // емайл адрес
                            textView(text = R.string.email)
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

                ActionBarDrawerToggle(act, this, toolbar,
                                      R.string.navigation_drawer_open,
                                      R.string.navigation_drawer_close).apply {
                    addDrawerListener(this)
                    drawerArrowDrawable = drawerArrowDrawable.apply { color = Color.WHITE }
                    syncState()
                }
                drawerLayout = this
            }
        }
    }

    private fun showScreen(position: Int) {
        when (mAdapter.items[position].type) {
            MainMenuType.Library -> act.startActivity<LibraryActivity>()
            MainMenuType.Storage -> act.startActivity<StorageActivity>()
            MainMenuType.Category -> act.startActivity<CategoryActivity>()
            MainMenuType.Catalogs -> act.startActivity<SiteCatalogActivity>()
            MainMenuType.Downloader -> act.startActivity<DownloadManagerActivity>()
            MainMenuType.Latest -> act.startActivity<LatestChapterActivity>()
            MainMenuType.Settings -> act.startActivity<SettingActivity>()
            MainMenuType.Default -> TODO()
        }

    }

}
