package com.san.kir.manger.components.Storage

import android.content.DialogInterface
import android.view.Gravity
import android.view.View
import com.san.kir.manger.R
import com.san.kir.manger.dbflow.models.Manga
import com.san.kir.manger.dbflow.wrapers.ChapterWrapper
import com.san.kir.manger.utils.delChapters
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.alert
import org.jetbrains.anko.customView
import org.jetbrains.anko.dip
import org.jetbrains.anko.linearLayout
import org.jetbrains.anko.padding
import org.jetbrains.anko.progressBar
import org.jetbrains.anko.textView


object StorageUtils {
    fun deleteReadChapters(ctx: AnkoContext<*>,
                           manga: Manga,
                           onDismiss: (() -> Unit)? = null): DialogInterface {
        return ctx.alert {
            messageResource = R.string.library_popupmenu_delete_read_chapters_message
            positiveButton(R.string.library_popupmenu_delete_read_chapters_ok) {
                ctx.alert {
                    var task: Job? = null
                    onCancelled {
                        task?.let {
                            if (!it.isCompleted)
                                it.cancel()
                        }
                        onDismiss?.invoke()
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
                                val chapters = ChapterWrapper.getChapters(
                                        manga.unic).filter { it.isRead }
                                val size = chapters.size

                                if (size == 0) {
                                    post { message.text = "Удалять нечего" }
                                } else
                                    chapters.forEachIndexed { index, chapter ->
                                        val (acc, max) = 1 to 1
                                        delChapters(chapter)
                                        if (acc == max)
                                            post { message.text = "Удаленно ${index + 1} из $size" }
                                        delay(20L)
                                    }

                                post {
                                    progress.visibility = View.GONE
                                    message.text = "Готово"
                                    onDismiss?.invoke()
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
