package com.san.kir.manger.ui.application_navigation.schedule.main

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.san.kir.manger.room.dao.MangaDao
import com.san.kir.manger.room.entities.Manga
import com.san.kir.manger.utils.coroutines.defaultLaunchInVM
import com.san.kir.manger.utils.coroutines.withMainContext
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class UpdateViewModel @Inject constructor(
    private val mangaDao: MangaDao,
) : ViewModel() {
    var items by mutableStateOf(listOf<Manga>())
        private set

    init {
        mangaDao.loadItems()
            .distinctUntilChanged()
            .onEach { withMainContext { items = it } }
            .launchIn(viewModelScope)
    }

    fun update(item: Manga) = defaultLaunchInVM {
        mangaDao.update(item)
    }
}
