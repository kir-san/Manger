package com.san.kir.manger.components.Storage

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.san.kir.manger.utils.DIR
import com.san.kir.manger.utils.getFullPath
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.runBlocking

class StorageMainDirAdapter(private val storageFragment: StorageMainDirFragment) : RecyclerView.Adapter<StorageMainDirViewHolder>() {

    private var catalog: List<StorageDir> = listOf()

    init {
        setHasStableIds(true)
        runBlocking(CommonPool) {
            catalog = getCatalog()
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StorageMainDirViewHolder {
        return StorageMainDirViewHolder(StorageMainDirItemView(storageFragment), parent)
    }

    override fun onBindViewHolder(holder: StorageMainDirViewHolder, position: Int) {
        holder.bind(catalog[position], storageFragment)
    }

    override fun getItemCount(): Int = catalog.size

    override fun getItemId(position: Int): Long = position.toLong()

    private suspend fun getCatalog() = getFullPath(DIR.MANGA)
            .listFiles()
            .map {
                StorageDir(name = it.name,
                           countDir = it.listFiles().filter { it.isDirectory }.size,
                           file = it)
            }
}
