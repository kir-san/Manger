package com.san.kir.manger.components.SitesCatalog

import android.graphics.Typeface
import android.view.ViewGroup
import android.widget.TextView
import com.san.kir.manger.EventBus.Binder
import com.san.kir.manger.Extending.AnkoExtend.textView
import com.san.kir.manger.Extending.AnkoExtend.visibleOrGone
import com.san.kir.manger.R
import com.san.kir.manger.components.CatalogForOneSite.CatalogForOneSiteActivity
import com.san.kir.manger.components.CatalogForOneSite.SiteCatalogElementViewModel
import com.san.kir.manger.components.Main.Main
import com.san.kir.manger.components.Parsing.EmptySiteCatalog
import com.san.kir.manger.components.Parsing.ManageSites
import com.san.kir.manger.components.Parsing.SiteCatalog
import com.san.kir.manger.picasso.Picasso
import com.san.kir.manger.room.DAO.update
import com.san.kir.manger.room.models.Site
import com.san.kir.manger.utils.ID
import com.san.kir.manger.utils.RecyclerViewAdapterFactory
import com.san.kir.manger.utils.log
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
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
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.textView
import org.jetbrains.anko.wrapContent

class SiteCatalogItemView : RecyclerViewAdapterFactory.AnkoView<Site>() {

    private object _id {
        val logo = ID.generate()
    }

    var site: SiteCatalog = EmptySiteCatalog()
    lateinit var name: TextView
    val link = Binder("")
    val volume = Binder(0 to 0)
    val isInit = Binder(false)
    val error = Binder(false)

    override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {
        relativeLayout {
            lparams(width = matchParent, height = dip(55))
            isClickable
            onClick {
                if (site.ID > -1)
                    startActivity<CatalogForOneSiteActivity>("id" to site.ID)
            }

            // Название
            name = textView {
                textSize = 16f
                setTypeface(typeface, Typeface.BOLD)
            }.lparams(width = wrapContent, height = wrapContent) {
                alignParentTop()
                margin = dip(6)
                rightOf(_id.logo)
            }

            // сайт
            textView(link) {}.lparams(width = wrapContent, height = wrapContent) {
                alignParentBottom()
                margin = dip(6)
                rightOf(_id.logo)
            }

            // иконка
            imageView {
                id = _id.logo
                link.bind {
                    if (it.isNotEmpty())
                        Picasso.with(this@with.ctx)
                                .load("http://www.google.com/s2/favicons?domain=$it")
                                .error(com.san.kir.manger.R.drawable.ic_error)
                                .into(this)
                }
            }.lparams(width = dip(50), height = dip(50)) {
                centerVertically()
                leftMargin = dip(5)
            }

            // Количество манги
            textView {
                volume.bind { (oldVolume, volume) ->
                    text = context.getString(com.san.kir.manger.R.string.site_volume,
                                             oldVolume,
                                             volume - oldVolume)
                }
            }.lparams(width = wrapContent, height = wrapContent) {
                alignParentRight()
                centerVertically()
                margin = dip(4)
            }

            // прогресс бар
            progressBar {
                isIndeterminate = true
                visibleOrGone(isInit)
            }.lparams(width = dip(12), height = dip(12)) {
                alignParentTop()
                alignParentEnd()
                margin = dip(4)
            }

            // Оповещение об ошибке
            imageView {
                setImageResource(R.drawable.unknown)
                visibleOrGone(error)
            }.lparams(width = dip(12), height = dip(12)) {
                alignParentTop()
                alignParentEnd()
                margin = dip(4)
            }

        }
    }

    override fun bind(item: Site, isSelected: Boolean, position: Int) {
        async(UI) {
            try {
                site = ManageSites.CATALOG_SITES[item.siteID]
                name.text = site.name
                link.item = site.host

                volume.item = item.oldVolume to item.volume

                if (!site.isInit) {
                    error.item = false
                    isInit.item = true
                    async {
                        site.init()

                        // Находим в базе данных наш сайт
                        with(Main.db.siteDao) {
                            loadSite(site.name)?.let {
                                // Сохраняем новое значение количества элементов
                                it.oldVolume = SiteCatalogElementViewModel.setSiteId(site.ID)
                                        .items()
                                        .size
                                it.volume = site.volume
                                // Обновляем наш сайт в базе данных
                                update(it)
                            }
                        }
                    }.join()
                }
            } catch (e: Exception) {
                log(e.toString())
                error.item = true
            } finally {
                volume.item = item.oldVolume to item.volume
                isInit.item = false
            }
        }
    }


}
