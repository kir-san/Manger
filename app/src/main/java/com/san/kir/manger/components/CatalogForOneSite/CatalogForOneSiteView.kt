package com.san.kir.manger.components.CatalogForOneSite

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
import android.widget.TextView
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.instance
import com.san.kir.manger.EventBus.Binder
import com.san.kir.manger.EventBus.negative
import com.san.kir.manger.EventBus.positive
import com.san.kir.manger.EventBus.toogle
import com.san.kir.manger.Extending.AnkoExtend.visibleOrGone
import com.san.kir.manger.R
import com.san.kir.manger.components.CatalogForOneSite.CatalogForOneSiteRecyclerPresenter.Companion.DATE
import com.san.kir.manger.components.CatalogForOneSite.CatalogForOneSiteRecyclerPresenter.Companion.NAME
import com.san.kir.manger.components.CatalogForOneSite.CatalogForOneSiteRecyclerPresenter.Companion.POP
import com.san.kir.manger.utils.ID
import com.san.kir.manger.utils.getDrawableCompat
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.above
import org.jetbrains.anko.alignParentBottom
import org.jetbrains.anko.appcompat.v7.toolbar
import org.jetbrains.anko.applyRecursively
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
import org.jetbrains.anko.lines
import org.jetbrains.anko.margin
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.padding
import org.jetbrains.anko.relativeLayout
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.support.v4.drawerLayout
import org.jetbrains.anko.support.v4.swipeRefreshLayout
import org.jetbrains.anko.support.v4.viewPager
import org.jetbrains.anko.textColor
import org.jetbrains.anko.textView
import org.jetbrains.anko.wrapContent

class CatalogForOneSiteView(inj: KodeinInjector,
                            private val adapter: CatalogForOneSiteRecyclerPresenter) : AnkoComponent<CatalogForOneSiteActivity> {
    private object _id {
        val appbar = ID.generate()
        val bottom = ID.generate()
        val progressbar = ID.generate()
    }

    private val filterAdapterList: List<CatalogFilter> by inj.instance()
    private val act: CatalogForOneSiteActivity by inj.instance()

    lateinit var drawer_layout: DrawerLayout
    lateinit var toolbar: Toolbar
    lateinit var swipe: SwipeRefreshLayout

    // Переключатели сортировок
    val isAction = Binder(false)
    private val date = Binder(true)
    private val name = Binder(false)
    private val pop = Binder(false)
    private val sortIndicator = Binder(false)

    init {
        sortIndicator.bind {
            // переключение порядка сортировки
            adapter.changeOrder(isReversed = it)
        }

    }

    override fun createView(ui: AnkoContext<CatalogForOneSiteActivity>) = with(ui) {
        // Адаптер с данными для фильтрации
        val pagerAdapter = CatalogForOneSiteFilterPagesAdapter(this.ctx, filterAdapterList)

        drawer_layout = drawerLayout {
            lparams(width = matchParent, height = matchParent)
            fitsSystemWindows = true

            relativeLayout {
                lparams(width = matchParent, height = matchParent)

                appBarLayout {
                    lparams(width = matchParent, height = wrapContent)
                    id = _id.appbar

                    toolbar = toolbar {
                        lparams(width = matchParent, height = wrapContent)
                        backgroundColor = Color.parseColor("#ff212121") // material_grey_900
                        setTitleTextColor(Color.WHITE)
                        overflowIcon = getDrawableCompat(R.drawable.dots_vertical)
                        act.setSupportActionBar(this)
                    }
                }
                horizontalProgressBar {
                    id = _id.progressbar
                    isIndeterminate = true
                    visibleOrGone(isAction)
                    progressDrawable = ContextCompat.getDrawable(this@with.ctx,
                                                                 R.drawable.storage_progressbar)
                }.lparams(width = matchParent, height = dip(10)) {
                    below(_id.appbar)
                }

                swipe = swipeRefreshLayout {
                    // Список
                    include<RecyclerView>(R.layout.recycler_view) {
                        layoutManager = LinearLayoutManager(this@with.ctx)
                        this@CatalogForOneSiteView.adapter.into(this)
                    }
                }.lparams(width = matchParent, height = matchParent) {
                    below(_id.progressbar)
                    above(_id.bottom)
                }


                // Нижняя панель для сортировки элементов
                linearLayout {
                    id = _id.bottom
                    backgroundColor = Color.parseColor("#ff212121") // material_grey_900

                    // переключение порядка сортировки
                    imageButton {
                        backgroundColor = Color.parseColor("#00ffffff")
                        onClick {
                            // переключение режима сортировки
                            sortIndicator.toogle()
                        }

                        sortIndicator.bind {
                            // Переключение иконки сортировки
                            backgroundResource =
                                    if (it) R.drawable.ic_sort_21
                                    else R.drawable.ic_sort_12
                        }

                    }.lparams(width = dip(30), height = dip(30)) {
                        margin = dip(6)
                    }

                    // Группа кнопок
                    linearLayout {
                        lparams(width = 0, height = matchParent) {
                            weight = 1f
                        }
                        gravity = Gravity.CENTER

                        // Сортировка по названию
                        textView(text = com.san.kir.manger.R.string.catalog_for_one_site_name) {
                            onClick {
                                name.positive()
                                date.negative()
                                pop.negative()
                                adapter.changeOrder(sortType = NAME)
                            }
                            name.bind { textColor = toogleColor(it) }
                        }

                        // Сортировка по дате
                        textView(text = com.san.kir.manger.R.string.catalog_for_one_site_date) {
                            onClick {
                                date.positive()
                                name.negative()
                                pop.negative()
                                adapter.changeOrder(sortType = DATE)
                            }
                            date.bind { textColor = toogleColor(it) }
                        }

                        // Сортировка по популярности
                        textView(text = com.san.kir.manger.R.string.catalog_for_one_site_pop) {
                            onClick {
                                pop.positive()
                                date.negative()
                                name.negative()
                                adapter.changeOrder(sortType = POP)
                            }
                            pop.bind { textColor = toogleColor(it) }
                        }

                    }.applyRecursively { view ->
                        when (view) {
                        // Применение одинаковых параметров для всех текстов
                            is TextView -> {
                                view.lparams(width = wrapContent,
                                             height = wrapContent) { weight = 1f }
                                view.isClickable = true
                                view.padding = dip(8)
                                view.textSize = 16f
                                view.lines = 1
                            }
                        }
                    }
                }.lparams(width = matchParent, height = wrapContent) {
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

            val toggle = object : ActionBarDrawerToggle(act, this, toolbar,
                                                        R.string.navigation_drawer_open,
                                                        R.string.navigation_drawer_close) {
                override fun onDrawerClosed(drawerView: View?) {
                    super.onDrawerClosed(drawerView)
                    act.title = "${act.mOldTitle}: ${adapter.changeOrder(filters = pagerAdapter.adapters)}"
                }

            }
            toggle.drawerArrowDrawable = toggle.drawerArrowDrawable.apply { color = Color.WHITE }
            addDrawerListener(toggle)
            toggle.syncState()
        }
        drawer_layout
    }

    // Переключает цвет текста
    private fun toogleColor(isVisible: Boolean): Int = if (isVisible) Color.WHITE else Color.GRAY
}
