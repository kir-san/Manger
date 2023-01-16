package com.san.kir.core.compose.runtime

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class CustomMutableStateDelegate<T>(
    private val scope: CoroutineScope,
    defaultValue: T,
    private val onChange: suspend (T) -> Unit
) : ReadWriteProperty<Any, T> {
    private val state: MutableState<T> = mutableStateOf(defaultValue)

    override fun getValue(thisRef: Any, property: KProperty<*>) = state.value

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        if (state.value != value) {
            state.value = value
            scope.launch {
                onChange(value)
            }
        }
    }
}
