package com.san.kir.manger.components.latest_chapters

import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import com.san.kir.ankofork.dip
import com.san.kir.ankofork.horizontalProgressBar
import com.san.kir.ankofork.matchParent
import com.san.kir.ankofork.recyclerview.recyclerView
import com.san.kir.ankofork.sdk28._LinearLayout
import com.san.kir.manger.R
import com.san.kir.manger.utils.extensions.doOnApplyWindowInstets
import com.san.kir.manger.utils.extensions.gone

class LatestChapterView(private val mAdapter: LatestChaptersRecyclerPresenter) {
    lateinit var action: ProgressBar

    fun view(view: _LinearLayout) = view.apply {
        action = horizontalProgressBar {
            lparams(width = matchParent, height = dip(10))
            isIndeterminate = true
            gone()
            progressDrawable =
                ContextCompat.getDrawable(context, R.drawable.storage_progressbar)
        }
        recyclerView {
            lparams(width = matchParent, height = matchParent)
            layoutManager = LinearLayoutManager(context)
            mAdapter.into(this)

            clipToPadding = false

            doOnApplyWindowInstets { view, insets, padding ->
                view.updatePadding(bottom = padding.bottom + insets.systemWindowInsetBottom)
                insets
            }
        }
    }
}
