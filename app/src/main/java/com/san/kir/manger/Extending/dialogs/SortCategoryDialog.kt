package com.san.kir.manger.Extending.dialogs

import android.content.Context
import com.san.kir.manger.R
import com.san.kir.manger.components.Main.Main
import com.san.kir.manger.room.DAO.update
import com.san.kir.manger.room.models.Category
import com.san.kir.manger.utils.ID
import com.san.kir.manger.utils.SortLibraryUtil
import org.jetbrains.anko.alert
import org.jetbrains.anko.checkBox
import org.jetbrains.anko.customView
import org.jetbrains.anko.dip
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.padding
import org.jetbrains.anko.radioButton
import org.jetbrains.anko.radioGroup
import org.jetbrains.anko.sdk25.coroutines.onCheckedChange
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.verticalLayout
import org.jetbrains.anko.wrapContent

class SortCategoryDialog(context: Context, category: Category) {
    init {
        context. alert {
            titleResource = R.string.library_menu_order_title

            customView {
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
                    }

                    checkBox {
                        setText(R.string.library_sort_dialog_reverse)
                        isChecked = category.isReverseSort
                        onCheckedChange { _, b -> category.isReverseSort = b }
                    }
                }
            }

            positiveButton("Изменить") {
                Main.db.categoryDao.update(category)
            }
            negativeButton("Я передумал") {}
        }.show()
    }
}
