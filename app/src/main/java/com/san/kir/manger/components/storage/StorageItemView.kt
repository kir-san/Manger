package com.san.kir.manger.components.storage

import android.arch.lifecycle.Observer
import android.graphics.Color
import android.support.v4.content.ContextCompat
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import com.san.kir.manger.R
import com.san.kir.manger.extending.anko_extend.onClick
import com.san.kir.manger.extending.anko_extend.roundedImageView
import com.san.kir.manger.extending.anko_extend.visibleOrGone
import com.san.kir.manger.extending.anko_extend.visibleOrInvisible
import com.san.kir.manger.room.models.Manga
import com.san.kir.manger.room.models.Storage
import com.san.kir.manger.utils.ID
import com.san.kir.manger.utils.RecyclerViewAdapterFactory
import com.san.kir.manger.utils.formatDouble
import com.san.kir.manger.utils.getFullPath
import com.san.kir.manger.utils.loadImage
import com.san.kir.manger.utils.log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.above
import org.jetbrains.anko.alert
import org.jetbrains.anko.alignParentEnd
import org.jetbrains.anko.below
import org.jetbrains.anko.centerVertically
import org.jetbrains.anko.dip
import org.jetbrains.anko.horizontalPadding
import org.jetbrains.anko.horizontalProgressBar
import org.jetbrains.anko.leftOf
import org.jetbrains.anko.leftPadding
import org.jetbrains.anko.margin
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.padding
import org.jetbrains.anko.relativeLayout
import org.jetbrains.anko.rightOf
import org.jetbrains.anko.textView
import org.jetbrains.anko.wrapContent
import kotlin.math.roundToInt

class StorageItemView(private val act: StorageActivity) :
    RecyclerViewAdapterFactory.AnkoView<Storage>() {

    private lateinit var root: RelativeLayout
    private lateinit var logo: ImageView
    private lateinit var name: TextView
    private lateinit var sizeText: TextView
    private lateinit var isExists: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var percent: TextView
    private lateinit var item: Storage

    private lateinit var observer: Observer<Double>

    private var job: Job? = null
    private var job2: Job? = null

    override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {
        relativeLayout {
            lparams(width = matchParent, height = dip(84)) { margin = dip(4) }

//            backgroundColor = Color.parseColor("#FFF4F2F2")
            padding = dip(2)

            logo = roundedImageView {
                id = ID.generate()
            }.lparams(width = dip(80), height = dip(80))

            sizeText = textView {
                id = ID.generate()
                textSize = 15f
                padding = dip(2)
                leftPadding = dip(5)
            }.lparams {
                centerVertically()
                rightOf(logo)
            }

            name = textView {
                id = ID.generate()
                textSize = 20f
                padding = dip(2)
                gravity = Gravity.CENTER_HORIZONTAL
                maxLines = 1
            }.lparams(width = matchParent) {
                above(sizeText)
                rightOf(logo)
            }

            isExists = textView(R.string.storage_not_in_bd) {
                textSize = 15f
                padding = dip(2)
            }.lparams {
                below(name)
                alignParentEnd()
            }

            percent = textView {
                id = ID.generate()
                gravity = Gravity.CENTER_HORIZONTAL
                textSize = 16f
            }.lparams(height = wrapContent, width = wrapContent) {
                below(sizeText)
                alignParentEnd()
            }

            progressBar = horizontalProgressBar {
                progressDrawable = ContextCompat.getDrawable(
                    this@with.ctx,
                    R.drawable.storage_progressbar
                )
                horizontalPadding = dip(3)
            }.lparams(height = dip(10), width = matchParent) {
                below(sizeText)
                rightOf(logo)
                leftOf(percent)
                topMargin = dip(5)
            }

            root = this
        }
    }

    override fun bind(item: Storage, isSelected: Boolean, position: Int) {
        this.item = item

        name.text = item.name
        sizeText.text = act.getString(
            R.string.storage_manga_item_size_text,
            formatDouble(item.sizeFull)
        )

        observer = Observer {
            val size = it?.roundToInt() ?: 0
            progressBar.max = size
            progressBar.progress = item.sizeFull.roundToInt()
            progressBar.secondaryProgress = item.sizeRead.roundToInt()

            if (size != 0) {
                percent.text = act.getString(
                    R.string.storage_manga_item_size_percent,
                    Math.round(item.sizeFull / size * 100)
                )
            }
        }
    }

    override fun onAttached() {
        job2 = act.launch(act.coroutineContext) {
            val manga = act.mViewModel.getMangaFromPath(item.path)

            withContext(Dispatchers.Main) {
                isExists.visibleOrGone(manga == null)
                root.onClick {
                    it?.menuOfActions(manga, item)
                }
            }

            manga?.let {
                if (it.logo.isNotEmpty()) {
                    job = loadImage(it.logo)
                        .errorColor(Color.TRANSPARENT)
                        .into(logo)

                } else logo.visibleOrInvisible(false)
            }
        }

        act.mViewModel.getStorageAllSize().observe(act, observer)
    }

    override fun onDetached() {
        root.setOnClickListener(null)

        act.mViewModel.getStorageAllSize().removeObserver(observer)

        job?.cancel()
        job2?.cancel()
    }

    private fun View.menuOfActions(manga: Manga?, item: Storage) {
        if (manga != null) {
            StorageDialogView(act).bind(manga)
        } else
            with(PopupMenu(act, this, Gravity.END)) {
                menu.add(0, 2, 0, R.string.storage_item_menu_full_delete)

                setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        2 -> {
                            context.alert {
                                messageResource = R.string.storage_item_alert_message
                                positiveButton(R.string.storage_item_alert_positive) {
                                    act.launch(act.coroutineContext) {
                                        getFullPath(item.path).deleteRecursively()
                                        act.mViewModel.storageDelete(item)
                                    }
                                }
                                negativeButton(R.string.storage_item_alert_negative) {
                                    log("")
                                }
                                show()
                            }
                        }
                    }
                    return@setOnMenuItemClickListener true
                }
                show()
            }
    }
}
