package com.san.kir.manger.extending.dialogs

import android.content.Context
import android.content.res.Configuration
import com.san.kir.manger.R
import com.san.kir.manger.components.main.Main
import com.san.kir.manger.extending.ankoExtend.onCheckedChange
import com.san.kir.manger.extending.ankoExtend.onClick
import com.san.kir.manger.extending.ankoExtend.onSeekBarChangeListener
import com.san.kir.manger.room.dao.updateAsync
import com.san.kir.manger.room.models.Category
import com.san.kir.manger.utils.ID
import com.san.kir.manger.utils.SortLibraryUtil
import org.jetbrains.anko.alert
import org.jetbrains.anko.checkBox
import org.jetbrains.anko.customView
import org.jetbrains.anko.dip
import org.jetbrains.anko.leftPadding
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.padding
import org.jetbrains.anko.radioButton
import org.jetbrains.anko.radioGroup
import org.jetbrains.anko.seekBar
import org.jetbrains.anko.space
import org.jetbrains.anko.support.v4.nestedScrollView
import org.jetbrains.anko.textView
import org.jetbrains.anko.verticalLayout
import org.jetbrains.anko.wrapContent

class SortCategoryDialog(context: Context, category: Category) {
    init {
        val portrait =
            context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT

        context.alert {
            titleResource = R.string.library_menu_order_title

            customView {
                nestedScrollView {
                    verticalLayout {
                        lparams(width = matchParent, height = wrapContent) {
                            padding = dip(16)
                        }

                        radioGroup {
                            id = ID.generate()
                            radioButton {
                                id = ID.generate()
                                setText(R.string.library_sort_dialog_add)
                                isChecked = category.typeSort == SortLibraryUtil.add
                                onClick { category.typeSort = SortLibraryUtil.add }
                            }

                            radioButton {
                                id = ID.generate()
                                setText(R.string.library_sort_dialog_abc)
                                isChecked = category.typeSort == SortLibraryUtil.abc
                                onClick { category.typeSort = SortLibraryUtil.abc }
                            }

                            radioButton {
                                id = ID.generate()
                                setText(R.string.library_sort_dialog_pop)
                                isChecked = category.typeSort == SortLibraryUtil.pop
                                onClick { category.typeSort = SortLibraryUtil.pop }
                            }
                        }

                        space().lparams(height = dip(10))

                        checkBox {
                            setText(R.string.library_sort_dialog_reverse)
                            isChecked = category.isReverseSort
                            onCheckedChange { _, b -> category.isReverseSort = b }
                        }

                        space().lparams(height = dip(10))

                        checkBox {
                            isChecked =
                                    if (portrait) category.isLargePortrait
                                    else category.isLargeLandscape

                            setText(
                                if (isChecked) R.string.category_dialog_large_cells
                                else R.string.category_dialog_small_cells
                            )
                            onCheckedChange { _, b ->
                                when (portrait) {
                                    true -> category.isLargePortrait = b
                                    false -> category.isLargeLandscape = b
                                }
                                setText(
                                    if (b) R.string.category_dialog_large_cells
                                    else R.string.category_dialog_small_cells
                                )
                            }
                        }

                        space().lparams(height = dip(10))

                        val spanText = textView {
                            text = ctx.getString(
                                R.string.category_dialog_span_text,
                                if (portrait) category.spanPortrait
                                else category.spanLandscape
                            )
                            leftPadding = dip(17)
                        }

                        seekBar {
                            progress = if (portrait) category.spanPortrait
                            else category.spanLandscape

                            max = if (portrait) 5 else 7
                            onSeekBarChangeListener {
                                onProgressChanged { _, i, _ ->
                                    if (i < 1) {
                                        progress = 1
                                    } else {
                                        if (portrait) category.spanPortrait = i
                                        else category.spanLandscape = i
                                        spanText.text = ctx.getString(
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

            positiveButton(R.string.category_dialog_positive) {
                Main.db.categoryDao.updateAsync(category)
            }
            negativeButton(R.string.category_dialog_negative) {}
        }.show()
    }
}
