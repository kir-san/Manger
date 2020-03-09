package com.san.kir.manger.components.category

import android.view.View
import androidx.activity.viewModels
import com.san.kir.ankofork.sdk28._LinearLayout
import com.san.kir.manger.components.drawer.DrawerActivity
import com.san.kir.manger.room.entities.Category
import com.san.kir.manger.view_models.CategoryViewModel

class CategoryActivity : DrawerActivity() {
    val mAdapter = CategoryRecyclerPresenter(this)
    val mViewModel by viewModels<CategoryViewModel>()

    override val _LinearLayout.customView: View
        get() = CategoryView(mAdapter).view(this)
}
