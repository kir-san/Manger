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
import com.san.kir.manger.components.main.Main
import com.san.kir.manger.extending.ankoExtend.onClick
import com.san.kir.manger.extending.ankoExtend.roundedImageView
import com.san.kir.manger.extending.ankoExtend.visibleOrGone
import com.san.kir.manger.extending.ankoExtend.visibleOrInvisible
import com.san.kir.manger.room.dao.getFromPath
import com.san.kir.manger.room.dao.loadAllSize
import com.san.kir.manger.room.models.Manga
import com.san.kir.manger.room.models.Storage
import com.san.kir.manger.utils.ID
import com.san.kir.manger.utils.RecyclerViewAdapterFactory
import com.san.kir.manger.utils.formatDouble
import com.san.kir.manger.utils.getFullPath
import com.san.kir.manger.utils.loadImage
import com.san.kir.manger.utils.log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.alert
import org.jetbrains.anko.alignParentBottom
import org.jetbrains.anko.alignParentEnd
import org.jetbrains.anko.below
import org.jetbrains.anko.dip
import org.jetbrains.anko.horizontalPadding
import org.jetbrains.anko.horizontalProgressBar
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
    private val mangaDao = Main.db.mangaDao
    private val storage = Main.db.storageDao

    private object Id {
        val name = ID.generate()
        val logo = ID.generate()
    }

    private lateinit var root: RelativeLayout
    private lateinit var logo: ImageView
    private lateinit var name: TextView
    private lateinit var sizeText: TextView
    private lateinit var isExists: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var percent: TextView

    override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {
        relativeLayout {
            lparams(width = matchParent, height = dip(84)) { margin = dip(4) }

//            backgroundColor = Color.parseColor("#FFF4F2F2")
            padding = dip(2)

            logo = roundedImageView {
                id = Id.logo
//                scaleType = ImageView.ScaleType.FIT_XY
            }.lparams(width = dip(80), height = dip(80))

            name = textView {
                id = Id.name
                textSize = 20f
                padding = dip(2)
                gravity = Gravity.CENTER_HORIZONTAL
                maxLines = 1
            }.lparams(width = matchParent) { rightOf(Id.logo) }

            sizeText = textView {
                textSize = 15f
                padding = dip(2)
                leftPadding = dip(5)
            }.lparams {
                below(Id.name)
                rightOf(Id.logo)
            }

            isExists = textView(R.string.storage_not_in_bd) {
                textSize = 15f
                padding = dip(2)
            }.lparams {
                below(Id.name)
                alignParentEnd()
            }

            progressBar = horizontalProgressBar {
                progressDrawable = ContextCompat.getDrawable(
                    this@with.ctx,
                    R.drawable.storage_progressbar
                )
                horizontalPadding = dip(3)
            }.lparams(height = dip(21), width = matchParent) {
                alignParentBottom()
                rightOf(Id.logo)
            }

            percent = textView {
                gravity = Gravity.CENTER_HORIZONTAL
                textSize = 15f
            }.lparams(height = wrapContent, width = matchParent) {
                alignParentBottom()
                rightOf(Id.logo)
            }

            root = this
        }
    }

    override fun bind(item: Storage, isSelected: Boolean, position: Int) {
        act.launch(act.coroutineContext) {
            val context = root.context
            val manga = mangaDao.getFromPath(item.path)

            withContext(Dispatchers.Main) {
                root.onClick { it?.menuOfActions(manga, item) }

                if (manga != null && manga.logo.isNotEmpty()) {
                    loadImage(manga.logo) {
                        errorColor(Color.TRANSPARENT)
                        into(logo)
                    }
                } else logo.visibleOrInvisible(false)

                name.text = item.name
                sizeText.text = context.getString(
                    R.string.storage_manga_item_size_text,
                    formatDouble(item.sizeFull)
                )
                isExists.visibleOrGone(manga == null)
            }

            Main.db.storageDao
                .loadAllSize()
                .observe(act, Observer {
                    act.launch(Dispatchers.Main) {
                        val size = it?.roundToInt() ?: 0
                        progressBar.max = size
                        progressBar.progress = item.sizeFull.roundToInt()
                        progressBar.secondaryProgress = item.sizeRead.roundToInt()

                        if (size != 0) {
                            percent.text = context.getString(
                                R.string.storage_manga_item_size_percent,
                                Math.round(item.sizeFull / size * 100)
                            )
                        }
                    }
                })
        }
    }

    private fun View.menuOfActions(manga: Manga?, item: Storage) {
        if (manga != null) {
            StorageDialogFragment().apply {
                bind(manga, act)
                show(act.supportFragmentManager, "storage")
            }
        } else
            with(PopupMenu(context, this, Gravity.END)) {
                menu.add(0, 2, 0, R.string.storage_item_menu_full_delete)

                setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        2 -> {
                            context.alert {
                                messageResource = R.string.storage_item_alert_message
                                positiveButton(R.string.storage_item_alert_positive) {
                                    act.launch(act.coroutineContext) {
                                        getFullPath(item.path).deleteRecursively()
                                        storage.delete(item)
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
