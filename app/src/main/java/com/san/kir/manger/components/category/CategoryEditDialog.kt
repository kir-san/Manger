package com.san.kir.manger.components.category

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View
import com.san.kir.manger.R
import com.san.kir.manger.components.main.Main
import com.san.kir.manger.eventBus.Binder
import com.san.kir.manger.extending.ankoExtend.onClick
import com.san.kir.manger.extending.ankoExtend.onSeekBarChangeListener
import com.san.kir.manger.extending.ankoExtend.textChangedListener
import com.san.kir.manger.extending.ankoExtend.textView
import com.san.kir.manger.extending.ankoExtend.typeText
import com.san.kir.manger.room.models.Category
import com.san.kir.manger.utils.CATEGORY_ALL
import com.san.kir.manger.utils.ID
import com.san.kir.manger.utils.SortLibraryUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.alert
import org.jetbrains.anko.checkBox
import org.jetbrains.anko.customView
import org.jetbrains.anko.design.textInputEditText
import org.jetbrains.anko.design.textInputLayout
import org.jetbrains.anko.dip
import org.jetbrains.anko.leftPadding
import org.jetbrains.anko.padding
import org.jetbrains.anko.radioButton
import org.jetbrains.anko.radioGroup
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

    init {
        GlobalScope.launch(Dispatchers.Main) {
            val catList =
                withContext(Dispatchers.Default) { categoryDao.getItems().map { it.name } }

            val isAll = oldName == CATEGORY_ALL
            val validate = Binder("")

            context.alert {
                titleResource =
                        if (oldName.isEmpty()) R.string.category_dialog_title_create
                        else R.string.category_dialog_title_edit
                customView {
                    nestedScrollView {
                        verticalLayout {
                            padding = dip(16)

                            textInputLayout {
                                textInputEditText {
                                    setText(cat.name)
                                    isEnabled = !isAll
                                    typeText()
                                    setHint(R.string.category_dialog_hint)
                                    textChangedListener {
                                        onTextChanged { text, _, _, _ ->
                                            text?.let {
                                                cat.name = it.toString()
                                                validate.item = when {
                                                    text.length < 3 -> context.getString(R.string.category_dialog_validate_length)
                                                    oldName == it.toString() -> context.getString(R.string.category_dialog_validate_equal)
                                                    catList.contains(it.toString()) -> context.getString(
                                                        R.string.category_dialog_validate_contain
                                                    )
                                                    else -> ""
                                                }
                                            }
                                        }
                                    }
                                }
                            }

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
                            }

                            space().lparams(height = dip(10))

                            checkBox {
                                setText(R.string.library_sort_dialog_reverse)
                                isChecked = cat.isReverseSort
                                setOnCheckedChangeListener { _, b -> cat.isReverseSort = b }
                            }

                            space().lparams(height = dip(10))

                            checkBox {
                                setText(R.string.library_sort_dialog_visible)
                                isChecked = !cat.isVisible
                                setOnCheckedChangeListener { _, b -> cat.isVisible = !b }
                            }

                            space().lparams(height = dip(10))

                            textView(R.string.category_dialog_portrait)

                            space().lparams(height = dip(5))

                            checkBox {
                                isChecked = cat.isLargePortrait

                                setText(
                                    if (isChecked) R.string.category_dialog_large_cells
                                    else R.string.category_dialog_small_cells
                                )
                                setOnCheckedChangeListener { _, b ->
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

                            textView(R.string.category_dialog_landscape)

                            space().lparams(height = dip(5))

                            checkBox {
                                isChecked = cat.isLargeLandscape
                                setText(
                                    if (isChecked) R.string.category_dialog_large_cells
                                    else R.string.category_dialog_small_cells
                                )
                                setOnCheckedChangeListener { _, b ->
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
                positiveButton(if (oldName.isEmpty()) R.string.category_dialog_create else R.string.category_dialog_edit) {
                    if (validate.item != "") {
                        if (oldName.isEmpty()) {
                            context.toast(R.string.category_dialog_not_create)
                            return@positiveButton
                        }
                        if (cat.name != oldName) {
                            cat.name = oldName
                            context.toast(R.string.category_dialog_save_old_name)
                        }
                    }
                    action(cat)
                }
                negativeButton(R.string.category_dialog_cancel) {}
            }.show()
        }
    }
}
