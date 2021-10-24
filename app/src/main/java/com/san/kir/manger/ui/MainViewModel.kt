package com.san.kir.manger.ui

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.san.kir.manger.data.datastore.MainRepository
import com.san.kir.manger.di.DefaultDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val main: MainRepository,
    @DefaultDispatcher private val default: CoroutineDispatcher
) : ViewModel() {
    private val _catalogReceiver = MutableStateFlow("")
    val catalogReceiver = _catalogReceiver.asStateFlow()

    fun catalogReceiver(value: String?) {
        _catalogReceiver.value = value ?: ""
    }

    private val _darkTheme = MutableStateFlow(true)
    val darkTheme = _darkTheme.asStateFlow()

    init {
        viewModelScope.launch(default) {
            main.data
                .collect { data ->
                    _darkTheme.update { data.theme }
                }
        }
    }
}

val LocalBaseViewModel =
    staticCompositionLocalOf<MainViewModel> { error("No MainViewModel found!") }
