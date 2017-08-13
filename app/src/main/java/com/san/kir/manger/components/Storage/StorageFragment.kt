package com.san.kir.manger.components.Storage

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.san.kir.manger.R
import com.san.kir.manger.dbflow.wrapers.MangaWrapper
import com.san.kir.manger.utils.DIR
import com.san.kir.manger.utils.SET_MEMORY
import com.san.kir.manger.utils.getShortPath
import com.san.kir.manger.utils.lengthMb
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import java.io.File

class StorageFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return RecyclerView(context).apply {
            launch(UI) {
                layoutManager = LinearLayoutManager(this@apply.context)
                adapter = StorageAdapter(getParentList())
            }
        }
    }

    override fun onResume() {
        super.onResume()
    }

    private suspend fun getChildList(path: String): MutableList<StorageItem> {
        fun isNew(file: File): Boolean {
            return !MangaWrapper.getAllPath().contains(getShortPath(file))
        }

        val tempList = File(SET_MEMORY, path).listFiles()
        val childList = mutableListOf<StorageItem>()
        if (tempList.isNotEmpty())
            tempList.forEach {
                childList.add(StorageItem(
                        name = it.name,
                        path = getShortPath(it),
                        size = it.lengthMb,
                        isNew = isNew(it)))
            }

        return childList
    }

    private suspend fun getParentList(): MutableList<StorageParentItem> {
        val localName = getString(R.string.storage_local)
        val parentList = mutableListOf<StorageParentItem>()
        val childList1 = getChildList(DIR.LOCAL)
        var size1 = 0f
        childList1.forEach { size1 += it.size }
        parentList.add(StorageParentItem("$localName: $size1 Мб", childList1))
        val mangaList = File(SET_MEMORY, DIR.MANGA).listFiles { _, s -> s != "local" }
        if (mangaList.isNotEmpty())
            mangaList.forEach {
                val childList = getChildList("${DIR.MANGA}/${it.name}")
                var size = 0f
                childList.forEach { size += it.size }
                parentList.add(StorageParentItem("${it.name}: $size Мб", childList))
            }
        return parentList
    }
}

