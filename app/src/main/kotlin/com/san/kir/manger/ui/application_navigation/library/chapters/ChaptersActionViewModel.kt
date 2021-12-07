package com.san.kir.manger.ui.application_navigation.library.chapters

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.san.kir.ankofork.dialogs.toast
import com.san.kir.core.support.ChapterStatus
import com.san.kir.core.utils.coroutines.defaultLaunch
import com.san.kir.core.utils.coroutines.withDefaultContext
import com.san.kir.data.db.dao.ChapterDao
import com.san.kir.data.db.dao.MangaDao
import com.san.kir.data.models.Manga
import com.san.kir.data.models.action
import com.san.kir.manger.R
import com.san.kir.manger.foreground_work.services.DownloadService
import com.san.kir.manger.ui.MainActivity
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.updateAndGet

class ChaptersActionViewModel @AssistedInject constructor(
    @Assisted private val mangaUnic: String,
    private val mangaDao: MangaDao,
    private val chapterDao: ChapterDao,
    private val context: Application,
) : ViewModel() {

    private val _manga = MutableStateFlow(Manga())
    val manga = _manga.asStateFlow()

    init {
        viewModelScope.defaultLaunch {
            mangaDao.loadItem(mangaUnic)
                .filterNotNull()
                .collect { manga ->
                    _manga.value = manga
                }
        }
    }

    fun downloadNextNotReadChapter() = viewModelScope.defaultLaunch {
        val chapter = chapterDao
            .getItemsNotReadAsc(mangaUnic)
            .first { it.action == ChapterStatus.DOWNLOADABLE }

        DownloadService.start(context, chapter)
    }

    fun downloadAllNotReadChapters() = viewModelScope.defaultLaunch {
        val count = chapterDao
            .getItemsNotReadAsc(mangaUnic)
            .filter { it.action == ChapterStatus.DOWNLOADABLE }
            .onEach { chapter ->
                DownloadService.start(context, chapter)
            }
            .size

        withDefaultContext {
            if (count == 0)
                context.toast(R.string.list_chapters_selection_load_error)
            else
                context.toast(context.getString(R.string.list_chapters_selection_load_ok, count))
        }
    }

    fun downloadAllChapters() = viewModelScope.defaultLaunch {
        val count = chapterDao
            .getItemsNotReadAsc(mangaUnic)
            .filter { it.action == ChapterStatus.DOWNLOADABLE }
            .onEach { chapter ->
                DownloadService.start(context, chapter)
            }
            .size
        withDefaultContext {
            if (count == 0)
                context.toast(R.string.list_chapters_selection_load_error)
            else
                context.toast(context.getString(R.string.list_chapters_selection_load_ok, count))
        }
    }

    fun updateManga(action: (Manga) -> Manga) = viewModelScope.defaultLaunch {
        mangaDao.update(_manga.updateAndGet(action))
    }


    @AssistedFactory
    interface Factory {
        fun create(mangaUnic: String): ChaptersActionViewModel
    }

    @Suppress("UNCHECKED_CAST")
    companion object {
        fun provideFactory(
            assistedFactory: Factory,
            mangaUnic: String
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return assistedFactory.create(mangaUnic) as T
            }
        }
    }
}

@Composable
fun chaptersActionViewModel(mangaUnic: String): ChaptersActionViewModel {
    val factory = EntryPointAccessors.fromActivity(
        LocalContext.current as MainActivity,
        MainActivity.ViewModelFactoryProvider::class.java,
    ).chaptersActionViewModelFactory()

    return viewModel(factory = ChaptersActionViewModel.provideFactory(factory, mangaUnic))
}