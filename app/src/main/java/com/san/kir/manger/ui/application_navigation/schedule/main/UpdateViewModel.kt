package com.san.kir.manger.ui.application_navigation.schedule.main

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.san.kir.data.db.dao.MangaDao
import com.san.kir.manger.data.room.entities.Manga
import com.san.kir.core.utils.coroutines.defaultLaunchInVM
import com.san.kir.core.utils.coroutines.withMainContext
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class UpdateViewModel @Inject constructor(
    private val mangaDao: com.san.kir.data.db.dao.MangaDao,
) : ViewModel() {
    var items by mutableStateOf(listOf<Manga>())
        private set

    init {
        mangaDao.loadItems()
            .distinctUntilChanged()
            .onEach { com.san.kir.core.utils.coroutines.withMainContext { items = it } }
            .launchIn(viewModelScope)
    }

    fun update(item: Manga) = defaultLaunchInVM {
        mangaDao.update(item)
    }
}
