package com.san.kir.manger.components.schedule

import android.support.v7.widget.RecyclerView
import com.san.kir.manger.components.main.Main
import com.san.kir.manger.utils.RecyclerPresenter
import com.san.kir.manger.utils.RecyclerViewAdapterFactory
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch

class AvailableUpdateRecyclerPresenter : RecyclerPresenter() {
    private val adapter = RecyclerViewAdapterFactory.createSimple { AvailableUpdateItemView() }

    override fun into(recyclerView: RecyclerView) {
        super.into(recyclerView)
        launch(UI) {
            recycler.adapter = adapter
            adapter.items = async { Main.db.mangaDao.loadAllManga() }.await()
            adapter.notifyDataSetChanged()
        }

    }
}
