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
import org.jetbrains.anko.leftPadding
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.padding
import org.jetbrains.anko.radioButton
import org.jetbrains.anko.radioGroup
import org.jetbrains.anko.sdk25.coroutines.onCheckedChange
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.sdk25.coroutines.onSeekBarChangeListener
import org.jetbrains.anko.sdk25.coroutines.textChangedListener
import org.jetbrains.anko.seekBar
import org.jetbrains.anko.space
import org.jetbrains.anko.support.v4.nestedScrollView
import org.jetbrains.anko.textColor
import org.jetbrains.anko.textView
import org.jetbrains.anko.toast
import org.jetbrains.anko.verticalLayout

class CategoryEditDialog(
    context: Context,
    cat: Category,
    action: CategoryEditDialog.(Category) -> Unit = {}
) {
    val oldName = cat.name

    private val categoryDao = Main.db.categoryDao
    private val catList = categoryDao.loadCategories().map { it.name }
    private val validate = Binder("")
    private val isAll = oldName == CATEGORY_ALL

    init {
        context.alert {
            title = if (oldName.isEmpty()) "Создание категории" else "Настройка категории"
            customView {
                nestedScrollView {
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
                            id = ID.generate()
                            radioButton {
                                id = ID.generate()
                                setText(R.string.library_sort_dialog_add)
                                isChecked = cat.typeSort == SortLibraryUtil.add
                                onClick { cat.typeSort = SortLibraryUtil.add }
                            }

                            radioButton {
                                id = ID.generate()
                                setText(R.string.library_sort_dialog_abc)
                                isChecked = cat.typeSort == SortLibraryUtil.abc
                                onClick { cat.typeSort = SortLibraryUtil.abc }
                            }

                            radioButton {
                                id = ID.generate()
                                setText(R.string.library_sort_dialog_pop)
                                isChecked = cat.typeSort == SortLibraryUtil.pop
                                onClick { cat.typeSort = SortLibraryUtil.pop }
                            }
                            radioButton {
                                id = ID.generate()
                                setText(R.string.library_sort_dialog_man)
                                isChecked = cat.typeSort == SortLibraryUtil.man
                                onClick { cat.typeSort = SortLibraryUtil.man }
                            }
                        }

                        space().lparams(height = dip(10))

                        checkBox {
                            setText(R.string.library_sort_dialog_reverse)
                            isChecked = cat.isReverseSort
                            onCheckedChange { _, b -> cat.isReverseSort = b }
                        }

                        space().lparams(height = dip(10))

                        checkBox {
                            setText(R.string.library_sort_dialog_visible)
                            isChecked = !cat.isVisible
                            onCheckedChange { _, b -> cat.isVisible = !b }
                        }

                        space().lparams(height = dip(10))

                        textView("Портретная ориентация")

                        space().lparams(height = dip(5))

                        checkBox {
                            isChecked = cat.isLargePortrait

                            setText(
                                if (isChecked) R.string.category_dialog_large_cells
                                else R.string.category_dialog_small_cells
                            )
                            onCheckedChange { _, b ->
                                cat.isLargePortrait = b
                                setText(
                                    if (b) R.string.category_dialog_large_cells
                                    else R.string.category_dialog_small_cells
                                )
                            }
                        }.lparams { leftMargin = dip(13) }

                        space().lparams(height = dip(5))

                        val spanText = textView {
                            text = ctx.getString(
                                R.string.category_dialog_span_text,
                                cat.spanPortrait
                            )
                            leftPadding = dip(17)
                        }

                        seekBar {
                            progress = cat.spanPortrait
                            max = 5

                            onSeekBarChangeListener {
                                onProgressChanged { _, i, _ ->
                                    if (i < 1) {
                                        progress = 1
                                    } else {
                                        cat.spanPortrait = i
                                        spanText.text = ctx.getString(
                                            R.string.category_dialog_span_text,
                                            i
                                        )
                                    }
                                }
                            }
                        }

                        space().lparams(height = dip(10))

                        textView("Ландшафтная ориентация")

                        space().lparams(height = dip(5))

                        checkBox {
                            isChecked = cat.isLargeLandscape
                            setText(
                                if (isChecked) R.string.category_dialog_large_cells
                                else R.string.category_dialog_small_cells
                            )
                            onCheckedChange { _, b ->
                                cat.isLargeLandscape = b
                                setText(
                                    if (b) R.string.category_dialog_large_cells
                                    else R.string.category_dialog_small_cells
                                )
                            }
                        }.lparams { leftMargin = dip(13) }

                        space().lparams(height = dip(5))

                        val spanText2 = textView {
                            text = ctx.getString(
                                R.string.category_dialog_span_text,
                                cat.spanLandscape
                            )
                            leftPadding = dip(17)
                        }

                        seekBar {
                            progress = cat.spanLandscape
                            max = 7

                            onSeekBarChangeListener {
                                onProgressChanged { _, i, _ ->
                                    if (i < 1) {
                                        progress = 1
                                    } else {
                                        cat.spanLandscape = i
                                        spanText2.text = ctx.getString(
                                            R.string.category_dialog_span_text,
                                            i
                                        )
                                    }
                                }
                            }
                        }
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

                action(cat)

            }
            negativeButton("Я передумал") {}
        }.show()

    }
}
