package com.san.kir.features.shikimori.logic

import com.san.kir.data.models.base.ShikimoriMangaItem
import com.san.kir.features.shikimori.logic.useCases.BindStatus
import com.san.kir.features.shikimori.logic.useCases.CheckingStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

internal interface Helper<T : ShikimoriMangaItem> {

    val unbindedItems: StateFlow<List<BindStatus<T>>>

    val hasAction: StateFlow<BackgroundTasks>

    fun send(checkingState: Boolean): (List<BindStatus<T>>) -> Unit

    fun send(): (CheckingStatus<T>) -> Unit

    fun updateLoading(loading: Boolean)

}

internal class HelperImpl<T : ShikimoriMangaItem> : Helper<T> {

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

    override fun send(): (CheckingStatus<T>) -> Unit = {
        it.items?.let { items -> _unbindedItems.value = items }
        _hasAction.update { old ->
            old.copy(checkBind = it.progress != null, progress = it.progress)
        }
    }

    override fun updateLoading(loading: Boolean) {
        _hasAction.update { old -> old.copy(loading = loading) }
    }
}

data class BackgroundTasks(
    val loading: Boolean = true,
    val checkBind: Boolean = true,
    val progress: Float? = null
)
