package com.san.kir.manger.extending.dialogs

import android.support.v7.widget.PopupMenu
import android.view.View
import com.san.kir.manger.R
import com.san.kir.manger.components.download_manager.DownloadManagerActivity
import com.san.kir.manger.extending.views.add
import com.san.kir.manger.utils.ID

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
                when (item.itemId) {
                    clearCompleted -> act.mViewModel.clearCompletedDownloads()
                    clearPaused -> act.mViewModel.clearPausedDownloads()
                    clearError -> act.mViewModel.clearErrorDownloads()
                    clearAll -> act.mViewModel.clearAllDownloads()
                }
                return@setOnMenuItemClickListener true
            }
            show()
        }
    }
}
