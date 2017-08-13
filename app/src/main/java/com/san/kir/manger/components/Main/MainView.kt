package com.san.kir.manger.components.Main

import android.graphics.Color
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.Toolbar
import android.view.Gravity
import android.view.View
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

class MainView(val activity: MainActivity) : AnkoComponent<MainActivity> {
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
    lateinit var navView: NavigationView
    lateinit var fragment: View

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
                        backgroundResource = com.san.kir.manger.R.color.material_grey_900
                        setTitleTextColor(Color.WHITE)
                        overflowIcon = getDrawableCompat(com.san.kir.manger.R.drawable.dots_vertical)
                        activity.setSupportActionBar(this)
                    }
                }

                fragment = frameLayout {
                    lparams(width = matchParent, height = matchParent)
                    id = _id.fragment
                }
            }

            navView = navigationView {
                fitsSystemWindows = true

                // Меню боковой панели
                menu.apply {
                    val group = 0
                    // TODO после добавлю возможность менять порядок элементов меню
                    val order = 0
                    add(group, _id.library, order, com.san.kir.manger.R.string.main_menu_library)
                    add(group, _id.storage, order, com.san.kir.manger.R.string.main_menu_storage)
                    add(group, _id.category, order, com.san.kir.manger.R.string.main_menu_category)
                    add(group, _id.catalogs, order, com.san.kir.manger.R.string.main_menu_catalogs)
                    add(group, _id.downloader, order, com.san.kir.manger.R.string.main_menu_downloader)
                    add(group, _id.latest, order, com.san.kir.manger.R.string.main_menu_latest)
                    add(group, _id.settings, order, com.san.kir.manger.R.string.action_settings)
                }

                setNavigationItemSelectedListener {
                    activity.onNavigationItemSelected(it.itemId)
                    this@drawerLayout.closeDrawer(android.support.v4.view.GravityCompat.START)
                    return@setNavigationItemSelectedListener true
                }

                // Шапка боковой панели
                addHeaderView(verticalLayout(com.san.kir.manger.R.style.ThemeOverlay_AppCompat_Dark) {
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
                        }. lparams(width = wrapContent, height = matchParent) {
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
                }.withoutParent())
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
}
