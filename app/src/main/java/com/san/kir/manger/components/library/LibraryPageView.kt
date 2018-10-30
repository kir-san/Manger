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
import com.san.kir.manger.extending.ankoExtend.onClick
import com.san.kir.manger.extending.ankoExtend.visibleOrGone
import com.san.kir.manger.room.dao.MangaFilter
import com.san.kir.manger.room.dao.loadMangas
import com.san.kir.manger.room.models.Category
import com.san.kir.manger.utils.AnkoActivityComponent
import com.san.kir.manger.utils.ID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.below
import org.jetbrains.anko.button
import org.jetbrains.anko.centerHorizontally
import org.jetbrains.anko.centerInParent
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.relativeLayout
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

    private fun bind() = GlobalScope.launch(Dispatchers.Main) {
        text.setText(R.string.library_help)
        btn.setText(R.string.library_help_go)

        btn.onClick {
            btn.context.startActivity<SiteCatalogActivity>()
        }

        Main.db.mangaDao
            .loadMangas(category, MangaFilter.ADD_TIME_ASC)
            .observe(act, Observer {
                GlobalScope.launch(Dispatchers.Default) {
                    val isVisible = it != null && it.isEmpty()

                    withContext(Dispatchers.Main) {
                        text.visibleOrGone(isVisible)
                        btn.visibleOrGone(isVisible)
                        recyclerView.visibleOrGone(isVisible.not())
                    }
                }
            })

        val portrait =
            act.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT

        span = if (portrait) category.spanPortrait else category.spanLandscape

        isLarge = if (portrait) category.isLargePortrait else category.isLargeLandscape


        recyclerView.layoutManager = GridLayoutManager(act, span)
        adapter.intoIsList(recyclerView, isLarge)

        Main.db.categoryDao
            .loadLiveCategory(category.name)
            .observe(act, Observer { cat ->
                GlobalScope.launch(Dispatchers.Default) {
                    cat?.let {
                        val newSpan = if (portrait) it.spanPortrait else it.spanLandscape
                        val newIsLarge = if (portrait) it.isLargePortrait else it.isLargeLandscape

                        if (span != newSpan || isLarge != newIsLarge) {
                            span = newSpan
                            isLarge = newIsLarge
                            withContext(Dispatchers.Main) {
                                recyclerView.layoutManager = GridLayoutManager(act, span)
                                adapter.intoIsList(recyclerView, isLarge)
                            }
                        }
                    }
                }
            })
    }
}
