package com.san.kir.manger.components.list_chapters

import android.graphics.Color
import android.view.Gravity
import android.view.ViewManager
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import com.san.kir.ankofork.AnkoContext
import com.san.kir.ankofork.dip
import com.san.kir.ankofork.include
import com.san.kir.ankofork.matchParent
import com.san.kir.ankofork.sdk28.backgroundColor
import com.san.kir.ankofork.sdk28.backgroundResource
import com.san.kir.ankofork.sdk28.imageButton
import com.san.kir.ankofork.sdk28.linearLayout
import com.san.kir.ankofork.sdk28.onClick
import com.san.kir.ankofork.sdk28.space
import com.san.kir.ankofork.verticalLayout
import com.san.kir.manger.R
import com.san.kir.manger.utils.ActivityView
import com.san.kir.manger.utils.extensions.BaseActivity
import com.san.kir.manger.utils.extensions.visibleOrGone

class ListChapterView(private val act: ListChaptersActivity) : ActivityView() {

    override fun createView(ui: AnkoContext<BaseActivity>) = with(ui) {
        val actionBarSize = dip(50) // Размер бара снизу
        val vm = act.mViewModel

        verticalLayout {
            // Корень
            lparams(width = matchParent, height = matchParent)

            // Виджет списка глав (используется такой способ, так как по другому скроллБар не работает)
            include<androidx.recyclerview.widget.RecyclerView>(R.layout.recycler_view) {
                layoutManager = object : androidx.recyclerview.widget.LinearLayoutManager(ctx) {
                    override fun supportsPredictiveItemAnimations() = false
                }
                act.mAdapter.into(this)
            }.lparams(width = matchParent, height = matchParent) {
                weight = 1f
            }

            // Бар внизу экрана с кнопками сортировки и фильтрации
            linearLayout {
                backgroundColor = Color.parseColor("#ff212121")
                gravity = Gravity.CENTER_HORIZONTAL
                visibleOrGone(vm.isVisibleBottom)

                // Кнопка переключения порядка сортировки
                btn {
                    onClick { vm.filter.item = vm.filter.item.inverse() }
                    vm.filter.bind {
                        backgroundResource =
                            if (it.isAsc) R.drawable.ic_sort_21
                            else R.drawable.ic_sort_12
                    }
                }

                space { }.lparams(width = dip(64))

                // Кнопка включения отображения всех глав
                btn {
                    onClick { vm.filter.item = vm.filter.item.toAll() }
                    vm.filter.bind {
                        backgroundResource =
                            if (it.isAll) R.drawable.ic_action_all_blue
                            else R.drawable.ic_action_all_white
                    }
                }

                // Кнопка включения отображения только прочитанных глав
                btn {
                    onClick { vm.filter.item = vm.filter.item.toRead() }
                    vm.filter.bind {
                        backgroundResource =
                            if (it.isRead) R.drawable.ic_action_read_blue
                            else R.drawable.ic_action_read_white
                    }
                }

                // Кнопка включения отображения только не прочитанных глав
                btn {
                    onClick { vm.filter.item = vm.filter.item.toNot() }
                    vm.filter.bind {
                        backgroundResource =
                            if (it.isNot) R.drawable.ic_action_not_read_blue
                            else R.drawable.ic_action_not_read_white
                    }
                }

            }.lparams(width = matchParent, height = actionBarSize)
        }
    }

    private fun ViewManager.btn(action: ImageButton.() -> Unit): ImageButton {
        val backColor = Color.parseColor("#00ffffff") // Цвет заднего фона
        val buttonSize = 35 // Размер кнопок
        return imageButton {
            action()
            scaleType = ImageView.ScaleType.CENTER
            backgroundColor = backColor
            layoutParams = LinearLayout.LayoutParams(dip(buttonSize), dip(buttonSize)).apply {
                gravity = Gravity.CENTER_VERTICAL
                leftMargin = dip(8)
                rightMargin = dip(8)
            }
        }
    }
}
