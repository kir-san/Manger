package com.san.kir.manger.components.latestChapters

import android.arch.lifecycle.Observer
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import com.san.kir.manger.R
import com.san.kir.manger.components.main.Main
import com.san.kir.manger.extending.ankoExtend.onClick
import com.san.kir.manger.room.models.DownloadItem
import com.san.kir.manger.room.models.DownloadStatus
import com.san.kir.manger.room.models.LatestChapter
import com.san.kir.manger.room.models.action
import com.san.kir.manger.room.models.isRead
import com.san.kir.manger.room.models.toDownloadItem
import com.san.kir.manger.utils.ChapterStatus
import com.san.kir.manger.utils.ID
import com.san.kir.manger.utils.RecyclerViewAdapterFactory
import com.san.kir.manger.utils.delChapters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.alert
import org.jetbrains.anko.alignParentBottom
import org.jetbrains.anko.alignParentEnd
import org.jetbrains.anko.alignParentLeft
import org.jetbrains.anko.alignParentTop
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.dip
import org.jetbrains.anko.frameLayout
import org.jetbrains.anko.imageView
import org.jetbrains.anko.leftOf
import org.jetbrains.anko.lines
import org.jetbrains.anko.margin
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.padding
import org.jetbrains.anko.progressBar
import org.jetbrains.anko.relativeLayout
import org.jetbrains.anko.textView
import org.jetbrains.anko.toast
import org.jetbrains.anko.wrapContent
import java.io.IOException

class LatestChaptersItemView(private val act: LatestChapterActivity) :
    RecyclerViewAdapterFactory.AnkoView<LatestChapter>() {
    private object Id { // id элементов для связи между собой
        val date = ID.generate()
        val name = ID.generate()
        val limit = ID.generate()
    }

    private val downloadManager by lazy { act.downloadManager }

    private var isDownload = false
    private lateinit var root: RelativeLayout
    private lateinit var name: TextView
    private lateinit var date: TextView
    private lateinit var stop: ImageView
    private lateinit var limit: FrameLayout
    private lateinit var manga: TextView
    private lateinit var start: ImageView
    private lateinit var delete: ImageView
    private lateinit var percent: TextView
    private lateinit var noAction: ImageView
    private lateinit var download: ImageView
    private lateinit var progressBar: ProgressBar

    override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {
        val bigBtnSize = dip(45)
        val smallBtnSize = dip(35)
        // Подкорень (требуется для отображения выделения и всего остального)
        relativeLayout {
            lparams(width = matchParent, height = dip(55)) {
                margin = dip(1)
            }

            name = textView {
                id = Id.name
                maxLines = 1
                padding = dip(5)
                typeface = Typeface.DEFAULT_BOLD
            }.lparams(width = wrapContent, height = wrapContent) {
                alignParentTop()
                alignParentLeft()
                gravity = Gravity.CENTER_VERTICAL
                leftOf(Id.limit)
            }

            manga = textView {
                padding = dip(5)
                lines = 1
            }.lparams(width = wrapContent, height = wrapContent) {
                alignParentLeft()
                alignParentBottom()
                leftOf(Id.date)
            }

            date = textView {
                id = Id.date
                padding = dip(5)
            }.lparams(width = wrapContent, height = wrapContent) {
                alignParentBottom()
                leftOf(Id.limit)
            }

            limit = frameLayout {
                id = Id.limit

                delete = imageView {
                    backgroundResource = R.drawable.ic_action_delete_black
                }.lparams(width = bigBtnSize, height = bigBtnSize) {
                    gravity = Gravity.CENTER
                }

                noAction = imageView {
                    backgroundResource = R.drawable.ic_action_download_black
                }.lparams(width = bigBtnSize, height = bigBtnSize) {
                    gravity = Gravity.CENTER
                }

                download = imageView {
                    backgroundResource = R.drawable.ic_action_download_green
                }.lparams(width = bigBtnSize, height = bigBtnSize) {
                    gravity = Gravity.CENTER
                }


                stop = imageView {
                    backgroundResource = R.drawable.ic_stop
                }.lparams(width = smallBtnSize, height = smallBtnSize) {
                    gravity = Gravity.CENTER
                }

                start = imageView {
                    backgroundResource = R.drawable.ic_start
                }.lparams(width = smallBtnSize, height = smallBtnSize) {
                    gravity = Gravity.CENTER
                }

                progressBar = progressBar { }.lparams(width = matchParent, height = matchParent)

                percent = textView {
                    textSize = 10f
                }.lparams(width = wrapContent, height = wrapContent) {
                    gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                    bottomMargin = dip(3)
                }

            }.lparams(height = matchParent, width = dip(55)) {
                alignParentEnd()
            }

            root = this
        }

    }

    private fun enableDownload() {
        progressBar.visibility = View.VISIBLE
        percent.visibility = View.VISIBLE
        stop.visibility = View.VISIBLE
        start.visibility = View.GONE
        delete.visibility = View.GONE
        noAction.visibility = View.GONE
        download.visibility = View.GONE
        isDownload = true
    }

    private fun pauseDownload() {
        progressBar.visibility = View.GONE
        percent.visibility = View.VISIBLE
        stop.visibility = View.GONE
        start.visibility = View.VISIBLE
        delete.visibility = View.GONE
        noAction.visibility = View.GONE
        download.visibility = View.GONE
        isDownload = false
    }

    private fun progressDownload(progress: Int, max: Int) {
        val current =
            if (max == 0) max
            else progress * 100 / max

        percent.text = act.getString(R.string.list_chapters_download_progress, current)
    }

    private fun disableDownload(chapter: LatestChapter) = GlobalScope.launch(Dispatchers.Main) {
        progressBar.visibility = View.GONE
        percent.visibility = View.GONE
        stop.visibility = View.GONE
        start.visibility = View.GONE
        delete.visibility = View.GONE
        noAction.visibility = View.GONE
        download.visibility = View.GONE

        when (chapter.action) {
            ChapterStatus.DELETE -> delete.visibility = View.VISIBLE
            ChapterStatus.NOT_LOADED -> noAction.visibility = View.VISIBLE
            ChapterStatus.DOWNLOADABLE -> download.visibility = View.VISIBLE
            else -> noAction.visibility = View.VISIBLE
        }

        isDownload = false
    }

    private fun initializeOnClicks(chapter: LatestChapter) {
        delete.onClick {
            act.alert(R.string.list_chapters_delete_text) {
                positiveButton(R.string.list_chapters_delete_yes) {
                    try {
                        val (acc, max) = delChapters(chapter) // При удалении главы
                        if (acc != max) // проверить результат
                        // если не удалено
                            act.toast(R.string.list_chapters_delete_not_delete)
                        else {
                            // если удалено
                            act.toast(R.string.list_chapters_delete_okay_delete)
                            disableDownload(chapter)
                        }
                    } catch (ex: IOException) {
                        // В случае ошибки
                        act.toast(R.string.list_chapters_delete_error)
                        ex.printStackTrace()
                    }
                }
                negativeButton(R.string.list_chapters_delete_no) {}
            }.show()
        }

        download.onClick {
            enableDownload()
            act.downloadManager.addOrStart(chapter.toDownloadItem())
        }
    }

    override fun bind(item: LatestChapter, isSelected: Boolean, position: Int) {
        name.text = item.name
        date.text = item.date
        manga.text = item.manga

        disableDownload(item)
        initializeOnClicks(item)

        GlobalScope.launch(Dispatchers.Main) {
            val color = when {
                item.isRead() -> Color.parseColor("#a5a2a2")
                else -> Color.TRANSPARENT
            }
            root.backgroundColor = color
            percent.backgroundColor = color
        }


        Main.db.downloadDao
            .loadLivedItem(item.site)
            .observe(act, Observer {
                changeVisibilityAndActions(it, item)
            })
    }

    private fun changeVisibilityAndActions(item: DownloadItem?, chapter: LatestChapter) {
        item?.let { downloadItem ->
            when (downloadItem.status) {
                DownloadStatus.queued,
                DownloadStatus.loading -> {
                    enableDownload()
                    progressDownload(downloadItem.downloadPages, downloadItem.totalPages)
                    limit.onClick {
                        downloadManager.pause(item)
                    }
                }
                DownloadStatus.pause -> {
                    pauseDownload()
                    limit.onClick {
                        downloadManager.start(item)
                    }
                }
                DownloadStatus.error -> {
                    pauseDownload()
                    limit.onClick {
                        downloadManager.retry(item)
                    }
                }
                DownloadStatus.completed -> {
                    disableDownload(chapter)
                }
                else -> {
                }
            }
        }
    }
}
