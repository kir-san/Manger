package com.san.kir.manger.components.schedule

import android.support.v7.widget.RecyclerView
import com.san.kir.manger.components.main.Main
import com.san.kir.manger.utils.RecyclerPresenter
import com.san.kir.manger.utils.RecyclerViewAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AvailableUpdateRecyclerPresenter : RecyclerPresenter() {
    private val adapter = RecyclerViewAdapterFactory.createSimple { AvailableUpdateItemView() }

    override fun into(recyclerView: RecyclerView) {
        super.into(recyclerView)
        GlobalScope.launch(Dispatchers.Main) {
            recycler.adapter = adapter
            adapter.items = withContext(Dispatchers.Default) { Main.db.mangaDao.loadAllManga() }
            adapter.notifyDataSetChanged()
        }

    }
}
