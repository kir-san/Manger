package com.san.kir.manger.components.DownloadManager

import android.arch.lifecycle.Observer
import android.graphics.Color
import android.support.v7.widget.LinearLayoutManager
import android.view.Gravity
import android.view.View
import android.view.ViewManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.san.kir.manger.Extending.AnkoExtend.expandableFrameLayout
import com.san.kir.manger.Extending.Views.ExpandableFrameLayout
import com.san.kir.manger.R
import com.san.kir.manger.components.Main.Main
import com.san.kir.manger.room.DAO.delete
import com.san.kir.manger.room.models.DownloadStatus
import org.jetbrains.anko.alert
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.dip
import org.jetbrains.anko.frameLayout
import org.jetbrains.anko.imageView
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.noButton
import org.jetbrains.anko.okButton
import org.jetbrains.anko.padding
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.startService
import org.jetbrains.anko.support.v4.nestedScrollView
import org.jetbrains.anko.textAppearance
import org.jetbrains.anko.textView
import org.jetbrains.anko.verticalLayout
import org.jetbrains.anko.wrapContent

class DownloadManagerView(private val act: DownloadManagerActivity) {

    private val downloadManager by lazy { act.downloadManager }
    private val dao = Main.db.downloadDao

    private val loadColor = "#FFF4F2F2"
    private val pauseColor = "#E6EE9C"
    private val errorColor = "#EF9A9A"
    private val completeColor = "#A5A2A2"

    private val loads = dao.loadLoadingDownloads()
    private val pause = dao.loadPauseDownloads()
    private val errors = dao.loadErrorDownloads()
    private val complete = dao.loadCompleteDownloads()

    private fun loadText(size: Int?) = "Загружается $size"
    private fun pauseText(size: Int?) = "Приостановлено $size"
    private fun errorText(size: Int?) = "Остановленно с ошибкой $size"
    private fun completeText(size: Int?) = "Загруженно $size"

    private val btnSize = 35

    fun view(view: LinearLayout): View = with(view) {
        nestedScrollView {
            verticalLayout {
                // Загружаемые
                expandBlock(loadColor,
                            { loads.observe(act, Observer { text = loadText(it?.size) }) },
                            loadingAdapter(act)) {
                    stopBtn {
                        downloadManager.pauseAllTask()
                    }
                }

                // Приостановленные
                expandBlock(pauseColor,
                            { pause.observe(act, Observer { text = pauseText(it?.size) }) },
                            pauseAdapter(act)) {
                    startBtn {
                        dao.loadItems()
                                .filter { it.status == DownloadStatus.pause }
                                .forEach { act.startService<DownloadService>("item" to it) }
                    }
                    deleteBtn {
                        dao.loadItems()
                                .filter { it.status == DownloadStatus.pause }
                                .forEach { dao.delete(it) }
                    }
                }

                // Остановленные с ошибкой
                expandBlock(errorColor,
                            { errors.observe(act, Observer { text = errorText(it?.size) }) },
                            errorAdapter(act)) {
                    startBtn {
                        dao.loadItems()
                                .filter { it.status == DownloadStatus.error }
                                .forEach { act.startService<DownloadService>("item" to it) }
                    }
                    deleteBtn {
                        dao.loadItems()
                                .filter { it.status == DownloadStatus.error }
                                .forEach { dao.delete(it) }
                    }
                }

                // Завершенные
                expandBlock(completeColor,
                            { complete.observe(act, Observer { text = completeText(it?.size) }) },
                            completeAdapter(act)) {
                    deleteBtn {
                        dao.loadItems()
                                .filter { it.status == DownloadStatus.completed }
                                .forEach { dao.delete(it) }
                    }
                }
            }
        }
    }

    private fun ViewManager.expandBlock(color: String,
                                        init: TextView.() -> Unit,
                                        adapter: DownloadManagerRecyclerPresenter,
                                        function: FrameLayout.() -> Unit) {
        var expand: ExpandableFrameLayout? = null

        verticalLayout {
            frameLayout {
                textView {
                    padding = dip(12)
                    gravity = Gravity.CENTER_HORIZONTAL
                    textAppearance = android.support.design.R.style.TextAppearance_AppCompat_Medium
                    backgroundColor = Color.parseColor(color)
                    init()
                    onClick {
                        expand?.toggle(true)
                    }
                }.lparams(width = matchParent, height = wrapContent)

                function()
            }
            expand = expandableFrameLayout {
                isExpanded = false
                duration = 300
                setParallax(0.5F)
                recyclerView {
                    layoutManager = LinearLayoutManager(context)
                    adapter.into(this)
                }
            }
        }
    }

    private fun FrameLayout.deleteBtn(action: () -> Unit): ImageView {
        return imageView {
            backgroundResource = R.drawable.ic_action_delete_black
            onClick {
                this@deleteBtn.context.alert {
                    title = "Очистить выбранную группу?"
                    okButton {
                        action()
                    }
                    noButton { }
                }.show()
            }
            scaleType = ImageView.ScaleType.FIT_XY
            padding = dip(5)
            layoutParams = FrameLayout.LayoutParams(dip(btnSize), dip(btnSize)).apply {
                gravity = Gravity.CENTER_VERTICAL or Gravity.END
                marginEnd = dip(9)
            }
        }
    }

    private fun FrameLayout.startBtn(action: () -> Unit): ImageView {
        return imageView {
            backgroundResource = R.drawable.ic_start
            onClick {
                this@startBtn.context.alert {
                    title = "Возобновить закачки из выбранной группы?"
                    okButton {
                        action()
                    }
                    noButton { }
                }.show()
            }
            scaleType = ImageView.ScaleType.FIT_XY
            padding = dip(5)
            layoutParams = FrameLayout.LayoutParams(dip(btnSize), dip(btnSize)).apply {
                gravity = Gravity.CENTER_VERTICAL or Gravity.START
                marginStart = dip(9)
            }
        }
    }

    private fun FrameLayout.stopBtn(action: () -> Unit): ImageView {
        return imageView {
            backgroundResource = R.drawable.ic_stop
            onClick {
                action()
            }
            scaleType = ImageView.ScaleType.FIT_XY
            padding = dip(5)
            layoutParams = FrameLayout.LayoutParams(dip(btnSize), dip(btnSize)).apply {
                gravity = Gravity.CENTER_VERTICAL or Gravity.START
                marginStart = dip(9)
            }
        }
    }
}
