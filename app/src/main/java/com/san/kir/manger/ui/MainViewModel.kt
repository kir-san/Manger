package com.san.kir.manger.ui

import android.content.Intent
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.ViewModel
import com.san.kir.manger.services.MangaUpdaterService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {
    private val _catalogReceiver = MutableStateFlow("")
    val catalogReceiver = _catalogReceiver.asStateFlow()

    fun catalogReceiver(value: String?) {
        _catalogReceiver.value = value ?: ""
    }

    private val _chaptersReceiver = MutableStateFlow(ChaptersMessage())
    val chaptersReceiver = _chaptersReceiver.asStateFlow()

    fun chaptersReceiver(intent: Intent) {
        _chaptersReceiver.value = ChaptersMessage(
            mangaName = intent.getStringExtra(MangaUpdaterService.ITEM_NAME) ?: "",
            isFoundNew = intent.getBooleanExtra(MangaUpdaterService.IS_FOUND_NEW, false),
            countNew = intent.getIntExtra(MangaUpdaterService.COUNT_NEW, 0),
        )
    }
}

data class ChaptersMessage(
    val mangaName: String = "",
    val isFoundNew: Boolean = false,
    val countNew: Int = -1,
)

val LocalBaseViewModel =
    staticCompositionLocalOf<MainViewModel> { error("No MainViewModel found!") }
