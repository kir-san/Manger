package com.san.kir.manger.components.ListChapters

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
import com.san.kir.manger.components.Main.Main
import com.san.kir.manger.components.Viewer.ViewerActivity
import com.san.kir.manger.room.models.Chapter
import com.san.kir.manger.room.models.DownloadItem
import com.san.kir.manger.room.models.DownloadStatus
import com.san.kir.manger.room.models.action
import com.san.kir.manger.room.models.countPages
import com.san.kir.manger.utils.CHAPTER_STATUS
import com.san.kir.manger.utils.ID
import com.san.kir.manger.utils.RecyclerViewAdapterFactory
import com.san.kir.manger.utils.delChapters
import com.san.kir.manger.utils.getFullPath
import com.san.kir.manger.utils.isFirstRun
import com.san.kir.manger.utils.isNotEmptyDirectory
import com.san.kir.manger.utils.log
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
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
import org.jetbrains.anko.longToast
import org.jetbrains.anko.margin
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.padding
import org.jetbrains.anko.progressBar
import org.jetbrains.anko.relativeLayout
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.sdk25.coroutines.onLongClick
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.textView
import org.jetbrains.anko.toast
import org.jetbrains.anko.wrapContent
import java.io.IOException

class ListChaptersItemView(private val act: ListChaptersActivity) :
    RecyclerViewAdapterFactory.AnkoView<Chapter>() {
    private object _id { // id элементов для связи между собой
        val date = ID.generate()
        val name = ID.generate()
        val limit = ID.generate()
    }

    private val actionMode = act.actionMode
    private val downloadManager by lazy { act.downloadManager }

    private var isDownload = false
    private lateinit var root: RelativeLayout
    private lateinit var name: TextView
    private lateinit var date: TextView
    private lateinit var stopBtn: ImageView
    private lateinit var limit: FrameLayout
    private lateinit var startBtn: ImageView
    private lateinit var deleteBtn: ImageView
    private lateinit var status: TextView
    private lateinit var percentProgress: TextView
    private lateinit var noAction: ImageView
    private lateinit var downloadBtn: ImageView
    private lateinit var progressBar: ProgressBar

    override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {
        val bigBtnSize = dip(45)
        val smallBtnSize = dip(35)

        relativeLayout {
            lparams(width = matchParent, height = dip(55)) {
                margin = dip(1)
            }

            name = textView {
                id = _id.name
                maxLines = 1
                padding = dip(5)
                typeface = Typeface.DEFAULT_BOLD
            }.lparams(width = wrapContent, height = wrapContent) {
                alignParentTop()
                alignParentLeft()
                gravity = Gravity.CENTER_VERTICAL
                leftOf(_id.limit)
            }

            status = textView {
                padding = dip(5)
            }.lparams(width = wrapContent, height = wrapContent) {
                alignParentLeft()
                alignParentBottom()
            }

            date = textView {
                id = _id.date
                padding = dip(5)
            }.lparams(width = wrapContent, height = wrapContent) {
                alignParentBottom()
                leftOf(_id.limit)
            }

            limit = frameLayout {
                id = _id.limit

                deleteBtn = imageView {
                    backgroundResource = R.drawable.ic_action_delete_black
                    visibility = View.GONE
                }.lparams(width = bigBtnSize, height = bigBtnSize) {
                    gravity = Gravity.CENTER
                }

                noAction = imageView {
                    backgroundResource = R.drawable.ic_action_download_black
                    visibility = View.GONE
                }.lparams(width = bigBtnSize, height = bigBtnSize) {
                    gravity = Gravity.CENTER
                }

                downloadBtn = imageView {
                    backgroundResource = R.drawable.ic_action_download_green
                    visibility = View.GONE
                }.lparams(width = bigBtnSize, height = bigBtnSize) {
                    gravity = Gravity.CENTER
                }

                stopBtn = imageView {
                    backgroundResource = R.drawable.ic_stop
                    visibility = View.GONE
                }.lparams(width = smallBtnSize, height = smallBtnSize) {
                    gravity = Gravity.CENTER
                }

                startBtn = imageView {
                    backgroundResource = R.drawable.ic_start
                    visibility = View.GONE
                }.lparams(width = smallBtnSize, height = smallBtnSize) {
                    gravity = Gravity.CENTER
                }

                progressBar = progressBar {
                    visibility = View.GONE
                }.lparams(width = matchParent, height = matchParent)

                percentProgress = textView {
                    visibility = View.GONE
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
        percentProgress.visibility = View.VISIBLE
        stopBtn.visibility = View.VISIBLE
        startBtn.visibility = View.GONE
        deleteBtn.visibility = View.GONE
        noAction.visibility = View.GONE
        downloadBtn.visibility = View.GONE
        isDownload = true
    }

    private fun pauseDownload() {
        progressBar.visibility = View.GONE
        percentProgress.visibility = View.VISIBLE
        stopBtn.visibility = View.GONE
        startBtn.visibility = View.VISIBLE
        deleteBtn.visibility = View.GONE
        noAction.visibility = View.GONE
        downloadBtn.visibility = View.GONE
        isDownload = false
    }

    private fun progressDownload(progress: Int, max: Int) {
        val current =
                if (max == 0) max
                else progress * 100 / max

        percentProgress.text = act.getString(R.string.list_chapters_download_progress, current)
    }

    private fun disableDownload(chapter: Chapter) = async(UI) {
        progressBar.visibility = View.GONE
        percentProgress.visibility = View.GONE
        stopBtn.visibility = View.GONE
        startBtn.visibility = View.GONE
        deleteBtn.visibility = View.GONE
        noAction.visibility = View.GONE
        downloadBtn.visibility = View.GONE

        when (chapter.action) {
            CHAPTER_STATUS.DELETE -> deleteBtn.visibility = View.VISIBLE
            CHAPTER_STATUS.NOT_LOADED -> noAction.visibility = View.VISIBLE
            CHAPTER_STATUS.DOWNLOADABLE -> downloadBtn.visibility = View.VISIBLE
            else -> noAction.visibility = View.VISIBLE
        }

        isDownload = false
    }

    private fun initializeOnClicks(chapter: Chapter, position: Int) {
        deleteBtn.onClick {
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

        downloadBtn.onClick {
            enableDownload()
            val item = DownloadItem(
                name = chapter.manga + " " + chapter.name,
                link = chapter.site,
                path = chapter.path
            )
            act.downloadManager.addOrStart(item)
        }

        root.onClick {
            // Открыть главу для чтения
            if (actionMode.hasFinish())
                if (isDownload)
                    act.toast(R.string.list_chapters_open_is_download)
                else {
                    if (getFullPath(chapter.path).isNotEmptyDirectory) {
                        isFirstRun = true
                        act.startActivity<ViewerActivity>(
                            "manga_name" to chapter.manga,
                            "chapter" to chapter.name,
                            "page_position" to chapter.progress
                        )
                    } else // Иначе показать сообщение
                        act.longToast(R.string.list_chapters_open_not_exists)
                }
            else // Иначе выделить елемент
                act.onListItemSelect(position)
        }

        root.onLongClick {
            act.onListItemSelect(position)
        }
    }

    private fun updateStatus(chapter: Chapter) = async(UI) {
        status.text = act.resources.getString(
            R.string.list_chapters_read,
            chapter.progress,
            chapter.countPages
        )
    }

    // Привязка значений
    override fun bind(item: Chapter, isSelected: Boolean, position: Int) {
        name.text = item.name
        date.text = item.date
        updateStatus(item)

        disableDownload(item)
        initializeOnClicks(item, position)

        val color = when {
            isSelected -> Color.parseColor("#9934b5e4")
            item.isRead -> Color.parseColor("#a5a2a2")
            else -> Color.parseColor("#FFF4F2F2")
        }
        root.backgroundColor = color
        percentProgress.backgroundColor = color

        Main.db.downloadDao
            .loadLivedItem(item.site)
            .observe(act, Observer {
                changeVisiblesAndActions(it, item)
            })
    }

    private fun changeVisiblesAndActions(item: DownloadItem?, chapter: Chapter) {
        item?.let {download ->
            log("download item: ${download.name}")
            log("download status: ${download.status}")
            when (download.status) {
                DownloadStatus.queued,
                DownloadStatus.loading -> {
                    enableDownload()
                    progressDownload(download.downloadPages, download.totalPages)
                    limit.onClick {
                        downloadManager.pause(download)
                    }
                }
                DownloadStatus.pause -> {
                    pauseDownload()
                    limit.onClick {
                        downloadManager.start(download)
                    }
                    updateStatus(chapter)
                }
                DownloadStatus.error -> {
                    pauseDownload()
                    limit.onClick {
                        downloadManager.retry(download)
                    }
                    updateStatus(chapter)
                }
                DownloadStatus.completed -> {
                    disableDownload(chapter)
                    updateStatus(chapter)
                }
                else -> {
                }
            }
        }
    }
}
