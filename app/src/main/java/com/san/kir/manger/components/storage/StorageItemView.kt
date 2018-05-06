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
import com.san.kir.manger.components.addManga.AddMangaActivity
import com.san.kir.manger.components.main.Main
import com.san.kir.manger.extending.ankoExtend.roundedImageView
import com.san.kir.manger.extending.ankoExtend.visibleOrGone
import com.san.kir.manger.room.dao.deleteAsync
import com.san.kir.manger.room.dao.getFromPath
import com.san.kir.manger.room.dao.loadAllSize
import com.san.kir.manger.room.models.Manga
import com.san.kir.manger.room.models.Storage
import com.san.kir.manger.utils.ID
import com.san.kir.manger.utils.RecyclerViewAdapterFactory
import com.san.kir.manger.utils.formatDouble
import com.san.kir.manger.utils.getFullPath
import com.san.kir.manger.utils.onError
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.alert
import org.jetbrains.anko.alignParentBottom
import org.jetbrains.anko.alignParentEnd
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.below
import org.jetbrains.anko.dip
import org.jetbrains.anko.horizontalPadding
import org.jetbrains.anko.horizontalProgressBar
import org.jetbrains.anko.margin
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.padding
import org.jetbrains.anko.relativeLayout
import org.jetbrains.anko.rightOf
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.startActivity
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
        async(UI) {
            val context = root.context
            val manga = async {mangaDao.getFromPath(item.path)}.await()

            root.onClick { it?.menuOfActions(manga, item) }

            if (manga != null) {
                if (manga.logo.isNotEmpty())
                    Picasso.with(logo.context)
                        .load(manga.logo)
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .into(logo, onError {
                            Picasso.with(logo.context)
                                .load(manga.logo)
                                .into(logo, onError {
                                    logo.backgroundColor = Color.TRANSPARENT
                                })
                        })
            } else logo.visibility = View.INVISIBLE

            name.text = item.name
            sizeText.text = context.getString(
                R.string.storage_manga_item_size_text,
                formatDouble(item.sizeFull)
            )
            isExists.visibleOrGone(manga == null)

            Main.db.storageDao
                .loadAllSize()
                .observe(act, Observer {
                    launch(UI) {
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
        with(PopupMenu(context, this, Gravity.END)) {
            if (manga != null) {
                menu.add(0, 0, 0, "Подробнее")
            } else {
                menu.add(0, 1, 0, "Добавить в библиотеку")
                menu.add(0, 2, 0, "Удалить совсем")
            }

            setOnMenuItemClickListener {
                when (it.itemId) {
                    0 -> {
                        StorageDialogFragment().apply {
                            bind(manga!!, act)
                            show(act.supportFragmentManager, "storage")
                        }
                    }
                    1 -> {
                        context.startActivity<AddMangaActivity>(Storage::class.java.canonicalName to item)
                    }
                    2 -> {
                        context.alert {
                            message = "Вы действительно хотите удалить?"
                            positiveButton("Да, конечно") {
                                getFullPath(item.path).deleteRecursively()
                                storage.deleteAsync(item)
                            }
                            negativeButton("Нет") {}
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
