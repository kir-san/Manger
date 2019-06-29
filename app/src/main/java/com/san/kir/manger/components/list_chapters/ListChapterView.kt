package com.san.kir.manger.components.list_chapters

import android.graphics.Color
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.ViewManager
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import com.san.kir.manger.R
import com.san.kir.manger.extending.BaseActivity
import com.san.kir.manger.extending.anko_extend.onClick
import com.san.kir.manger.extending.anko_extend.visibleOrGone
import com.san.kir.manger.utils.AnkoActivityComponent
import com.san.kir.manger.utils.ID
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.above
import org.jetbrains.anko.alignParentBottom
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.below
import org.jetbrains.anko.dip
import org.jetbrains.anko.imageButton
import org.jetbrains.anko.include
import org.jetbrains.anko.linearLayout
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.relativeLayout
import org.jetbrains.anko.space
import org.jetbrains.anko.verticalLayout

class ListChapterView(private val act: ListChaptersActivity) :
    AnkoActivityComponent() {
    private object Id {
        val progressBar = ID.generate()
        val bottomBar = ID.generate()
    }

    override fun createView(ui: AnkoContext<BaseActivity>) = with(ui) {
        val actionBarSize = dip(50) // Размер бара снизу
        val vm = act.mViewModel

        verticalLayout {
            // Корень
            relativeLayout {
                lparams(width = matchParent, height = matchParent)

                // Бар внизу экрана с кнопками сортировки и фильтрации
                linearLayout {
                    id = Id.bottomBar
                    backgroundColor = Color.parseColor("#ff212121")
                    gravity = Gravity.CENTER_HORIZONTAL
                    visibleOrGone(vm.isVisibleBottom)

                    // Кнопка переключения порядка сортировки
                    btn {
                        onClick { vm.toggleFilterInverse() }
                        vm.sortIndicator.bind {
                            backgroundResource =
                                if (it) R.drawable.ic_sort_21
                                else R.drawable.ic_sort_12
                        }
                    }

                    space { }.lparams(width = dip(34))

                    // Кнопка включения отображения всех глав
                    btn {
                        onClick { vm.filterIndicator.item = vm.filterStateHelp.allRead }
                        vm.filterIndicator.bind {
                            backgroundResource =
                                if (it == vm.filterStateHelp.allRead) R.drawable.ic_action_all_blue
                                else R.drawable.ic_action_all_white
                        }
                    }

                    // Кнопка включения отображения только прочитанных глав
                    btn {
                        onClick { vm.filterIndicator.item = vm.filterStateHelp.isRead }
                        vm.filterIndicator.bind {
                            backgroundResource =
                                if (it == vm.filterStateHelp.isRead) R.drawable.ic_action_read_blue
                                else R.drawable.ic_action_read_white
                        }
                    }

                    // Кнопка включения отображения только не прочитанных глав
                    btn {
                        onClick { vm.filterIndicator.item = vm.filterStateHelp.notRead }
                        vm.filterIndicator.bind {
                            backgroundResource =
                                if (it == vm.filterStateHelp.notRead) R.drawable.ic_action_not_read_blue
                                else R.drawable.ic_action_not_read_white
                        }
                    }

                }.lparams(width = matchParent, height = actionBarSize) { alignParentBottom() }

                // Виджет списка глав (используется такой способ, так как по другому скроллБар не работает)
                include<RecyclerView>(R.layout.recycler_view) {
                    layoutManager = object : LinearLayoutManager(ctx) {
                        override fun supportsPredictiveItemAnimations() = false
                    }
                    act.mAdapter.into(this)
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
}
