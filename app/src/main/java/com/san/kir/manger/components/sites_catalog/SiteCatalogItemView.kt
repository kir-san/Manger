package com.san.kir.manger.components.sites_catalog

import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.github.kittinunf.result.coroutines.failure
import com.san.kir.ankofork.AnkoContext
import com.san.kir.ankofork.dip
import com.san.kir.ankofork.margin
import com.san.kir.ankofork.matchParent
import com.san.kir.ankofork.sdk28.imageView
import com.san.kir.ankofork.sdk28.linearLayout
import com.san.kir.ankofork.sdk28.onClick
import com.san.kir.ankofork.sdk28.progressBar
import com.san.kir.ankofork.sdk28.textView
import com.san.kir.ankofork.startActivity
import com.san.kir.ankofork.verticalLayout
import com.san.kir.manger.R
import com.san.kir.manger.components.catalog_for_one_site.CatalogForOneSiteActivity
import com.san.kir.manger.components.parsing.ManageSites
import com.san.kir.manger.components.parsing.SiteCatalog
import com.san.kir.manger.room.entities.Site
import com.san.kir.manger.utils.RecyclerViewAdapterFactory
import com.san.kir.manger.utils.loadImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SiteCatalogItemView(private val act: SiteCatalogActivity) :
    RecyclerViewAdapterFactory.AnkoView<Site>() {

    private lateinit var root: LinearLayout
    private lateinit var name: TextView
    private lateinit var link: TextView
    private lateinit var icon: ImageView
    private lateinit var volume: TextView
    private lateinit var isInit: ProgressBar
    private lateinit var isError: ImageView

    override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {
        linearLayout {
            lparams(width = matchParent, height = dip(60))

            gravity = Gravity.CENTER_VERTICAL

            icon = imageView().lparams(width = dip(40), height = dip(40)) {
                margin = dip(16)
            }

            verticalLayout {
                name = textView {
                    textSize = 16f
                }

                link = textView {
                    textSize = 14f
                }
            }.lparams(width = matchParent) {
                weight = 1f
            }

            isError = imageView {
                visibility = View.GONE
                setImageResource(R.drawable.unknown)
            }.lparams(width = dip(15), height = dip(15)) {
                marginStart = dip(16)
            }

            isInit = progressBar {
                isIndeterminate = true
                visibility = View.GONE
            }.lparams(width = dip(15), height = dip(15)) {
                marginStart = dip(16)
            }

            volume = textView {
                textSize = 14f
            }.lparams {
                margin = dip(16)
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
                errorResId(R.drawable.ic_error)
                into(icon)
            }
        }

        volume.text = root.context.getString(
            R.string.site_volume,
            item.oldVolume,
            item.volume - item.oldVolume
        )

        act.lifecycleScope.launch(Dispatchers.Main) {
            val site: SiteCatalog =
                ManageSites.CATALOG_SITES.first { it.catalogName == item.catalogName }
            if (!site.isInit) {
                isError.visibility = View.GONE
                isInit.visibility = View.VISIBLE

                act.mViewModel
                    .updateSiteInfo(site)
                    .failure {
                        isError.visibility = View.VISIBLE
                    }

                volume.text = root.context.getString(
                    R.string.site_volume,
                    item.oldVolume,
                    item.volume - item.oldVolume
                )
                isInit.visibility = View.GONE
            }
        }
    }
}
