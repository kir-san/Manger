package com.san.kir.manger.components.catalog_for_one_site

import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewManager
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.san.kir.ankofork.AnkoComponent
import com.san.kir.ankofork.AnkoContext
import com.san.kir.ankofork.Binder
import com.san.kir.ankofork.appcompat.toolbar
import com.san.kir.ankofork.design.navigationView
import com.san.kir.ankofork.design.themedAppBarLayout
import com.san.kir.ankofork.dip
import com.san.kir.ankofork.horizontalProgressBar
import com.san.kir.ankofork.include
import com.san.kir.ankofork.leftPadding
import com.san.kir.ankofork.matchParent
import com.san.kir.ankofork.rightPadding
import com.san.kir.ankofork.sdk28.backgroundResource
import com.san.kir.ankofork.sdk28.button
import com.san.kir.ankofork.sdk28.imageButton
import com.san.kir.ankofork.sdk28.linearLayout
import com.san.kir.ankofork.sdk28.onClick
import com.san.kir.ankofork.sdk28.space
import com.san.kir.ankofork.support.drawerLayout
import com.san.kir.ankofork.support.viewPager
import com.san.kir.ankofork.toggle
import com.san.kir.ankofork.verticalLayout
import com.san.kir.ankofork.wrapContent
import com.san.kir.manger.R
import com.san.kir.manger.components.catalog_for_one_site.CatalogForOneSiteRecyclerPresenter.Companion.DATE
import com.san.kir.manger.components.catalog_for_one_site.CatalogForOneSiteRecyclerPresenter.Companion.NAME
import com.san.kir.manger.components.catalog_for_one_site.CatalogForOneSiteRecyclerPresenter.Companion.POP
import com.san.kir.manger.utils.extensions.doOnApplyWindowInstets
import com.san.kir.manger.utils.extensions.visibleOrGone

class CatalogForOneSiteView(
    private val act: CatalogForOneSiteActivity,
    private val presenter: CatalogForOneSiteRecyclerPresenter
) : AnkoComponent<CatalogForOneSiteActivity> {

    lateinit var drawerLayout: DrawerLayout
    lateinit var toolbar: Toolbar

    val isAction = Binder(true)
    private val sort = Binder(DATE) { sortType ->
        presenter.changeOrder(sortType = sortType)
    }
    private val sortIndicator = Binder(false)

    init {
        sortIndicator.bind {
            // переключение порядка сортировки
            presenter.changeOrder(isReversed = it)
        }

    }

    override fun createView(ui: AnkoContext<CatalogForOneSiteActivity>) = with(ui) {
        // Адаптер с данными для фильтрации
        drawerLayout {
            lparams(width = matchParent, height = matchParent)

            systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)

            verticalLayout {
                lparams(width = matchParent, height = matchParent)

                doOnApplyWindowInstets { v, insets, padding ->
                    v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                        // Получаем размер выреза, если есть
                        val cutoutRight = insets.displayCutout?.safeInsetRight ?: 0
                        val cutoutLeft = insets.displayCutout?.safeInsetLeft ?: 0
                        // Вычитаем из WindowInsets размер выреза, для fullscreen
                        rightMargin = insets.systemWindowInsetRight - cutoutRight
                        leftMargin = insets.systemWindowInsetLeft - cutoutLeft
                    }
                    v.updatePadding(
                        bottom = padding.bottom + insets.systemWindowInsetBottom
                    )
                    insets
                }

                themedAppBarLayout(R.style.ThemeOverlay_AppCompat_DayNight_ActionBar) {
                    lparams(width = matchParent, height = wrapContent)

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

                horizontalProgressBar {
                    isIndeterminate = true
                    visibleOrGone(isAction)
                    progressDrawable = ContextCompat.getDrawable(
                        this@with.ctx, R.drawable.storage_progressbar
                    )
                }.lparams(width = matchParent, height = dip(10))

                // Список
                include<RecyclerView>(R.layout.recycler_view) {
                    layoutManager = LinearLayoutManager(act)
                    this@CatalogForOneSiteView.presenter.into(this)
                }.lparams(width = matchParent, height = matchParent) {
                    weight = 1f
                }

                // Нижняя панель для сортировки элементов
                linearLayout {
                    gravity = Gravity.CENTER_HORIZONTAL

                    // переключение порядка сортировки
                    btn {
                        onClick { sortIndicator.toggle() }
                        sortIndicator.bind {
                            backgroundResource =
                                if (it) R.drawable.ic_sort_21
                                else R.drawable.ic_sort_12
                        }
                    }

                    space { }.lparams(width = dip(64))

                    btn {
                        // Сортировка по названию
                        onClick { sort.item = NAME }
                        sort.bind { sortType ->
                            backgroundResource =
                                if (sortType == NAME) R.drawable.ic_abc_blue
                                else R.drawable.ic_abc
                        }
                    }

                    btn {
                        // Сортировка по дате
                        onClick { sort.item = DATE }
                        sort.bind { sortType ->
                            backgroundResource =
                                if (sortType == DATE) R.drawable.ic_date_range_blue
                                else R.drawable.ic_date_range
                        }
                    }

                    btn {
                        // Сортировка по популярности
                        onClick { sort.item = POP }
                        sort.bind { sortType ->
                            backgroundResource =
                                if (sortType == POP) R.drawable.ic_rate_blue
                                else R.drawable.ic_rate
                        }
                    }

                    space { }.lparams(width = dip(64))

                    btn {
                        // Сортировка по популярности
                        onClick { act.reloadCatalogDialog() }
                        backgroundResource = R.drawable.ic_update
                    }

                }.lparams(width = matchParent, height = dip(50))
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
                    viewPager {
                        include<TabLayout>(R.layout.tab_layout).apply {
                            setupWithViewPager(this@viewPager)
                        }
                        adapter = presenter.pagerAdapter
                    }.lparams(height = matchParent) {
                        weight = 1f
                    }

                    button("Очистить") {
                        doOnApplyWindowInstets { v, insets, padding ->
                            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                                bottomMargin = insets.systemWindowInsetBottom
                            }
                            insets
                        }
                        onClick {
                            presenter.pagerAdapter.adapters.forEach { it.clearSelected() }
                        }
                    }.lparams(width = matchParent)
                }
            }.lparams(width = matchParent, height = matchParent) {
                gravity = GravityCompat.START
            }

            val toggle = object : ActionBarDrawerToggle(
                act, this, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
            ) {
                override fun onDrawerClosed(drawerView: View) {
                    super.onDrawerClosed(drawerView)
                    act.title =
                        "${act.mOldTitle}: ${presenter.changeOrder(filters = presenter.pagerAdapter.adapters)}"
                }

            }
            addDrawerListener(toggle)
            toggle.syncState()

            drawerLayout = this
        }
    }

    private fun ViewManager.btn(action: ImageButton.() -> Unit): ImageButton {
        val buttonSize = 35 // Размер кнопок
        return imageButton {
            action()
            scaleType = ImageView.ScaleType.CENTER
            layoutParams = LinearLayout.LayoutParams(dip(buttonSize), dip(buttonSize)).apply {
                gravity = Gravity.CENTER_VERTICAL
                leftMargin = dip(8)
                rightMargin = dip(8)
            }
        }
    }
}
