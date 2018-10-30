package com.san.kir.manger.components.catalogForOneSite

import android.graphics.Color
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v4.view.PagerTabStrip
import android.support.v4.widget.DrawerLayout
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.Gravity
import android.view.View
import android.view.ViewManager
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import com.san.kir.manger.R
import com.san.kir.manger.components.catalogForOneSite.CatalogForOneSiteRecyclerPresenter.Companion.DATE
import com.san.kir.manger.components.catalogForOneSite.CatalogForOneSiteRecyclerPresenter.Companion.NAME
import com.san.kir.manger.components.catalogForOneSite.CatalogForOneSiteRecyclerPresenter.Companion.POP
import com.san.kir.manger.eventBus.Binder
import com.san.kir.manger.eventBus.toggle
import com.san.kir.manger.extending.ankoExtend.onClick
import com.san.kir.manger.extending.ankoExtend.visibleOrGone
import com.san.kir.manger.utils.ID
import com.san.kir.manger.utils.getDrawableCompat
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.above
import org.jetbrains.anko.alignParentBottom
import org.jetbrains.anko.appcompat.v7.toolbar
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.below
import org.jetbrains.anko.design.appBarLayout
import org.jetbrains.anko.design.navigationView
import org.jetbrains.anko.dip
import org.jetbrains.anko.horizontalProgressBar
import org.jetbrains.anko.imageButton
import org.jetbrains.anko.include
import org.jetbrains.anko.linearLayout
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.relativeLayout
import org.jetbrains.anko.space
import org.jetbrains.anko.support.v4.drawerLayout
import org.jetbrains.anko.support.v4.swipeRefreshLayout
import org.jetbrains.anko.support.v4.viewPager
import org.jetbrains.anko.wrapContent

class CatalogForOneSiteView(
    private val act: CatalogForOneSiteActivity,
    private val presenter: CatalogForOneSiteRecyclerPresenter
) : AnkoComponent<CatalogForOneSiteActivity> {
    private object Id {
        val appbar = ID.generate()
        val bottom = ID.generate()
        val progressbar = ID.generate()
    }

    lateinit var drawerLayout: DrawerLayout
    lateinit var toolbar: Toolbar
    lateinit var swipe: SwipeRefreshLayout

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
        val pagerAdapter =
            CatalogForOneSiteFilterPagesAdapter(this.ctx, presenter.filterAdapterList)

        drawerLayout {
            lparams(width = matchParent, height = matchParent)
            fitsSystemWindows = true

            relativeLayout {
                lparams(width = matchParent, height = matchParent)

                appBarLayout {
                    lparams(width = matchParent, height = wrapContent)
                    id = Id.appbar

                    toolbar = toolbar {
                        lparams(width = matchParent, height = wrapContent)
                        backgroundColor = Color.parseColor("#ff212121") // material_grey_900
                        setTitleTextColor(Color.WHITE)
                        overflowIcon = getDrawableCompat(R.drawable.dots_vertical)
                        act.setSupportActionBar(this)
                    }
                }
                horizontalProgressBar {
                    id = Id.progressbar
                    isIndeterminate = true
                    visibleOrGone(isAction)
                    progressDrawable = ContextCompat.getDrawable(
                        this@with.ctx,
                        R.drawable.storage_progressbar
                    )
                }.lparams(width = matchParent, height = dip(10)) {
                    below(Id.appbar)
                }

                swipe = swipeRefreshLayout {
                    // Список
                    include<RecyclerView>(R.layout.recycler_view) {
                        layoutManager = LinearLayoutManager(this@with.ctx)
                        this@CatalogForOneSiteView.presenter.into(this)
                    }
                }.lparams(width = matchParent, height = matchParent) {
                    below(Id.progressbar)
                    above(Id.bottom)
                }


                // Нижняя панель для сортировки элементов
                linearLayout {
                    id = Id.bottom
                    backgroundColor = Color.parseColor("#ff212121") // material_grey_900
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

                    space { }.lparams(width = dip(34))

                    btn {
                        // Сортировка по названию
                        onClick {
                            sort.item = NAME
                        }
                        sort.bind { sortType ->
                            backgroundResource =
                                    if (sortType == NAME) R.drawable.ic_abc_blue
                                    else R.drawable.ic_abc_white
                        }
                    }

                    btn {
                        // Сортировка по дате
                        onClick {
                            sort.item = DATE
                        }
                        sort.bind { sortType ->
                            backgroundResource =
                                    if (sortType == DATE) R.drawable.ic_date_range_blue
                                    else R.drawable.ic_date_range_white
                        }
                    }

                    btn {
                        // Сортировка по популярности
                        onClick {
                            sort.item = POP
                        }
                        sort.bind { sortType ->
                            backgroundResource =
                                    if (sortType == POP) R.drawable.ic_rate_blue
                                    else R.drawable.ic_rate_white
                        }
                    }

                    space { }.lparams(width = dip(34))

                    btn {
                        // Сортировка по популярности
                        onClick {
                            act.reloadCatalogDialog()
                        }
                        backgroundResource = R.drawable.ic_update

                    }

                }.lparams(width = matchParent, height = dip(50)) {
                    alignParentBottom()
                }
            }

            navigationView {
                viewPager {
                    include<PagerTabStrip>(R.layout.page_tab_strip)
                    adapter = pagerAdapter
                }
            }.lparams(width = wrapContent, height = matchParent) {
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
                            "${act.mOldTitle}: ${presenter.changeOrder(filters = pagerAdapter.adapters)}"
                }

            }
            toggle.drawerArrowDrawable = toggle.drawerArrowDrawable.apply { color = Color.WHITE }
            addDrawerListener(toggle)
            toggle.syncState()

            drawerLayout = this
        }
    }

    private fun ViewManager.btn(action: ImageButton.() -> Unit): ImageButton {
//        val backColor = Color.parseColor("#00ffffff") // Цвет заднего фона
        val buttonSize = 38 // Размер кнопок
        return imageButton {
            action()
            scaleType = ImageView.ScaleType.CENTER
//            backgroundColor = backColor
            layoutParams = LinearLayout.LayoutParams(dip(buttonSize), dip(buttonSize)).apply {
                gravity = Gravity.CENTER_VERTICAL
                leftMargin = dip(12)
                rightMargin = dip(12)
            }
        }
    }
}
