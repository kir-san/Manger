package com.san.kir.manger.components.CatalogForOneSite

import android.graphics.Color
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.view.PagerTabStrip
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.Gravity
import android.view.View
import android.widget.TextView
import com.san.kir.manger.EventBus.Binder
import com.san.kir.manger.EventBus.BinderRx
import com.san.kir.manger.Extending.AnkoExtend.bind
import com.san.kir.manger.utils.ID
import com.san.kir.manger.utils.getDrawableCompat
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
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
import org.jetbrains.anko.support.v4.viewPager
import org.jetbrains.anko.textColor
import org.jetbrains.anko.textView
import org.jetbrains.anko.wrapContent

class CatalogForOneSiteView(val act: CatalogForOneSiteActivity) : AnkoComponent<CatalogForOneSiteActivity> {
    private object _id {
        val appbar = ID.generate()
        val bottom = ID.generate()
    }

    lateinit var drawer_layout: DrawerLayout
    lateinit var toolbar: Toolbar
    lateinit var navView: NavigationView

    val adapter: BinderRx<CatalogForOneSiteAdapter?> = BinderRx(null)

    // Переключатели сортировок
    private val date = Binder(true)
    private val name = Binder(false)
    private val pop = Binder(false)

    override fun createView(ui: AnkoContext<CatalogForOneSiteActivity>) = with(ui) {
        // Адаптер с данными для фильтрации
        val pagerAdapter = CatalogForOneSiteFilterPagesAdapter(this.ctx, act.filterAdapterList)

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
                        backgroundResource = com.san.kir.manger.R.color.material_grey_900
                        setTitleTextColor(Color.WHITE)
                        overflowIcon = getDrawableCompat(com.san.kir.manger.R.drawable.dots_vertical)
                        act.setSupportActionBar(this)
                    }
                }

                // Список
                include<RecyclerView>(com.san.kir.manger.R.layout.recycler_view) {
                    layoutManager = LinearLayoutManager(this@with.ctx)

                    bind(this@CatalogForOneSiteView.adapter) {
                        adapter = it
                    }
                }.lparams(width = matchParent, height = matchParent) {
                    below(_id.appbar) // Ниже тулбара
                    above(_id.bottom) // Выше нижней панели
                }

                // Нижняя панель для сортировки элементов
                linearLayout {
                    id = _id.bottom
                    backgroundResource = com.san.kir.manger.R.color.material_grey_900

                    // переключение порядка сортировки
                    imageButton {
                        backgroundColor = Color.parseColor("#00ffffff")
                        backgroundResource = com.san.kir.manger.R.drawable.ic_triangle_down

                        var isUp = false
                        onClick {
                            isUp = !isUp
                            backgroundResource =
                                    if (isUp) com.san.kir.manger.R.drawable.ic_triangle_up
                                    else com.san.kir.manger.R.drawable.ic_triangle_down
                            adapter.item?.changeOrder(isReversed = isUp)
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
                                name.item = true
                                date.item = false
                                pop.item = false
                                adapter.item?.changeOrder(sortType = com.san.kir.manger.components.CatalogForOneSite.CatalogForOneSiteAdapter.NAME)
                            }
                            bind(name) { textColor = toogleColor(it) }
                        }

                        // Сортировка по дате
                        textView(text = com.san.kir.manger.R.string.catalog_for_one_site_date) {
                            onClick {
                                date.item = true
                                name.item = false
                                pop.item = false
                                adapter.item?.changeOrder(sortType = com.san.kir.manger.components.CatalogForOneSite.CatalogForOneSiteAdapter.DATE)
                            }
                            bind(date) { textColor = toogleColor(it) }
                        }

                        // Сортировка по популярности
                        textView(text = com.san.kir.manger.R.string.catalog_for_one_site_pop) {
                            onClick {
                                pop.item = true
                                date.item = false
                                name.item = false
                                adapter.item?.changeOrder(sortType = com.san.kir.manger.components.CatalogForOneSite.CatalogForOneSiteAdapter.POP)
                            }
                            bind(pop) { textColor = toogleColor(it) }
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

            navView = navigationView {
                viewPager {
                    include<PagerTabStrip>(com.san.kir.manger.R.layout.page_tab_strip)
                    adapter = pagerAdapter
                }
            }.lparams(width = wrapContent, height = matchParent) {
                gravity = GravityCompat.START
            }

            val toggle = object : ActionBarDrawerToggle(act, this, toolbar,
                                                        com.san.kir.manger.R.string.navigation_drawer_open,
                                                        com.san.kir.manger.R.string.navigation_drawer_close) {
                override fun onDrawerClosed(drawerView: View?) {
                    super.onDrawerClosed(drawerView)
                    launch(UI) {
                        act.title = "${act.mOldTitle}: ${adapter.item?.changeOrder(filters = pagerAdapter.adapters)}"
                    }
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
