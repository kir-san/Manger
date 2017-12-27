package com.san.kir.manger.components.Drawer

import android.arch.lifecycle.Observer
import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView
import com.san.kir.manger.Extending.BaseActivity
import com.san.kir.manger.R
import com.san.kir.manger.components.Main.Main
import com.san.kir.manger.room.DAO.MainMenuType
import com.san.kir.manger.room.DAO.loadAllSize
import com.san.kir.manger.room.DAO.loadPagedLatestChapters
import com.san.kir.manger.room.DAO.loadPagedSites
import com.san.kir.manger.room.DAO.updateStorageItems
import com.san.kir.manger.room.models.MainMenuItem
import com.san.kir.manger.utils.RecyclerViewAdapterFactory
import com.san.kir.manger.utils.formatDouble
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.dip
import org.jetbrains.anko.frameLayout
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.padding
import org.jetbrains.anko.textView

class MainMenuItemView(private val act: BaseActivity) : RecyclerViewAdapterFactory.AnkoView<MainMenuItem>() {
    val db = Main.db
//    private val isVisible = Binder(false)

    private lateinit var name: TextView
    private lateinit var type: TextView

    override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {
        frameLayout {
            lparams(width = matchParent)
            padding = dip(10)

            name = textView { textSize = 17.5f }.lparams { gravity = Gravity.CENTER_VERTICAL }

            type = textView {
                textSize = 17.5f
                backgroundColor = Color.LTGRAY
                padding = dip(4)
            }.lparams {
                gravity = Gravity.CENTER_VERTICAL or Gravity.END
            }
        }
    }

    override fun bind(item: MainMenuItem, isSelected: Boolean, position: Int) {
        name.text = item.name
        type.setCounter(item.type)

//        isVisible.item = item.isVisible
    }

    private fun TextView.setCounter(type: MainMenuType) {
        when (type) {
            MainMenuType.Library -> text = "${db.mangaDao.loadAllManga().size}"
            MainMenuType.Storage -> {
                Main.db.storageDao
                        .loadAllSize()
                        .observe(act, Observer {
                            text = context.getString(R.string.main_menu_storage_size_mb,
                                                     formatDouble(it))
                        })
                Main.db.storageDao.updateStorageItems()
            }
            MainMenuType.Category -> {
                Main.db.categoryDao
                        .loadLiveCategories()
                        .observe(act, Observer { text = "${it?.size}" })
            }
            MainMenuType.Catalogs -> {
                Main.db.siteDao
                        .loadPagedSites()
                        .observe(act, Observer {
                            text = "${it?.size} - ${it?.sumBy { it.volume }}"
                        })
            }
            MainMenuType.Downloader -> {
                Main.db.downloadDao
                        .loadLoadingDownloads()
                        .observe(act, Observer {
                            text = it?.size.toString()
                        })
            }
            MainMenuType.Latest -> {
                Main.db.latestChapterDao
                        .loadPagedLatestChapters()
                        .observe(act, Observer { text = "${it?.size}" })
            }
            MainMenuType.Settings -> text = "^_^"
            MainMenuType.Default -> text = ""
        }
    }
}
