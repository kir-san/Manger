package com.san.kir.manger.components.library

import android.content.res.Configuration
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import com.san.kir.ankofork.AnkoContext
import com.san.kir.ankofork.matchParent
import com.san.kir.ankofork.recyclerview.recyclerView
import com.san.kir.ankofork.sdk28.button
import com.san.kir.ankofork.sdk28.onClick
import com.san.kir.ankofork.sdk28.textView
import com.san.kir.ankofork.startActivity
import com.san.kir.ankofork.verticalLayout
import com.san.kir.ankofork.wrapContent
import com.san.kir.manger.R
import com.san.kir.manger.components.sites_catalog.SiteCatalogActivity
import com.san.kir.manger.room.entities.Category
import com.san.kir.manger.utils.ActivityView
import com.san.kir.manger.utils.ID
import com.san.kir.manger.utils.extensions.BaseActivity
import com.san.kir.manger.utils.extensions.doOnApplyWindowInstets
import com.san.kir.manger.utils.extensions.visibleOrGone
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LibraryPageView(
    val adapter: LibraryItemsRecyclerPresenter,
    val category: Category,
    val act: LibraryActivity
) : ActivityView() {
    private lateinit var text1: TextView
    private lateinit var text2: TextView
    private lateinit var btn: Button
    private lateinit var recyclerView: androidx.recyclerview.widget.RecyclerView

    override fun createView(ui: AnkoContext<BaseActivity>): View {
        return with(ui) {
            verticalLayout {
                lparams(width = matchParent, height = matchParent)

                gravity = Gravity.CENTER_VERTICAL

                // текст при пустой странице
                text1 = textView(R.string.library_help) {
                    gravity = Gravity.CENTER
                    visibleOrGone(false)
                    textSize = 16.5f
                }

                // кнопка перехода в каталог
                btn = button(R.string.library_help_go) {
                    id = ID.generate()
                    visibleOrGone(false)
                }.lparams(width = wrapContent) {
                    gravity = Gravity.CENTER_HORIZONTAL
                }

                text2 = textView(R.string.library_help2) {
                    gravity = Gravity.CENTER
                    visibleOrGone(false)
                    textSize = 16.5f
                }


                // список элементов
                recyclerView = recyclerView {
                    lparams(width = matchParent, height = matchParent)
                    visibleOrGone(false)
                    setHasFixedSize(true)
                    clipToPadding = false

                    doOnApplyWindowInstets { view, insets, padding ->
                        view.updatePadding(bottom = padding.bottom + insets.systemWindowInsetBottom)
                        insets
                    }
                }



                bind()
            }
        }
    }

    private fun bind() {
        act.lifecycleScope.launch(Dispatchers.Main) {
            btn.onClick {
                btn.context.startActivity<SiteCatalogActivity>()
            }

            val portrait =
                act.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
            val span = if (portrait) category.spanPortrait else category.spanLandscape
            val isLarge = if (portrait) category.isLargePortrait else category.isLargeLandscape

            recyclerView.layoutManager = (
                    if (isLarge) androidx.recyclerview.widget.GridLayoutManager(act, span)
                    else androidx.recyclerview.widget.LinearLayoutManager(act)
                    ).apply { initialPrefetchItemCount = 40 }

            adapter.intoIsList(recyclerView, isLarge) { list ->
                act.lifecycleScope.launch(Dispatchers.Main) {
                    if (list != null) {
                        val isVisible = withContext(Dispatchers.Main) { list.isEmpty() }

                        text1.visibleOrGone(isVisible)
                        text2.visibleOrGone(isVisible)
                        btn.visibleOrGone(isVisible)
                        recyclerView.visibleOrGone(isVisible.not())

                    }
                }
            }
        }
    }
}
