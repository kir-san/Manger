package com.san.kir.manger.components.Storage

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
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.instance
import com.san.kir.manger.App.Companion.context
import com.san.kir.manger.R
import com.san.kir.manger.components.AddManga.AddMangaActivity
import com.san.kir.manger.components.Main.Main
import com.san.kir.manger.photoview.onError
import com.san.kir.manger.picasso.NetworkPolicy
import com.san.kir.manger.picasso.Picasso
import com.san.kir.manger.room.DAO.delete
import com.san.kir.manger.room.DAO.getFromPath
import com.san.kir.manger.room.DAO.loadAllSize
import com.san.kir.manger.room.models.Manga
import com.san.kir.manger.room.models.Storage
import com.san.kir.manger.utils.ID
import com.san.kir.manger.utils.RecyclerViewAdapterFactory
import com.san.kir.manger.utils.formatDouble
import com.san.kir.manger.utils.getFullPath
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.alert
import org.jetbrains.anko.alignParentBottom
import org.jetbrains.anko.alignParentEnd
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.below
import org.jetbrains.anko.dip
import org.jetbrains.anko.horizontalPadding
import org.jetbrains.anko.horizontalProgressBar
import org.jetbrains.anko.imageView
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

class StorageItemView(injector: KodeinInjector) : RecyclerViewAdapterFactory.AnkoView<Storage>() {
    private val act: StorageActivity by injector.instance()
    private val mangas = Main.db.mangaDao
    private val storage = Main.db.storageDao

    private object _id {
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
            backgroundColor = Color.parseColor("#FFF4F2F2")
            padding = dip(2)

            logo = imageView {
                id = _id.logo
                scaleType = ImageView.ScaleType.FIT_XY
            }.lparams(width = dip(80), height = matchParent)

            name = textView {
                id = _id.name
                textSize = 20f
                padding = dip(2)
                gravity = Gravity.CENTER_HORIZONTAL
                maxLines = 1
            }.lparams(width = matchParent) { rightOf(_id.logo) }

            sizeText = textView {
                textSize = 16f
                padding = dip(2)
            }.lparams {
                below(_id.name)
                rightOf(_id.logo)
            }

            isExists = textView(R.string.storage_not_in_bd) {
                textSize = 16f
                padding = dip(2)
            }.lparams {
                below(_id.name)
                alignParentEnd()
            }

            progressBar = horizontalProgressBar {
                progressDrawable = ContextCompat.getDrawable(this@with.ctx,
                                                             R.drawable.storage_progressbar)
                horizontalPadding = dip(3)
            }.lparams(height = dip(22), width = matchParent) {
                alignParentBottom()
                rightOf(_id.logo)
            }

            percent = textView {
                gravity = Gravity.CENTER_HORIZONTAL
                textSize = 16f
            }.lparams(height = wrapContent, width = matchParent) {
                alignParentBottom()
                rightOf(_id.logo)
            }

            lparams(width = matchParent, height = dip(84)) { margin = dip(1) }

            root = this
        }
    }

    override fun bind(item: Storage, isSelected: Boolean, position: Int) {
        val manga = mangas.getFromPath(item.path)

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
            else
                logo.backgroundResource = manga.color
        } else logo.visibility = View.INVISIBLE

        name.text = item.name
        sizeText.text = context.getString(R.string.storage_manga_item_size_text,
                                          formatDouble(item.sizeFull))
        isExists.visibility = if (manga == null) View.VISIBLE else View.GONE

        Main.db.storageDao
                .loadAllSize()
                .observe(act, Observer {
                    val size = it?.roundToInt() ?: 0
                    progressBar.max = size
                    progressBar.progress = item.sizeFull.roundToInt()
                    progressBar.secondaryProgress = item.sizeRead.roundToInt()
                    if (size != 0)
                        percent.text = context.getString(R.string.storage_manga_item_size_percent,
                                                         Math.round(item.sizeFull / size * 100))
                })
    }

    private fun View.menuOfActions(manga: Manga?,
                                   item: Storage) {
        with(PopupMenu(context,
                       this,
                       Gravity.END)) {
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
                                storage.delete(item)
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
