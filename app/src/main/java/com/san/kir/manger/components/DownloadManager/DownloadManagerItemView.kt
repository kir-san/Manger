package com.san.kir.manger.components.DownloadManager

import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView.ScaleType.CENTER_CROP
import com.san.kir.manger.EventBus.Binder
import com.san.kir.manger.Extending.AnkoExtend.bind
import com.san.kir.manger.R
import com.san.kir.manger.components.ChaptersDownloader.ChaptersDownloader
import com.san.kir.manger.dbflow.models.DownloadItem
import com.san.kir.manger.utils.ID
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.alignParentLeft
import org.jetbrains.anko.alignParentRight
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.bottomOf
import org.jetbrains.anko.centerVertically
import org.jetbrains.anko.dip
import org.jetbrains.anko.horizontalProgressBar
import org.jetbrains.anko.imageButton
import org.jetbrains.anko.leftOf
import org.jetbrains.anko.lines
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.relativeLayout
import org.jetbrains.anko.sameBottom
import org.jetbrains.anko.sameTop
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.textView
import org.jetbrains.anko.wrapContent

class DownloadManagerItemView(private val adapter: DownloadManagerAdapter) :
        AnkoComponent<ViewGroup> {
    private object _id {
        val cancel = ID.generate()
        val percent = ID.generate()
        val name = ID.generate()
        val progress: Int = ID.generate()
        val bar = ID.generate()
    }

    val name = Binder("")
    var progressBinder = Binder(0)
    var maxBinder = 0
    var item: DownloadItem? = null

    fun createView(parent: ViewGroup): View {
        return createView(AnkoContext.create(parent.context, parent))
    }

    override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {
        relativeLayout {
            lparams(width = matchParent, height = wrapContent)

            // Кнопка отмены
            imageButton {
                id = _id.cancel

                backgroundColor = Color.TRANSPARENT
                isClickable = true
                scaleType = CENTER_CROP
                backgroundResource = R.drawable.ic_cancel_black

                onClick {
                    item?.let {
                        // На нажатие кнопки отменить загрузку и удалить элемент из списка
                        ChaptersDownloader.cancelTask(
                                item!!)
                        adapter.removeItem(item!!)
                    }
                }
            }.lparams(width = dip(40), height = dip(40)) {
                alignParentRight()
                centerVertically()
                rightMargin = dip(5)
            }

            // Название
            textView {
                id = _id.name
                lines = 1
                textSize = 17f
                bind(name) { text = it }
            }.lparams(width = wrapContent, height = wrapContent) {
                alignParentLeft()
                sameTop(_id.cancel)
                marginEnd = dip(5)
                marginStart = dip(5)
                leftOf(_id.cancel)
            }

            // прогресс количеством
            textView {
                id = _id.progress
                bind(progressBinder) { post { text = "загруженно $it из $maxBinder" } }
                textSize = 13f
            }.lparams(width = wrapContent, height = wrapContent) {
                alignParentLeft()
                marginStart = dip(5)
                bottomOf(_id.name)
            }

            // прогресс в процентах
            textView {
                id = _id.percent
                bind(progressBinder) {
                    try {
                        post { text = "${it * 100 / maxBinder} %" }
                    } catch (ex: Exception) {
                        text = "-1 %"
                    }
                }
            }.lparams(width = wrapContent, height = wrapContent) {
                leftOf(_id.cancel)
                sameBottom(_id.bar)
            }

            // прогрессбар
            horizontalProgressBar {
                id = _id.bar
                lparams(width = wrapContent, height = wrapContent) {
                    alignParentLeft()
                    marginEnd = dip(5)
                    marginStart = dip(5)
                    leftOf(_id.percent)
                    bottomOf(_id.progress)
                }
//                bind(maxBinder) { post { max = it } }
                bind(progressBinder) {
                    post {
                        progress = it
                        max = maxBinder
                    }
                }
            }
        }
    }

    fun bind(item: DownloadItem) {
        ChaptersDownloader.bus.onEvent(1) {
            if (it.link == item.link)
                adapter.removeItem(item)
        }

        this.item = item
        name.item = item.name
        item.progress.bind(0) { progressBinder.item = it }
        item.max.bind(0) { maxBinder = it }


    }
}
