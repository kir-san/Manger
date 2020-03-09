package com.san.kir.manger.components.category

import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.setMargins
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import com.san.kir.ankofork.design.floatingActionButton
import com.san.kir.ankofork.dip
import com.san.kir.ankofork.matchParent
import com.san.kir.ankofork.recyclerview.recyclerView
import com.san.kir.ankofork.sdk28.frameLayout
import com.san.kir.ankofork.sdk28.onClick
import com.san.kir.ankofork.wrapContent
import com.san.kir.manger.R
import com.san.kir.manger.utils.extensions.doOnApplyWindowInstets

class CategoryView(private val mAdapter: CategoryRecyclerPresenter) {
     fun view(view: LinearLayout) = with(view) {
         frameLayout {
             lparams(width = matchParent, height = matchParent)

             recyclerView {
                 lparams(width = matchParent, height = matchParent)
                 setHasFixedSize(true)
                 layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
                 mAdapter.into(this)

                 clipToPadding = false

                 doOnApplyWindowInstets { view, insets, padding ->
                     view.updatePadding(bottom = padding.bottom + insets.systemWindowInsetBottom)
                     insets
                 }
             }

             floatingActionButton {
                 setImageResource(R.drawable.ic_add)
                 onClick {
                     mAdapter.addCategory()
                 }

                 doOnApplyWindowInstets { view, insets, _ ->
                     view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                         bottomMargin = insets.systemWindowInsetBottom + dip(16)
                     }
                     insets
                 }
             }.lparams(width = wrapContent, height = wrapContent) {
                 gravity = Gravity.BOTTOM or Gravity.END
                 setMargins(dip(16))
             }
         }
    }
}
