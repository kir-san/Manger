package com.san.kir.manger.ui.application_navigation.additional_manga_screens

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.san.kir.core.utils.coroutines.defaultLaunch
import com.san.kir.core.utils.log
import com.san.kir.data.db.dao.CategoryDao
import com.san.kir.data.db.dao.MangaDao
import com.san.kir.data.models.Manga
import com.san.kir.manger.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MangaEditViewModel @Inject constructor(
    ctx: Application,
    categoryDao: CategoryDao,
    private val mangaDao: MangaDao,
) : ViewModel() {
    var mangaUnic by mutableStateOf("")
    var manga by mutableStateOf(Manga())

    val categoryNames = categoryDao.loadItems().map { l -> l.map { it.name } }
    val statuses by lazy {
        listOf(
            ctx.getString(R.string.manga_status_unknown),
            ctx.getString(R.string.manga_status_continue),
            ctx.getString(R.string.manga_status_complete)
        )
    }

    init {
        snapshotFlow { mangaUnic }
            .flatMapLatest {
                log(it)
                val manga = mangaDao.loadItem(it)
                manga
            }
            .filterNotNull()
            .onEach {
                if (it.name.isNotEmpty())
                    com.san.kir.core.utils.coroutines.withMainContext {
                        manga = it
                    }
            }.launchIn(viewModelScope)
    }

    fun update() = viewModelScope.defaultLaunch { mangaDao.update(manga) }
}
