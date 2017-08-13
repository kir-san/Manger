package com.san.kir.manger.components.LatestChapters

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.san.kir.manger.R
import com.san.kir.manger.components.ChaptersDownloader.ChaptersDownloader
import com.san.kir.manger.utils.showNever

class LatestChaptersFragment : Fragment() {
    private val adapter by lazy {
        LatestChaptersAdapter()
    }
    private val recyclerView by lazy {
        RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@LatestChaptersFragment.adapter
        }
    }

    override fun onCreateView(inflater: LayoutInflater?,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        ChaptersDownloader.bus.register(2)
        setHasOptionsMenu(true)
        titleUpdate()
        return recyclerView
    }

    private fun titleUpdate() {
        if (adapter.itemCount > 0)
            activity.title = getString(R.string.main_menu_latest_count, adapter.itemCount)
        else
            activity.setTitle(R.string.main_menu_latest)
    }

    override fun onDestroyView() {
        ChaptersDownloader.bus.unregister(2)
        super.onDestroyView()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater?) {
        menu.clear()
        menu.add(0, 0, 0, "Скачать новое")
                .showNever()
                .setIcon(R.drawable.ic_action_download_white)
                .apply {
                    if (!adapter.hasNewChapters()) isEnabled = false
                }
        menu.add(1, 1, 1, "Очистить")
                .showNever()
                .setIcon(R.drawable.ic_action_delete_white)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            0 -> {
                adapter.downloadNewChapters()
                item.isEnabled = false
            }
            1 -> {
                adapter.clearHistory()
                titleUpdate()
            }
        }
        return true
    }
}
