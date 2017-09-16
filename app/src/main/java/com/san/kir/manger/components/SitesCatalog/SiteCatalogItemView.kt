package com.san.kir.manger.components.SitesCatalog

import android.graphics.Typeface
import android.view.View
import android.view.ViewGroup
import com.san.kir.manger.App
import com.san.kir.manger.EventBus.Binder
import com.san.kir.manger.Extending.AnkoExtend.bind
import com.san.kir.manger.R
import com.san.kir.manger.components.CatalogForOneSite.CatalogForOneSiteActivity
import com.san.kir.manger.components.Parsing.SiteCatalog
import com.san.kir.manger.picasso.Picasso
import com.san.kir.manger.utils.ID
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.run
import org.jetbrains.anko.AnkoComponent
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
import org.jetbrains.anko.toast
import org.jetbrains.anko.wrapContent
import kotlin.coroutines.experimental.CoroutineContext

class SiteCatalogItemView : AnkoComponent<ViewGroup> {

    private object _id {
        val name = ID.generate()
        val logo = ID.generate()
        val link = ID.generate()
        val volume = ID.generate()
    }

    var siteId = (-1)
    val name = Binder("")
    val link = Binder("")
    val volume = Binder(0 to 0)
    val isInit = Binder(false)

    fun createView(parent: ViewGroup): View {
        return createView(AnkoContext.create(parent.context, parent))
    }

    override fun createView(ui: AnkoContext<ViewGroup>) = with(ui) {
        relativeLayout {
            lparams(width = matchParent, height = dip(55))
            isClickable
            onClick {
                if (siteId > -1)
                    startActivity<CatalogForOneSiteActivity>("id" to siteId)
            }

            // Название
            textView {
                id = _id.name
                textSize = 16f
                setTypeface(typeface, Typeface.BOLD)
                bind(name) { text = it }
            }.lparams(width = wrapContent, height = wrapContent) {
                alignParentTop()
                margin = dip(6)
                rightOf(_id.logo)
            }

            // сайт
            textView {
                id = _id.link

                bind(link) { text = it }
            }.lparams(width = wrapContent, height = wrapContent) {
                alignParentBottom()
                margin = dip(6)
                rightOf(_id.logo)
            }


            // иконка
            imageView {
                id = _id.logo

                bind(link) {
                    //                    println(it)
                    if (it.isNotEmpty())
//                        getPathForUrl(it,
//                                      { background = Drawable.createFromPath(it) },
//                                      { backgroundResource = R.drawable.ic_error })
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
                id = _id.volume

                bind(volume) { (oldVolume, volume) ->
                    text = context.getString(com.san.kir.manger.R.string.site_volume, oldVolume, volume - oldVolume)
                }
            }.lparams(width = wrapContent, height = wrapContent) {
                alignParentRight()
                centerVertically()
                margin = dip(4)
            }


            // прогресс бар
            progressBar {
                isIndeterminate = true
                bind(isInit) {
                    visibility = if (it) View.VISIBLE else View.GONE
                }
            }.lparams(width = dip(12), height = dip(12)) {
                alignParentTop()
                alignParentEnd()
                margin = dip(4)
            }


        }
    }

    fun bind(el: SiteCatalog, context: CoroutineContext) {
        siteId = el.ID
        name.item = el.name
        link.item = el.host
        volume.item = el.oldVolume to el.volume

        if (!el.isInit) {
            isInit.item = true
            launch(context) {
                try {
                    el.init()
                } catch (ex: Throwable) {
                    run(UI) {
                        App.context.toast(R.string.catalog_for_one_site_on_error_load)
                    }
                } finally {
                    run(UI) {
                        volume.item = el.oldVolume to el.volume
                        isInit.item = false
                    }
                }
            }
        }

    }
}
