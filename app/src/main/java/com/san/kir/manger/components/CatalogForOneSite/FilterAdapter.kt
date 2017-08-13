package com.san.kir.manger.components.CatalogForOneSite

import android.support.v7.widget.RecyclerView
import android.util.SparseBooleanArray
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import com.san.kir.manger.utils.ID
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.checkBox
import org.jetbrains.anko.find
import org.jetbrains.anko.linearLayout
import org.jetbrains.anko.lines
import org.jetbrains.anko.sdk25.coroutines.onCheckedChange
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.textView

class FilterAdapter : RecyclerView.Adapter<FilterAdapter.ViewHolder>() {
    private var catalog: List<String> = listOf()
    private val checkedId = ID.generate()
    private val nameId = ID.generate()
    private val selectedName: MutableList<String> = mutableListOf()
    private val selected: SparseBooleanArray = SparseBooleanArray()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = parent.context.linearLayout {
            val check = checkBox {
                id = checkedId
            }
            textView {
                id = nameId
                lines = 1
                onClick {
                    check.performClick()
                }
            }.lparams { weight = 1f }
        }
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(catalog[position], selected.get(position))
    }

    override fun getItemCount(): Int {
        return catalog.size
    }

    fun getSelected(): List<String> {
        return selectedName
    }

    fun clear() {
        selectedName.clear()
        selected.clear()
        catalog = listOf()
        launch(UI) {
            notifyDataSetChanged()
        }
    }

    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val name = v.find<TextView>(nameId)
        val check = v.find<CheckBox>(checkedId)

        fun bind(name: String, isCheck: Boolean) {
            this.name.text = name
            this.check.isChecked = isCheck

            check.onCheckedChange { _, b ->
                if (b) {
                    selected.put(adapterPosition, true)
                    selectedName.add(name)
                } else {
                    selected.delete(adapterPosition)
                    selectedName.remove(name)
                }
            }
        }
    }

    private val set = mutableSetOf<String>()
    suspend fun addAll(genres: List<String>) {
        set.addAll(genres)
    }

    suspend fun add(type: String) {
        set.add(type)

    }

    suspend fun finishAdd() {
        catalog = set.sorted()
        launch(UI) {
            notifyDataSetChanged()
        }
    }
}
