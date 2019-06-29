package com.san.kir.manger.components.latest_chapters

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
import com.san.kir.manger.extending.anko_extend.onClick
import com.san.kir.manger.extending.anko_extend.visibleOrGone
import com.san.kir.manger.room.models.DownloadItem
import com.san.kir.manger.room.models.LatestChapter
import com.san.kir.manger.room.models.action
import com.san.kir.manger.room.models.toDownloadItem
import com.san.kir.manger.utils.ID
import com.san.kir.manger.utils.RecyclerViewAdapterFactory
import com.san.kir.manger.utils.enums.ChapterStatus
import com.san.kir.manger.utils.enums.DownloadStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.AnkoContext
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
import org.jetbrains.anko.wrapContent


class LatestChaptersItemView(private val act: LatestChapterActivity) :
    RecyclerViewAdapterFactory.AnkoView<LatestChapter>() {
    private object Id { // id элементов для связи между собой
        val date = ID.generate()
        val name = ID.generate()
        val limit = ID.generate()
    }

    private var isDownload = false
    private lateinit var root: RelativeLayout
    private lateinit var name: TextView
    private lateinit var date: TextView
    private lateinit var stop: ImageView
    private lateinit var limit: FrameLayout
    private lateinit var manga: TextView
    private lateinit var restart: ImageView
    private lateinit var percent: TextView
    private lateinit var download: ImageView
    private lateinit var progressBar: ProgressBar

    override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {
        val btnSize = dip(35)
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

                restart = imageView {
                    backgroundResource = R.drawable.ic_update_black
                }.lparams(width = btnSize, height = btnSize) {
                    gravity = Gravity.CENTER
                }

                download = imageView {
                    backgroundResource = R.drawable.ic_action_download_green
                }.lparams(width = btnSize, height = btnSize) {
                    gravity = Gravity.CENTER
                }


                stop = imageView {
                    backgroundResource = R.drawable.ic_stop
                }.lparams(width = btnSize, height = btnSize) {
                    gravity = Gravity.CENTER
                }

                progressBar = progressBar { }.lparams(width = dip(17), height = dip(17)) {
                    gravity = Gravity.TOP or Gravity.END
                }

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
        restart.visibility = View.GONE
        download.visibility = View.GONE
        isDownload = true
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
        restart.visibility = View.GONE
        download.visibility = View.GONE

        when (chapter.action) {
            ChapterStatus.DELETE -> restart.visibleOrGone(true)
            ChapterStatus.DOWNLOADABLE -> download.visibleOrGone(true)
        }

        isDownload = false
    }

    private fun initializeOnClicks(chapter: LatestChapter) {
        restart.onClick {
            DownloadService.start(act, chapter.toDownloadItem())
        }

        download.onClick {
            DownloadService.add(act, chapter.toDownloadItem())
        }

        stop.onClick {
            DownloadService.pause(act, chapter.toDownloadItem())
        }
    }

    override fun bind(item: LatestChapter, isSelected: Boolean, position: Int) {
        name.text = item.name
        date.text = item.date
        manga.text = item.manga

        disableDownload(item)
        initializeOnClicks(item)

        act.launch(act.coroutineContext) {
            val color = when {
                act.mViewModel.isChapterRead(item) -> Color.parseColor("#a5a2a2")
                else -> Color.TRANSPARENT
            }
            withContext(Dispatchers.Main) {
                root.backgroundColor = color
                percent.backgroundColor = color
            }
        }

        act.mViewModel
            .getDownloadItems(item)
            .observe(act, Observer {
                changeVisibilityAndActions(it, item)
            })
    }

    private fun changeVisibilityAndActions(item: DownloadItem?, chapter: LatestChapter) {
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
                }
                else -> {
                }
            }
        }
    }
}
