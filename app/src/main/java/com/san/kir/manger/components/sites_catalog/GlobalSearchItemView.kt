package com.san.kir.manger.components.sites_catalog

import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.san.kir.ankofork.AnkoContext
import com.san.kir.ankofork.dialogs.longToast
import com.san.kir.ankofork.dip
import com.san.kir.ankofork.margin
import com.san.kir.ankofork.matchParent
import com.san.kir.ankofork.sdk28.backgroundColor
import com.san.kir.ankofork.sdk28.imageView
import com.san.kir.ankofork.sdk28.linearLayout
import com.san.kir.ankofork.sdk28.lines
import com.san.kir.ankofork.sdk28.onClick
import com.san.kir.ankofork.sdk28.textView
import com.san.kir.ankofork.verticalLayout
import com.san.kir.manger.R
import com.san.kir.manger.components.parsing.ManageSites
import com.san.kir.manger.extending.dialogs.AddMangaDialog
import com.san.kir.manger.extending.dialogs.MangaInfoDialog
import com.san.kir.manger.room.entities.SiteCatalogElement
import com.san.kir.manger.room.entities.authorsList
import com.san.kir.manger.room.entities.genresList
import com.san.kir.manger.utils.RecyclerViewAdapterFactory
import com.san.kir.manger.utils.extensions.visibleOrGone
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GlobalSearchItemView(private val act: GlobalSearchActivity) :
    RecyclerViewAdapterFactory.AnkoView<SiteCatalogElement>() {

    private lateinit var root: LinearLayout
    private lateinit var name: TextView
    private lateinit var addBtn: ImageView
    private lateinit var updBtn: ImageView
    private lateinit var catalogName: TextView

    override fun createView(ui: AnkoContext<ViewGroup>)= with(ui) {
        linearLayout {
            lparams(width = matchParent, height = dip(60))
            gravity = Gravity.CENTER_VERTICAL

            verticalLayout {
                name = textView {
                    textSize = 16f
                    lines = 1
                }

                catalogName = textView {
                    textSize = 14f
                }
            }.lparams(width = matchParent) {
                marginStart = dip(16)
                weight = 1f
            }

            addBtn = imageView {
                setImageResource(android.R.drawable.ic_input_add)
                visibleOrGone(false)
            }.lparams(width = dip(35), height = dip(35)) {
                margin = dip(16)
            }

            updBtn = imageView {
                setImageResource(R.drawable.ic_action_update)
                visibleOrGone(false)
            }.lparams(width = dip(35), height = dip(35)) {
                margin = dip(16)
            }

            root = this
        }
    }

    override fun bind(item: SiteCatalogElement, isSelected: Boolean, position: Int) {
        val onAddManga = fun() {
            addBtn.visibility = View.INVISIBLE
            root.backgroundColor = Color.parseColor("#a5a2a2")
            item.isAdded = true

            act.lifecycleScope.launch(Dispatchers.Default) {
                act.mViewModel.update(item)
            }
        }

        root.backgroundColor = when {
            item.isAdded -> Color.parseColor("#a5a2a2")
            else -> Color.TRANSPARENT
        }

        root.onClick { MangaInfoDialog(act, item, onAddManga) }

        name.text = item.name
        catalogName.text = item.catalogName

        addBtn.visibleOrGone(!item.isAdded)
        updBtn.visibleOrGone(item.isAdded)

        addBtn.onClick { AddMangaDialog(act, item, onAddManga) }
        updBtn.onClick {
            updBtn.visibleOrGone(false)

            act.lifecycleScope.launch(Dispatchers.Default) {
                val oldManga = act.mViewModel.getMangaItem(item.shotLink)
                val updItem = ManageSites.getFullElement(item)
                oldManga.authorsList = updItem.authors
                oldManga.logo = updItem.logo
                oldManga.about = updItem.about
                oldManga.genresList = updItem.genres
                oldManga.host = updItem.host
                oldManga.shortLink = updItem.shotLink
                oldManga.status = updItem.statusEdition
                act.mViewModel.mangaUpdate(oldManga)
            }

            updBtn.context.longToast("Информация о манге ${item.name} обновлена")
        }

        act.lifecycleScope.launch(Dispatchers.Default) {
            val isContain = act.mViewModel.mangaContain(item)
            if (item.isAdded != isContain) {
                item.isAdded = isContain
                act.mViewModel.update(item)
                withContext(Dispatchers.Main) {
                    bind(item, isSelected, position)
                }
            }
        }
    }

}
