package com.san.kir.storage.ui.storages

import android.app.Application
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.san.kir.background.works.StoragesUpdateWorker
import com.san.kir.core.utils.viewModel.BaseViewModel
import com.san.kir.data.models.base.Storage
import com.san.kir.data.models.extend.MangaLogo
import com.san.kir.storage.logic.repo.StorageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class StoragesViewModel @Inject constructor(
    private val context: Application,
    private val storageRepository: StorageRepository
) : BaseViewModel<StoragesEvent, StoragesState>() {
    private var job: Job? = null
    private val mangas = MutableStateFlow(persistentListOf<MangaLogo?>())
    private val backgroundState = MutableStateFlow<BackgroundState>(BackgroundState.Load)

    override val tempState = combine(
        storageRepository.items.findMangaForStorage(),
        mangas,
        backgroundState
    ) { items, mangas, background ->
        StoragesState(items.toPersistentList(), mangas, background)
    }

    override val defaultState = StoragesState(
        items = persistentListOf(),
        mangas = persistentListOf(),
        background = BackgroundState.Load
    )

    override suspend fun onEvent(event: StoragesEvent) {
        when (event) {
            is StoragesEvent.Delete -> storageRepository.delete(event.item)
        }
    }

    init {
        StoragesUpdateWorker.runTask(context)

        WorkManager.getInstance(context)
            .getWorkInfosByTagLiveData(StoragesUpdateWorker.tag)
            .asFlow()
            .onEach { works ->
                if (works.isEmpty()) {
                    backgroundState.update { BackgroundState.None }
                } else {
                    if (works.all { it.state.isFinished }) {
                        backgroundState.update { BackgroundState.None }
                    } else {
                        backgroundState.update { BackgroundState.Load }
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun Flow<List<Storage>>.findMangaForStorage(): Flow<List<Storage>> =
        distinctUntilChanged()
            .onEach { items ->
                job?.cancel()
                job = viewModelScope.launch {
                    items.forEachIndexed { index, storage ->
                        mangas.update { items ->
                            if (items.getOrNull(index) == null)
                                items.add(index, storageRepository.mangaFromPath(storage.path))
                            else
                                items
                        }
                    }
                }
            }
}
