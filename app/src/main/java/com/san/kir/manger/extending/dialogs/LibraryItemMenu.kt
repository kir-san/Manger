package com.san.kir.manger.extending.dialogs

import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import com.san.kir.ankofork.dialogs.alert
import com.san.kir.manger.R
import com.san.kir.manger.components.library.LibraryActivity
import com.san.kir.manger.components.storage.StorageDialogView
import com.san.kir.manger.room.entities.Manga
import com.san.kir.manger.utils.extensions.log
import com.san.kir.manger.workmanager.MangaDeleteWorker

class LibraryItemMenu(act: LibraryActivity, anchor: View?, manga: Manga) {

    private val about: MenuItem
    private val setCat: MenuItem
    private val storage: MenuItem
    private val delete: MenuItem

    init {
        PopupMenu(act, anchor).apply {
            about = menu.add(R.string.library_popupmenu_about)
            setCat = menu.add(R.string.library_popupmenu_set_category)
            storage = menu.add(R.string.library_popupmenu_storage)
            delete = menu.add(R.string.library_popupmenu_delete)

            setOnMenuItemClickListener(Listener(act, manga, anchor))

            show()
        }
    }

    inner class Listener(
        private val act: LibraryActivity,
        private val manga: Manga,
        private val view: View?
    ) :
        PopupMenu.OnMenuItemClickListener {

        override fun onMenuItemClick(menuItem: MenuItem): Boolean {

            when (menuItem) {
                about -> AboutMangaDialog(act, manga)
                delete -> deleteDialog(act, manga)
                setCat -> ChangeCategoryDialog(act, view, manga)
                storage -> StorageDialogView(act).bind(manga)
                else -> return false
            }

            return true
        }

        private fun deleteDialog(act: LibraryActivity, manga: Manga) {
            act.alert(
                R.string.library_popupmenu_delete_message, R.string.library_popupmenu_delete_title
            ) {
                neutralPressed(R.string.library_popupmenu_delete_no) { log("") }
                positiveButton(R.string.library_popupmenu_delete_ok) {
                    MangaDeleteWorker.addTask(act, manga)
                }
                negativeButton(R.string.library_popupmenu_delete_ok_with_files) {
                    MangaDeleteWorker.addTask(act, manga, true)
                }
            }.show()
        }
    }
}
