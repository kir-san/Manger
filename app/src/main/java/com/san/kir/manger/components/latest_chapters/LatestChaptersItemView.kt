package com.san.kir.manger.components.latest_chapters

import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet.PARENT_ID
import androidx.lifecycle.lifecycleScope
import com.san.kir.ankofork.AnkoContext
import com.san.kir.ankofork.constraint_layout.ConstraintSetBuilder.Side.BASELINE
import com.san.kir.ankofork.constraint_layout.ConstraintSetBuilder.Side.BOTTOM
import com.san.kir.ankofork.constraint_layout.ConstraintSetBuilder.Side.END
import com.san.kir.ankofork.constraint_layout.ConstraintSetBuilder.Side.START
import com.san.kir.ankofork.constraint_layout.ConstraintSetBuilder.Side.TOP
import com.san.kir.ankofork.constraint_layout.applyConstraintSet
import com.san.kir.ankofork.constraint_layout.constraintLayout
import com.san.kir.ankofork.constraint_layout.matchConstraint
import com.san.kir.ankofork.dip
import com.san.kir.ankofork.matchParent
import com.san.kir.ankofork.sdk28.backgroundColor
import com.san.kir.ankofork.sdk28.backgroundResource
import com.san.kir.ankofork.sdk28.imageView
import com.san.kir.ankofork.sdk28.onClick
import com.san.kir.ankofork.sdk28.textResource
import com.san.kir.ankofork.sdk28.textView
import com.san.kir.ankofork.topPadding
import com.san.kir.ankofork.wrapContent
import com.san.kir.manger.R
import com.san.kir.manger.room.entities.Chapter
import com.san.kir.manger.room.entities.DownloadItem
import com.san.kir.manger.room.entities.toDownloadItem
import com.san.kir.manger.services.DownloadService
import com.san.kir.manger.utils.RecyclerViewAdapterFactory
import com.san.kir.manger.utils.enums.DownloadStatus
import com.san.kir.manger.utils.extensions.visibleOrGone
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class LatestChaptersItemView(private val act: LatestChapterActivity) :
    RecyclerViewAdapterFactory.AnkoView<Chapter>() {

    private var isDownload = false
    private lateinit var root: ConstraintLayout
    private lateinit var name: TextView
    private lateinit var date: TextView
    private lateinit var stopBtn: ImageView
    private lateinit var manga: TextView
    private lateinit var downloadBtn: ImageView
    private lateinit var item: Chapter

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

            manga = textView {
                id = View.generateViewId()
                maxLines = 1
                textSize = 14f
            }.lparams(width = matchConstraint, height = wrapContent)

            date = textView {
                id = View.generateViewId()
                textSize = 14f
                gravity = Gravity.END
            }.lparams(width = wrapContent, height = wrapContent)

            downloadBtn = imageView {
                id = View.generateViewId()
                backgroundResource = R.drawable.ic_action_download
                visibility = View.GONE
            }.lparams(width = btnSize, height = btnSize)

            stopBtn = imageView {
                id = View.generateViewId()
                backgroundResource = R.drawable.ic_clear
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

                date {
                    connect(
                        TOP to BOTTOM of name margin dip(3),
                        END to START of stopBtn margin dip(16)
                    )
                }

                manga {
                    connect(
                        START to START of name,
                        BASELINE to BASELINE of date,
                        END to START of date margin dip(16)
                    )
                }
            }

            root = this
        }
    }

    @ExperimentalCoroutinesApi
    override fun bind(item: Chapter, isSelected: Boolean, position: Int) {
        this.item = item

        name.text = item.name
        manga.text = item.manga

        disableDownload(item)
        initializeOnClicks(item)

        act.lifecycleScope.launch(Dispatchers.Default) {
            val color = when {
                item.isRead -> Color.parseColor("#a5a2a2")
                else -> Color.TRANSPARENT
            }
            withContext(Dispatchers.Main) {
                root.backgroundColor = color
            }

            act.mViewModel.getDownloadItems(item)
                .onEach { changeVisibilityAndActions(it, item) }
                .flowOn(Dispatchers.Main)
        }
    }

    override fun onDetached() {
        downloadBtn.setOnClickListener(null)
        stopBtn.setOnClickListener(null)
    }

    private fun disableDownload(chapter: Chapter) =
        act.lifecycleScope.launch(Dispatchers.Main) {
            stopBtn.visibleOrGone(false)
            downloadBtn.visibleOrGone(true)
            date.text = chapter.date

            isDownload = false
        }

    private fun initializeOnClicks(chapter: Chapter) {
        downloadBtn.onClick {
            DownloadService.addOrStart(act, chapter.toDownloadItem())
        }

        stopBtn.onClick {
            DownloadService.pause(act, chapter.toDownloadItem())
        }
    }

    private fun changeVisibilityAndActions(item: DownloadItem?, chapter: Chapter) {
        item?.let { download ->
            when (download.status) {
                DownloadStatus.queued -> {
                    enableDownload()
                    queueStatus()
                }
                DownloadStatus.loading -> {
                    enableDownload()
                    progressDownload(download.downloadPages, download.totalPages)
                }
                DownloadStatus.pause,
                DownloadStatus.completed -> {
                    disableDownload(chapter)
                }
                else -> { // for when work
                }
            }
        }
    }

    private fun queueStatus() {
        date.textResource = R.string.list_chapters_queue
    }

    private fun enableDownload() {
        stopBtn.visibleOrGone(true)
        downloadBtn.visibleOrGone(false)
        isDownload = true
    }

    private fun progressDownload(progress: Int, max: Int) {
        val current =
            if (max == 0) max
            else progress * 100 / max

        date.text = act.getString(R.string.list_chapters_download_progress, current)
    }
}
