package com.san.kir.manger.ui

import android.content.Intent
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.san.kir.manger.data.datastore.MainRepository
import com.san.kir.manger.services.MangaUpdaterService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val main: MainRepository,
) : ViewModel() {
    private val _catalogReceiver = MutableStateFlow("")
    val catalogReceiver = _catalogReceiver.asStateFlow()

    fun catalogReceiver(value: String?) {
        _catalogReceiver.value = value ?: ""
    }

    private val _chaptersReceiver = MutableSharedFlow<ChaptersMessage>()
    val chaptersReceiver = _chaptersReceiver.asSharedFlow()

    fun chaptersReceiver(intent: Intent) {
        _chaptersReceiver.tryEmit(
            ChaptersMessage(
                mangaName = intent.getStringExtra(MangaUpdaterService.ITEM_NAME) ?: "",
                isFoundNew = intent.getBooleanExtra(MangaUpdaterService.IS_FOUND_NEW, false),
                countNew = intent.getIntExtra(MangaUpdaterService.COUNT_NEW, 0),
            )
        )
    }

    private val _darkTheme = MutableStateFlow(true)
    val darkTheme = _darkTheme.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.Default) {
            main.data
                .collect { data ->
                    _darkTheme.update { data.theme }
                }
        }
    }
}

data class ChaptersMessage(
    val mangaName: String = "",
    val isFoundNew: Boolean = false,
    val countNew: Int = -1,
)

val LocalBaseViewModel =
    staticCompositionLocalOf<MainViewModel> { error("No MainViewModel found!") }
