package com.san.kir.manger.components.drawer

import android.content.Intent
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.drawerlayout.widget.DrawerLayout
import com.san.kir.ankofork.AnkoContextImpl
import com.san.kir.ankofork.appcompat.toolbar
import com.san.kir.ankofork.design.navigationView
import com.san.kir.ankofork.design.themedAppBarLayout
import com.san.kir.ankofork.leftPadding
import com.san.kir.ankofork.matchParent
import com.san.kir.ankofork.rightPadding
import com.san.kir.ankofork.sdk28._LinearLayout
import com.san.kir.ankofork.support.drawerLayout
import com.san.kir.ankofork.verticalLayout
import com.san.kir.ankofork.wrapContent
import com.san.kir.manger.R
import com.san.kir.manger.components.download_manager.DownloadManagerActivity
import com.san.kir.manger.utils.extensions.BaseActivity
import com.san.kir.manger.utils.extensions.doOnApplyWindowInstets

class DrawerView(private val act: DrawerActivity) {
    lateinit var drawerLayout: DrawerLayout
    private lateinit var toolbar: Toolbar

    fun init() = Unit

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
                        showScreen()
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

    private fun showScreen() {

        val activity =
            DownloadManagerActivity::class.java
//            LatestChapterActivity::class.java
//            SettingActivity::class.java
//            ScheduleActivity::class.java
//            StatisticActivity::class.java
//            LibraryActivity::class.java


        val intent = Intent(act, activity)
        act.startActivity(intent)
        act.finish()
    }

}
