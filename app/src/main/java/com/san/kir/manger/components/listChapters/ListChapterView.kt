package com.san.kir.manger.components.listChapters

import android.graphics.Color
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.ViewManager
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import com.san.kir.manger.R
import com.san.kir.manger.eventBus.Binder
import com.san.kir.manger.extending.ankoExtend.visibleOrGone
import com.san.kir.manger.room.dao.ChapterFilter
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

class ListChapterView(private val adapterPresenter: ListChaptersRecyclerPresenter) :
    AnkoComponent<ListChaptersActivity> {
    private object Id {
        val progressBar = ID.generate()
        val bottomBar = ID.generate()
    }

    private var _filterState: Filter = FilterAsc
    private val filterIndicator = Binder(ChapterFilter.ALL_READ_ASC)
    private val sortIndicator = Binder(false) // Индикатор сортировки
    val isVisibleBottom = Binder(true) // Отображение бара снизу
    val isAction = Binder(false)
    val isUpdate = Binder(true)


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

        val actionBarSize = dip(50) // Размер бара снизу

        verticalLayout {
            // Корень
            relativeLayout {
                lparams(width = matchParent, height = matchParent)

                // ПрогрессБар для отображения поиска новых глав
                horizontalProgressBar {
                    id = Id.progressBar
                    isIndeterminate = true
                    visibleOrGone(isAction, isUpdate)
                }.lparams(width = matchParent, height = wrapContent) {
                    alignParentTop()
                }

                // Бар внизу экрана с кнопками сортировки и фильтрации
                linearLayout {
                    id = Id.bottomBar
                    backgroundColor = Color.parseColor("#ff212121")
                    visibleOrGone(isVisibleBottom)

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
                            onClick { filterIndicator.item = _filterState.allRead }
                            filterIndicator.bind {
                                backgroundResource =
                                        if (it == _filterState.allRead) R.drawable.ic_action_all_blue
                                        else R.drawable.ic_action_all_white
                            }
                        }

                        // Кнопка включения отображения только прочитанных глав
                        btn {
                            onClick { filterIndicator.item = _filterState.isRead }
                            filterIndicator.bind {
                                backgroundResource =
                                        if (it == _filterState.isRead) R.drawable.ic_action_read_blue
                                        else R.drawable.ic_action_read_white
                            }
                        }

                        // Кнопка включения отображения только не прочитанных глав
                        btn {
                            onClick { filterIndicator.item = _filterState.notRead }
                            filterIndicator.bind {
                                backgroundResource =
                                        if (it == _filterState.notRead) R.drawable.ic_action_not_read_blue
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
                    below(Id.progressBar) // Начинается от прогрессБара
                    above(Id.bottomBar) // Заканчивается на нижнем меню
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
                leftMargin = dip(12)
                rightMargin = dip(12)
            }
        }
    }

    private interface Filter {
        val allRead: ChapterFilter
        val isRead: ChapterFilter
        val notRead: ChapterFilter
        fun reverse(): Filter
        val isAsc: Boolean
    }

    private object FilterAsc : Filter {
        override val allRead = ChapterFilter.ALL_READ_ASC
        override val isRead = ChapterFilter.IS_READ_ASC
        override val notRead = ChapterFilter.NOT_READ_ASC
        override fun reverse() = FilterDesc
        override val isAsc = true
    }

    private object FilterDesc : Filter {
        override val allRead = ChapterFilter.ALL_READ_DESC
        override val isRead = ChapterFilter.IS_READ_DESC
        override val notRead = ChapterFilter.NOT_READ_DESC
        override fun reverse() = FilterAsc
        override val isAsc = false
    }
}
