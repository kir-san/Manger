package com.san.kir.manger.components.Category

import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.san.kir.manger.EventBus.Binder
import com.san.kir.manger.EventBus.toogle
import com.san.kir.manger.Extending.AnkoExtend.invisibleOrVisible
import com.san.kir.manger.Extending.AnkoExtend.textView
import com.san.kir.manger.R
import com.san.kir.manger.room.models.Category
import com.san.kir.manger.utils.CATEGORY_ALL
import com.san.kir.manger.utils.ID
import com.san.kir.manger.utils.RecyclerViewAdapterFactory
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.alert
import org.jetbrains.anko.alignParentRight
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.baselineOf
import org.jetbrains.anko.centerVertically
import org.jetbrains.anko.dip
import org.jetbrains.anko.imageView
import org.jetbrains.anko.leftOf
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.relativeLayout
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.wrapContent

class CategoryItemView(private val adapter: CategoryRecyclerPresenter)
    : RecyclerViewAdapterFactory.AnkoView<Category>() {
    private object _id {
        val delete = ID.generate()
    }

    private var _category = Category()
    private val name = Binder("")
    private val isVisible = Binder(false)

    override fun bind(item: Category, isSelected: Boolean, position: Int) {
        _category = item
        name.item = _category.name
        isVisible.item = _category.isVisible
    }

    override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {
        val sizeBtn = dip(35)

        relativeLayout {
            lparams(width = matchParent, height = dip(60))

            onClick { edit() }

            // название
            textView(name) {
                textSize = 18f
            }.lparams(width = matchParent, height = wrapContent) {
                centerVertically()
                gravity = Gravity.CENTER_VERTICAL
                leftMargin = dip(16)
            }

            // переключение видимости
            imageView {
                scaleType = ImageView.ScaleType.CENTER_CROP
                onClick {
                    _category.isVisible = isVisible.toogle()
                    adapter.update(_category)
                }
                isVisible.bind {
                    backgroundResource =
                            if (it) R.drawable.ic_visibility
                            else R.drawable.ic_visibility_off
                }
            }.lparams(width = sizeBtn, height = sizeBtn) {
                centerVertically()
                rightMargin = dip(2)
                leftOf(_id.delete)
                baselineOf(_id.delete)
            }

            // удаление
            imageView {
                id = _id.delete
                scaleType = ImageView.ScaleType.CENTER_CROP
                backgroundResource = R.drawable.ic_action_delete_black

                name.bind {
                    invisibleOrVisible(it == CATEGORY_ALL)
                    if (it != CATEGORY_ALL) {
                        isClickable = false
                    } else {
                        onClick {
                            this@imageView.context.alert(message = "Вы действительно хотите удалить?") {
                                positiveButton("Да") {
                                    adapter.remove(_category)
                                }
                                negativeButton("Нет") { }
                            }.show()
                        }
                    }
                }


            }.lparams(width = sizeBtn, height = sizeBtn) {
                alignParentRight()
                centerVertically()
                rightMargin = dip(8)
            }
        }
    }

    private fun View.edit() {
        CategoryEditDialog(context, _category) {
            name.item = _category.name
            adapter.update(_category, oldName)
        }
    }
}
