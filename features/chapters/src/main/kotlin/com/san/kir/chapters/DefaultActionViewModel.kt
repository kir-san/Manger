package com.san.kir.chapters

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.san.kir.core.download.DownloadService
import com.san.kir.core.support.ChapterStatus
import com.san.kir.core.utils.coroutines.defaultLaunch
import com.san.kir.core.utils.coroutines.withMainContext
import com.san.kir.core.utils.toast
import com.san.kir.data.db.dao.ChapterDao
import com.san.kir.data.db.dao.MangaDao
import com.san.kir.data.models.base.Manga
import com.san.kir.data.models.base.action
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DefaultActionViewModel @Inject constructor(
    private val mangaDao: MangaDao,
    private val chapterDao: ChapterDao,
    private val context: Application,
) : ViewModel() {

    private val mangaUnic = MutableStateFlow("")

    fun setMangaUnic(manga: String) {
        mangaUnic.update { manga }
    }

    val manga = mangaUnic
        .filterNot { it.isEmpty() }
        .flatMapLatest { mangaDao.loadItemByName(it) }
        .filterNotNull()
        .stateIn(viewModelScope, SharingStarted.Lazily, Manga())

    fun downloadNextNotReadChapter() = viewModelScope.defaultLaunch {
        val chapter = chapterDao
            .getItemsNotReadAsc(mangaUnic.value)
            .first { it.action == ChapterStatus.DOWNLOADABLE }

        DownloadService.start(context, chapter)
    }

    fun downloadAllNotReadChapters() = viewModelScope.defaultLaunch {
        val count = chapterDao
            .getItemsNotReadAsc(mangaUnic.value)
            .filter { it.action == ChapterStatus.DOWNLOADABLE }
            .onEach { chapter ->
                DownloadService.start(context, chapter)
            }
            .size

        withMainContext {
            if (count == 0)
                context.toast(R.string.list_chapters_selection_load_error)
            else
                context.toast(context.getString(R.string.list_chapters_selection_load_ok, count))
        }
    }

    fun downloadAllChapters() = viewModelScope.defaultLaunch {
        val count = chapterDao
            .getItemsNotReadAsc(mangaUnic.value)
            .filter { it.action == ChapterStatus.DOWNLOADABLE }
            .onEach { chapter ->
                DownloadService.start(context, chapter)
            }
            .size
        withMainContext {
            if (count == 0)
                context.toast(R.string.list_chapters_selection_load_error)
            else
                context.toast(context.getString(R.string.list_chapters_selection_load_ok, count))
        }
    }

    fun updateManga(action: (Manga) -> Manga) = viewModelScope.defaultLaunch {
        mangaDao.update(action(manga.value))
    }
}
