package com.san.kir.core.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

private inline fun <T> List<T>.mutate(mutation: MutableList<T>.() -> Unit): List<T> {
    val mutableList = toMutableList()
    mutableList.mutation()
    return mutableList
}

fun <T> List<T>.add(item: T) = mutate { add(item) }
fun <T> List<T>.add(index: Int, item: T) = mutate { add(index, item) }
fun <T> List<T>.addAll(items: Collection<T>) = mutate { addAll(items) }
fun <T> List<T>.addAll(index: Int, items: Collection<T>) = mutate { addAll(index, items) }
fun <T> List<T>.set(index: Int, item: T) = mutate { set(index, item) }
fun <T> List<T>.removeAt(index: Int) = mutate { removeAt(index) }
fun <T> MutableStateFlow<List<T>>.set(index: Int, item: T) = update { it.set(index, item) }
fun <T> MutableStateFlow<List<T>>.addAll(index: Int, items: Collection<T>) =
    update { it.addAll(index, items) }

fun <T> MutableStateFlow<List<T>>.add(item: T) = update { it.add(item) }
fun <T> MutableStateFlow<List<T>>.removeAt(index: Int) = update { it.removeAt(index) }
fun <T> MutableStateFlow<List<T>>.listMap(transform: (T) -> T) = update { it.map(transform) }
fun <T> StateFlow<List<T>>.get(index: Int): T = value[index]

fun <T> StateFlow<List<T>>.indexOfFirst(predicate: (T) -> Boolean) = value.indexOfFirst(predicate)
