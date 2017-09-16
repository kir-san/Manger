package com.san.kir.manger.components.Main

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    val title: MutableLiveData<String> = MutableLiveData()
}
