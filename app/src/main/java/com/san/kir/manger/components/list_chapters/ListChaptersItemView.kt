package com.san.kir.manger.components.list_chapters

import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet.PARENT_ID
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.san.kir.ankofork.AnkoContext
import com.san.kir.ankofork.constraint_layout.ConstraintSetBuilder.Side.BASELINE
import com.san.kir.ankofork.constraint_layout.ConstraintSetBuilder.Side.BOTTOM
import com.san.kir.ankofork.constraint_layout.ConstraintSetBuilder.Side.END
import com.san.kir.ankofork.constraint_layout.ConstraintSetBuilder.Side.START
import com.san.kir.ankofork.constraint_layout.ConstraintSetBuilder.Side.TOP
import com.san.kir.ankofork.constraint_layout.applyConstraintSet
import com.san.kir.ankofork.constraint_layout.constraintLayout
import com.san.kir.ankofork.dialogs.longToast
import com.san.kir.ankofork.dialogs.toast
import com.san.kir.ankofork.dip
import com.san.kir.ankofork.matchParent
import com.san.kir.ankofork.sdk28.backgroundColor
import com.san.kir.ankofork.sdk28.backgroundResource
import com.san.kir.ankofork.sdk28.imageView
import com.san.kir.ankofork.sdk28.onClick
import com.san.kir.ankofork.sdk28.progressBar
import com.san.kir.ankofork.sdk28.textResource
import com.san.kir.ankofork.sdk28.textView
import com.san.kir.ankofork.startActivity
import com.san.kir.ankofork.topPadding
import com.san.kir.ankofork.wrapContent
import com.san.kir.manger.R
import com.san.kir.manger.components.viewer.ViewerActivity
import com.san.kir.manger.room.entities.Chapter
import com.san.kir.manger.room.entities.DownloadItem
import com.san.kir.manger.room.entities.countPages
import com.san.kir.manger.room.entities.toDownloadItem
import com.san.kir.manger.services.DownloadService
import com.san.kir.manger.utils.RecyclerViewAdapterFactory
import com.san.kir.manger.utils.enums.DownloadStatus
import com.san.kir.manger.utils.extensions.onLongClick
import com.san.kir.manger.utils.extensions.visibleOrGone
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ListChaptersItemView(private val act: ListChaptersActivity) :
    RecyclerViewAdapterFactory.AnkoView<Chapter>() {

    private val actionMode = act.actionMode

    private var isDownload = false

    private lateinit var root: ConstraintLayout
    private lateinit var name: TextView
    private lateinit var date: TextView
    private lateinit var stopBtn: ImageView
    private lateinit var downloadBtn: ImageView
    private lateinit var status: TextView
    private lateinit var indicator: ProgressBar
    private lateinit var deleteIndicator: ImageView

    private lateinit var item: Chapter

    private lateinit var observer: Observer<DownloadItem?>

    override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {
        val btnSize = dip(40)

        constraintLayout {
            lparams(width = matchParent, height = dip(64))

            name = textView {
                id = View.generateViewId()
                maxLines = 1
                textSize = 16f
                typeface = Typeface.DEFAULT_BOLD
            }.lparams(width = 0, height = wrapContent)

            indicator = progressBar {
                id = View.generateViewId()
                visibility = View.GONE
            }.lparams(width = dip(20), height = dip(20))

            status = textView {
                id = View.generateViewId()
                textSize = 14f
            }.lparams(width = wrapContent, height = wrapContent)

            deleteIndicator = imageView {
                id = View.generateViewId()
                backgroundResource = R.drawable.ic_action_delete_black
                visibleOrGone(false)
            }.lparams(width = dip(20), height = dip(20))

            date = textView {
                id = View.generateViewId()
                textSize = 14f
                gravity = Gravity.END
            }.lparams(width = 0, height = wrapContent)

            downloadBtn = imageView {
                id = View.generateViewId()
                backgroundResource = R.drawable.ic_file_download_black
                visibility = View.GONE
            }.lparams(width = btnSize, height = btnSize)

            stopBtn = imageView {
                id = View.generateViewId()
                backgroundResource = R.drawable.ic_clear_black
                visibility = View.GONE
            }.lparams(width = btnSize, height = btnSize)

            applyConstraintSet {
                downloadBtn {
                    connect(
                        END to END of PARENT_ID margin dip(12),
                        TOP to TOP of PARENT_ID,
                        BOTTOM to BOTTOM of PARENT_ID margin dip(7)
                    )
                }

                stopBtn {
                    connect(
                        END to START of downloadBtn margin dip(12),
                        TOP to TOP of PARENT_ID,
                        BOTTOM to BOTTOM of PARENT_ID margin dip(7)
                    )
                }

                name {
                    connect(
                        START to START of PARENT_ID margin dip(16),
                        TOP to TOP of PARENT_ID,
                        END to START of stopBtn margin dip(16)
                    )

                    topPadding = dip(10)
                }

                indicator {
                    connect(
                        START to START of name,
                        TOP to BOTTOM of name margin dip(3)
                    )
                }

                status {
                    connect(
                        START to END of indicator,
                        TOP to BOTTOM of name margin dip(3)
                    )
                }

                deleteIndicator {
                    connect(
                        START to END of status margin dip(16),
                        TOP to TOP of status,
                        BOTTOM to BOTTOM of status
                    )
                }

                date {
                    connect(
                        BASELINE to BASELINE of status,
                        END to START of stopBtn margin dip(16),
                        START to END of deleteIndicator margin dip(16)
                    )
                }
            }

            root = this
        }
    }

    override fun bind(item: Chapter, isSelected: Boolean, position: Int) {
        this.item = item

        name.text = item.name
        date.text = item.date

        updateStatus(item)

        disableDownload()

        val color = when {
            isSelected -> Color.parseColor("#9934b5e4")
            item.isRead -> Color.parseColor("#a5a2a2")
            else -> Color.TRANSPARENT
        }
        root.backgroundColor = color

        observer = Observer {
            changeVisibleAndActions(it, item)
        }
    }

    override fun onAttached(position: Int) {
        initializeOnClicks(item, position)

        act.mViewModel.getDownloadItem(item).observe(act, observer)
    }

    override fun onDetached() {
        downloadBtn.setOnClickListener(null)
        stopBtn.setOnClickListener(null)
        root.setOnClickListener(null)
        root.setOnLongClickListener(null)

        act.mViewModel.getDownloadItem(item).removeObserver(observer)
    }

    private fun updateStatus(chapter: Chapter) = act.lifecycleScope.launch(Dispatchers.Main) {
        status.text = act.resources.getString(
            R.string.list_chapters_read,
            chapter.progress,
            chapter.pages.size,
            chapter.countPages
        )
        deleteIndicator.visibleOrGone(chapter.countPages > 0)
    }

    private fun disableDownload() = act.lifecycleScope.launch(Dispatchers.Main)  {
        indicator.visibleOrGone(false)
        stopBtn.visibleOrGone(false)
        downloadBtn.visibleOrGone(true)

        isDownload = false
    }

    private fun initializeOnClicks(chapter: Chapter, position: Int) {
        downloadBtn.onClick {
            DownloadService.addOrStart(act, chapter.toDownloadItem())
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
        indicator.visibleOrGone(true)
        stopBtn.visibleOrGone(true)
        downloadBtn.visibleOrGone(false)
        deleteIndicator.visibleOrGone(false)
        isDownload = true
    }

    private fun changeVisibleAndActions(item: DownloadItem?, chapter: Chapter) {
        item?.let { download ->
            when (download.status) {
                DownloadStatus.queued -> {
                    enableDownload()
                    queueStatus()
                }
                DownloadStatus.loading -> {
                    enableDownload()
                    loadingStatus(download.downloadPages, download.totalPages)
                }
                DownloadStatus.pause,
                DownloadStatus.completed -> {
                    disableDownload()
                    updateStatus(chapter)
                }
                else -> {
                }
            }
        }
    }

    private fun queueStatus() {
        status.textResource = R.string.list_chapters_queue
    }

    private fun loadingStatus(progress: Int, max: Int) {
        val current =
            if (max == 0) max
            else progress * 100 / max

        status.text = act.getString(R.string.list_chapters_download_progress, current)
    }
}
