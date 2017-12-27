package com.san.kir.manger.components.Category

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View
import com.san.kir.manger.EventBus.Binder
import com.san.kir.manger.Extending.AnkoExtend.textView
import com.san.kir.manger.Extending.AnkoExtend.typeText
import com.san.kir.manger.R
import com.san.kir.manger.components.Main.Main
import com.san.kir.manger.room.models.Category
import com.san.kir.manger.utils.CATEGORY_ALL
import com.san.kir.manger.utils.ID
import com.san.kir.manger.utils.SortLibraryUtil
import org.jetbrains.anko.alert
import org.jetbrains.anko.checkBox
import org.jetbrains.anko.customView
import org.jetbrains.anko.dip
import org.jetbrains.anko.editText
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.padding
import org.jetbrains.anko.radioButton
import org.jetbrains.anko.radioGroup
import org.jetbrains.anko.sdk25.coroutines.onCheckedChange
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.sdk25.coroutines.textChangedListener
import org.jetbrains.anko.textColor
import org.jetbrains.anko.toast
import org.jetbrains.anko.topPadding
import org.jetbrains.anko.verticalLayout

class CategoryEditDialog(context: Context,
                         cat: Category,
                         action: CategoryEditDialog.(Category) -> Unit = {}) {
    private object _id  {
        val group = ID.generate()
        val add = ID.generate()
        val abc = ID.generate()
    }

    val oldName = cat.name
    private val catList = Main.db.categoryDao.loadCategories().map { it.name }
    private val validate = Binder("")
    private val isAll = oldName == CATEGORY_ALL

    init {
        context.alert {
            title = if (oldName.isEmpty()) "Создание категории" else "Настройка категории"
            customView {
                verticalLayout {
                    padding = dip(16)

                    editText(cat.name) {
                        isEnabled = !isAll
                        typeText()
                        hint = "Введите название"
                        textChangedListener {
                            onTextChanged { text, _, _, _ ->
                                text?.let {
                                    cat.name = it.toString()
                                    validate.item = when {
                                        text.length < 3 -> "Слишком коротко"
                                        oldName == it.toString() -> "Старое имя"
                                        catList.contains(it.toString()) -> "Название занято"
                                        else -> ""
                                    }
                                }
                            }
                        }
                    }.lparams(width = matchParent)

                    textView(validate) {
                        textColor = Color.RED
                        visibility =
                                if (isAll) View.INVISIBLE
                                else View.VISIBLE
                    }.lparams {
                        gravity = Gravity.END
                    }

                    radioGroup {
                        id = _id.group
                        topPadding = dip(10)
                        radioButton {
                            id = _id.add
                            setText(R.string.library_sort_dialog_add)
                            isChecked = cat.typeSort == SortLibraryUtil.add
                            onClick { cat.typeSort = SortLibraryUtil.add }
                        }

                        radioButton {
                            id = _id.abc
                            setText(R.string.library_sort_dialog_abc)
                            isChecked = cat.typeSort == SortLibraryUtil.abc
                            onClick { cat.typeSort = SortLibraryUtil.abc }
                        }
                    }

                    checkBox {
                        topPadding = dip(10)
                        setText(R.string.library_sort_dialog_reverse)
                        isChecked = cat.isReverseSort

                        onCheckedChange { _, b -> cat.isReverseSort = b }
                    }

                    checkBox {
                        topPadding = dip(10)
                        setText(R.string.library_sort_dialog_visible)
                        isChecked = !cat.isVisible

                        onCheckedChange { _, b -> cat.isVisible = !b }
                    }
                }
            }
            positiveButton(if (oldName.isEmpty()) "Создать" else "Изменить") {
                if (validate.item != "") {
                    if (oldName.isEmpty()) {
                        context.toast("Категория не была создана")
                        return@positiveButton
                    }

                    if (cat.name != oldName) {
                        cat.name = oldName
                        context.toast("Имя не прошло проверку, сохранено старое")
                    }
                }

                if (!isAll) {
                    action(cat)
                }

            }
            negativeButton("Я передумал") {}
        }.show()

    }
}
