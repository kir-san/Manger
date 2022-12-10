package com.san.kir.core.utils

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList

inline fun <T, R> List<T>.mapP(transform: (T) -> R): PersistentList<R> {
    return map(transform).toPersistentList()
}
