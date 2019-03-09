package com.san.kir.manger.components.sites_catalog

import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.san.kir.manger.R
import com.san.kir.manger.components.parsing.ManageSites
import com.san.kir.manger.extending.anko_extend.onClick
import com.san.kir.manger.extending.anko_extend.visibleOrInvisible
import com.san.kir.manger.extending.dialogs.AddMangaDialog
import com.san.kir.manger.extending.dialogs.MangaInfoDialog
import com.san.kir.manger.room.models.SiteCatalogElement
import com.san.kir.manger.room.models.authorsList
import com.san.kir.manger.room.models.genresList
import com.san.kir.manger.utils.ID
import com.san.kir.manger.utils.RecyclerViewAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.alignParentEnd
import org.jetbrains.anko.alignParentRight
import org.jetbrains.anko.alignParentStart
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.below
import org.jetbrains.anko.centerVertically
import org.jetbrains.anko.dip
import org.jetbrains.anko.imageView
import org.jetbrains.anko.leftOf
import org.jetbrains.anko.longToast
import org.jetbrains.anko.margin
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.relativeLayout
import org.jetbrains.anko.textView
import org.jetbrains.anko.wrapContent

class GlobalSearchItemView(private val act: GlobalSearchActivity) :
    RecyclerViewAdapterFactory.AnkoView<SiteCatalogElement>() {

    private lateinit var root: RelativeLayout
    private lateinit var name: TextView
    private lateinit var addBtn: ImageView
    private lateinit var updBtn: ImageView
    private lateinit var catalogName: TextView

    override fun createView(ui: AnkoContext<ViewGroup>)= with(ui) {
        relativeLayout {
            addBtn = imageView {
                id = ID.generate()
                scaleType = ImageView.ScaleType.FIT_XY
                setImageResource(android.R.drawable.ic_input_add)
                visibleOrInvisible(false)
            }.lparams(width = dip(40), height = dip(40)) {
                alignParentEnd()
                centerVertically()
            }

            name = textView {
                id = ID.generate()
                maxLines = 1
                textSize = 16f
            }.lparams(width = matchParent, height = wrapContent) {
                margin = dip(4)
                leftOf(addBtn)
            }

            updBtn = imageView {
                scaleType = ImageView.ScaleType.FIT_XY
                setImageResource(R.drawable.ic_action_update_white)
                visibleOrInvisible(false)
            }.lparams(width = dip(40), height = dip(40)) {
                alignParentRight()
                centerVertically()
            }

            catalogName = textView {
                maxLines = 1
            }.lparams(width = matchParent, height = wrapContent) {
                margin = dip(4)
                alignParentStart()
                below(name)
                leftOf(addBtn)
            }

            root = this
        }
    }

    override fun bind(item: SiteCatalogElement, isSelected: Boolean, position: Int) {
        val onAddManga = fun() {
            addBtn.visibility = View.INVISIBLE
            root.backgroundColor = Color.parseColor("#a5a2a2")
            item.isAdded = true

            act.launch(Dispatchers.Default) {
                act.mViewModel.siteCatalogUpdate(item)
            }
        }

        root.backgroundColor = when {
            item.isAdded -> Color.parseColor("#a5a2a2")
            else -> Color.TRANSPARENT
        }

        root.onClick { MangaInfoDialog(act, item, onAddManga) }

        name.text = item.name
        catalogName.text = item.catalogName

        addBtn.visibleOrInvisible(!item.isAdded)
        updBtn.visibleOrInvisible(item.isAdded)

        addBtn.onClick { AddMangaDialog(act, item, onAddManga) }
        updBtn.onClick {
            updBtn.visibleOrInvisible(false)

            act.launch(Dispatchers.Default) {
                val oldManga = act.mViewModel.getMangaItem(item.name)
                val updItem = ManageSites.getFullElement(item).await()
                oldManga.authorsList = updItem.authors
                oldManga.logo = updItem.logo
                oldManga.about = updItem.about
                oldManga.genresList = updItem.genres
                oldManga.site = updItem.link
                oldManga.status = updItem.statusEdition
                act.mViewModel.mangaUpdate(oldManga)
            }

            updBtn.context.longToast("Информация о манге ${item.name} обновлена")
        }

        act.launch(Dispatchers.Default) {
            val isContain = act.mViewModel.mangaContain(item)
            if (item.isAdded != isContain) {
                item.isAdded = isContain
                act.mViewModel.siteCatalogUpdate(item)
                withContext(Dispatchers.Main) {
                    bind(item, isSelected, position)
                }
            }
        }
    }

}
