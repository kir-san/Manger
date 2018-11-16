package com.san.kir.manger.extending.dialogs

import android.support.v7.widget.PopupMenu
import android.view.View
import com.san.kir.manger.R
import com.san.kir.manger.components.downloadManager.DownloadManagerActivity
import com.san.kir.manger.extending.views.add
import com.san.kir.manger.room.models.DownloadStatus
import com.san.kir.manger.utils.ID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ClearDownloadsMenu(act: DownloadManagerActivity, parent: View) {
    init {
        PopupMenu(act, parent).apply {
            val clearCompleted = ID.generate()
            val clearPaused = ID.generate()
            val clearError = ID.generate()
            val clearAll = ID.generate()

            menu.add(clearCompleted, R.string.download_activity_option_submenu_clean_completed)
            menu.add(clearPaused, R.string.download_activity_option_submenu_clean_paused)
            menu.add(clearError, R.string.download_activity_option_submenu_clean_error)
            menu.add(clearAll, R.string.download_activity_option_submenu_clean_all)

            setOnMenuItemClickListener { item ->
                GlobalScope.launch(Dispatchers.Default) {
                    when (item.itemId) {
                        clearCompleted -> {
                            act.dao.getItems()
                                .filter { it.status == DownloadStatus.completed }
                                .forEach { act.dao.delete(it) }

                        }
                        clearPaused -> {
                            act.dao.getItems()
                                .filter { it.status == DownloadStatus.pause }
                                .forEach { act.dao.delete(it) }
                        }
                        clearError -> {
                            act.dao.getItems()
                                .filter { it.status == DownloadStatus.error }
                                .forEach { act.dao.delete(it) }
                        }
                        clearAll -> {
                            act.dao.getItems()
                                .filter {
                                    it.status == DownloadStatus.completed
                                            || it.status == DownloadStatus.pause
                                            || it.status == DownloadStatus.error
                                }.forEach { act.dao.delete(it) }
                        }
                    }
                }
                return@setOnMenuItemClickListener true
            }
            show()
        }
    }
}
