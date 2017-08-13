package com.san.kir.manger.components.Category

import android.support.design.widget.Snackbar
import android.support.v4.view.MotionEventCompat
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import com.san.kir.manger.EventBus.BinderRx
import com.san.kir.manger.Extending.AnkoExtend.bind
import com.san.kir.manger.R
import com.san.kir.manger.dbflow.models.Category
import com.san.kir.manger.utils.ID
import com.san.kir.manger.utils.categoryAll
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.alert
import org.jetbrains.anko.alignParentBottom
import org.jetbrains.anko.alignParentRight
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.centerVertically
import org.jetbrains.anko.dip
import org.jetbrains.anko.editText
import org.jetbrains.anko.imageView
import org.jetbrains.anko.leftOf
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.relativeLayout
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.textView
import org.jetbrains.anko.wrapContent

class CategoryItemView(private val adapter: CategoryAdapter) : AnkoComponent<ViewGroup> {
    private object _id {
        val category_name = ID.generate()
        val edit = ID.generate()
        val category_name_edit = ID.generate()
        val okay = ID.generate()
        val handle = ID.generate()
        val visible = ID.generate()
        val delete = ID.generate()
    }

    var _category = Category()
    val isMain = BinderRx(true)
    val name = BinderRx("")
    val isEdit = BinderRx(false)
    val isVisible = BinderRx(false)
    var holder: RecyclerView.ViewHolder? = null

    fun bind(cat: Category,
             holder: CategoryViewHolder) {
        _category = cat
        isMain.item = _category.name == categoryAll
        name.item = _category.name
        isVisible.item = _category.isVisible
        this.holder = holder
    }

    fun createView(parent: ViewGroup): View {
        return createView(AnkoContext.create(parent.context, parent))
    }

    override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {
        val sizeBtn = dip(35)
        var edit: EditText

        relativeLayout {
            lparams(width = matchParent, height = dip(60))
            isFocusable = true

            // название
            textView {
                id = _id.category_name
                textSize = 18f

                bind(name) {
                    text = it
                }
                bind(isEdit) {
                    visibility = if (it) View.GONE else View.VISIBLE
                }
            }.lparams(width = matchParent, height = wrapContent) {
                centerVertically()
                gravity = Gravity.CENTER_VERTICAL
                leftMargin = dip(16)
                leftOf(_id.edit)
            }

            // редактор названия
            edit = editText {
                id = _id.category_name_edit
                inputType = EditorInfo.TYPE_CLASS_TEXT
                visibility = View.GONE

                bind(name) {
                    setText(it)
                }
                bind(isEdit) {
                    visibility = if (it) View.VISIBLE else View.GONE
                }
            }.lparams(width = matchParent, height = wrapContent) {
                alignParentBottom()
                leftMargin = dip(16)
                leftOf(_id.okay)
            }

            // Перемещение элемента
            imageView {
                id = _id.handle
                scaleType = ImageView.ScaleType.CENTER_CROP
                backgroundResource = R.drawable.ic_reorder_black
                setOnTouchListener { _, event ->
                    if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN)
                        adapter.mDragStartListener.onStartDrag(holder)

                    return@setOnTouchListener false
                }
            }.lparams(width = sizeBtn, height = sizeBtn) {
                alignParentRight()
                centerVertically()
                rightMargin = dip(8)
            }

            // переключение видимости
            imageView {
                id = _id.visible
                isClickable = true
                scaleType = ImageView.ScaleType.CENTER_CROP

                onClick {
                    isVisible.item = !isVisible.item
                    _category.isVisible = isVisible.item
                    _category.update()
//                    var index = 0
//                    adapter.mCategories.forEachIndexed { i, c -> if (c == _category) index = i }
//                    adapter.mCategories[index].isVisible = isVisible.item
                }

                bind(isVisible) {
                    backgroundResource =
                            if (it) R.drawable.ic_visibility
                            else R.drawable.ic_visibility_off
                }
            }.lparams(width = sizeBtn, height = sizeBtn) {
                centerVertically()
                rightMargin = dip(2)
                leftOf(_id.handle)
            }

            // удаление
            imageView {
                id = _id.delete
                isClickable = true
                scaleType = ImageView.ScaleType.CENTER_CROP
                backgroundResource = R.drawable.ic_action_delete_black

                onClick {
                    if (adapter.isLastItem())
                        Snackbar.make(view.rootView,
                                      "Должен остаться хотя бы один элемент",
                                      Snackbar.LENGTH_LONG).show()
                    else
                        this@imageView.context.alert(message = "Вы действительно хотите удалить?") {
                            positiveButton("Да") {
                                _category.delete()
                                val index = adapter.mCategories.indexOf(_category)
                                adapter.mCategories.remove(_category)
                                adapter.notifyItemRemoved(index)
                            }
                            negativeButton("Нет") { }
                        }.show()

                }

                bind(isMain) {
                    visibility = if (it) View.GONE else View.VISIBLE
                }
            }.lparams(width = sizeBtn, height = sizeBtn) {
                centerVertically()
                rightMargin = dip(2)
                leftOf(_id.visible)
            }

            // включение режима редактирования
            imageView {
                id = _id.edit
                isClickable = true
                scaleType = ImageView.ScaleType.CENTER_CROP
                backgroundResource = R.drawable.ic_action_edit_black
                visibility = View.GONE

                onClick { isEdit.item = !isEdit.item }
                bind(isMain) {
                    visibility =
                            if (it) View.GONE
                            else View.VISIBLE
                }
                bind(isEdit) {
                    visibility =
                            if (it) View.GONE
                            else View.VISIBLE
                }
            }.lparams(width = sizeBtn, height = sizeBtn) {
                centerVertically()
                rightMargin = dip(2)
                leftOf(_id.delete)
            }

            // сохранение нового названия
            imageView {
                id = _id.okay
                isClickable = true
                scaleType = ImageView.ScaleType.CENTER_CROP
                backgroundResource = R.drawable.ic_action_okay_green
                visibility = View.GONE

                onClick {
                    isEdit.item = !isEdit.item
                    name.item = edit.text.toString()
                    _category.name = edit.text.toString()
                    _category.update()
                }

                bind(isEdit) {
                    visibility = if (it) View.VISIBLE else View.GONE
                }
            }.lparams(width = sizeBtn, height = sizeBtn) {
                centerVertically()
                rightMargin = dip(2)
                leftOf(_id.delete)
            }
        }
    }
}
