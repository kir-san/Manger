package com.san.kir.manger.components.list_chapters

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
import com.san.kir.manger.components.download_manager.DownloadService
import com.san.kir.manger.components.viewer.ViewerActivity
import com.san.kir.manger.extending.anko_extend.onClick
import com.san.kir.manger.extending.anko_extend.onLongClick
import com.san.kir.manger.extending.anko_extend.visibleOrGone
import com.san.kir.manger.extending.launchUI
import com.san.kir.manger.room.models.Chapter
import com.san.kir.manger.room.models.DownloadItem
import com.san.kir.manger.room.models.action
import com.san.kir.manger.room.models.countPages
import com.san.kir.manger.room.models.toDownloadItem
import com.san.kir.manger.utils.ID
import com.san.kir.manger.utils.RecyclerViewAdapterFactory
import com.san.kir.manger.utils.enums.ChapterStatus
import com.san.kir.manger.utils.enums.DownloadStatus
import com.san.kir.manger.utils.log
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.alignParentBottom
import org.jetbrains.anko.alignParentEnd
import org.jetbrains.anko.alignParentLeft
import org.jetbrains.anko.alignParentTop
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.dip
import org.jetbrains.anko.endOf
import org.jetbrains.anko.frameLayout
import org.jetbrains.anko.imageView
import org.jetbrains.anko.leftOf
import org.jetbrains.anko.longToast
import org.jetbrains.anko.margin
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.padding
import org.jetbrains.anko.progressBar
import org.jetbrains.anko.relativeLayout
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.textView
import org.jetbrains.anko.toast
import org.jetbrains.anko.wrapContent

class ListChaptersItemView(private val act: ListChaptersActivity) :
    RecyclerViewAdapterFactory.AnkoView<Chapter>() {
    private object Id {
        val date = ID.generate()
        val name = ID.generate()
        val limit = ID.generate()
    }

    private val actionMode = act.actionMode

    private var isDownload = false

    private lateinit var root: RelativeLayout
    private lateinit var name: TextView
    private lateinit var date: TextView
    private lateinit var limit: FrameLayout
    private lateinit var stopBtn: ImageView
    private lateinit var restartBtn: ImageView
    private lateinit var downloadBtn: ImageView
    private lateinit var status: TextView
    private lateinit var percentProgress: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var deleteIndicator: ImageView

    private lateinit var item: Chapter

    private lateinit var observer: Observer<DownloadItem?>

    override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {
        val btnSize = dip(35)

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

            status = textView {
                padding = dip(5)
                id = ID.generate()
            }.lparams(width = wrapContent, height = wrapContent) {
                alignParentLeft()
                alignParentBottom()
            }

            deleteIndicator = imageView {
                backgroundResource = R.drawable.ic_action_delete_black
                padding = dip(10)
                visibleOrGone(false)
            }.lparams(width = dip(20), height = dip(20)) {
                bottomMargin = dip(3)
                alignParentBottom()
                endOf(status)
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

                restartBtn = imageView {
                    backgroundResource = R.drawable.ic_update_black
                    visibility = View.GONE
                }.lparams(width = btnSize, height = btnSize) {
                    gravity = Gravity.CENTER
                }

                downloadBtn = imageView {
                    backgroundResource = R.drawable.ic_action_download_green
                    visibility = View.GONE
                }.lparams(width = btnSize, height = btnSize) {
                    gravity = Gravity.CENTER
                }

                stopBtn = imageView {
                    backgroundResource = R.drawable.ic_stop
                    visibility = View.GONE
                }.lparams(width = btnSize, height = btnSize) {
                    gravity = Gravity.CENTER
                }

                progressBar = progressBar {
                    visibility = View.GONE
                }.lparams(width = dip(17), height = dip(17)) {
                    gravity = Gravity.TOP or Gravity.END
                }

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

    override fun bind(item: Chapter, isSelected: Boolean, position: Int) {
        this.item = item

        name.text = item.name
        date.text = item.date

        updateStatus(item)

        disableDownload(item)

        val color = when {
            isSelected -> Color.parseColor("#9934b5e4")
            item.isRead -> Color.parseColor("#a5a2a2")
            else -> Color.TRANSPARENT
        }
        root.backgroundColor = color
        percentProgress.backgroundColor = color

        observer = Observer {
            changeVisibleAndActions(it, item)
        }
    }

    override fun onAttached(position: Int) {
        initializeOnClicks(item, position)

        act.mViewModel
            .getDownloadItem(item)
            .observe(act, observer)
    }

    override fun onDetached() {
        restartBtn.setOnClickListener(null)
        downloadBtn.setOnClickListener(null)
        root.setOnClickListener(null)
        root.setOnLongClickListener(null)

        act.mViewModel
            .getDownloadItem(item)
            .removeObserver(observer)
    }

    private fun updateStatus(chapter: Chapter) = act.launchUI {
        status.text = act.resources.getString(
            R.string.list_chapters_read,
            chapter.progress,
            chapter.pages.size,
            chapter.countPages
        )
        deleteIndicator.visibleOrGone(chapter.countPages > 0)
    }

    private fun disableDownload(chapter: Chapter) = act.launchUI {
        progressBar.visibleOrGone(false)
        percentProgress.visibleOrGone(false)
        stopBtn.visibleOrGone(false)
        restartBtn.visibleOrGone(false)
        downloadBtn.visibleOrGone(false)

        when (chapter.action) {
            ChapterStatus.DELETE -> restartBtn.visibleOrGone(true)
            ChapterStatus.DOWNLOADABLE -> downloadBtn.visibleOrGone(true)
        }

        isDownload = false
    }

    private fun initializeOnClicks(chapter: Chapter, position: Int) {
        restartBtn.onClick {
            log("restart btn is clicked")
            DownloadService.start(act, chapter.toDownloadItem())
        }

        downloadBtn.onClick {
            DownloadService.add(act, chapter.toDownloadItem())
        }

        stopBtn.onClick {
            DownloadService.pause(act, chapter.toDownloadItem())
        }

        root.onClick {
            // Открыть главу для чтения
            if (actionMode.hasFinish())
                if (isDownload)
                    act.toast(R.string.list_chapters_open_is_download)
                else {
                    if (chapter.pages.isNullOrEmpty() || chapter.pages.any { it.isBlank() }) {
                        act.longToast(R.string.list_chapters_open_not_exists)
                    } else {
                        act.startActivity<ViewerActivity>(
                            "chapter" to chapter,
                            "is" to act.manga.isAlternativeSort
                        )
                    }
                }
            else // Иначе выделить елемент
                act.onListItemSelect(position)
        }

        root.onLongClick {
            act.onListItemSelect(position)
        }
    }

    private fun enableDownload() {
        progressBar.visibility = View.VISIBLE
        percentProgress.visibility = View.VISIBLE
        stopBtn.visibility = View.VISIBLE
        restartBtn.visibility = View.GONE
        downloadBtn.visibility = View.GONE
        isDownload = true
    }

    private fun changeVisibleAndActions(item: DownloadItem?, chapter: Chapter) {
        item?.let { download ->
            when (download.status) {
                DownloadStatus.queued,
                DownloadStatus.loading -> {
                    enableDownload()
                    progressDownload(download.downloadPages, download.totalPages)
                }
                DownloadStatus.pause,
                DownloadStatus.completed -> {
                    disableDownload(chapter)
                    updateStatus(chapter)
                }
                else -> {
                }
            }
        }
    }

    private fun progressDownload(progress: Int, max: Int) {
        val current =
            if (max == 0) max
            else progress * 100 / max

        percentProgress.text = act.getString(R.string.list_chapters_download_progress, current)
    }
}
