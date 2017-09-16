package com.san.kir.manger.components.Storage

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.san.kir.manger.utils.DIR
import com.san.kir.manger.utils.getFullPath
import com.san.kir.manger.utils.getShortPath
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.runBlocking
import java.io.File

class StorageMangaDirAdapter(private val storageFragment: StorageMangaDirFragment,
                             dir: String? = null) :
        RecyclerView.Adapter<StorageMangaDirViewHolder>() {

    private var catalog: List<StorageItem> = listOf()

    init {
        setHasStableIds(true)
        runBlocking(CommonPool) {
            catalog =
                    if (dir != null) getCatalog(dir)
                    else getCatalog()

            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StorageMangaDirViewHolder {
        return StorageMangaDirViewHolder(StorageMangaDirItemView(storageFragment), parent)
    }

    override fun onBindViewHolder(holder: StorageMangaDirViewHolder, position: Int) {
        holder.bind(catalog[position])
    }

    override fun getItemCount(): Int = catalog.size
    override fun getItemId(position: Int): Long = position.toLong()
    override fun getItemViewType(position: Int) = position

    private suspend fun getCatalog(dir: String) = getCatalog(getFullPath(dir))
    private suspend fun getCatalog(dir: File) = dir
            .listFiles()
            .map {
                StorageItem(name = it.name,
                            path = getShortPath(it))
            }


    private suspend fun getCatalog(): List<StorageItem> {
        return getFullPath(DIR.MANGA)
                .listFiles()
                .fold(emptyList()) { list, dir ->
                    list + getCatalog(dir)
                }
    }
}

