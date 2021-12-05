package com.san.kir.manger.ui.application_navigation.additional_manga_screens

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.san.kir.data.db.dao.ChapterDao
import com.san.kir.data.db.dao.MangaDao
import com.san.kir.data.db.dao.StorageDao
import com.san.kir.data.db.dao.searchNewItems
import com.san.kir.manger.data.room.entities.Manga
import com.san.kir.manger.data.room.entities.Storage
import com.san.kir.manger.data.room.entities.getSizeAndIsNew
import com.san.kir.manger.ui.MainActivity
import com.san.kir.core.utils.coroutines.defaultDispatcher
import com.san.kir.core.utils.coroutines.defaultLaunchInVM
import com.san.kir.manger.utils.extensions.getFullPath
import com.san.kir.manger.utils.extensions.shortPath
import com.san.kir.manger.foreground_work.workmanager.AllChapterDelete
import com.san.kir.manger.foreground_work.workmanager.ChapterDeleteWorker
import com.san.kir.manger.foreground_work.workmanager.ReadChapterDelete
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

@OptIn(FlowPreview::class)
class MangaStorageViewModel @AssistedInject constructor(
    @Assisted private val mangaUnic: String,
    private val storageDao: com.san.kir.data.db.dao.StorageDao,
    private val mangaDao: com.san.kir.data.db.dao.MangaDao,
    private val chapterDao: com.san.kir.data.db.dao.ChapterDao,
    private val ctx: Application,
) : ViewModel() {

    private val _manga = MutableStateFlow(Manga())
    val manga = _manga.asStateFlow()

    private val _storage = MutableStateFlow(Storage())
    val storage = _storage.asStateFlow()

    init {
        defaultLaunchInVM {
            mangaDao
                .loadItem(mangaUnic)
                .filterNotNull()
                .onEach { manga ->
                    _manga.update { manga }
                }
                .map { manga -> getFullPath(manga.path).shortPath }
                .flatMapMerge { path -> storageDao.flowItem(path) }
                .mapNotNull { it }
                .onEach { storage ->
                    _storage.update { storage }
                    storage.let { s ->
                        defaultLaunchInVM {
                            val updatedS = s.getSizeAndIsNew(mangaDao, chapterDao)

                            if (s.sizeFull != updatedS.sizeFull || s.sizeRead != updatedS.sizeRead) {
                                storageDao.update(updatedS)
                            }
                        }
                    }
                }
                .collect()
        }

        defaultLaunchInVM {
            storageDao.searchNewItems(mangaDao, chapterDao)
        }
    }

    val generalSize: Flow<Double> =
        storageDao
            .flowItems()
            .map { list -> list.sumOf { item -> item.sizeFull } }
            .flowOn(com.san.kir.core.utils.coroutines.defaultDispatcher)

    fun deleteChapters(type: DeleteStatus) {
        when (type) {
            DeleteStatus.All ->
                ChapterDeleteWorker.addTask<AllChapterDelete>(ctx, manga.value)
            DeleteStatus.Read ->
                ChapterDeleteWorker.addTask<ReadChapterDelete>(ctx, manga.value)
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(mangaUnic: String): MangaStorageViewModel
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
fun mangaStorageViewModel(mangaUnic: String): MangaStorageViewModel {
    val factory = EntryPointAccessors.fromActivity(
        LocalContext.current as MainActivity,
        MainActivity.ViewModelFactoryProvider::class.java,
    ).mangaStorageViewModelFactory()

    return viewModel(factory = MangaStorageViewModel.provideFactory(factory, mangaUnic))
}
