package com.san.kir.manger.ui

import androidx.lifecycle.ViewModel
import com.san.kir.manger.data.datastore.MainRepository
import com.san.kir.manger.utils.coroutines.defaultLaunchInVM
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val main: MainRepository,
) : ViewModel() {

    private val _darkTheme = MutableStateFlow(true)
    val darkTheme = _darkTheme.asStateFlow()

    init {
        defaultLaunchInVM {
            main.data
                .collect { data ->
                    _darkTheme.update { data.theme }
                }
        }
    }
}
