package com.san.kir.features.shikimori

import com.san.kir.data.models.base.ShikimoriMangaItem
import com.san.kir.features.shikimori.useCases.BindStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

interface Helper<T : ShikimoriMangaItem> {

    val unbindedItems: StateFlow<List<BindStatus<T>>>

    val hasAction: StateFlow<BackgroundTasks>

    fun send(checkingState: Boolean): (List<BindStatus<T>>) -> Unit

    fun updateLoading(loading: Boolean)

}

class HelperImpl<T : ShikimoriMangaItem> : Helper<T> {

    // Манга без привязки
    private val _unbindedItems = MutableStateFlow(emptyList<BindStatus<T>>())
    override val unbindedItems = _unbindedItems.asStateFlow()

    // Индикация о выполнении фоновых операций
    private val _hasAction = MutableStateFlow(BackgroundTasks())
    override val hasAction = _hasAction.asStateFlow()

    override fun send(checkingState: Boolean): (List<BindStatus<T>>) -> Unit = {
        _unbindedItems.value = it
        _hasAction.update { old -> old.copy(checkBind = checkingState) }
    }

    override fun updateLoading(loading: Boolean) {
        _hasAction.update { old -> old.copy(loading = loading) }
    }
}

data class BackgroundTasks(
    val loading: Boolean = true,
    val checkBind: Boolean = true,
)
