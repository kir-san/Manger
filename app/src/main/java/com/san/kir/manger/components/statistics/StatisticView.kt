package com.san.kir.manger.components.statistics

import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import com.san.kir.ankofork.matchParent
import com.san.kir.ankofork.recyclerview.recyclerView
import com.san.kir.ankofork.sdk28._LinearLayout
import com.san.kir.manger.utils.extensions.doOnApplyWindowInstets

class StatisticView(private val mAdapter: StatisticRecyclerPresenter) {
    fun view(view: _LinearLayout) = with(view) {
        recyclerView {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            mAdapter.into(this)
            lparams(matchParent, matchParent)

            clipToPadding = false

            doOnApplyWindowInstets { view, insets, padding ->
                view.updatePadding(bottom = padding.bottom + insets.systemWindowInsetBottom)
                insets
            }
        }
    }
}
