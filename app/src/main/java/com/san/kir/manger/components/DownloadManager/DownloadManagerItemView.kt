package com.san.kir.manger.components.DownloadManager

import android.graphics.Color
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView.ScaleType.CENTER_CROP
import android.widget.ProgressBar
import android.widget.TextView
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.instance
import com.san.kir.manger.R
import com.san.kir.manger.room.models.DownloadItem
import com.san.kir.manger.utils.ID
import com.san.kir.manger.utils.RecyclerViewAdapterFactory
import com.san.kir.manger.utils.bytesToMbytes
import com.san.kir.manger.utils.formatDouble
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.alignParentLeft
import org.jetbrains.anko.alignParentRight
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.bottomOf
import org.jetbrains.anko.centerVertically
import org.jetbrains.anko.dip
import org.jetbrains.anko.horizontalMargin
import org.jetbrains.anko.horizontalProgressBar
import org.jetbrains.anko.imageButton
import org.jetbrains.anko.leftOf
import org.jetbrains.anko.lines
import org.jetbrains.anko.margin
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.padding
import org.jetbrains.anko.relativeLayout
import org.jetbrains.anko.rightOf
import org.jetbrains.anko.sameBottom
import org.jetbrains.anko.sameTop
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.startService
import org.jetbrains.anko.textView
import org.jetbrains.anko.wrapContent

/*class DownloadManagerItemView(injector: KodeinInjector) :
        RecyclerViewAdapterFactory.AnkoView<DownloadItem>() {
    private object _id {
        val cancel = ID.generate()
        val percent = ID.generate()
        val name = ID.generate()
        val progress = ID.generate()
        val bar = ID.generate()
    }

    private val act: DownloadManagerActivity by injector.instance()
    private val downloadManage by lazy { act.downloadManager }

    private lateinit var root: RelativeLayout
    private lateinit var name: TextView
    private lateinit var btn: ImageButton
    private lateinit var progressBar: ProgressBar
    private lateinit var progressSize: TextView
    private lateinit var progressCount: TextView
    private lateinit var progressPercent: TextView

    override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {
        relativeLayout {
            lparams(width = matchParent, height = wrapContent) {
                margin = dip(1)
            }

            // Кнопка отмены
            btn = imageButton {
                id = _id.cancel
                backgroundColor = Color.TRANSPARENT
                scaleType = CENTER_CROP
                padding = dip(4)
            }.lparams(width = dip(40), height = dip(40)) {
                alignParentRight()
                centerVertically()
                rightMargin = dip(5)
            }

            // Название
            name = textView {
                id = _id.name
                lines = 1
                textSize = 17f
            }.lparams(width = wrapContent, height = wrapContent) {
                alignParentLeft()
                sameTop(_id.cancel)
                marginEnd = dip(5)
                marginStart = dip(5)
                leftOf(_id.cancel)
            }

            // прогресс количеством
            progressCount = textView {
                id = _id.progress
                textSize = 13f
            }.lparams(width = wrapContent, height = wrapContent) {
                alignParentLeft()
                horizontalMargin = dip(5)
                bottomOf(_id.name)
            }

            progressSize = textView {
                textSize = 13f

            }.lparams(width = wrapContent, height = wrapContent) {
                horizontalMargin = dip(5)
                bottomOf(_id.name)
                rightOf(_id.progress)
            }

            // прогресс в процентах
            progressPercent = textView {
                id = _id.percent
            }.lparams(width = wrapContent, height = wrapContent) {
                leftOf(_id.cancel)
                sameBottom(_id.bar)
            }

            // прогрессбар
            progressBar = horizontalProgressBar {
                id = _id.bar
            }.lparams(width = wrapContent, height = wrapContent) {
                alignParentLeft()
                marginEnd = dip(5)
                marginStart = dip(5)
                leftOf(_id.percent)
                bottomOf(_id.progress)
            }

            root = this
        }
    }

    private var mainItem = DownloadItem()
    override fun bind(item: DownloadItem, isSelected: Boolean, position: Int) {
        mainItem = item
        name.text = item.name
        progressSize.text = act.getString(R.string.download_item_progress_size,
                                          formatDouble(bytesToMbytes(item.downloadSize)))

        changeStatus(item.status, item)

        progressCount.text = act.getString(R.string.download_item_progress_text,
                                           item.downloadPages,
                                           item.totalPages)
    }

    private fun changeStatus(downloadStatus: Int, item: DownloadItem) {
        when (downloadStatus) {
            DownloadStatus.pause -> {
                root.backgroundColor = Color.parseColor("#E6EE9C")

                progressBar.isIndeterminate = false

                btn.backgroundResource = R.drawable.ic_start
                btn.onClick {
                    act.startService<DownloadService>("item" to item)
                }
            }
            DownloadStatus.loading -> {
                root.backgroundColor = Color.parseColor("#FFF4F2F2")

                btn.backgroundResource = R.drawable.ic_stop
                btn.onClick {
                    downloadManage.pauseTask(item)
                }

                progressBar.visibility = View.VISIBLE
                progressBar.isIndeterminate = true
                progressPercent.text = "0 %"

                if (item.totalPages != 0) {
                    progressBar.isIndeterminate = false
                    progressBar.progress = item.downloadPages
                    progressBar.max = item.totalPages
                    progressPercent.text =
                            act.getString(R.string.download_item_progress_percent,
                                          item.downloadPages * 100 / item.totalPages)
                }
            }
            DownloadStatus.error -> {
                root.backgroundColor = Color.parseColor("#EF9A9A")

                progressBar.visibility = View.GONE
                progressPercent.visibility = View.GONE

                btn.backgroundResource = R.drawable.ic_update_black
                btn.onClick {
                    act.startService<DownloadService>("item" to item)
                }
            }
            DownloadStatus.completed -> {
                root.backgroundColor = Color.parseColor("#a5a2a2")

                val totalTime = item.totalTime / 1000
                if (totalTime < 60) {
                    progressSize.text = act.getString(R.string.download_item_final_size_with_sec,
                                                      formatDouble(bytesToMbytes(item.downloadSize)),
                                                      totalTime)
                } else {
                    val mins = totalTime / 60
                    val secs = totalTime % 60
                    progressSize.text = act.getString(R.string.download_item_final_size_with_min_and_sec,
                                                      formatDouble(bytesToMbytes(item.downloadSize)),
                                                      mins,
                                                      secs)
                }

                btn.visibility = View.GONE

                progressBar.visibility = View.GONE
                progressPercent.visibility = View.GONE
                progressCount.visibility = View.GONE
            }
        }
    }
}*/

class DownloadLoadingItemView(injector: KodeinInjector) :
        RecyclerViewAdapterFactory.AnkoView<DownloadItem>() {
    private object _id {
        val cancel = ID.generate()
        val percent = ID.generate()
        val name = ID.generate()
        val progress = ID.generate()
        val bar = ID.generate()
    }

    private val act: DownloadManagerActivity by injector.instance()
    private val downloadManage by lazy { act.downloadManager }

    private lateinit var name: TextView
    private lateinit var btn: ImageButton
    private lateinit var progressBar: ProgressBar
    private lateinit var progressSize: TextView
    private lateinit var progressCount: TextView
    private lateinit var progressPercent: TextView

    override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {
        relativeLayout {
            lparams(width = matchParent, height = wrapContent) {
                margin = dip(1)
            }
            backgroundColor = Color.parseColor("#FFF4F2F2")

            // Кнопка отмены
            btn = imageButton {
                id = _id.cancel
                backgroundColor = Color.TRANSPARENT
                scaleType = CENTER_CROP
                padding = dip(4)
                backgroundResource = R.drawable.ic_stop
            }.lparams(width = dip(40), height = dip(40)) {
                alignParentRight()
                centerVertically()
                rightMargin = dip(5)
            }

            // Название
            name = textView {
                id = _id.name
                lines = 1
                textSize = 17f
            }.lparams(width = wrapContent, height = wrapContent) {
                alignParentLeft()
                sameTop(_id.cancel)
                marginEnd = dip(5)
                marginStart = dip(5)
                leftOf(_id.cancel)
            }

            // прогресс количеством
            progressCount = textView {
                id = _id.progress
                textSize = 13f
            }.lparams(width = wrapContent, height = wrapContent) {
                alignParentLeft()
                horizontalMargin = dip(5)
                bottomOf(_id.name)
            }

            progressSize = textView {
                textSize = 13f

            }.lparams(width = wrapContent, height = wrapContent) {
                horizontalMargin = dip(5)
                bottomOf(_id.name)
                rightOf(_id.progress)
            }

            // прогресс в процентах
            progressPercent = textView {
                id = _id.percent
                text = "0 %"
            }.lparams(width = wrapContent, height = wrapContent) {
                leftOf(_id.cancel)
                sameBottom(_id.bar)
            }

            // прогрессбар
            progressBar = horizontalProgressBar {
                id = _id.bar
                isIndeterminate = true
            }.lparams(width = wrapContent, height = wrapContent) {
                alignParentLeft()
                marginEnd = dip(5)
                marginStart = dip(5)
                leftOf(_id.percent)
                bottomOf(_id.progress)
            }
        }
    }

    override fun bind(item: DownloadItem, isSelected: Boolean, position: Int) {
        name.text = item.name

        progressSize.text = act.getString(R.string.download_item_progress_size,
                                          formatDouble(bytesToMbytes(item.downloadSize)))

        progressCount.text = act.getString(R.string.download_item_progress_text,
                                           item.downloadPages,
                                           item.totalPages)

        btn.onClick {
            downloadManage.pauseTask(item)
        }

        if (item.totalPages != 0) {
            progressBar.isIndeterminate = false
            progressBar.progress = item.downloadPages
            progressBar.max = item.totalPages

            progressPercent.text =
                    act.getString(R.string.download_item_progress_percent,
                                  item.downloadPages * 100 / item.totalPages)
        }
    }
}

class DownloadPauseItemView :
        RecyclerViewAdapterFactory.AnkoView<DownloadItem>() {
    private object _id {
        val cancel = ID.generate()
        val percent = ID.generate()
        val name = ID.generate()
        val progress = ID.generate()
        val bar = ID.generate()
    }

    private lateinit var name: TextView
    private lateinit var btn: ImageButton
    private lateinit var progressBar: ProgressBar
    private lateinit var progressSize: TextView
    private lateinit var progressCount: TextView
    private lateinit var progressPercent: TextView

    override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {
        relativeLayout {
            lparams(width = matchParent, height = wrapContent) {
                margin = dip(1)
            }
            backgroundColor = Color.parseColor("#E6EE9C")

            btn = imageButton {
                id = _id.cancel
                backgroundColor = Color.TRANSPARENT
                scaleType = CENTER_CROP
                padding = dip(4)
                backgroundResource = R.drawable.ic_start
            }.lparams(width = dip(40), height = dip(40)) {
                alignParentRight()
                centerVertically()
                rightMargin = dip(5)
            }

            name = textView {
                id = _id.name
                lines = 1
                textSize = 17f
            }.lparams(width = wrapContent, height = wrapContent) {
                alignParentLeft()
                sameTop(_id.cancel)
                marginEnd = dip(5)
                marginStart = dip(5)
                leftOf(_id.cancel)
            }

            progressCount = textView {
                id = _id.progress
                textSize = 13f
            }.lparams(width = wrapContent, height = wrapContent) {
                alignParentLeft()
                horizontalMargin = dip(5)
                bottomOf(_id.name)
            }

            progressSize = textView {
                textSize = 13f

            }.lparams(width = wrapContent, height = wrapContent) {
                horizontalMargin = dip(5)
                bottomOf(_id.name)
                rightOf(_id.progress)
            }

            progressPercent = textView {
                id = _id.percent
            }.lparams(width = wrapContent, height = wrapContent) {
                leftOf(_id.cancel)
                sameBottom(_id.bar)
            }

            progressBar = horizontalProgressBar {
                id = _id.bar
                isIndeterminate = false
            }.lparams(width = wrapContent, height = wrapContent) {
                alignParentLeft()
                marginEnd = dip(5)
                marginStart = dip(5)
                leftOf(_id.percent)
                bottomOf(_id.progress)
            }
        }
    }

    override fun bind(item: DownloadItem, isSelected: Boolean, position: Int) {
        name.text = item.name

        val context = name.context

        progressSize.text = context.getString(R.string.download_item_progress_size,
                                          formatDouble(bytesToMbytes(item.downloadSize)))

        btn.onClick {
            context.startService<DownloadService>("item" to item)
        }

        progressCount.text = context.getString(R.string.download_item_progress_text,
                                           item.downloadPages,
                                           item.totalPages)
    }
}

class DownloadErrorItemView :
        RecyclerViewAdapterFactory.AnkoView<DownloadItem>() {
    private object _id {
        val cancel = ID.generate()
        val name = ID.generate()
        val progress = ID.generate()
    }

    private lateinit var name: TextView
    private lateinit var btn: ImageButton
    private lateinit var progressSize: TextView
    private lateinit var progressCount: TextView

    override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {
        relativeLayout {
            lparams(width = matchParent, height = wrapContent) {
                margin = dip(1)
            }
            backgroundColor = Color.parseColor("#EF9A9A")

            btn = imageButton {
                id = _id.cancel
                backgroundColor = Color.TRANSPARENT
                scaleType = CENTER_CROP
                padding = dip(4)
                backgroundResource = R.drawable.ic_update_black
            }.lparams(width = dip(40), height = dip(40)) {
                alignParentRight()
                centerVertically()
                rightMargin = dip(5)
            }

            name = textView {
                id = _id.name
                lines = 1
                textSize = 17f
            }.lparams(width = wrapContent, height = wrapContent) {
                alignParentLeft()
                sameTop(_id.cancel)
                marginEnd = dip(5)
                marginStart = dip(5)
                leftOf(_id.cancel)
            }

            progressCount = textView {
                id = _id.progress
                textSize = 13f
            }.lparams(width = wrapContent, height = wrapContent) {
                alignParentLeft()
                horizontalMargin = dip(5)
                bottomOf(_id.name)
            }

            progressSize = textView {
                textSize = 13f

            }.lparams(width = wrapContent, height = wrapContent) {
                horizontalMargin = dip(5)
                bottomOf(_id.name)
                rightOf(_id.progress)
            }
        }
    }

    override fun bind(item: DownloadItem, isSelected: Boolean, position: Int) {
        name.text = item.name
        val context = name.context

        progressSize.text = context.getString(R.string.download_item_progress_size,
                                          formatDouble(bytesToMbytes(item.downloadSize)))

        progressCount.text = context.getString(R.string.download_item_progress_text,
                                           item.downloadPages,
                                           item.totalPages)

        btn.onClick {
            context.startService<DownloadService>("item" to item)
        }
    }
}

class DownloadCompleteItemView :
        RecyclerViewAdapterFactory.AnkoView<DownloadItem>() {
    private object _id {
        val name = ID.generate()
    }

    private lateinit var name: TextView
    private lateinit var progressSize: TextView

    override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {
        relativeLayout {
            lparams(width = matchParent, height = wrapContent) {
                margin = dip(1)
            }
            backgroundColor = Color.parseColor("#a5a2a2")

            name = textView {
                id = _id.name
                lines = 1
                textSize = 17f
            }.lparams(width = matchParent, height = wrapContent) {
                alignParentLeft()
                marginEnd = dip(5)
                marginStart = dip(5)
            }

            progressSize = textView {
                textSize = 13f
            }.lparams(width = wrapContent, height = wrapContent) {
                horizontalMargin = dip(5)
                bottomOf(_id.name)
            }
        }
    }

    override fun bind(item: DownloadItem, isSelected: Boolean, position: Int) {
        name.text = item.name

        val context = name.context

        val totalTime = item.totalTime / 1000
        if (totalTime < 60) {
            progressSize.text =
                    context.getString(R.string.download_item_final_size_with_sec,
                                      formatDouble(bytesToMbytes(item.downloadSize)),
                                      totalTime)
        } else {
            val mins = totalTime / 60
            val secs = totalTime % 60
            progressSize.text =
                    context.getString(R.string.download_item_final_size_with_min_and_sec,
                                      formatDouble(bytesToMbytes(item.downloadSize)),
                                      mins,
                                      secs)
        }
    }
}
