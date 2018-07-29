package com.san.kir.manger.components.catalogForOneSite

import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.san.kir.manger.components.main.Main
import com.san.kir.manger.extending.dialogs.AddMangaDialog
import com.san.kir.manger.extending.dialogs.MangaInfoDialog
import com.san.kir.manger.room.dao.contain
import com.san.kir.manger.room.models.SiteCatalogElement
import com.san.kir.manger.utils.ID
import com.san.kir.manger.utils.RecyclerViewAdapterFactory
import com.san.kir.manger.utils.listStrToString
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.alignParentBottom
import org.jetbrains.anko.alignParentRight
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.centerVertically
import org.jetbrains.anko.dip
import org.jetbrains.anko.imageView
import org.jetbrains.anko.leftOf
import org.jetbrains.anko.margin
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.relativeLayout
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.textColor
import org.jetbrains.anko.textView
import org.jetbrains.anko.wrapContent

class CatalogForOneSiteItemView : RecyclerViewAdapterFactory.AnkoView<SiteCatalogElement>() {
    val categoryDao = Main.db.categoryDao

    private object Id {
        val add = ID.generate()
        val statusEdition = ID.generate()
    }

    private lateinit var root: RelativeLayout
    private lateinit var name: TextView
    private lateinit var addBtn: ImageView
    private lateinit var authors: TextView
    private lateinit var statusEdition: TextView

    override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {
        relativeLayout {
            lparams(width = matchParent, height = dip(50))

            name = textView {
                maxLines = 1
                textSize = 16f
                textColor = Color.BLACK
            }.lparams(width = matchParent, height = wrapContent) {
                margin = dip(4)
                leftOf(Id.add)
            }

            addBtn = imageView {
                id = Id.add
                scaleType = ImageView.ScaleType.FIT_XY
                setImageResource(android.R.drawable.ic_input_add)
            }.lparams(width = dip(40), height = dip(40)) {
                alignParentRight()
                centerVertically()
            }

            authors = textView {
                maxLines = 1
            }.lparams(width = matchParent, height = wrapContent) {
                alignParentBottom()
                margin = dip(4)
                leftOf(Id.statusEdition)
            }

            statusEdition = textView {
                id = Id.statusEdition
            }.lparams(width = wrapContent, height = wrapContent) {
                alignParentBottom()
                margin = dip(4)
                leftOf(Id.add)
            }

            root = this
        }
    }

    override fun bind(item: SiteCatalogElement, isSelected: Boolean, position: Int) {
        val onAddManga = fun() {
            addBtn.visibility = View.INVISIBLE
            root.backgroundColor = Color.DKGRAY
            item.isAdded = true
            SiteCatalogElementViewModel.update(item)
        }

        root.backgroundColor = when {
            item.isAdded -> Color.DKGRAY
            position % 2 != 0 -> Color.LTGRAY
            else -> Color.WHITE
        }

        root.onClick { MangaInfoDialog(root.context, item, onAddManga) }

        name.text = item.name
        authors.text = listStrToString(item.authors)
        statusEdition.text = item.statusEdition

        addBtn.visibility = if (item.isAdded) View.INVISIBLE else View.VISIBLE

        addBtn.onClick { AddMangaDialog(root.context, item, onAddManga) }

        launch {
            val isContain = Main.db.mangaDao.contain(item)
            if (item.isAdded != isContain) {
                item.isAdded = isContain
                SiteCatalogElementViewModel.update(item)
                bind(item, isSelected, position)
            }
        }
    }
}
