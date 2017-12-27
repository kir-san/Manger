package com.san.kir.manger.components.ListChapters

import android.graphics.Color
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.View
import android.view.ViewManager
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import com.san.kir.manger.EventBus.Binder
import com.san.kir.manger.Extending.AnkoExtend.visibleOrGone
import com.san.kir.manger.R
import com.san.kir.manger.room.DAO.ChapterFilter
import com.san.kir.manger.utils.ID
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.above
import org.jetbrains.anko.alignParentBottom
import org.jetbrains.anko.alignParentTop
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.below
import org.jetbrains.anko.dip
import org.jetbrains.anko.horizontalProgressBar
import org.jetbrains.anko.imageButton
import org.jetbrains.anko.include
import org.jetbrains.anko.linearLayout
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.relativeLayout
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.verticalLayout
import org.jetbrains.anko.wrapContent

class ListChapterView(private val adapterPresenter: ListChaptersRecyclerPresenter) : AnkoComponent<ListChaptersActivity> {
    private object _id {
        val progressBar = ID.generate()
        val bottomBar = ID.generate()
    }

    private var _filterState: Filter = FilterAsc
    private val filterIndicator = Binder(ChapterFilter.ALL_READ_ASC)
    private val sortIndicator = Binder(false) // Индикатор сортировки
    val isVisibleBottom = Binder(true) // Отображение бара снизу
    val isAction = Binder(false) // Отображения прогрессБара


    var filterState: String
        get() = filterIndicator.item.name
        set(value) {
            val state = ChapterFilter.valueOf(value)
            _filterState = when (state) {
                ChapterFilter.ALL_READ_ASC,
                ChapterFilter.NOT_READ_ASC,
                ChapterFilter.IS_READ_ASC -> {
                    FilterAsc
                }
                ChapterFilter.ALL_READ_DESC,
                ChapterFilter.NOT_READ_DESC,
                ChapterFilter.IS_READ_DESC -> {
                    FilterDesc
                }
            }
            sortIndicator.item = _filterState.isAsc
            filterIndicator.item = state
        }

    override fun createView(ui: AnkoContext<ListChaptersActivity>) = with(ui) {
        filterIndicator.bind {
            adapterPresenter.changeOrder(it)
            sortIndicator.item = _filterState.isAsc
        }

        // Нужные переменные
        val actionBarSize = dip(50) // Размер бара снизу

        verticalLayout {
            // Корень
            relativeLayout {
                lparams(width = matchParent, height = matchParent)

                // ПрогрессБар для отображения поиска новых глав
                horizontalProgressBar {
                    id = _id.progressBar
                    isIndeterminate = true
                    visibleOrGone(isAction)
                }.lparams(width = matchParent, height = wrapContent) {
                    alignParentTop()
                }

                // Бар внизу экрана с кнопками сортировки и фильтрации
                linearLayout {
                    id = _id.bottomBar

                    backgroundColor = Color.parseColor("#ff212121")

                    isVisibleBottom.bind {
                        visibility = if (it) View.VISIBLE else View.GONE
                    }

                    // Кнопка переключения порядка сортировки
                    btn {
                        onClick {
                            _filterState = _filterState.reverse()
                            filterIndicator.item = filterIndicator.item.inverse()
                        }
                        sortIndicator.bind {
                            backgroundResource =
                                    if (it) R.drawable.ic_sort_21
                                    else R.drawable.ic_sort_12
                        }
                    }

                    // Блок кнопок переключения сортировки
                    linearLayout {
                        lparams(width = matchParent, height = actionBarSize)

                        backgroundColor = Color.parseColor("#ff212121")
                        gravity = Gravity.CENTER_HORIZONTAL

                        // Кнопка включения отображения всех глав
                        btn {
                            onClick { filterIndicator.item = _filterState.ALL_READ }
                            filterIndicator.bind {
                                backgroundResource =
                                        if (it == _filterState.ALL_READ) R.drawable.ic_action_all_blue
                                        else R.drawable.ic_action_all_white
                            }
                        }

                        // Кнопка включения отображения только прочитанных глав
                        btn {
                            onClick { filterIndicator.item = _filterState.IS_READ }
                            filterIndicator.bind {
                                backgroundResource =
                                        if (it == _filterState.IS_READ) R.drawable.ic_action_read_blue
                                        else R.drawable.ic_action_read_white
                            }
                        }

                        // Кнопка включения отображения только не прочитанных глав
                        btn {
                            onClick { filterIndicator.item = _filterState.NOT_READ }
                            filterIndicator.bind {
                                backgroundResource =
                                        if (it == _filterState.NOT_READ) R.drawable.ic_action_not_read_blue
                                        else R.drawable.ic_action_not_read_white
                            }
                        }
                    }
                }.lparams(width = matchParent, height = actionBarSize) { alignParentBottom() }

                // Виджет списка глав (используется такой способ, так как по другому скроллБар не работает)
                include<RecyclerView>(R.layout.recycler_view) {
                    layoutManager = LinearLayoutManager(ctx)
                    adapterPresenter.into(this)
                }.lparams(width = matchParent, height = matchParent) {
                    below(_id.progressBar) // Начинается от прогрессБара
                    above(_id.bottomBar) // Заканчивается на нижнем меню
                }
            }

        }
    }

    private fun ViewManager.btn(action: ImageButton.() -> Unit): ImageButton {
        val backColor = Color.parseColor("#00ffffff") // Цвет заднего фона
        val buttonSize = 38 // Размер кнопок
        return imageButton {
            action()
            scaleType = ImageView.ScaleType.CENTER
            backgroundColor = backColor
            layoutParams = LinearLayout.LayoutParams(dip(buttonSize), dip(buttonSize)).apply {
                gravity = Gravity.CENTER_VERTICAL
                leftMargin = dip(10)
                rightMargin = dip(10)
            }
        }
    }

    private interface Filter {
        val ALL_READ: ChapterFilter
        val IS_READ: ChapterFilter
        val NOT_READ: ChapterFilter
        fun reverse(): Filter
        val isAsc: Boolean
    }

    private object FilterAsc : Filter {
        override val ALL_READ = ChapterFilter.ALL_READ_ASC
        override val IS_READ = ChapterFilter.IS_READ_ASC
        override val NOT_READ = ChapterFilter.NOT_READ_ASC
        override fun reverse() = FilterDesc
        override val isAsc = true
    }

    private object FilterDesc : Filter {
        override val ALL_READ = ChapterFilter.ALL_READ_DESC
        override val IS_READ = ChapterFilter.IS_READ_DESC
        override val NOT_READ = ChapterFilter.NOT_READ_DESC
        override fun reverse() = FilterAsc
        override val isAsc = false
    }
}
