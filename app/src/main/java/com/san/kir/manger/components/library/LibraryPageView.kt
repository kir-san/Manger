package com.san.kir.manger.components.library

import android.arch.lifecycle.Observer
import android.content.res.Configuration
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.san.kir.manger.R
import com.san.kir.manger.components.sites_catalog.SiteCatalogActivity
import com.san.kir.manger.extending.BaseActivity
import com.san.kir.manger.extending.anko_extend.onClick
import com.san.kir.manger.extending.anko_extend.visibleOrGone
import com.san.kir.manger.room.models.Category
import com.san.kir.manger.utils.AnkoActivityComponent
import com.san.kir.manger.utils.ID
import com.san.kir.manger.utils.enums.MangaFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.above
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
    private lateinit var text1: TextView
    private lateinit var text2: TextView
    private lateinit var btn: Button
    private lateinit var recyclerView: RecyclerView

    override fun createView(ui: AnkoContext<BaseActivity>) = with(ui) {
        relativeLayout {
            lparams(width = matchParent, height = matchParent)

            // кнопка перехода в каталог
            btn = button(R.string.library_help_go) {
                id = ID.generate()
                gravity = Gravity.CENTER
                visibility = View.GONE
            }.lparams {
                centerInParent()

            }

            // текст при пустой странице
            text1 = textView(R.string.library_help) {
                gravity = Gravity.CENTER
                visibility = View.GONE
                textSize = 16.5f
            }.lparams {
                above(btn)
                centerHorizontally()
            }

            text2 = textView(R.string.library_help2) {
                gravity = Gravity.CENTER
                visibility = View.GONE
                textSize = 16.5f
            }.lparams {
                below(btn)
                centerHorizontally()
            }


            // список элементов
            recyclerView = recyclerView {
                lparams(width = matchParent, height = matchParent)
                setHasFixedSize(true)
            }
            bind()
        }
    }

    private fun bind() = act.launch(Dispatchers.Main) {
        btn.onClick {
            btn.context.startActivity<SiteCatalogActivity>()
        }

        act.mViewModel
            .loadMangas(category, MangaFilter.ABC_SORT_ASC)
            .observe(act, Observer {
                act.launch(Dispatchers.Default) {
                    val isVisible = it != null && it.isEmpty()

                    withContext(Dispatchers.Main) {
                        text1.visibleOrGone(isVisible)
                        text2.visibleOrGone(isVisible)
                        btn.visibleOrGone(isVisible)
                        recyclerView.visibleOrGone(isVisible.not())
                    }
                }
            })

        val portrait = act.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
        val span = if (portrait) category.spanPortrait else category.spanLandscape
        val isLarge = if (portrait) category.isLargePortrait else category.isLargeLandscape

        recyclerView.layoutManager = (
                if (isLarge) GridLayoutManager(act, span)
                else LinearLayoutManager(act)
                ).apply { initialPrefetchItemCount = 40 }

        adapter.intoIsList(recyclerView, isLarge)
    }
}
