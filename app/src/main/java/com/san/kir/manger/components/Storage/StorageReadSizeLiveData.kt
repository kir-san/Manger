package com.san.kir.manger.components.Storage

import android.arch.lifecycle.LiveData
import com.san.kir.manger.dbflow.wrapers.ChapterWrapper
import com.san.kir.manger.utils.getFullPath
import com.san.kir.manger.utils.lengthMb
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch

class StorageReadSizeLiveData(private val mangaName: String) : LiveData<Long>() {
    override fun onActive() {
        update()
    }

    fun update() = launch(CommonPool) {
        postValue(ChapterWrapper.getChapters(mangaName)
                          .filter { it.isRead }
                          .map { getFullPath(it.path) }
                          .map { it.lengthMb }
                          .sum())
    }
}
