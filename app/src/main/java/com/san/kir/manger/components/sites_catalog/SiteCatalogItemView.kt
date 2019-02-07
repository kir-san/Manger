package com.san.kir.manger.components.sites_catalog

import android.graphics.Typeface
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import com.san.kir.manger.R
import com.san.kir.manger.components.catalog_for_one_site.CatalogForOneSiteActivity
import com.san.kir.manger.components.parsing.ManageSites
import com.san.kir.manger.components.parsing.SiteCatalog
import com.san.kir.manger.extending.ankoExtend.onClick
import com.san.kir.manger.room.models.Site
import com.san.kir.manger.utils.ID
import com.san.kir.manger.utils.RecyclerViewAdapterFactory
import com.san.kir.manger.utils.loadImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.alignParentBottom
import org.jetbrains.anko.alignParentEnd
import org.jetbrains.anko.alignParentRight
import org.jetbrains.anko.alignParentTop
import org.jetbrains.anko.centerVertically
import org.jetbrains.anko.dip
import org.jetbrains.anko.imageView
import org.jetbrains.anko.margin
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.progressBar
import org.jetbrains.anko.relativeLayout
import org.jetbrains.anko.rightOf
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.textView
import org.jetbrains.anko.wrapContent

class SiteCatalogItemView(private val act: SiteCatalogActivity) :
    RecyclerViewAdapterFactory.AnkoView<Site>() {
    private object Id {
        val logo = ID.generate()
    }

    private lateinit var root: RelativeLayout
    private lateinit var name: TextView
    private lateinit var link: TextView
    private lateinit var icon: ImageView
    private lateinit var volume: TextView
    private lateinit var isInit: ProgressBar
    private lateinit var isError: ImageView

    override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {
        relativeLayout {
            lparams(width = matchParent, height = dip(55))

            name = textView {
                textSize = 16f
                setTypeface(typeface, Typeface.BOLD)
            }.lparams(width = wrapContent, height = wrapContent) {
                alignParentTop()
                margin = dip(6)
                rightOf(Id.logo)
            }

            link = textView {}.lparams(width = wrapContent, height = wrapContent) {
                alignParentBottom()
                margin = dip(6)
                rightOf(Id.logo)
            }

            icon = imageView {
                id = Id.logo
            }.lparams(width = dip(50), height = dip(50)) {
                centerVertically()
                leftMargin = dip(5)
            }

            volume = textView {}.lparams(width = wrapContent, height = wrapContent) {
                alignParentRight()
                centerVertically()
                margin = dip(4)
            }

            isInit = progressBar {
                isIndeterminate = true
                visibility = View.GONE
            }.lparams(width = dip(12), height = dip(12)) {
                alignParentTop()
                alignParentEnd()
                margin = dip(4)
            }

            isError = imageView {
                visibility = View.GONE
                setImageResource(R.drawable.unknown)
            }.lparams(width = dip(12), height = dip(12)) {
                alignParentTop()
                alignParentEnd()
                margin = dip(4)
            }

            root = this
        }
    }

    override fun bind(item: Site, isSelected: Boolean, position: Int) {
        root.onClick {
            act.startActivity<CatalogForOneSiteActivity>("site" to item.catalogName)
        }

        name.text = item.name
        link.text = item.host

        if (item.host.isNotEmpty()) {
            loadImage("http://www.google.com/s2/favicons?domain=${item.host}") {
                errorResId(com.san.kir.manger.R.drawable.ic_error)
                into(icon)
            }
        }

        volume.text = root.context.getString(
            com.san.kir.manger.R.string.site_volume,
            item.oldVolume,
            item.volume - item.oldVolume
        )
        act.launch(Dispatchers.Main + act.job) {
            val site: SiteCatalog =
                ManageSites.CATALOG_SITES.first { it.catalogName == item.catalogName }
            if (!site.isInit) {
                isError.visibility = View.GONE
                isInit.visibility = View.VISIBLE

                act.mViewModel
                    .updateSiteInfo(site)
                    .await()
                    .onFailure {
                        isError.visibility = View.VISIBLE
                    }

                volume.text = root.context.getString(
                    com.san.kir.manger.R.string.site_volume,
                    item.oldVolume,
                    item.volume - item.oldVolume
                )
                isInit.visibility = View.GONE
            }
        }
    }
}
