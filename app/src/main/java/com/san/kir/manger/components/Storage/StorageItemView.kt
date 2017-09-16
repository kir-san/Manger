package com.san.kir.manger.components.Storage

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.view.Gravity
import android.view.View
import android.view.ViewManager
import android.widget.LinearLayout
import android.widget.TextView
import com.san.kir.manger.EventBus.Binder
import com.san.kir.manger.Extending.AnkoExtend.bind
import com.san.kir.manger.Extending.AnkoExtend.diagramForManga
import com.san.kir.manger.Extending.BaseFragment
import com.san.kir.manger.Extending.Views.DiagramForManga
import com.san.kir.manger.R
import com.san.kir.manger.dbflow.models.Manga
import com.san.kir.manger.utils.ID
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.dip
import org.jetbrains.anko.imageView
import org.jetbrains.anko.leftPadding
import org.jetbrains.anko.linearLayout
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.padding
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.textView
import org.jetbrains.anko.verticalLayout
import org.jetbrains.anko.wrapContent
import javax.inject.Inject

class StorageItemView @Inject constructor() : AnkoComponent<StorageItemFragment> {
    private object _id {
        val diagram = ID.generate()
    }

    private lateinit var fragment: BaseFragment
    private val model by lazy { ViewModelProviders.of(fragment)[StorageViewModel::class.java] }
    private var _manga = Manga()
    private val allSize = Binder(0L)
    private val mangaSize = Binder(0L)
    private val readSize = Binder(0L)

    fun createView(parent: StorageItemFragment): View {
        return createView(AnkoContext.create(parent.context, parent))
    }

    override fun createView(ui: AnkoContext<StorageItemFragment>) = with(ui) {
        verticalLayout {
            diagramForManga {
                lparams(width = matchParent, height = wrapContent)
                id = _id.diagram

                bind(allSize) { setData(all = it) }
                bind(mangaSize) { setData(manga = it) }
                bind(readSize) { setData(read = it) }
            }
            storageItem(color = DiagramForManga.YELLOW, textBinder = allSize) { view, size ->
                view.text = "Всего занято: $size Мб"
            }
            storageItem(color = DiagramForManga.BLUE, textBinder = mangaSize) { view, size ->
                view.text = "Занято этой мангой: $size Мб"
            }
            storageItem(color = DiagramForManga.GRAY,
                        textBinder = readSize,
                        icon = R.drawable.ic_action_delete_black) { view, size ->
                view.text = "Прочитанное в этой манге: $size Мб"
            }.onClick {
                StorageUtils.deleteReadChapters(this@with, _manga) {
                    launch(UI) {
                        model.allSize.update()
                        model.dirSize(_manga).update()
                        model.readSize(_manga).update()
                    }
                }
            }
        }
    }

    fun bind(manga: Manga, fragment: BaseFragment) {
        this.fragment = fragment
        _manga = manga
        model.allSize.observe(fragment, Observer { allSize.item = it ?: 0 })
        model.dirSize(manga).observe(fragment, Observer { mangaSize.item = it ?: 0 })
        model.readSize(manga).observe(fragment, Observer { readSize.item = it ?: 0 })
    }

    private fun ViewManager.storageItem(color: Int,
                                        textBinder: Binder<Long>,
                                        icon: Int = 0,
                                        actionBinder: (TextView, Long) -> Unit): LinearLayout {
        return linearLayout {
            lparams(width = matchParent, height = dip(30))
            padding = dip(4)

            imageView {
                backgroundColor = color
            }.lparams(width = dip(50), height = dip(28))

            textView {
                leftPadding = dip(4)
                bind(textBinder) {
                    actionBinder.invoke(this, it)
                }
            }.lparams {
                gravity = Gravity.CENTER_VERTICAL
            }

            if (icon > 0)
                imageView {
                    backgroundResource = icon
                }
        }
    }
}
