package com.san.kir.manger.utils

import android.arch.paging.PagedListAdapter
import android.graphics.Color
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.util.SparseBooleanArray
import android.view.View
import android.view.ViewGroup
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext

typealias ItemMove<T> = RecyclerViewAdapterFactory.DraggableRecyclerViewAdapter<T>.(fromPosition: Int, toPosition: Int) -> Unit
typealias BindAction<T> = (T, Boolean, Int) -> Unit

object RecyclerViewAdapterFactory {
    fun <T> createSimple(view: () -> AnkoView<T>) =
        RecyclerViewAdapter(view)

    fun <T> createSimple2(init: SimpleAnkoView<T>.() -> Unit): RecyclerViewAdapter<T> {
        val view = SimpleAnkoView<T>()
        view.init()
        return createSimple { view }
    }

    fun <T> createDraggable(
        view: () -> AnkoView<T>,
        itemMove: ItemMove<T>?
    ) = DraggableRecyclerViewAdapter(view, itemMove)

    fun <T> createPaging(
        view: () -> AnkoView<T>,
        areItemsTheSame: (oldItem: T, newItem: T) -> Boolean,
        areContentsTheSame: (oldItem: T, newItem: T) -> Boolean
    ) = RecyclerPagingAdapter(view, object : DiffUtil.ItemCallback<T>() {
        override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
            return areItemsTheSame(oldItem, newItem)
        }

        override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
            return areContentsTheSame(oldItem, newItem)
        }
    })

    open class DraggableRecyclerViewAdapter<T>(
        private val view: () -> AnkoView<T>,
        private val itemMove: ItemMove<T>?
    ) : RecyclerView.Adapter<ViewHolder<T>>(), ItemTouchHelperAdapter {
        var selectedItems = BooleanArray(0)
            private set
        var items: List<T> = listOf()
            set(value) {
                selectedItems = BooleanArray(value.size)
                field = value
            }


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<T> {
            return ViewHolder(view(), parent)
        }

        override fun onBindViewHolder(holder: ViewHolder<T>, position: Int) {
            holder.bind(items[position], selectedItems[position])
        }

        override fun getItemCount() = items.size

        override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
            itemMove?.let { it(fromPosition, toPosition) }
            return true
        }
    }

    class RecyclerViewAdapter<T>(view: () -> AnkoView<T>) :
        DraggableRecyclerViewAdapter<T>(view, null) {
        init {
//            setHasStableIds(true)
        }

        override fun getItemId(position: Int) = position.toLong()
        override fun getItemViewType(position: Int) = position
    }

    // Адаптер для использования с paging library
    class RecyclerPagingAdapter<T>(
        val view: () -> AnkoView<T>,
        diffCallback: DiffUtil.ItemCallback<T>
    ) : PagedListAdapter<T, ViewHolder<T>>(diffCallback) {
        init {
            setHasStableIds(true)
        }

        val selectedItems = SparseBooleanArray()
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<T> {
            return ViewHolder(view(), parent)
        }

        override fun onBindViewHolder(holder: ViewHolder<T>, position: Int) {
            holder.bind(getItem(position), selectedItems[position])
        }

        fun item(position: Int): T? = getItem(position)
        override fun getItemId(position: Int) = position.toLong()
        override fun getItemViewType(position: Int) = position
    }

    class ViewHolder<in T>(val view: AnkoView<T>, parent: ViewGroup) :
        RecyclerView.ViewHolder(view.createView(parent)),
        ItemTouchHelperViewHolder {
        fun bind(item: T?, isSelected: Boolean) {
            item?.let { view.bind(it, isSelected, adapterPosition) }
        }

        override fun onItemSelected() {
            itemView.setBackgroundColor(Color.LTGRAY)
        }

        override fun onItemClear() {
            itemView.setBackgroundColor(0)
        }
    }

    abstract class AnkoView<in T> : AnkoComponent<ViewGroup> {

        open fun createView(parent: ViewGroup): View {
            return createView(AnkoContext.create(parent.context, parent))
        }

        abstract fun bind(item: T, isSelected: Boolean, position: Int)
    }

    class SimpleAnkoView<T> : AnkoView<T>() {
        private lateinit var _createViewAction: (AnkoContext<ViewGroup>.() -> View)
        private lateinit var _bindAction: BindAction<T>

        fun createView(action: AnkoContext<ViewGroup>.() -> View) {
            _createViewAction = action
        }

        override fun createView(ui: AnkoContext<ViewGroup>): View {
            return with(ui) {
                _createViewAction()
            }
        }

        fun bind(action: BindAction<T>) {
            _bindAction = action
        }

        override fun bind(item: T, isSelected: Boolean, position: Int) {
            _bindAction.invoke(item, isSelected, position)
        }

    }
}


