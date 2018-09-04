package com.san.kir.manger.extending.dialogs

import android.support.v7.widget.PopupMenu
import android.view.View
import com.san.kir.manger.R
import com.san.kir.manger.components.downloadManager.DownloadManagerActivity
import com.san.kir.manger.room.models.DownloadStatus
import com.san.kir.manger.utils.ID
import kotlinx.coroutines.experimental.launch

class ClearDownloadsMenu(act: DownloadManagerActivity, parent: View) {
    init {
        PopupMenu(act, parent).apply {
            val clearCompleted = ID.generate()
            val clearPaused = ID.generate()
            val clearError = ID.generate()
            val clearAll = ID.generate()

            menu.add(
                0,
                clearCompleted,
                0,
                R.string.download_activity_option_submenu_clean_completed
            )
            menu.add(
                0,
                clearPaused,
                0,
                R.string.download_activity_option_submenu_clean_paused
            )
            menu.add(
                0,
                clearError,
                0,
                R.string.download_activity_option_submenu_clean_error
            )
            menu.add(
                0,
                clearAll,
                0,
                R.string.download_activity_option_submenu_clean_all
            )

            setOnMenuItemClickListener { item ->
                launch {
                    when (item.itemId) {
                        clearCompleted -> {
                            act.dao.loadItems()
                                .filter { it.status == DownloadStatus.completed }
                                .forEach { act.dao.delete(it) }

                        }
                        clearPaused -> {
                            act.dao.loadItems()
                                .filter { it.status == DownloadStatus.pause }
                                .forEach { act.dao.delete(it) }
                        }
                        clearError -> {
                            act.dao.loadItems()
                                .filter { it.status == DownloadStatus.error }
                                .forEach { act.dao.delete(it) }
                        }
                        clearAll -> {
                            act.dao.loadItems()
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
