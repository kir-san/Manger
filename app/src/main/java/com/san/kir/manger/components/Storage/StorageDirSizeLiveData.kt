package com.san.kir.manger.components.Storage

import android.arch.lifecycle.LiveData
import com.san.kir.manger.utils.lengthMb
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import java.io.File

class StorageDirSizeLiveData(private val file: File) : LiveData<Long>() {

    override fun onActive() {
        update()
    }

    fun update() = async(CommonPool) {
        postValue(file.lengthMb)
    }

}
