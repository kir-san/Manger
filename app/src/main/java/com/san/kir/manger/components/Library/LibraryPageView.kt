package com.san.kir.manger.components.Library

import android.arch.lifecycle.Observer
import android.content.res.Configuration
import android.support.v7.widget.GridLayoutManager
import android.view.Gravity
import android.view.View
import com.github.salomonbrys.kodein.KodeinInjector
import com.github.salomonbrys.kodein.instance
import com.san.kir.manger.R
import com.san.kir.manger.components.Main.Main
import com.san.kir.manger.room.DAO.MangaFilter
import com.san.kir.manger.room.DAO.loadMangas
import com.san.kir.manger.room.models.Category
import com.san.kir.manger.utils.ID
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.below
import org.jetbrains.anko.button
import org.jetbrains.anko.centerHorizontally
import org.jetbrains.anko.centerInParent
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.relativeLayout
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.textView
import org.jetbrains.anko.wrapContent

class LibraryPageView(
    val category: Category,
    val injector: KodeinInjector
) : AnkoComponent<LibraryActivity> {
    private object _id {
        val text = ID.generate()
    }

    private var span = 0
    private var isLarge = true

    private val act: LibraryActivity by injector.instance()
    val adapter = LibraryItemsRecyclerPresenter(category, injector)

    fun createView(act: LibraryActivity) = createView(AnkoContext.create(act, act))

    override fun createView(ui: AnkoContext<LibraryActivity>) = with(ui) {
        val portrait = ctx.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
        span = if (portrait) category.spanPortrait else category.spanLandscape
        isLarge = if (portrait) category.isLargePortrait else category.isLargeLandscape

        relativeLayout {
            lparams(width = matchParent, height = matchParent)

            // текст при пустой странице
            textView {
                id = _id.text
                gravity = Gravity.CENTER
                setText(R.string.library_help)

                Main.db.mangaDao
                    .loadMangas(category, MangaFilter.ADD_TIME_ASC)
                    .observe(act, Observer {
                        if (it != null)
                            visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
                    })
            }.lparams(width = matchParent, height = wrapContent) {
                centerInParent()
            }

            // кнопка перехода в каталоге
            button {
                setText(R.string.library_help_go)
                visibility = View.GONE

                onClick { startActivity<Main>("launch" to "catalog") }
                Main.db.mangaDao
                    .loadMangas(category, MangaFilter.ADD_TIME_ASC)
                    .observe(act, Observer {
                        if (it != null)
                            visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
                    })
            }.lparams(width = wrapContent, height = wrapContent) {
                centerHorizontally()
                below(_id.text)
            }

            // список элементов
            recyclerView {
                lparams(width = matchParent, height = matchParent)
                setHasFixedSize(true)
                Main.db.categoryDao.loadLiveCategory(category.name)
                    .observe(act, Observer {
                        it?.let {
                            val newSpan = if (portrait) it.spanPortrait else it.spanLandscape
                            val newIsLarge =
                                if (portrait) it.isLargePortrait else it.isLargeLandscape

                            if (span != newSpan || isLarge != newIsLarge) {
                                span = newSpan
                                isLarge = newIsLarge
                                layoutManager = GridLayoutManager(this@with.ctx, span)
                                this@LibraryPageView.adapter.intoIsList(this, isLarge)
                            }
                        }
                    })
                layoutManager = GridLayoutManager(this@with.ctx, span)
                this@LibraryPageView.adapter.intoIsList(this, isLarge)
            }
        }
    }
}
