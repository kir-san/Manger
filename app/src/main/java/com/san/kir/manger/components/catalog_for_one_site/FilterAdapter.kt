package com.san.kir.manger.components.catalog_for_one_site

import android.util.SparseBooleanArray
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import com.san.kir.ankofork.dip
import com.san.kir.ankofork.find
import com.san.kir.ankofork.margin
import com.san.kir.ankofork.matchParent
import com.san.kir.ankofork.sdk28.checkBox
import com.san.kir.ankofork.sdk28.linearLayout
import com.san.kir.ankofork.sdk28.lines
import com.san.kir.ankofork.sdk28.onClick
import com.san.kir.ankofork.sdk28.textView
import com.san.kir.manger.utils.ID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class FilterAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<FilterAdapter.ViewHolder>() {
    var catalog: List<String> = listOf()

    private val checkedId = ID.generate()
    private val nameId = ID.generate()

    private val selectedName: MutableList<String> = mutableListOf()
    private val selected: SparseBooleanArray = SparseBooleanArray()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = parent.context.linearLayout {
            lparams(width = matchParent)
            gravity = Gravity.CENTER_VERTICAL

            val check = checkBox {
                id = checkedId
            }.lparams {
                margin = dip(8)
            }

            textView {
                id = nameId
                lines = 1
            }

            onClick {
                check.performClick()
            }
        }
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(catalog[position], selected.get(position))
    }

    override fun getItemCount() = catalog.size

    fun getSelected() = selectedName

    fun clearSelected() {
        selectedName.clear()
        selected.clear()
        notifyDataSetChanged()
    }

    inner class ViewHolder(v: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(v) {
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
