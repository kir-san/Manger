package com.san.kir.manger.ui.application_navigation.schedule.main

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.san.kir.core.utils.coroutines.defaultLaunch
import com.san.kir.core.utils.coroutines.withMainContext
import com.san.kir.data.db.dao.MangaDao
import com.san.kir.data.models.base.Manga
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

    fun update(item: Manga) = viewModelScope.defaultLaunch {
        mangaDao.update(item)
    }
}
