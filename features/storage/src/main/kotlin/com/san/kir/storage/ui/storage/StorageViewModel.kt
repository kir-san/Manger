package com.san.kir.storage.ui.storage

import android.app.Application
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.san.kir.background.works.AllChapterDelete
import com.san.kir.background.works.ChapterDeleteWorker
import com.san.kir.background.works.ReadChapterDelete
import com.san.kir.background.works.StoragesUpdateWorker
import com.san.kir.core.utils.coroutines.defaultDispatcher
import com.san.kir.core.utils.getFullPath
import com.san.kir.core.utils.shortPath
import com.san.kir.core.utils.viewModel.BaseViewModel
import com.san.kir.data.models.base.Manga
import com.san.kir.data.models.base.Storage
import com.san.kir.storage.logic.repo.StorageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
internal class StorageViewModel @Inject constructor(
    private val context: Application,
    private val storageRepository: StorageRepository,
) : BaseViewModel<StorageEvent, StorageState>() {

    private val backgroundState = MutableStateFlow<BackgroundState>(BackgroundState.None)
    private val storage = MutableStateFlow(Storage())
    private val manga = MutableStateFlow(Manga())

    override val tempState = combine(
        backgroundState,
        storageRepository.fullSize.distinctUntilChanged(),
        storage,
        manga
    ) { background, size, storage, manga ->
        StorageState(
            background = background,
            mangaName = manga.name,
            item = storage,
            size = size,
        )
    }
    override val defaultState = StorageState()

    override suspend fun onEvent(event: StorageEvent) {
        when (event) {
            is StorageEvent.Set     -> set(event.mangaId, event.hasUpdate)

            StorageEvent.DeleteAll  ->
                ChapterDeleteWorker.addTask<AllChapterDelete>(context, manga.value)

            StorageEvent.DeleteRead ->
                ChapterDeleteWorker.addTask<ReadChapterDelete>(context, manga.value)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun set(mangaId: Long, hasUpdate: Boolean) {
        if (hasUpdate) StoragesUpdateWorker.runTask(context)

        storageRepository
            .loadManga(mangaId)
            .filterNotNull()
            .onEach { manga.value = it }
            .flatMapLatest { manga ->
                storageRepository.storageFromFile(getFullPath(manga.path).shortPath)
            }
            .filterNotNull()
            .onEach { storage.value = it }
            .flowOn(defaultDispatcher)
            .launchIn(viewModelScope)

        combine(
            WorkManager.getInstance(context)
                .getWorkInfosByTagLiveData(StoragesUpdateWorker.tag)
                .asFlow(),
            WorkManager.getInstance(context)
                .getWorkInfosByTagLiveData(ChapterDeleteWorker.tag)
                .asFlow(),
        ) { stors, chaps ->
            if (chaps.any { it.state.isFinished.not() }) {
                backgroundState.update { BackgroundState.Deleting }
            } else if (stors.any { it.state.isFinished.not() }) {
                backgroundState.update { BackgroundState.Load }
            } else {
                backgroundState.update { BackgroundState.None }
            }
        }.launchIn(viewModelScope)
    }
}
