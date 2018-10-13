package com.san.kir.manger.components.downloadManager

import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import com.san.kir.manger.R
import com.san.kir.manger.components.main.Main
import com.san.kir.manger.extending.ankoExtend.invisibleOrVisible
import com.san.kir.manger.extending.ankoExtend.roundedImageView
import com.san.kir.manger.extending.ankoExtend.visibleOrGone
import com.san.kir.manger.extending.ankoExtend.visibleOrInvisible
import com.san.kir.manger.extending.views.RoundedImageView
import com.san.kir.manger.room.models.DownloadItem
import com.san.kir.manger.room.models.DownloadStatus
import com.san.kir.manger.utils.ID
import com.san.kir.manger.utils.RecyclerViewAdapterFactory
import com.san.kir.manger.utils.TimeFormat
import com.san.kir.manger.utils.bytesToMb
import com.san.kir.manger.utils.formatDouble
import com.san.kir.manger.utils.loadImage
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.alignParentEnd
import org.jetbrains.anko.alignParentStart
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.bottomOf
import org.jetbrains.anko.centerVertically
import org.jetbrains.anko.dip
import org.jetbrains.anko.endOf
import org.jetbrains.anko.horizontalMargin
import org.jetbrains.anko.horizontalProgressBar
import org.jetbrains.anko.imageButton
import org.jetbrains.anko.lines
import org.jetbrains.anko.margin
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.padding
import org.jetbrains.anko.relativeLayout
import org.jetbrains.anko.sameBottom
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.startOf
import org.jetbrains.anko.textView
import org.jetbrains.anko.wrapContent

class DownloadManagerItemView(private val act: DownloadManagerActivity) :
    RecyclerViewAdapterFactory.AnkoView<DownloadItem>() {

    private object Id {
        val logo = ID.generate()
        val name = ID.generate()
        val progress = ID.generate()
        val start = ID.generate()
        val percent = ID.generate()
        val bar = ID.generate()
    }

    private val gMargin = 2
    private val gPadding = 3
    private val logoSize = 50
    private val nameTextSize = 16.5f
    private val btnSize = 40

    private lateinit var logo: RoundedImageView
    private lateinit var name: TextView
    private lateinit var progressText: TextView
    private lateinit var startBtn: ImageButton
    private lateinit var retryBtn: ImageButton
    private lateinit var stopBtn: ImageButton
    private lateinit var progressBar: ProgressBar
    private lateinit var progressPercent: TextView

    override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {
        relativeLayout {
            lparams(width = matchParent, height = wrapContent) {
                margin = dip(gMargin)
                padding = dip(gPadding)
            }

            logo = roundedImageView {
                id = Id.logo
            }.lparams(width = dip(logoSize), height = dip(logoSize)) {
                alignParentStart()
                centerVertically()
            }

            name = textView {
                id = Id.name
                lines = 1
                textSize = nameTextSize
            }.lparams(width = matchParent, height = wrapContent) {
                horizontalMargin = dip(5)
                topMargin = dip(2)
                endOf(Id.logo)
                startOf(Id.start)
            }

            progressText = textView {
                id = Id.progress
                textSize = 14f
            }.lparams(width = wrapContent, height = wrapContent) {
                horizontalMargin = dip(5)
                topMargin = dip(3)
                bottomOf(Id.name)
                endOf(Id.logo)
                startOf(Id.start)
            }

            startBtn = imageButton {
                id = Id.start
                visibility = View.INVISIBLE
                backgroundResource = R.drawable.ic_start
                padding = dip(10)
            }.lparams(width = dip(btnSize), height = dip(btnSize)) {
                alignParentEnd()
                centerVertically()
            }

            retryBtn = imageButton {
                visibility = View.GONE
                backgroundResource = R.drawable.ic_update_black
                padding = dip(10)
            }.lparams(width = dip(btnSize), height = dip(btnSize)) {
                alignParentEnd()
                centerVertically()
            }

            stopBtn = imageButton {
                visibility = View.GONE
                backgroundResource = R.drawable.ic_stop
                padding = dip(10)
            }.lparams(width = dip(btnSize), height = dip(btnSize)) {
                alignParentEnd()
                centerVertically()
            }

            progressBar = horizontalProgressBar {
                visibility = View.INVISIBLE
                id = Id.bar
            }.lparams(width = matchParent, height = wrapContent) {
                horizontalMargin = dip(5)
                bottomOf(Id.progress)
                endOf(Id.logo)
                startOf(Id.percent)
            }

            progressPercent = textView {
                id = Id.percent
                text = "0 %"
                visibility = View.INVISIBLE
            }.lparams(width = wrapContent, height = wrapContent) {
                sameBottom(Id.bar)
                startOf(Id.start)
            }
        }
    }

    override fun bind(item: DownloadItem, isSelected: Boolean, position: Int) {
        val context = name.context
        launch {
            val isLoadOrQueue = item.status == DownloadStatus.queued
                    || item.status == DownloadStatus.loading
            launch(UI) {
                name.text = context.getString(R.string.download_item_name, item.manga, item.name)

                startBtn.invisibleOrVisible(item.status != DownloadStatus.pause)
                retryBtn.visibleOrGone(item.status == DownloadStatus.error)
                stopBtn.visibleOrGone(isLoadOrQueue)
                progressPercent.visibleOrInvisible(isLoadOrQueue)
                progressBar.visibleOrInvisible(isLoadOrQueue)
                progressBar.isIndeterminate = item.status == DownloadStatus.queued

                startBtn.onClick {
                    act.downloadManager.start(item)
                }
                retryBtn.onClick {
                    act.downloadManager.retry(item)
                }
                stopBtn.onClick {
                    act.downloadManager.pause(item)
                }

                if (item.status == DownloadStatus.loading) {
                    progressBar.progress = item.downloadPages
                    progressBar.max = item.totalPages

                    progressPercent.text =
                            act.getString(
                                R.string.download_item_progress_percent,
                                if (item.totalPages == 0) 0
                                else item.downloadPages * 100 / item.totalPages
                            )
                }
            }

            when (item.status) {
                DownloadStatus.completed -> {
                    val time = TimeFormat(item.totalTime / 1000)

                    launch(UI) {
                        progressText.text =
                                context.getString(
                                    R.string.download_item_final_size_with_time,
                                    formatDouble(bytesToMb(item.downloadSize)),
                                    time.toString(context)
                                )
                    }
                }
                else -> {
                    val pages = context.getString(
                        R.string.download_item_progress_text,
                        item.downloadPages,
                        item.totalPages
                    )
                    val size = context.getString(
                        R.string.download_item_progress_size,
                        formatDouble(bytesToMb(item.downloadSize))
                    )
                    launch(UI) {
                        progressText.text =
                                context.getString(R.string.download_item_progress, pages, size)
                    }
                }
            }

        }
        launch(UI) {
            val manga = Main.db.mangaDao.loadMangaOrNull(item.manga)
            if (manga != null) {
                loadImage(manga.logo).into(logo)
            }
        }
    }
}
