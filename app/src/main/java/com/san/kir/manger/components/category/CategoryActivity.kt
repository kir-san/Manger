package com.san.kir.manger.components.category

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.Gravity
import android.view.View
import com.san.kir.manger.R
import com.san.kir.manger.components.drawer.DrawerActivity
import com.san.kir.manger.extending.ankoExtend.onClick
import com.san.kir.manger.room.models.Category
import com.san.kir.manger.view_models.CategoryViewModel
import org.jetbrains.anko._LinearLayout
import org.jetbrains.anko.design.floatingActionButton
import org.jetbrains.anko.dip
import org.jetbrains.anko.frameLayout
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.wrapContent

class CategoryActivity : DrawerActivity() {
    private val mAdapter = CategoryRecyclerPresenter(this)
    val mViewModel by lazy {
        ViewModelProviders.of(this).get(CategoryViewModel::class.java)
    }

    override val _LinearLayout.customView: View
        get() = frameLayout {
            lparams(width = matchParent, height = matchParent)

            recyclerView {
                lparams(width = matchParent, height = matchParent)
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(context)
                mAdapter.into(this)
            }

            floatingActionButton {
                setImageResource(R.drawable.ic_add_white)
                onClick {
                    addCategory()
                }
            }.lparams(width = wrapContent, height = wrapContent) {
                gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                bottomMargin = dip(5)
                topMargin = dip(2)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle(R.string.main_menu_category)
    }

    private fun addCategory() {
        CategoryEditDialog(this, Category()) { cat ->
            mAdapter.add(cat)
        }
    }
}
