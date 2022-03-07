package com.san.kir.chapters

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

// Управление выделением
@OptIn(ExperimentalCoroutinesApi::class)
class SelectionControl {
    // Хранение элементов
    private val _items = MutableStateFlow<List<Boolean>>(emptyList())
    val items = _items.asStateFlow()

    // Индикатор активации
    private val _isEnable = MutableStateFlow(false)
    val isEnable = _isEnable.asStateFlow()

    // Текущее количество элементов
    private val currentCount: Int
        get() = _items.value.count()

    // Количество выделеных элементов
    private val selectedCount: Int
        get() = _items.value.count { it }

    // Текущее соотояние
    private val currentEnableState: Boolean
        get() = _isEnable.value

    // Пересоздание списка для выделения на изменение размера
    fun updateSelectedSize(newSize: Int) {
        if (currentCount != newSize)
            _items.update { List(newSize) { false } }
    }

    // Переключение выделения для элемента с указаным индексом
    fun toggleSelection(index: Int) {
        _items.update { oldList ->
            oldList.toMutableList().apply {
                set(index, get(index).not())
            }
        }
        checkEnable()
    }

    // Очистка выделения и отключение режима
    fun clear() {
        _items.update { List(currentCount) { false } }
        _isEnable.value = false
    }

    // Выделение всех элементов
    fun fullFill() {
        _items.update { List(currentCount) { true } }
    }

    // Выделение всех элементов ниже одного единственного выделенного элемента
    fun belowFill() {
        if (selectedCount == 1) {
            val start = _items.value.indexOf(true)
            _items.update {
                List(start) { false } + List(currentCount - start) { true }
            }
        }
    }

    // Выделение всех элементов вышу одного единственного выделенного элемента
    fun aboveFill() {
        if (selectedCount == 1) {
            val start = _items.value.indexOf(true)
            _items.update {
                List(start + 1) { true } + List(currentCount - start - 1) { false }
            }
        }
    }

    // Вспомогательная функция для объединения с другим списком
    fun <T> with(items: List<T>): List<Pair<Boolean, T>> {
        return _items.value.zip(items)
    }

    // активация и дезактивация режима выделения
    private fun checkEnable() {
        if (selectedCount > 0 && currentEnableState.not()) {
            _isEnable.value = true
        } else if (selectedCount <= 0 && currentEnableState) {
            _isEnable.value = false
        }
    }
}
