package com.san.kir.manger.components.DownloadManager

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
import com.san.kir.manger.utils.showAlways
import org.jetbrains.anko.support.v4.act

class DownloadManagerFragment : Fragment() {
    private val downAdap = DownloadManagerAdapter()

    override fun onCreateView(inflater: LayoutInflater?,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        ChaptersDownloader.bus.register(1)
        setHasOptionsMenu(true)

        downAdap.onSizeChanged {
            if (it > 0)
                act.title = getString(R.string.main_menu_downloader_count, it)
            else
                act.setTitle(R.string.main_menu_downloader)
        }

        return RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
            adapter = downAdap
        }
    }

    override fun onDestroyView() {
        ChaptersDownloader.bus.unregister(1)
        super.onDestroyView()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater?) {
        menu.clear()
        menu.add(0, 0, 0, "Отменить все").showAlways()
                .setIcon(R.drawable.ic_cancel)

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == 0) {
            ChaptersDownloader.cancelAll()
            downAdap.removeAll()
        }
        return true
    }
}
