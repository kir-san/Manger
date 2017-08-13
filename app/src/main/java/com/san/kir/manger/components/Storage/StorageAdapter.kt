package com.san.kir.manger.components.Storage

import android.annotation.SuppressLint
import android.view.ViewGroup
import com.bignerdranch.expandablerecyclerview.Adapter.ExpandableRecyclerAdapter
import com.bignerdranch.expandablerecyclerview.Model.ParentListItem
import com.bignerdranch.expandablerecyclerview.ViewHolder.ChildViewHolder
import com.bignerdranch.expandablerecyclerview.ViewHolder.ParentViewHolder

class StorageAdapter(parent: MutableList<StorageParentItem>) :
        ExpandableRecyclerAdapter<StorageAdapter.ViewHolderParent,
                StorageAdapter.ViewHolderChild>(parent) {

    override fun onCreateParentViewHolder(parentViewGroup: ViewGroup): ViewHolderParent {
        return ViewHolderParent(StorageParentView(), parentViewGroup)
    }

    override fun onCreateChildViewHolder(childViewGroup: ViewGroup): ViewHolderChild? {
        return ViewHolderChild(StorageChildView(), childViewGroup)
    }

    override fun onBindParentViewHolder(parentViewHolder: ViewHolderParent,
                                        position: Int,
                                        parentListItem: ParentListItem?) {
        parentViewHolder.bind(parentListItem as StorageParentItem)
    }

    override fun onBindChildViewHolder(childViewHolder: ViewHolderChild,
                                       position: Int,
                                       childListItem: Any?) {
        childViewHolder.bind(childListItem as StorageItem)
    }

    class ViewHolderParent(val view: StorageParentView, parent: ViewGroup) :
            ParentViewHolder(view.createView(parent)) {

        fun bind(el: StorageParentItem) {
            view.bind(el)
        }

        @SuppressLint("NewApi")
        override fun setExpanded(expanded: Boolean) {
            super.setExpanded(expanded)
            view.expanded.item = if (expanded) 180f else 0.0f
        }
    }

    class ViewHolderChild(val view: StorageChildView, parent: ViewGroup) :
            ChildViewHolder(view.createView(parent)) {

        fun bind(el: StorageItem) {
            view.bind(el)
        }
    }
}
