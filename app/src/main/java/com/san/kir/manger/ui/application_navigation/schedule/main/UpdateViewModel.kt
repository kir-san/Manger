package com.san.kir.manger.ui.application_navigation.schedule.main

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.san.kir.manger.di.DefaultDispatcher
import com.san.kir.manger.di.MainDispatcher
import com.san.kir.manger.room.dao.MangaDao
import com.san.kir.manger.room.entities.Manga
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class UpdateViewModel @Inject constructor(
    private val mangaDao: MangaDao,
    @DefaultDispatcher private val default: CoroutineDispatcher,
    @MainDispatcher main: CoroutineDispatcher,
) : ViewModel() {
    var items by mutableStateOf(listOf<Manga>())
        private set

    init {
        mangaDao.loadItems()
            .distinctUntilChanged()
            .onEach { withContext(main) { items = it } }
            .launchIn(viewModelScope)
    }

    fun update(item: Manga) = viewModelScope.launch(default) {
        mangaDao.update(item)
    }
}
