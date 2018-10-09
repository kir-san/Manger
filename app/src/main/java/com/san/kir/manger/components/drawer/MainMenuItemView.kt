package com.san.kir.manger.components.drawer

import android.arch.lifecycle.Observer
import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView
import com.san.kir.manger.R
import com.san.kir.manger.components.main.Main
import com.san.kir.manger.extending.BaseActivity
import com.san.kir.manger.room.dao.MainMenuType
import com.san.kir.manger.room.dao.loadAllSize
import com.san.kir.manger.room.dao.loadPagedLatestChapters
import com.san.kir.manger.room.dao.loadPagedPlannedTasks
import com.san.kir.manger.room.dao.loadPagedSites
import com.san.kir.manger.room.dao.updateStorageItems
import com.san.kir.manger.room.models.MainMenuItem
import com.san.kir.manger.utils.RecyclerViewAdapterFactory
import com.san.kir.manger.utils.formatDouble
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.dip
import org.jetbrains.anko.frameLayout
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.padding
import org.jetbrains.anko.textView

class MainMenuItemView(private val act: BaseActivity) :
    RecyclerViewAdapterFactory.AnkoView<MainMenuItem>() {
    private lateinit var name: TextView
    private lateinit var type: TextView

    override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {
        frameLayout {
            lparams(width = matchParent)
            padding = dip(9)

            name = textView { textSize = 17.5f }.lparams { gravity = Gravity.CENTER_VERTICAL }

            type = textView {
                textSize = 17.5f
                padding = dip(4)
            }.lparams {
                gravity = Gravity.CENTER_VERTICAL or Gravity.END
            }
        }
    }

    override fun bind(item: MainMenuItem, isSelected: Boolean, position: Int) {
        launch(UI) {
            name.text = item.name
            type.setCounter(item.type)
        }
    }

    private fun TextView.setCounter(type: MainMenuType) {
        when (type) {
            MainMenuType.Library -> {
                Main.db.mangaDao
                    .loadMangaAbcSortAsc()
                    .observe(act, Observer { text = it?.size.toString() })
            }
            MainMenuType.Storage -> {
                Main.db.storageDao
                    .loadAllSize()
                    .observe(act, Observer {
                        text = context.getString(
                            R.string.main_menu_storage_size_mb,
                            formatDouble(it)
                        )
                    })
                Main.db.storageDao.updateStorageItems()
            }
            MainMenuType.Category -> {
                Main.db.categoryDao
                    .loadLiveCategories()
                    .observe(act, Observer { text = it?.size.toString() })
            }
            MainMenuType.Catalogs -> {
                Main.db.siteDao
                    .loadPagedSites()
                    .observe(act, Observer {
                        text = context.getString(R.string.main_menu_item_catalogs,
                                                 it?.size,
                                                 it?.sumBy { it.volume })
                    })
            }
            MainMenuType.Downloader -> {
                Main.db.downloadDao
                    .loadLoadingDownloads()
                    .observe(act, Observer { text = it?.size.toString() })
            }
            MainMenuType.Latest -> {
                Main.db.latestChapterDao
                    .loadPagedLatestChapters()
                    .observe(act, Observer { text = it?.size.toString() })
            }
            MainMenuType.Schedule -> {
                Main.db.plannedDao
                    .loadPagedPlannedTasks()
                    .observe(act, Observer { text = it?.size.toString() })
            }
            MainMenuType.Settings -> text = "^_^"
            MainMenuType.Default -> text = ""
        }
    }
}
