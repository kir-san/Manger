package com.san.kir.manger.components.category

import android.view.Gravity
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.san.kir.manger.R
import com.san.kir.manger.extending.anko_extend.invisibleOrVisible
import com.san.kir.manger.extending.anko_extend.onClick
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
import org.jetbrains.anko.textView
import org.jetbrains.anko.wrapContent

class CategoryItemView(
    private val act: CategoryActivity,
    private val adapter: CategoryRecyclerPresenter
) :
    RecyclerViewAdapterFactory.AnkoView<Category>() {

    private lateinit var root: RelativeLayout
    private lateinit var name: TextView
    private lateinit var visibleBtn: ImageView
    private lateinit var deleteBtn: ImageView

    override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {
        val sizeBtn = dip(35)

        relativeLayout {
            lparams(width = matchParent, height = dip(60))

            name = textView {
                textSize = 18f
            }.lparams(width = matchParent, height = wrapContent) {
                centerVertically()
                gravity = Gravity.CENTER_VERTICAL
                leftMargin = dip(16)
            }

            // удаление
            deleteBtn = imageView {
                id = ID.generate()
                scaleType = ImageView.ScaleType.CENTER_CROP
                backgroundResource = R.drawable.ic_action_delete_black
            }.lparams(width = sizeBtn, height = sizeBtn) {
                alignParentRight()
                centerVertically()
                rightMargin = dip(8)
            }

            // переключение видимости
            visibleBtn = imageView {
                scaleType = ImageView.ScaleType.CENTER_CROP
            }.lparams(width = sizeBtn, height = sizeBtn) {
                centerVertically()
                rightMargin = dip(2)
                leftOf(deleteBtn)
                baselineOf(deleteBtn)
            }

            root = this
        }
    }

    override fun bind(item: Category, isSelected: Boolean, position: Int) {
        root.onClick {
            CategoryEditDialog(act, item) { cat ->
                act.mViewModel.categoryUpdateWithManga(cat, oldName)
                bind(item, isSelected, position)
            }
        }

        name.text = item.name

        visibleBtn.backgroundResource =
            if (item.isVisible) R.drawable.ic_visibility
            else R.drawable.ic_visibility_off
        visibleBtn.onClick {
            item.isVisible = !item.isVisible
            act.mViewModel.categoryUpdate(item)
            bind(item, isSelected, position)
        }

        deleteBtn.invisibleOrVisible(item.name == CATEGORY_ALL)
        if (item.name == CATEGORY_ALL) {
            deleteBtn.isClickable = false
        } else {
            deleteBtn.onClick {
                act.alert(R.string.category_item_question_delete) {
                    positiveButton(R.string.category_item_question_delete_yes) {
                        adapter.remove(item)
                    }
                    negativeButton(R.string.category_item_question_delete_no) { }
                }.show()
            }
        }
    }
}
