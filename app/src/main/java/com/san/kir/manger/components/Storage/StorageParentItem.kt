package com.san.kir.manger.components.Storage

import com.bignerdranch.expandablerecyclerview.Model.ParentListItem
import com.san.kir.manger.components.Storage.StorageItem


class StorageParentItem(val name: String,
                        private val list: MutableList<StorageItem>) : ParentListItem {

    override fun getChildItemList() = list
    override fun isInitiallyExpanded() = false
}
