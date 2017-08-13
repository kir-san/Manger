package com.san.kir.manger.components.Storage

import android.view.View
import com.san.kir.manger.EventBus.BinderRx
import com.san.kir.manger.Extending.AnkoExtend.bind
import com.san.kir.manger.Extending.AnkoExtend.diagramForManga
import com.san.kir.manger.Extending.AnkoExtend.storageItem
import com.san.kir.manger.Extending.Views.DiagramForManga
import com.san.kir.manger.R
import com.san.kir.manger.dbflow.models.Manga
import com.san.kir.manger.utils.ID
import com.san.kir.manger.utils.log
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.verticalLayout
import org.jetbrains.anko.wrapContent

class StorageItemView : AnkoComponent<StorageItemFragment> {
    private object _id {
        val diagram = ID.generate()
    }

    private var _manga = Manga()
    private val allSize = BinderRx(0L)
    private val mangaSize = BinderRx(0L)
    private val readSize = BinderRx(0L)

    fun createView(parent: StorageItemFragment): View {
        return createView(AnkoContext.create(parent.context, parent))
    }

    override fun createView(ui: AnkoContext<StorageItemFragment>) = with(ui) {
        verticalLayout {
            diagramForManga {
                lparams(width = matchParent, height = wrapContent)
                id = _id.diagram

                bind(allSize) {
                    log = "all $it"
                    setData(all = it)
                }
                bind(mangaSize) {
                    log = "manga $it"
                    setData(manga = it)
                }
                bind(readSize) {
                    log = "read $it"
                    setData(read = it)
                }
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
                        StorageUpdate.onAllSize { allSize.item = it }
                        delay(200L)
                        StorageUpdate.onReadSize(_manga) { readSize.item = it }
                        delay(200L)
                        StorageUpdate.onMangaSize(_manga) { mangaSize.item = it }

                    }
                }
            }
        }
    }

    fun bind(manga: Manga) {
        _manga = manga
        StorageUpdate.onAllSize { allSize.item = it }
        StorageUpdate.onMangaSize(_manga) { mangaSize.item = it }
        StorageUpdate.onReadSize(_manga) { readSize.item = it }
    }
}
