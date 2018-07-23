package com.san.kir.manger.components.library

import android.arch.lifecycle.Observer
import android.content.res.Configuration
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.san.kir.manger.R
import com.san.kir.manger.components.main.Main
import com.san.kir.manger.components.sitesCatalog.SiteCatalogActivity
import com.san.kir.manger.extending.BaseActivity
import com.san.kir.manger.extending.ankoExtend.visibleOrGone
import com.san.kir.manger.room.dao.MangaFilter
import com.san.kir.manger.room.dao.loadMangas
import com.san.kir.manger.room.models.Category
import com.san.kir.manger.utils.AnkoActivityComponent
import com.san.kir.manger.utils.ID
import com.san.kir.manger.utils.log
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
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

class LibraryPageView(
    val adapter: LibraryItemsRecyclerPresenter,
    val category: Category,
    val act: LibraryActivity
) : AnkoActivityComponent() {
    private object Id {
        val text = ID.generate()
    }

    private var span = 0
    private var isLarge = true

    private lateinit var text: TextView
    private lateinit var btn: Button
    private lateinit var recyclerView: RecyclerView

    override fun createView(ui: AnkoContext<BaseActivity>) = with(ui) {
        relativeLayout {
            lparams(width = matchParent, height = matchParent)

            // текст при пустой странице
            text = textView {
                id = Id.text
                gravity = Gravity.CENTER
                visibility = View.GONE
            }.lparams {
                centerInParent()
            }

            // кнопка перехода в каталог
            btn = button {
                visibility = View.GONE
            }.lparams {
                centerHorizontally()
                below(Id.text)
            }

            // список элементов
            recyclerView = recyclerView {
                lparams(width = matchParent, height = matchParent)
                setHasFixedSize(true)
            }
            bind()
        }
    }

    private fun bind() = launch(UI) {
        text.setText(R.string.library_help)
        btn.setText(R.string.library_help_go)

        btn.onClick {
            log("start activity")
            btn.context.startActivity<SiteCatalogActivity>()
        }

        Main.db.mangaDao
            .loadMangas(category, MangaFilter.ADD_TIME_ASC)
            .observe(act, Observer {
                launch {
                    val isVisible = it != null && it.isEmpty()

                    launch(UI) {
                        text.visibleOrGone(isVisible)
                        btn.visibleOrGone(isVisible)
                        recyclerView.visibleOrGone(isVisible.not())
                    }
                }
            })

        val portrait =
            async { act.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT }
        span =
                async {
                    if (portrait.await()) category.spanPortrait else category.spanLandscape
                }.await()
        isLarge =
                async {
                    if (portrait.await()) category.isLargePortrait else category.isLargeLandscape
                }.await()

        recyclerView.layoutManager = GridLayoutManager(act, span)
        adapter.intoIsList(recyclerView, isLarge)

        Main.db.categoryDao
            .loadLiveCategory(category.name)
            .observe(act, Observer {
                launch {
                    it?.let {
                        val newSpan = if (portrait.await()) it.spanPortrait else it.spanLandscape
                        val newIsLarge =
                            if (portrait.await()) it.isLargePortrait else it.isLargeLandscape

                        if (span != newSpan || isLarge != newIsLarge) {
                            span = newSpan
                            isLarge = newIsLarge
                            launch(UI) {
                                recyclerView.layoutManager = GridLayoutManager(act, span)
                                adapter.intoIsList(recyclerView, isLarge)
                            }
                        }
                    }
                }
            })

    }
}
