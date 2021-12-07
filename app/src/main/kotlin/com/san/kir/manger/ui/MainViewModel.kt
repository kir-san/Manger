package com.san.kir.manger.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.san.kir.data.store.MainStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    main: MainStore,
) : ViewModel() {

    private val _darkTheme = MutableStateFlow(true)
    val darkTheme = _darkTheme.asStateFlow()

    init {
        main.data
            .onEach { data ->
                _darkTheme.update { data.theme }
            }.launchIn(viewModelScope)
    }
}
