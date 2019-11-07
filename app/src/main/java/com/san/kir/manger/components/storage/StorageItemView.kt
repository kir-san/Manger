package com.san.kir.manger.components.storage

import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.san.kir.ankofork.AnkoContext
import com.san.kir.ankofork.dialogs.alert
import com.san.kir.ankofork.dialogs.longToast
import com.san.kir.ankofork.dip
import com.san.kir.ankofork.horizontalProgressBar
import com.san.kir.ankofork.margin
import com.san.kir.ankofork.matchParent
import com.san.kir.ankofork.sdk28.linearLayout
import com.san.kir.ankofork.sdk28.onClick
import com.san.kir.ankofork.sdk28.textView
import com.san.kir.ankofork.verticalLayout
import com.san.kir.ankofork.verticalMargin
import com.san.kir.manger.R
import com.san.kir.manger.room.entities.Manga
import com.san.kir.manger.room.entities.Storage
import com.san.kir.manger.utils.RecyclerViewAdapterFactory
import com.san.kir.manger.utils.extensions.formatDouble
import com.san.kir.manger.utils.extensions.getFullPath
import com.san.kir.manger.utils.extensions.log
import com.san.kir.manger.utils.extensions.roundedImageView
import com.san.kir.manger.utils.extensions.visibleOrGone
import com.san.kir.manger.utils.extensions.visibleOrInvisible
import com.san.kir.manger.utils.loadImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

class StorageItemView(private val act: StorageActivity) :
    RecyclerViewAdapterFactory.AnkoView<Storage>() {

    private lateinit var root: LinearLayout
    private lateinit var logo: ImageView
    private lateinit var name: TextView
    private lateinit var sizeText: TextView
    private lateinit var isExists: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var item: Storage

    private var job: Job? = null
    private var job2: Job? = null

    override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {
        linearLayout {
            lparams(width = matchParent)

            gravity = Gravity.CENTER_VERTICAL

            logo = roundedImageView {
            }.lparams(width = dip(55), height = dip(55)) {
                margin = dip(16)
            }

            verticalLayout {
                name = textView {
                    textSize = 17f
                    maxLines = 1
                }

                linearLayout {
                    sizeText = textView {
                        textSize = 14f
                    }.lparams(width = matchParent) {
                        weight = 1f
                    }

                    isExists = textView(R.string.storage_not_in_bd) {
                        textSize = 15f
                        visibleOrGone(false)
                    }


                }.lparams(width = matchParent)

                progressBar = horizontalProgressBar {
                    progressDrawable = ContextCompat.getDrawable(
                        this@with.ctx, R.drawable.storage_progressbar
                    )
                }.lparams(height = dip(10), width = matchParent) {
                    verticalMargin = dip(6)
                }

            }.lparams(width = matchParent) {
                weight = 1f
                marginEnd = dip(16)
            }

            root = this
        }
    }

    override fun bind(item: Storage, isSelected: Boolean, position: Int) {
        this.item = item

        name.text = item.name
        sizeText.text = act.getString(
            R.string.storage_manga_item_size_text,
            formatDouble(item.sizeFull),
            0
        )

        job2 = act.lifecycleScope.launch(Dispatchers.Main) {
            val manga =
                withContext(Dispatchers.Default) { act.mViewModel.getMangaFromPath(item.path) }

            isExists.visibleOrGone(manga == null)
            root.onClick {
                it?.menuOfActions(manga, item)
            }

            manga?.let {
                if (it.logo.isNotEmpty()) {
                    job = loadImage(it.logo)
                        .errorColor(Color.TRANSPARENT)
                        .into(logo)

                } else logo.visibleOrInvisible(false)
            }


            val size = withContext(Dispatchers.Default) { act.mViewModel.allSize().roundToInt() }
            progressBar.max = size
            progressBar.progress = item.sizeFull.roundToInt()
            progressBar.secondaryProgress = item.sizeRead.roundToInt()

            log("size = ${size}")

            if (size != 0) {
                sizeText.text = act.getString(
                    R.string.storage_manga_item_size_text,
                    formatDouble(item.sizeFull),
                    (item.sizeFull / size * 100).roundToInt()
                )
            }
        }
    }

    override fun onDetached() {
        root.setOnClickListener(null)

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
                                    act.lifecycleScope.launch(Dispatchers.Main) {
                                        runCatching {
                                            withContext(Dispatchers.Default) {
                                                act.mViewModel.storageDelete(item)
                                                getFullPath(item.path).deleteRecursively()
                                            }
                                        }.onFailure {
                                            act.longToast(it.toString())
                                        }
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
