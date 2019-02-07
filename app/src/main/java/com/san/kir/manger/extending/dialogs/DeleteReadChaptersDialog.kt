package com.san.kir.manger.extending.dialogs

import android.view.Gravity
import android.view.View
import com.san.kir.manger.R
import com.san.kir.manger.extending.BaseActivity
import com.san.kir.manger.repositories.ChapterRepository
import com.san.kir.manger.room.models.Manga
import com.san.kir.manger.utils.delChapters
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import org.jetbrains.anko.alert
import org.jetbrains.anko.customView
import org.jetbrains.anko.dip
import org.jetbrains.anko.linearLayout
import org.jetbrains.anko.padding
import org.jetbrains.anko.progressBar
import org.jetbrains.anko.textResource
import org.jetbrains.anko.textView

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

                            task = act.async {
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
