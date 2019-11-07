package com.san.kir.manger.utils

import com.san.kir.manger.utils.enums.SortLibrary

object SortLibraryUtil {
    const val add = "add"
    const val abc = "abc"
    const val pop = "pop"

    fun toType(type: String): SortLibrary {
        return when (type) {
            add -> SortLibrary.AddTime
            abc -> SortLibrary.AbcSort
            pop -> SortLibrary.Populate
            else -> SortLibrary.AddTime
        }
    }

    fun toString(type: SortLibrary): String {
        return when (type) {
            SortLibrary.AddTime -> add
            SortLibrary.AbcSort -> abc
            SortLibrary.Populate -> pop
        }
    }

}
