package com.san.kir.manger.extending.dialogs

import android.content.Context
import android.view.View
import android.widget.PopupMenu
import com.san.kir.manger.R
import com.san.kir.manger.components.library.LibraryActivity
import com.san.kir.manger.components.main.Main
import com.san.kir.manger.components.storage.StorageDialogFragment
import com.san.kir.manger.room.dao.removeWithChapters
import com.san.kir.manger.room.models.Manga
import org.jetbrains.anko.alert

class LibraryItemMenu(context: Context,
                      anchor: View?,
                      manga: Manga,
                      act: LibraryActivity,
                      position: Int) {
    private val mangaDao = Main.db.mangaDao

    init {
        PopupMenu(context, anchor).apply {
            val about = menu.add(R.string.library_popupmenu_about)
            val setCat = menu.add(R.string.library_popupmenu_set_category)
            val deleteRead = menu.add(R.string.library_popupmenu_delete_read_chapters)
            val storage = menu.add(R.string.library_popupmenu_storage)
            val delete = menu.add(R.string.library_popupmenu_delete)
            val select = menu.add(R.string.library_popupmenu_select)

            setOnMenuItemClickListener {
                when (it) {
                    about -> AboutMangaDialog(context, manga)
                    delete -> {
                        context.alert(R.string.library_popupmenu_delete_message,
                                      R.string.library_popupmenu_delete_title) {
                            positiveButton(R.string.library_popupmenu_delete_ok) {
                                mangaDao.removeWithChapters(manga)
                            }
                            neutralPressed(R.string.library_popupmenu_delete_no) {}
                            negativeButton(R.string.library_popupmenu_delete_ok_with_files) {
                                mangaDao.removeWithChapters(manga, true)
                            }
                        }.show()
                    }
                    setCat -> ChangeCategoryDialog(context, anchor, manga)
                    select -> act.onListItemSelect(position)
                    storage -> {
                        StorageDialogFragment().apply {
                            bind(manga, act)
                            show(act.supportFragmentManager, "storage")
                        }
                    }
                    deleteRead -> DeleteReadChaptersDialog(context, manga)
                    else -> return@setOnMenuItemClickListener false
                }
                return@setOnMenuItemClickListener true
            }

            show()
        }
    }
}
