package com.san.kir.manger.extending.dialogs

import android.view.Gravity
import android.widget.ProgressBar
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.work.WorkManager
import com.san.kir.ankofork.dialogs.alert
import com.san.kir.ankofork.dialogs.customView
import com.san.kir.ankofork.dip
import com.san.kir.ankofork.padding
import com.san.kir.ankofork.sdk28.linearLayout
import com.san.kir.ankofork.sdk28.progressBar
import com.san.kir.ankofork.sdk28.textResource
import com.san.kir.ankofork.sdk28.textView
import com.san.kir.manger.R
import com.san.kir.manger.room.entities.Manga
import com.san.kir.manger.utils.extensions.BaseActivity
import com.san.kir.manger.utils.extensions.gone
import com.san.kir.manger.workmanager.ChapterDeleteWorker
import com.san.kir.manger.workmanager.ReadChapterDelete
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DeleteReadChaptersDialog(
    private val act: BaseActivity,
    private val manga: Manga,
    private val read: Double? = null
) {
    init {
        act.alert {
            messageResource = R.string.library_popupmenu_delete_read_chapters_message
            positiveButton(R.string.library_popupmenu_delete_read_chapters_ok) {
                yesDialog()
            }
            negativeButton(R.string.library_popupmenu_delete_read_chapters_no) {}
        }.show()
    }

    private fun yesDialog() {
        act.alert {

            customView {
                linearLayout {
                    padding = dip(16)
                    val progress = progressBar()
                    val message = textView {
                        textResource =
                            R.string.library_popupmenu_delete_read_chapters_deleting
                        padding = dip(10)
                    }.lparams { gravity = Gravity.CENTER }

                    actionDelete(progress, message)
                }
            }
        }.show()
    }

    private fun actionDelete(progress: ProgressBar, message: TextView) =
        act.lifecycleScope.launch(Dispatchers.Main) {
            if (read != null && read == 0.0) {
                message.textResource =
                    R.string.library_popupmenu_delete_read_chapters_delete_nothing
            } else {
                message.textResource = R.string.library_popupmenu_delete_read_chapters_delete

                ChapterDeleteWorker.addTask<ReadChapterDelete>(act, manga)
            }

            WorkManager
                .getInstance(act)
                .getWorkInfosByTagLiveData(ChapterDeleteWorker.tag)
                .observe(act, Observer { works ->
                    if (works.isNotEmpty() && works.all { it.state.isFinished }) {
                        progress.gone()
                        message.textResource =
                            R.string.library_popupmenu_delete_read_chapters_ready
                    }
                })

        }
}

