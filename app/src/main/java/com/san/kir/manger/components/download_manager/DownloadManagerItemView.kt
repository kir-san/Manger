package com.san.kir.manger.components.download_manager

import android.view.Gravity
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.san.kir.ankofork.AnkoContext
import com.san.kir.ankofork.dip
import com.san.kir.ankofork.horizontalProgressBar
import com.san.kir.ankofork.margin
import com.san.kir.ankofork.matchParent
import com.san.kir.ankofork.sdk28.backgroundResource
import com.san.kir.ankofork.sdk28.frameLayout
import com.san.kir.ankofork.sdk28.imageButton
import com.san.kir.ankofork.sdk28.imageView
import com.san.kir.ankofork.sdk28.linearLayout
import com.san.kir.ankofork.sdk28.lines
import com.san.kir.ankofork.sdk28.onClick
import com.san.kir.ankofork.sdk28.textView
import com.san.kir.ankofork.verticalLayout
import com.san.kir.ankofork.wrapContent
import com.san.kir.manger.R
import com.san.kir.manger.room.entities.DownloadItem
import com.san.kir.manger.services.DownloadService
import com.san.kir.manger.utils.RecyclerViewAdapterFactory
import com.san.kir.manger.utils.TimeFormat
import com.san.kir.manger.utils.enums.DownloadStatus
import com.san.kir.manger.utils.extensions.RoundedImageView
import com.san.kir.manger.utils.extensions.bytesToMb
import com.san.kir.manger.utils.extensions.formatDouble
import com.san.kir.manger.utils.extensions.gone
import com.san.kir.manger.utils.extensions.roundedImageView
import com.san.kir.manger.utils.extensions.visibleOrGone
import com.san.kir.manger.utils.extensions.visibleOrInvisible
import com.san.kir.manger.utils.loadImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class DownloadManagerItemView(private val act: DownloadManagerActivity) :
    RecyclerViewAdapterFactory.AnkoView<DownloadItem>() {

    private val errorSize = 15

    private val btnSize = 35

    private lateinit var isError: ImageView
    private lateinit var logo: RoundedImageView
    private lateinit var name: TextView
    private lateinit var progressText: TextView
    private lateinit var startBtn: ImageButton
    private lateinit var stopBtn: ImageButton
    private lateinit var progressBar: ProgressBar

    override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {
        linearLayout {
            lparams(width = matchParent, height = dip(80))

            val logoSize = 45
            frameLayout {
                logo = roundedImageView { }

                isError = imageView {
                    backgroundResource = R.drawable.unknown
                    visibleOrInvisible(false)
                }.lparams(width = dip(errorSize), height = dip(errorSize)) {
                    gravity = Gravity.TOP or Gravity.END
                }
            }.lparams(width = dip(logoSize), height = dip(logoSize)) {
                margin = dip(16)
                gravity = Gravity.CENTER_VERTICAL
            }

            verticalLayout {
                lparams(width = matchParent, height = wrapContent) {
                    gravity = Gravity.CENTER_VERTICAL
                    weight = 1f
                }

                name = textView {
                    lines = 1
                    textSize = 16f
                }.lparams(width = wrapContent, height = wrapContent)

                progressText = textView {
                    textSize = 14f
                }.lparams(width = wrapContent, height = wrapContent)

                progressBar = horizontalProgressBar {
                    visibleOrGone(false)
                }.lparams(width = matchParent, height = wrapContent)
            }

            startBtn = imageButton {
                visibleOrGone(false)
                backgroundResource = R.drawable.ic_start
            }.lparams(width = dip(btnSize), height = dip(btnSize)) {
                gravity = Gravity.CENTER_VERTICAL
                margin = dip(16)
            }

            stopBtn = imageButton {
                gone()
                backgroundResource = R.drawable.ic_stop
            }.lparams(width = dip(btnSize), height = dip(btnSize)) {
                gravity = Gravity.CENTER_VERTICAL
                margin = dip(16)
            }
        }
    }

    override fun bind(item: DownloadItem, isSelected: Boolean, position: Int) {
        val context = name.context
        act.lifecycleScope.launch(Dispatchers.Default) {
            val isLoadOrQueue = item.status == DownloadStatus.queued
                    || item.status == DownloadStatus.loading

            withContext(Dispatchers.Main) {
                name.text = context.getString(R.string.download_item_name, item.manga, item.name)

                startBtn.visibleOrGone(item.status == DownloadStatus.pause)
                stopBtn.visibleOrGone(isLoadOrQueue)
                progressBar.visibleOrGone(isLoadOrQueue)
                progressBar.isIndeterminate = item.status == DownloadStatus.queued

                startBtn.onClick {
//                    DownloadService.start(act, item)
                }
                stopBtn.onClick {
//                    DownloadService.pause(act, item)
                }

                if (item.status == DownloadStatus.loading) {
                    progressBar.progress = item.downloadPages
                    progressBar.max = item.totalPages
                }

                isError.visibleOrGone(item.isError)
            }

            when (item.status) {
                DownloadStatus.completed -> {
                    val time = TimeFormat(item.totalTime / 1000)

                    withContext(Dispatchers.Main) {
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
                    withContext(Dispatchers.Main) {
                        progressText.text =
                            context.getString(R.string.download_item_progress, pages, size)
                    }
                }
            }


            val manga = act.mViewModel.getMangaItemOrNull(item)
            if (manga != null) {
                loadImage(manga.logo).into(logo)
            }
        }
    }
}
