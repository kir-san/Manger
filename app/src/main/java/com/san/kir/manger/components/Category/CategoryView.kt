package com.san.kir.manger.components.Category


import android.support.design.widget.Snackbar
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import com.san.kir.manger.EventBus.BinderRx
import com.san.kir.manger.EventBus.toogle
import com.san.kir.manger.Extending.AnkoExtend.bind
import com.san.kir.manger.R
import com.san.kir.manger.utils.OnStartDragListener
import com.san.kir.manger.utils.SimpleItemTouchHelperCallback
import org.jetbrains.anko.AnkoComponent
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.design.floatingActionButton
import org.jetbrains.anko.dip
import org.jetbrains.anko.editText
import org.jetbrains.anko.frameLayout
import org.jetbrains.anko.imageView
import org.jetbrains.anko.linearLayout
import org.jetbrains.anko.margin
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.wrapContent

class CategoryView : AnkoComponent<CategoryFragment> {
    private val isAddMode = BinderRx(false)

    private val mItemTouchHelper: ItemTouchHelper by lazy {
        ItemTouchHelper(SimpleItemTouchHelperCallback(mAdapter))
    }

    private val mAdapter: CategoryAdapter = CategoryAdapter(object : OnStartDragListener {
        override fun onStartDrag(viewHolder: RecyclerView.ViewHolder?) {
            mItemTouchHelper.startDrag(viewHolder)
        }
    })


    fun createView(parent: CategoryFragment): View {
        return createView(AnkoContext.create(parent.context, parent))
    }

    override fun createView(ui: AnkoContext<CategoryFragment>) = with(ui) {
        frameLayout {
            lparams(width = matchParent, height = matchParent) {}

            recyclerView {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(context)

                adapter = mAdapter
                mItemTouchHelper.attachToRecyclerView(this)
            }.lparams(width = matchParent, height = matchParent)


            linearLayout {
                backgroundResource = android.R.color.darker_gray

                visibility = View.GONE
                bind(isAddMode) { visibility = if (it) View.VISIBLE else View.GONE }

                val edit = editText {
                    inputType = InputType.TYPE_CLASS_TEXT
                }.lparams(width = dip(250), height = wrapContent) {
                    gravity = Gravity.BOTTOM
                    bottomMargin = dip(2)
                    leftMargin = dip(8)
                }

                imageView {
                    scaleType = ImageView.ScaleType.FIT_XY
                    backgroundResource = R.drawable.ic_action_okay_green
                    onClick {
                        if (edit.text.toString().isNotEmpty()) {
                            mAdapter.addCategory(edit.text.toString())
                            edit.setText("")
                        } else {
                            Snackbar.make(this@frameLayout,
                                          "Пустым здесь нет места",
                                          Snackbar.LENGTH_SHORT).show()
                        }
                        isAddMode.toogle()
                    }
                }.lparams(width = dip(50), height = dip(50)) {
                    gravity = Gravity.CENTER
                    margin = dip(2)
                }

            }.lparams(width = wrapContent, height = wrapContent) {
                gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            }

            floatingActionButton {
                setImageResource(R.drawable.ic_add_white)
                bind(isAddMode) {
                    visibility = if (it) View.GONE else View.VISIBLE
                }
                onClick {
                    isAddMode.toogle()
                }
            }.lparams(width = wrapContent, height = wrapContent) {
                gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                bottomMargin = dip(5)
                topMargin = dip(2)
            }
        }
    }
}
