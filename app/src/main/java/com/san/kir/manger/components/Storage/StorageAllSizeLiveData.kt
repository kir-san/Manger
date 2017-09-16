package com.san.kir.manger.components.Storage

import android.arch.lifecycle.LiveData
import com.san.kir.manger.utils.DIR
import com.san.kir.manger.utils.getFullPath
import com.san.kir.manger.utils.lengthMb
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async

class StorageAllSizeLiveData : LiveData<Long>() {
    override fun onActive() {
        update()
    }

    fun update() = async(CommonPool) {
        postValue(getFullPath(DIR.MANGA).lengthMb)
    }
}
