package com.san.kir.manger.ui.application_navigation.schedule.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.san.kir.core.utils.coroutines.defaultLaunch
import com.san.kir.data.db.dao.MangaDao
import com.san.kir.data.models.extend.MiniManga
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class UpdateViewModel @Inject constructor(
    private val mangaDao: MangaDao,
) : ViewModel() {
    val items = mangaDao
        .loadMiniItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    fun update(item: MiniManga, update: Boolean) = viewModelScope.defaultLaunch {
        mangaDao.update(item.id, update)
    }
}
