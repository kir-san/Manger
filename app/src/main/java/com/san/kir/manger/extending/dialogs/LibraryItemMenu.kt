package com.san.kir.manger.extending.dialogs

import android.view.View
import android.widget.PopupMenu
import com.san.kir.manger.R
import com.san.kir.manger.components.library.LibraryActivity
import com.san.kir.manger.components.storage.StorageDialogView
import com.san.kir.manger.room.models.Manga
import com.san.kir.manger.utils.log
import org.jetbrains.anko.alert

class LibraryItemMenu(
    act: LibraryActivity,
    anchor: View?,
    manga: Manga
) {
    init {
        PopupMenu(act, anchor).apply {
            val about = menu.add(R.string.library_popupmenu_about)
            val setCat = menu.add(R.string.library_popupmenu_set_category)
            val storage = menu.add(R.string.library_popupmenu_storage)
            val delete = menu.add(R.string.library_popupmenu_delete)

            setOnMenuItemClickListener { menuItem ->
                when (menuItem) {
                    about -> AboutMangaDialog(act, manga)
                    delete -> {
                        act.alert(
                            R.string.library_popupmenu_delete_message,
                            R.string.library_popupmenu_delete_title
                        ) {
                            positiveButton(R.string.library_popupmenu_delete_ok) {
                                act.mViewModel.removeWithChapters(manga)
                            }
                            neutralPressed(R.string.library_popupmenu_delete_no) {
                                log("")
                            }
                            negativeButton(R.string.library_popupmenu_delete_ok_with_files) {
                                act.mViewModel.removeWithChapters(manga, true)
                            }
                        }.show()
                    }
                    setCat -> ChangeCategoryDialog(act, anchor, manga)
                    storage -> {
                        StorageDialogView(act).bind(manga)
                    }
                    else -> return@setOnMenuItemClickListener false
                }
                return@setOnMenuItemClickListener true
            }

            show()
        }
    }
}
