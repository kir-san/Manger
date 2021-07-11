package com.san.kir.manger.ui

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {
    private val _catalogReceiver = MutableStateFlow("")
    val catalogReceiver: StateFlow<String>
        get() = _catalogReceiver

    fun catalogReceiver(value: String?) {
        _catalogReceiver.value = value ?: ""
    }
}

val LocalBaseViewModel = staticCompositionLocalOf<MainViewModel> { error("No MainViewModel found!") }
