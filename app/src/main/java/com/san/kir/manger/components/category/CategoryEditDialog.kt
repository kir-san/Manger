package com.san.kir.manger.components.category

import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewManager
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.san.kir.ankofork.design.textInputEditText
import com.san.kir.ankofork.design.textInputLayout
import com.san.kir.ankofork.dialogs.alert
import com.san.kir.ankofork.dialogs.customView
import com.san.kir.ankofork.dialogs.toast
import com.san.kir.ankofork.dip
import com.san.kir.ankofork.leftPadding
import com.san.kir.ankofork.padding
import com.san.kir.ankofork.sdk28.checkBox
import com.san.kir.ankofork.sdk28.onClick
import com.san.kir.ankofork.sdk28.onSeekBarChangeListener
import com.san.kir.ankofork.sdk28.radioButton
import com.san.kir.ankofork.sdk28.radioGroup
import com.san.kir.ankofork.sdk28.seekBar
import com.san.kir.ankofork.sdk28.space
import com.san.kir.ankofork.sdk28.textChangedListener
import com.san.kir.ankofork.sdk28.textColor
import com.san.kir.ankofork.sdk28.textView
import com.san.kir.ankofork.support.nestedScrollView
import com.san.kir.ankofork.verticalLayout
import com.san.kir.manger.R
import com.san.kir.manger.room.entities.Category
import com.san.kir.manger.utils.CATEGORY_ALL
import com.san.kir.manger.utils.ID
import com.san.kir.manger.utils.SortLibraryUtil
import com.san.kir.manger.utils.extensions.typeText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CategoryEditDialog(
    private val act: CategoryActivity,
    private val cat: Category,
    action: CategoryEditDialog.(Category) -> Unit = {}
) {
    val oldName = cat.name
    private var validate = ""
    private val isAll = oldName == CATEGORY_ALL
    private lateinit var catList: List<String>

    private lateinit var validateView: TextView
    private lateinit var portraitSpanText: TextView
    private lateinit var landscapeSpanText: TextView

    init {
        act.lifecycleScope.launch(Dispatchers.Main) {

            catList = act.mViewModel.categoryNames()

            act.alert {
                titleResource =
                    if (oldName.isEmpty()) R.string.category_dialog_title_create
                    else R.string.category_dialog_title_edit

                customView { ui() }

                positiveButton(if (oldName.isEmpty()) R.string.category_dialog_create else R.string.category_dialog_edit) {
                    if (validate != "") {
                        if (oldName.isEmpty()) {
                            act.toast(R.string.category_dialog_not_create)
                            return@positiveButton
                        }
                        if (cat.name != oldName) {
                            cat.name = oldName
                            act.toast(R.string.category_dialog_save_old_name)
                        }
                    }
                    action(cat)
                }
                negativeButton(R.string.category_dialog_cancel) {}
            }.show()
        }
    }

    private fun ViewManager.ui() {
        nestedScrollView {
            verticalLayout {
                padding = dip(16)

                textInputView()

                validateView = textView {
                    textColor = Color.RED
                    visibility = if (isAll) View.INVISIBLE else View.VISIBLE
                }.lparams {
                    gravity = Gravity.END
                }

                sortChangeRadioGroup()

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

                portraitSpanText = textView {
                    text = act.getString(
                        R.string.category_dialog_span_text,
                        cat.spanPortrait
                    )
                    leftPadding = dip(17)
                }

                portraitSpanChange()

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

                landscapeSpanText = textView {
                    text = act.getString(
                        R.string.category_dialog_span_text,
                        cat.spanLandscape
                    )
                    leftPadding = dip(17)
                }

                landscapeSpanChange()
            }
        }
    }

    private fun ViewManager.textInputView() {
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
                            validate = when {
                                text.length < 3 -> context.getString(R.string.category_dialog_validate_length)
                                oldName == it.toString() -> context.getString(R.string.category_dialog_validate_equal)
                                catList.contains(it.toString()) -> context.getString(
                                    R.string.category_dialog_validate_contain
                                )
                                else -> ""
                            }
                            validateView.text = validate
                        }
                    }
                }
            }
        }
    }

    private fun ViewManager.sortChangeRadioGroup() {
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
    }

    private fun ViewManager.portraitSpanChange() {
        seekBar {
            progress = cat.spanPortrait
            max = 5

            onSeekBarChangeListener {
                onProgressChanged { _, i, _ ->
                    if (i < 1) {
                        progress = 1
                    } else {
                        cat.spanPortrait = i
                        portraitSpanText.text = act.getString(
                            R.string.category_dialog_span_text, i
                        )
                    }
                }
            }
        }
    }

    private fun ViewManager.landscapeSpanChange() {
        seekBar {
            progress = cat.spanLandscape
            max = 7

            onSeekBarChangeListener {
                onProgressChanged { _, i, _ ->
                    if (i < 1) {
                        progress = 1
                    } else {
                        cat.spanLandscape = i
                        landscapeSpanText.text = act.getString(
                            R.string.category_dialog_span_text,
                            i
                        )
                    }
                }
            }
        }
    }
}
