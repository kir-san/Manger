package com.san.kir.manger.components.Storage

import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import com.san.kir.manger.EventBus.Binder
import com.san.kir.manger.Extending.AnkoExtend.bind
import com.san.kir.manger.R
import com.san.kir.manger.utils.ID
import com.san.kir.manger.utils.PopupMenu
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.alignParentLeft
import org.jetbrains.anko.alignParentRight
import org.jetbrains.anko.baselineOf
import org.jetbrains.anko.below
import org.jetbrains.anko.centerVertically
import org.jetbrains.anko.dip
import org.jetbrains.anko.leftOf
import org.jetbrains.anko.lines
import org.jetbrains.anko.margin
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.padding
import org.jetbrains.anko.relativeLayout
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.textColor
import org.jetbrains.anko.textView
import org.jetbrains.anko.wrapContent

class StorageChildView : AnkoComponent<ViewGroup> {
    private object _id {
        val new = ID.generate()
        val name = ID.generate()
        val size = ID.generate()
        val path = ID.generate()
    }

    val item: Binder<StorageItem?> = Binder(null)

    fun createView(parent: ViewGroup): View {
        return createView(AnkoContext.create(parent.context, parent))
    }

    override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {
        relativeLayout {
            lparams(width = matchParent, height = wrapContent) {
                margin = dip(1)
            }
            isClickable = true
            padding = dip(3)

            onClick { item.item?.let { PopupMenu.storage(this@relativeLayout, it) } }

            // Название
            textView {
                id = _id.name
                lines = 1
                setTextAppearance(android.R.attr.textAppearanceLarge)

                bind(item) { text = it?.name ?: "" }
            }.lparams(width = wrapContent, height = wrapContent) {
                alignParentLeft()
                leftOf(_id.new)
            }


            // Пометка Новая ли манга
            textView {
                id = _id.new
                lines = 1
                textColor = Color.parseColor("#ffcc0000")

                bind(item) {
                    if (it?.isNew ?: false)
                        setText(R.string.storage_list_item_new)

                }
            }.lparams(width = wrapContent, height = wrapContent) {
                baselineOf(_id.name)
                alignParentRight()
                margin = dip(3)
            }


            // Место хранения
            textView {
                id = _id.path
                lines = 1
                setTextAppearance(android.R.attr.textAppearanceSmall)

                bind(item) { text = it?.path ?: "" }
            }.lparams(width = wrapContent, height = wrapContent) {
                alignParentLeft()
                below(_id.name)
                leftOf(_id.size)
            }


            // Размер манги в памяти
            textView {
                id = _id.size
                lines = 1
                setTextAppearance(android.R.attr.textAppearanceSmall)

                bind(item) {
                    text = context.getString(R.string.storage_list_item_size, it?.size ?: 0)
                }
            }.lparams(width = wrapContent, height = wrapContent) {
                baselineOf(_id.path)
                alignParentRight()
                centerVertically()
                rightMargin = dip(3)
            }

        }
    }

    fun bind(item: StorageItem) {
        this.item.item = item
    }
}
