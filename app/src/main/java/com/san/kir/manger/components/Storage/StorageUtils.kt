package com.san.kir.manger.components.Storage

import android.content.Context
import android.view.Gravity
import android.view.View
import com.san.kir.manger.R
import com.san.kir.manger.components.Main.Main
import com.san.kir.manger.room.models.Manga
import com.san.kir.manger.utils.delChapters
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.alert
import org.jetbrains.anko.customView
import org.jetbrains.anko.dip
import org.jetbrains.anko.linearLayout
import org.jetbrains.anko.padding
import org.jetbrains.anko.progressBar
import org.jetbrains.anko.textView


object StorageUtils {
    private val chaptersDao = Main.db.chapterDao
    fun deleteReadChapters(ctx: Context,
                           manga: Manga,
                           function: () -> Unit) {
        ctx.alert {
            messageResource = R.string.library_popupmenu_delete_read_chapters_message
            positiveButton(R.string.library_popupmenu_delete_read_chapters_ok) {
                ctx.alert {
                    var task: Job? = null
                    onCancelled {
                        task?.cancel()
                    }
                    customView {
                        linearLayout {
                            padding = dip(16)
                            val progress = progressBar()
                            val message = textView {
                                text = "Удаление..."
                                padding = dip(10)
                            }.lparams { gravity = Gravity.CENTER }

                            task = launch(CommonPool) {
                                val chapters = chaptersDao.loadChapters(
                                        manga.unic).filter { it.isRead }
                                val size = chapters.size

                                if (size == 0) {
                                    post { message.text = "Удалять нечего" }
                                } else {
                                    post { message.text = "Удаляю..." }
                                    delChapters(chapters)
                                }

                                post {
                                    progress.visibility = View.GONE
                                    message.text = "Готово"
                                    function.invoke()
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
