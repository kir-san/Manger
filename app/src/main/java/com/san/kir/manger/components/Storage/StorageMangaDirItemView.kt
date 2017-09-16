package com.san.kir.manger.components.Storage

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.graphics.Color
import android.support.v4.content.ContextCompat
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import com.san.kir.manger.EventBus.Binder
import com.san.kir.manger.EventBus.BinderRx
import com.san.kir.manger.Extending.AnkoExtend.bind
import com.san.kir.manger.R
import com.san.kir.manger.components.AddManga.AddMangaActivity
import com.san.kir.manger.dbflow.models.Manga
import com.san.kir.manger.dbflow.wrapers.MangaWrapper
import com.san.kir.manger.picasso.Callback
import com.san.kir.manger.picasso.NetworkPolicy
import com.san.kir.manger.picasso.Picasso
import com.san.kir.manger.utils.ID
import com.san.kir.manger.utils.getFullPath
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.alert
import org.jetbrains.anko.alignParentEnd
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.below
import org.jetbrains.anko.dip
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

class StorageMangaDirItemView(val fragment: StorageMangaDirFragment) : AnkoComponent<ViewGroup> {
    private object _id {
        val name = ID.generate()
        val bar = ID.generate()
        val root = ID.generate()
        val dir = ID.generate()
        val logo = ID.generate()
        val isBD = ID.generate()
    }

    private val dir = Binder(StorageItem())
    private val manga = Binder<Manga?>(null)
    private val allSize = BinderRx(1)
    private val dirSize = BinderRx(1)

    fun createView(parent: ViewGroup): View {
        return createView(AnkoContext.create(parent.context, parent))
    }

    override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {
        relativeLayout {
            id = _id.root

            backgroundColor = Color.LTGRAY

            onClick {
                bind(manga) { m ->
                    if (m != null)
                        with(StorageItemFragment()) {
                            bind(m, fragment)
                            show(fragment.fragmentManager, "storage")
                        }
                    else
                        with(PopupMenu(this@relativeLayout.context,
                                       this@relativeLayout,
                                       Gravity.END)) {
                            menu.add(0, 0, 0, "Добавить в библиотеку")
                            menu.add(0, 1, 0, "Удалить совсем")

                            setOnMenuItemClickListener {
                                when (it.itemId) {
                                    0 -> {
                                        startActivity<AddMangaActivity>(StorageItem::class.java.canonicalName to dir.item)
                                    }
                                    1 -> {
                                        bind(dir) { d ->
                                            alert {
                                                message = "Вы действительно хотите удалить?"
                                                positiveButton("Да, конечно") {
                                                    getFullPath(d.path).deleteRecursively()
                                                    fragment.update()
                                                }
                                                negativeButton("Нет") {}
                                                show()
                                            }
                                        }
                                    }
                                }

                                return@setOnMenuItemClickListener true
                            }
                            show()
                        }
                }
            }

            // лого
            imageView {
                id = _id.logo
                scaleType = ImageView.ScaleType.FIT_XY

                bind(manga) {
                    if (it != null)
                        Picasso.with(this@with.ctx)
                                .load(it.logo)
                                .networkPolicy(NetworkPolicy.OFFLINE)
                                .into(this, object : Callback {
                                    override fun onSuccess() {}
                                    override fun onError(e: Exception?) {
                                        Picasso.with(this@with.ctx)
                                                .load(it.logo)
                                                .error(it.color)
                                                .into(this@imageView)
                                    }
                                })
                    else backgroundColor = Color.TRANSPARENT
                }
            }.lparams(width = dip(70), height = matchParent)

            // Название
            textView {
                id = _id.name
                bind(dir) {
                    text = it.name
                }
                textSize = 20f

                padding = dip(2)
                gravity = Gravity.CENTER_HORIZONTAL
            }.lparams(width = matchParent) {
                rightOf(_id.logo)
            }

            // Размер папки
            textView {
                id = _id.dir
                bind(dirSize) { text = "Место в памяти: $it Мб" }
                textSize = 16f
                padding = dip(2)
            }.lparams {
                below(_id.name)
                rightOf(_id.logo)
            }

            // Индикатор отсутствия в базе
            textView {
                id = _id.isBD
                setText(R.string.storage_not_in_bd)
                textSize = 16f
                padding = dip(2)
                bind(manga) {
                    visibility = if (it == null) View.VISIBLE else View.GONE
                }
            }.lparams {
                below(_id.name)
                alignParentEnd()
            }

            horizontalProgressBar {
                id = _id.bar

                bind(dirSize) { dS ->
                    bind(allSize) { aS ->
                        max = aS
                        secondaryProgress = dS
                    }
                }

                progressDrawable = ContextCompat.getDrawable(this@with.ctx,
                                                             R.drawable.storage_progressbar)

                padding = dip(1)

                lparams(height = dip(16), width = matchParent) {
                    below(_id.dir)
                    rightOf(_id.logo)
                }
            }

            textView {
                bind(dirSize) { dS ->
                    bind(allSize) { aS ->
                        text = "${Math.round(dS.toFloat() / aS.toFloat() * 100)}%"
                    }
                }
                gravity = Gravity.CENTER_HORIZONTAL
                textSize = 16f
            }.lparams(height = wrapContent, width = matchParent) {
                below(_id.dir)
                bottomMargin = dip(5)
                rightOf(_id.logo)
            }

            lparams(width = matchParent, height = dip(70)) {
                margin = dip(1)
            }
        }
    }

    fun bind(storageDir: StorageItem) {
        dir.item = storageDir
        manga.item = MangaWrapper.getFromPath(storageDir.path)
        val model = ViewModelProviders.of(fragment)[StorageViewModel::class.java]
        model.allSize.observe(fragment, Observer {
            allSize.item = it?.toInt() ?: 0
        })

        model.dirSize(storageDir.path).observe(fragment, Observer {
            dirSize.item = it?.toInt() ?: 0
        })
    }
}
