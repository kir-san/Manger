package com.san.kir.manger.extending.dialogs

import android.view.Gravity
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.san.kir.ankofork.dialogs.alert
import com.san.kir.ankofork.dialogs.customView
import com.san.kir.ankofork.dip
import com.san.kir.ankofork.padding
import com.san.kir.ankofork.sdk28.linearLayout
import com.san.kir.ankofork.sdk28.progressBar
import com.san.kir.ankofork.sdk28.textResource
import com.san.kir.ankofork.sdk28.textView
import com.san.kir.manger.R
import com.san.kir.manger.repositories.ChapterRepository
import com.san.kir.manger.room.entities.Manga
import com.san.kir.manger.utils.extensions.BaseActivity
import com.san.kir.manger.utils.extensions.delChapters
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class DeleteReadChaptersDialog(act: BaseActivity, manga: Manga, function: (() -> Unit)? = null) {
    init {
        act.alert {
            messageResource = R.string.library_popupmenu_delete_read_chapters_message
            positiveButton(R.string.library_popupmenu_delete_read_chapters_ok) {
                act.alert {
                    var task: Job? = null
                    onCancelled {
                        task?.cancel()
                    }
                    customView {
                        linearLayout {
                            padding = dip(16)
                            val progress = progressBar()
                            val message = textView {
                                textResource =
                                        R.string.library_popupmenu_delete_read_chapters_deleting
                                padding = dip(10)
                            }.lparams { gravity = Gravity.CENTER }

                            task = act.lifecycleScope.launch {
                                val chapters = ChapterRepository(act)
                                    .getItems(manga.unic)
                                    .filter { chapter -> chapter.isRead }
                                val size = chapters.size

                                if (size == 0) {
                                    post {
                                        message.textResource =
                                                R.string.library_popupmenu_delete_read_chapters_delete_nothing
                                    }
                                } else {
                                    post {
                                        message.textResource =
                                                R.string.library_popupmenu_delete_read_chapters_delete
                                    }
                                    delChapters(chapters)
                                }

                                post {
                                    progress.visibility = View.GONE
                                    message.textResource =
                                            R.string.library_popupmenu_delete_read_chapters_ready
                                    function?.invoke()
                                }
                            }
                        }
                    }
                }.show()
            }
            negativeButton(R.string.library_popupmenu_delete_read_chapters_no) {}
        }.show()
    }
}
