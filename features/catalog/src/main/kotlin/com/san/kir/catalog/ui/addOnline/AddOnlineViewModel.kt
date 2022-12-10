package com.san.kir.catalog.ui.addOnline

import androidx.lifecycle.viewModelScope
import com.san.kir.core.utils.viewModel.BaseViewModel
import com.san.kir.data.parsing.SiteCatalogsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
internal class AddOnlineViewModel @Inject constructor(
    private val manager: SiteCatalogsManager,
) : BaseViewModel<AddOnlineEvent, AddOnlineState>() {
    private val siteNames: List<String> = manager.catalog.map { it.catalogName }
    private var job: Job? = null

    private val isCheckingUrlState = MutableStateFlow(false)
    private val validatesCatalogsState = MutableStateFlow(siteNames)
    private val isErrorAvailableState = MutableStateFlow(false)
    private val isEnableAddingState = MutableStateFlow(false)

    override val tempState = combine(
        isCheckingUrlState,
        validatesCatalogsState,
        isErrorAvailableState,
        isEnableAddingState
    ) { check, validate, error, add ->
        AddOnlineState(check, validate.toPersistentList(), error, add)
    }

    override val defaultState = AddOnlineState(
        isCheckingUrl = false,
        validatesCatalogs = siteNames.toPersistentList(),
        isErrorAvailable = false,
        isEnableAdding = false
    )

    override suspend fun onEvent(event: AddOnlineEvent) {
        when (event) {
            is AddOnlineEvent.Update -> {
                checkUrl(event.text)
                isErrorAvailableState.value = false
            }
        }
    }

    private fun checkUrl(text: String) {
        job?.cancel()
        isCheckingUrlState.value = false
        job = viewModelScope.launch {

            if (text.isNotBlank()) {
                // список сайтов подходящий под введеный адрес
                val temp = siteNames
                    .filter { it.contains(text) }

                // Если список не пуст, то отображаем его
                if (temp.isNotEmpty()) {
                    isEnableAddingState.value = false
                    validatesCatalogsState.update { temp }
                } else {
                    // Если в списке что-то есть
                    // то получаем соответствующий сайт
                    val site = siteNames
                        .filter { text.contains(it) }

                    //Если есть хоть один сайт, то проверяем валидность
                    if (site.isNotEmpty()) {
                        validatesCatalogsState.update { site }

                        isCheckingUrlState.value = true
                        delay(3.seconds)
                        isErrorAvailableState.value = manager.elementByUrl(text) == null
                        isEnableAddingState.value = isErrorAvailableState.value.not()
                        isCheckingUrlState.value = false
                    } else {
                        isEnableAddingState.value = false
                        validatesCatalogsState.update { emptyList() }
                    }
                }
            }
            // Если нет текста, то отображается список
            // доступных сайтов
            else {
                isEnableAddingState.value = false
                validatesCatalogsState.update { siteNames }
            }
        }
    }
}
