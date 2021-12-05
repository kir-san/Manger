package com.san.kir.manger.ui.application_navigation.library.chapters

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.san.kir.ankofork.dialogs.toast
import com.san.kir.manger.R
import com.san.kir.data.db.dao.ChapterDao
import com.san.kir.data.db.dao.MangaDao
import com.san.kir.manger.data.room.entities.Manga
import com.san.kir.manger.foreground_work.services.DownloadService
import com.san.kir.manger.ui.MainActivity
import com.san.kir.core.utils.coroutines.defaultLaunchInVM
import com.san.kir.core.utils.coroutines.withDefaultContext
import com.san.kir.core.support.ChapterStatus
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.MutableStateFlow

class ChaptersActionViewModel @AssistedInject constructor(
    @Assisted private val mangaUnic: String,
    private val mangaDao: com.san.kir.data.db.dao.MangaDao,
    private val chapterDao: com.san.kir.data.db.dao.ChapterDao,
    private val context: Application,
) : ViewModel() {

    private val _manga = MutableStateFlow(Manga())
    val manga = _manga.asStateFlow()

    init {
        defaultLaunchInVM {
            mangaDao.loadItem(mangaUnic)
                .filterNotNull()
                .collect { manga ->
                    _manga.value = manga
                }
        }
    }

    fun downloadNextNotReadChapter() = defaultLaunchInVM {
        val chapter = chapterDao
            .getItemsNotReadAsc(mangaUnic)
            .first { it.action == com.san.kir.core.support.ChapterStatus.DOWNLOADABLE }

        DownloadService.start(context, chapter)
    }

    fun downloadAllNotReadChapters() = defaultLaunchInVM {
        val count = chapterDao
            .getItemsNotReadAsc(mangaUnic)
            .filter { it.action == com.san.kir.core.support.ChapterStatus.DOWNLOADABLE }
            .onEach { chapter ->
                DownloadService.start(context, chapter)
            }
            .size

        com.san.kir.core.utils.coroutines.withDefaultContext {
            if (count == 0)
                context.toast(R.string.list_chapters_selection_load_error)
            else
                context.toast(context.getString(R.string.list_chapters_selection_load_ok, count))
        }
    }

    fun downloadAllChapters() = defaultLaunchInVM {
        val count = chapterDao
            .getItemsNotReadAsc(mangaUnic)
            .filter { it.action == com.san.kir.core.support.ChapterStatus.DOWNLOADABLE }
            .onEach { chapter ->
                DownloadService.start(context, chapter)
            }
            .size
        com.san.kir.core.utils.coroutines.withDefaultContext {
            if (count == 0)
                context.toast(R.string.list_chapters_selection_load_error)
            else
                context.toast(context.getString(R.string.list_chapters_selection_load_ok, count))
        }
    }

    fun updateManga(action: (Manga) -> Manga) = defaultLaunchInVM {
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
