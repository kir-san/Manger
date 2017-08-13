package com.san.kir.manger.components.ListChapters

import android.graphics.Color
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import com.san.kir.manger.EventBus.Binder
import com.san.kir.manger.EventBus.BinderRx
import com.san.kir.manger.EventBus.toogle
import com.san.kir.manger.Extending.AnkoExtend.bind
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
import kotlin.properties.Delegates

class ListChapterView(private val act: ListChaptersActivity) : AnkoComponent<ListChaptersActivity> {

    private object _id {
        val progressBar = ID.generate()
        val bottomBar = ID.generate()
    }

    val viewAll = Binder(false) // Все главы
    val viewIsRead = Binder(false) // Только прочитанные
    val viewNotRead = Binder(false) // Только не прочитанные

    //    val filterIndicator = BinderRx(ListChaptersAdapter.ALL_READ) // Индикатор фильтрации
    var filterIndicator by Delegates.observable(ListChaptersAdapter.ALL_READ) { _, old, new ->
        if (old != new) {
            // переключение режима фильтрации
            viewAll.item = false
            viewIsRead.item = false
            viewNotRead.item = false
            when (new) {
                ListChaptersAdapter.ALL_READ -> { // отображение всех глав
                    adapter.item!!.changeOrder(filter = ListChaptersAdapter.ALL_READ)
                    viewAll.item = true
                }
                ListChaptersAdapter.IS_READ -> { // отображение только прочитанных
                    adapter.item!!.changeOrder(filter = ListChaptersAdapter.IS_READ)
                    viewIsRead.item = true
                }
                ListChaptersAdapter.NOT_READ -> { // отображение только не прочитанных
                    adapter.item!!.changeOrder(filter = ListChaptersAdapter.NOT_READ)
                    viewNotRead.item = true
                }
            }
        }
    } // Индикатор фильтрации


    val sortIndicator = BinderRx(false) // Индикатор сортировки

    val isVisibleBottom = Binder(true) // Отображение бара снизу
    val isVisibleProgress = Binder(false) // Отображения прогрессБара

    val adapter: Binder<ListChaptersAdapter?> = Binder(null) // Адаптер


    init {
        sortIndicator.bind(0) {
            // переключение порядка сортировки
            adapter.item?.changeOrder(isReversed = !it)
        }
    }

    override fun createView(ui: AnkoContext<ListChaptersActivity>) = with(ui) {

        // Нужные переменные
        val actionBarSize = dip(50) // Размер бара снизу
        val buttonSize = dip(38) // Размер кнопок
        val buttonMargin = dip(10) // Растояния от кнопок
        val backColor = Color.parseColor("#00ffffff") // Цвет заднего фона

        verticalLayout {
            // Корень
            relativeLayout {
                lparams(width = matchParent, height = matchParent)

                // ПрогрессБар для отображения поиска новых глав
                horizontalProgressBar {
                    lparams(width = matchParent, height = wrapContent) {
                        alignParentTop()
                    }
                    id = _id.progressBar

                    visibility = View.GONE // по умолчанию прогрессбар скрыт
                    isIndeterminate = true

                    bind(isVisibleProgress) {
                        // переключение видимости
                        visibility =
                                if (it) View.VISIBLE
                                else View.GONE
                    }
                }

                // Бар внизу экрана с кнопками сортировки и фильтрации
                linearLayout {
                    id = _id.bottomBar

                    backgroundColor = Color.parseColor("#ff212121")

                    bind(isVisibleBottom) {
                        visibility =
                                if (it) View.VISIBLE
                                else View.GONE
                    }

                    // Кнопка переключения порядка сортировки
                    imageButton {
                        scaleType = ImageView.ScaleType.CENTER_CROP
                        backgroundColor = backColor

                        onClick {
                            // переключение режима сортировки
                            sortIndicator.toogle()
                        }
                        bind(sortIndicator) {
                            // Переключение иконки сортировки
                            backgroundResource =
                                    if (!it) com.san.kir.manger.R.drawable.ic_sort_21
                                    else com.san.kir.manger.R.drawable.ic_sort_12
                        }
                    }.lparams(width = buttonSize, height = buttonSize) {
                        leftMargin = buttonMargin
                        gravity = Gravity.CENTER_VERTICAL
                    }

                    // Блок кнопок переключения сортировки
                    linearLayout {
                        lparams(width = matchParent, height = actionBarSize)

                        backgroundColor = Color.parseColor("#ff212121")
                        gravity = Gravity.CENTER_HORIZONTAL

                        // Кнопка включения отображения всех глав
                        imageButton {
                            scaleType = ImageView.ScaleType.CENTER
                            backgroundColor = backColor

                            onClick {
                                filterIndicator = ListChaptersAdapter.ALL_READ
                            }
                            bind(viewAll) {
                                // Переключение иконки
                                backgroundResource =
                                        if (it) com.san.kir.manger.R.drawable.ic_action_all_blue
                                        else com.san.kir.manger.R.drawable.ic_action_all_white
                            }
                        }.lparams(width = buttonSize, height = buttonSize) {
                            gravity = Gravity.CENTER_VERTICAL
                            leftMargin = buttonMargin
                            rightMargin = buttonMargin
                        }

                        // Кнопка включения отображения только прочитанных глав
                        imageButton {
                            scaleType = ImageView.ScaleType.CENTER
                            backgroundColor = backColor

                            onClick {
                                filterIndicator = ListChaptersAdapter.IS_READ
                            }
                            bind(viewIsRead) {
                                // переключение иконки
                                backgroundResource =
                                        if (it) com.san.kir.manger.R.drawable.ic_action_read_blue
                                        else com.san.kir.manger.R.drawable.ic_action_read_white
                            }
                        }.lparams(width = buttonSize, height = buttonSize) {
                            gravity = Gravity.CENTER_VERTICAL
                            leftMargin = buttonMargin
                            rightMargin = buttonMargin
                        }

                        // Кнопка включения отображения только не прочитанных глав
                        imageButton {
                            scaleType = ImageView.ScaleType.CENTER
                            backgroundColor = backColor

                            onClick {
                                filterIndicator = ListChaptersAdapter.NOT_READ
                            }
                            bind(viewNotRead) {
                                // переключение иконки
                                backgroundResource =
                                        if (it) com.san.kir.manger.R.drawable.ic_action_not_read_blue
                                        else com.san.kir.manger.R.drawable.ic_action_not_read_white
                            }
                        }.lparams(width = buttonSize, height = buttonSize) {
                            gravity = Gravity.CENTER_VERTICAL
                            leftMargin = buttonMargin
                            rightMargin = buttonMargin
                        }
                    }
                }.lparams(width = matchParent, height = actionBarSize) { alignParentBottom() }

                // Виджет списка глав (используется такой способ, так как по другому скроллБар не работает)
                include<RecyclerView>(com.san.kir.manger.R.layout.recycler_view) {
                    lparams(width = matchParent, height = matchParent) {
                        below(_id.progressBar) // Начинается от прогрессБара
                        above(_id.bottomBar) // Заканчивается на нижнем меню
                    }
                    layoutManager = LinearLayoutManager(this@with.ctx)

                    bind(this@ListChapterView.adapter) {
                        adapter = it
                    }

                }
            }

        }
    }
}
