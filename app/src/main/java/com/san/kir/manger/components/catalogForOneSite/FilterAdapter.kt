package com.san.kir.manger.components.catalogForOneSite

import android.support.v7.widget.RecyclerView
import android.util.SparseBooleanArray
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import com.san.kir.manger.extending.ankoExtend.onClick
import com.san.kir.manger.utils.ID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.checkBox
import org.jetbrains.anko.find
import org.jetbrains.anko.linearLayout
import org.jetbrains.anko.lines
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

    override fun getItemCount() = catalog.size

    fun getSelected() = selectedName

    fun clear() {
        selectedName.clear()
        selected.clear()
        catalog = listOf()
        notifyDataSetChanged()
    }

    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        private val name = v.find<TextView>(nameId)
        private val check = v.find<CheckBox>(checkedId)

        fun bind(name: String, isCheck: Boolean) {
            this.name.text = name
            this.check.isChecked = isCheck

            check.setOnCheckedChangeListener { _, b ->
                GlobalScope.launch(Dispatchers.Default) {
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
    }

    private val set = hashSetOf<String>()
    fun add(vararg type: String) {
        set.addAll(type)
    }

    fun finishAdd() {
        catalog = set.sorted()
        notifyDataSetChanged()
    }
}
