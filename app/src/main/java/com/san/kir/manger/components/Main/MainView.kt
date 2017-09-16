package com.san.kir.manger.components.Main

import android.graphics.Color
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.Toolbar
import android.view.Gravity
import android.view.View
import android.view.ViewManager
import com.san.kir.manger.R
import com.san.kir.manger.utils.ID
import com.san.kir.manger.utils.getDrawableCompat
import com.san.kir.manger.utils.withoutParent
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.appcompat.v7.toolbar
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.design.appBarLayout
import org.jetbrains.anko.design.navigationView
import org.jetbrains.anko.dip
import org.jetbrains.anko.frameLayout
import org.jetbrains.anko.imageView
import org.jetbrains.anko.leftPadding
import org.jetbrains.anko.linearLayout
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.padding
import org.jetbrains.anko.rightPadding
import org.jetbrains.anko.sp
import org.jetbrains.anko.support.v4.drawerLayout
import org.jetbrains.anko.textView
import org.jetbrains.anko.topPadding
import org.jetbrains.anko.verticalLayout
import org.jetbrains.anko.wrapContent

class MainView(private val activity: MainActivity) : AnkoComponent<MainActivity> {

    object _id {
        val library = ID.generate()
        val storage = ID.generate()
        val category = ID.generate()
        val catalogs = ID.generate()
        val downloader = ID.generate()
        val latest = ID.generate()
        val settings = ID.generate()
        val fragment = ID.generate()
    }

    lateinit var drawer_layout: DrawerLayout
    lateinit var toolbar: Toolbar

    override fun createView(ui: AnkoContext<MainActivity>) = with(ui) {
        drawer_layout = drawerLayout {
            lparams(width = matchParent, height = matchParent)
            fitsSystemWindows = true

            verticalLayout {
                lparams(width = matchParent, height = matchParent)

                appBarLayout {
                    lparams(width = matchParent, height = wrapContent)
                    toolbar = toolbar {
                        lparams(width = matchParent, height = wrapContent)
                        backgroundResource = R.color.material_grey_900
                        setTitleTextColor(Color.WHITE)
                        overflowIcon = getDrawableCompat(R.drawable.dots_vertical)
                        activity.setSupportActionBar(this)
                    }
                }

                frameLayout {
                    lparams(width = matchParent, height = matchParent)
                    id = _id.fragment
                }
            }

            navigationView {
                fitsSystemWindows = true

                // Меню боковой панели
                menu.apply {
                    // TODO после добавлю возможность менять порядок элементов меню
                    add(0, _id.library, 0, com.san.kir.manger.R.string.main_menu_library)
                    add(0, _id.storage, 0, com.san.kir.manger.R.string.main_menu_storage)
                    add(0, _id.category, 0, com.san.kir.manger.R.string.main_menu_category)
                    add(0, _id.catalogs, 0, com.san.kir.manger.R.string.main_menu_catalogs)
                    add(0, _id.downloader, 0, com.san.kir.manger.R.string.main_menu_downloader)
                    add(0, _id.latest, 0, com.san.kir.manger.R.string.main_menu_latest)
                    add(0, _id.settings, 0, com.san.kir.manger.R.string.action_settings)
                }

                setNavigationItemSelectedListener {
                    activity.onNavigationItemSelected(it.itemId)
                    this@drawerLayout.closeDrawer(android.support.v4.view.GravityCompat.START)
                    return@setNavigationItemSelectedListener true
                }

                // Шапка боковой панели
                addHeaderView(headerView())

            }.lparams(width = wrapContent, height = matchParent) {
                gravity = GravityCompat.START
            }

            ActionBarDrawerToggle(activity, this, toolbar,
                                  R.string.navigation_drawer_open,
                                  R.string.navigation_drawer_close).apply {
                addDrawerListener(this)
                drawerArrowDrawable = drawerArrowDrawable.apply { color = Color.WHITE }
                syncState()
            }
        }
        drawer_layout
    }

    private fun ViewManager.headerView(): View {
        return this.verticalLayout(com.san.kir.manger.R.style.ThemeOverlay_AppCompat_Dark) {
            lparams(width = matchParent, height = wrapContent)
            backgroundResource = com.san.kir.manger.R.color.material_grey_900
            padding = dip(16)

            linearLayout {
                // Иконка приложения
                imageView { backgroundResource = com.san.kir.manger.R.mipmap.ic_launcher }

                // Название приложения
                textView(text = com.san.kir.manger.R.string.app_name) {
                    gravity = Gravity.CENTER_VERTICAL
                    leftPadding = dip(10)
                    rightPadding = dip(10)
                    textSize = sp(8).toFloat()
                }.lparams(width = wrapContent, height = matchParent)
                // Текущая версия приложения
                textView(text = com.san.kir.manger.BuildConfig.VERSION_NAME) {
                    gravity = Gravity.CENTER_VERTICAL
                }.lparams(width = wrapContent, height = matchParent) {
                    topMargin = dip(5)
                }

            }

            // Имя автора, точнее никнейм
            textView(text = com.san.kir.manger.R.string.name) {
                topPadding = dip(10)
            }

            // емайл адрес
            textView(text = com.san.kir.manger.R.string.email)

            // избавляет от родителя
        }.withoutParent()
    }
}
