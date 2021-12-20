package com.san.kir.manger.ui.application_navigation.additional_manga_screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.san.kir.data.parsing.SiteCatalogsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MangaAddOnlineViewModel @Inject constructor(
    private val manager: SiteCatalogsManager,
) : ViewModel() {
    private val siteNames: List<String> = manager.catalog.map { it.catalogName }

    private val _enteredText = MutableStateFlow("")
    val enteredText = _enteredText.onEach { text ->
        updateValidate(text)
        isErrorAvailable.value = false
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), "")

    fun updateEnteredText(text: String) {
        _enteredText.update { text }
    }

    val isCheckingUrl = MutableStateFlow(false)
    val validate = MutableStateFlow(siteNames)
    val isErrorAvailable = MutableStateFlow(false)
    val isEnableAdding = MutableStateFlow(false)

    private fun updateValidate(text: String) {
        if (text.isNotBlank()) {
            // список сайтов подходящий под введеный адрес
            val temp = siteNames
                .filter { it.contains(text) }

            // Если список не пуст, то отображаем его
            if (temp.isNotEmpty()) {
                isEnableAdding.value = false
                validate.update { temp }
            } else {
                // Если в списке что-то есть
                // то получаем соответствующий сайт
                val site = siteNames
                    .filter { text.contains(it) }

                //Если есть хоть один сайт, то включаем кнопку
                if (site.isNotEmpty()) {
                    isEnableAdding.value = true
                    validate.update { site }
                } else {
                    isEnableAdding.value = false
                    validate.update { emptyList() }
                }
            }
        }
        // Если нет текста, то отображается список
        // доступных сайтов
        else {
            isEnableAdding.value = false
            validate.update { siteNames }
        }
    }

    fun checkUrl(url: String = enteredText.value, onSuccess: (String) -> Unit) =
        viewModelScope.launch {
            isCheckingUrl.value = true
            isEnableAdding.value = false

            val element = manager.getElementOnline(url)

            if (element != null)
                onSuccess(url)
            else
                isErrorAvailable.value = true

            isCheckingUrl.value = false
        }
}
