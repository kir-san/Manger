package com.san.kir.manger.utils

import com.san.kir.manger.components.Library.LibraryItemsAdapter
import com.san.kir.manger.dbflow.models.Manga


object LibraryAdaptersCount {
    private var isInit = false
    private var adapters: MutableList<LibraryItemsAdapter> = arrayListOf()
    fun init(adapters: MutableList<LibraryItemsAdapter>) {
        this.adapters = adapters
        isInit = true
    }

    fun delete(manga: Manga) {
        if (isInit){
            manga.delete()
            adapters.forEach {
                it.update() }
        }
    }

    fun update() {
        if (isInit) {
            adapters.forEach {
                it.update()
            }
        }
    }
}
