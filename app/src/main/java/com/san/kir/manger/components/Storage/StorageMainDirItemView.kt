package com.san.kir.manger.components.Storage

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.graphics.Color
import android.support.v4.content.ContextCompat
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import com.san.kir.manger.EventBus.Binder
import com.san.kir.manger.EventBus.BinderRx
import com.san.kir.manger.Extending.AnkoExtend.bind
import com.san.kir.manger.R
import com.san.kir.manger.utils.ID
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.alignParentEnd
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.baselineOf
import org.jetbrains.anko.below
import org.jetbrains.anko.dip
import org.jetbrains.anko.horizontalProgressBar
import org.jetbrains.anko.margin
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.padding
import org.jetbrains.anko.relativeLayout
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.textView
import org.jetbrains.anko.wrapContent

class StorageMainDirItemView(val storageFragment: StorageMainDirFragment) : AnkoComponent<ViewGroup> {
    private object _id {
        val name = ID.generate()
        val count = ID.generate()
        val bar = ID.generate()
        val root = ID.generate()
        val dir = ID.generate()
    }

    private val dir = Binder(StorageDir())

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
                storageFragment.router.showScreen(StorageMangaDirFragment.newInitstate(dir.item.name, dir.item.file), isMangaDir = true)
            }

            // Название
            textView {
                id = _id.name
                bind(dir) {
                    text = it.name
                }
                textSize = 20f

                padding = dip(2)
                gravity = Gravity.CENTER_HORIZONTAL
            }.lparams(width = matchParent)

            // Размер папки
            textView {
                id = _id.dir
                bind(dirSize) { text = "Место в памяти: $it Мб" }
                textSize = 16f
                padding = dip(2)
            }.lparams {
                below(_id.name)
            }

            // Количество
            textView {
                id = _id.count
                bind(dir) {
                    text = "Содержит папок: ${it.countDir}"
                }
                textSize = 16f

                padding = dip(2)
            }.lparams {
                alignParentEnd()
                baselineOf(_id.dir)
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
            }

            lparams(width = matchParent, height = wrapContent) {
                margin = dip(1)
            }
        }
    }

    fun bind(storageDir: StorageDir,
             storageFragment: StorageMainDirFragment) {
        dir.item = storageDir
        val model = ViewModelProviders.of(storageFragment)[StorageViewModel::class.java]
        model.allSize.observe(storageFragment, Observer {
            allSize.item = it?.toInt() ?: 0
        })

        model.dirSize(storageDir.file).observe(storageFragment, Observer {
            dirSize.item = it?.toInt() ?: 0
        })
    }
}
