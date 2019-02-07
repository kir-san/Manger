package com.san.kir.manger.components.schedule

import android.support.v7.widget.LinearLayoutManager
import com.san.kir.manger.extending.BaseActivity
import com.san.kir.manger.utils.AnkoActivityComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.linearLayout
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.recyclerview.v7.recyclerView

class AvailableUpdateView(private val act: ScheduleActivity) : AnkoActivityComponent() {
    override fun createView(ui: AnkoContext<BaseActivity>) = with(ui) {
        linearLayout {
            lparams(width = matchParent, height = matchParent)

            recyclerView {
                layoutManager = LinearLayoutManager(context)
                setHasFixedSize(true)
                AvailableUpdateRecyclerPresenter(act).into(this)
            }.lparams(width = matchParent, height = matchParent)
        }
    }
}
