package com.san.kir.manger.components.category

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.core.view.setMargins
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import com.san.kir.ankofork.design.floatingActionButton
import com.san.kir.ankofork.dip
import com.san.kir.ankofork.matchParent
import com.san.kir.ankofork.recyclerview.recyclerView
import com.san.kir.ankofork.sdk28._LinearLayout
import com.san.kir.ankofork.sdk28.frameLayout
import com.san.kir.ankofork.sdk28.onClick
import com.san.kir.ankofork.wrapContent
import com.san.kir.manger.R
import com.san.kir.manger.components.drawer.DrawerActivity
import com.san.kir.manger.room.entities.Category
import com.san.kir.manger.utils.extensions.doOnApplyWindowInstets
import com.san.kir.manger.view_models.CategoryViewModel

class CategoryActivity : DrawerActivity() {
    private val mAdapter = CategoryRecyclerPresenter(this)
    val mViewModel by viewModels<CategoryViewModel>()

    override val _LinearLayout.customView: View
        get() = frameLayout {
            lparams(width = matchParent, height = matchParent)

            recyclerView {
                lparams(width = matchParent, height = matchParent)
                setHasFixedSize(true)
                layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
                mAdapter.into(this)

                clipToPadding = false

                doOnApplyWindowInstets { view, insets, padding ->
                    view.updatePadding(bottom = padding.bottom + insets.systemWindowInsetBottom)
                    insets
                }
            }

            floatingActionButton {
                setImageResource(R.drawable.ic_add)
                onClick {
                    addCategory()
                }

                doOnApplyWindowInstets { view, insets, padding ->
                    view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                        bottomMargin = insets.systemWindowInsetBottom + dip(16)
                    }
                    insets
                }
            }.lparams(width = wrapContent, height = wrapContent) {
                gravity = Gravity.BOTTOM or Gravity.END
                setMargins(dip(16))
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
