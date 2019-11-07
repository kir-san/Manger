package com.san.kir.manger.components.schedule

import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.san.kir.manger.utils.RecyclerPresenter
import com.san.kir.manger.utils.RecyclerViewAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AvailableUpdateRecyclerPresenter(private val act: ScheduleActivity) : RecyclerPresenter() {
    private val adapter = RecyclerViewAdapterFactory.createSimple { AvailableUpdateItemView(act) }

    override fun into(recyclerView: RecyclerView) {
        super.into(recyclerView)
        act.lifecycleScope.launchWhenResumed {
            recycler.adapter = adapter
            adapter.items = withContext(Dispatchers.Default) { act.mViewModel.getMangaItems() }
            adapter.notifyDataSetChanged()
        }

    }
}
