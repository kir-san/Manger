package com.san.kir.manger.extending.dialogs

import android.content.Context
import android.view.Gravity
import android.view.View
import com.san.kir.manger.R
import com.san.kir.manger.components.main.Main
import com.san.kir.manger.room.models.Manga
import com.san.kir.manger.utils.delChapters
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.alert
import org.jetbrains.anko.customView
import org.jetbrains.anko.dip
import org.jetbrains.anko.linearLayout
import org.jetbrains.anko.padding
import org.jetbrains.anko.progressBar
import org.jetbrains.anko.textResource
import org.jetbrains.anko.textView

class DeleteReadChaptersDialog(context: Context, manga: Manga, function: (() -> Unit)? = null) {
    init {
        context.alert {
            messageResource = R.string.library_popupmenu_delete_read_chapters_message
            positiveButton(R.string.library_popupmenu_delete_read_chapters_ok) {
                context.alert {
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

                            task = async {
                                val chapters =
                                    Main.db.chapterDao.loadChapters(manga.unic).filter { it.isRead }
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
