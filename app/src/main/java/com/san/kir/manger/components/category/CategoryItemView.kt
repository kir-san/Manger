package com.san.kir.manger.components.category

import android.view.Gravity
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.san.kir.ankofork.AnkoContext
import com.san.kir.ankofork.dialogs.alert
import com.san.kir.ankofork.dip
import com.san.kir.ankofork.matchParent
import com.san.kir.ankofork.sdk28.backgroundResource
import com.san.kir.ankofork.sdk28.imageView
import com.san.kir.ankofork.sdk28.linearLayout
import com.san.kir.ankofork.sdk28.onClick
import com.san.kir.ankofork.sdk28.textView
import com.san.kir.ankofork.wrapContent
import com.san.kir.manger.R
import com.san.kir.manger.room.entities.Category
import com.san.kir.manger.utils.CATEGORY_ALL
import com.san.kir.manger.utils.ID
import com.san.kir.manger.utils.RecyclerViewAdapterFactory
import com.san.kir.manger.utils.extensions.invisibleOrVisible
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CategoryItemView(
    private val act: CategoryActivity,
    private val adapter: CategoryRecyclerPresenter
) :
    RecyclerViewAdapterFactory.AnkoView<Category>() {

    private lateinit var root: LinearLayout
    private lateinit var name: TextView
    private lateinit var visibleBtn: ImageView
    private lateinit var deleteBtn: ImageView

    override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {
        val sizeBtn = dip(35)

        linearLayout {
            lparams(width = matchParent, height = dip(56))

            name = textView {
                textSize = 16f
            }.lparams(width = matchParent, height = wrapContent) {
                gravity = Gravity.CENTER_VERTICAL
                leftMargin = dip(16)
                weight = 1f
            }

            // переключение видимости
            visibleBtn = imageView {
            }.lparams(width = sizeBtn, height = sizeBtn) {
                gravity = Gravity.CENTER_VERTICAL
                leftMargin = dip(16)
                rightMargin = dip(16)
            }

            // удаление
            deleteBtn = imageView {
                id = ID.generate()
                backgroundResource = R.drawable.ic_action_delete_t
            }.lparams(width = sizeBtn, height = sizeBtn) {
                gravity = Gravity.CENTER_VERTICAL
                rightMargin = dip(16)
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

        toggleVisible(item.isVisible)

        visibleBtn.backgroundResource =
            if (item.isVisible) R.drawable.ic_visibility
            else R.drawable.ic_visibility_off
        visibleBtn.onClick {
            item.isVisible = !item.isVisible
            toggleVisible(item.isVisible)
            act.lifecycleScope.launch(Dispatchers.Default) {
                act.mViewModel.update(item)
            }
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

    private fun toggleVisible(isOn: Boolean) {
        visibleBtn.backgroundResource =
            if (isOn) R.drawable.ic_visibility
            else R.drawable.ic_visibility_off
    }
}
