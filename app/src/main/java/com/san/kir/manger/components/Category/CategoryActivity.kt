package com.san.kir.manger.components.Category

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.instance
import com.san.kir.manger.R
import com.san.kir.manger.components.Drawer.DrawerActivity
import com.san.kir.manger.room.models.Category
import org.jetbrains.anko.design.floatingActionButton
import org.jetbrains.anko.dip
import org.jetbrains.anko.frameLayout
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.wrapContent

class CategoryActivity : DrawerActivity() {
    private val _adapter = CategoryRecyclerPresenter()

    override val LinearLayout.customView: View
        get() = frameLayout {
            lparams(width = matchParent, height = matchParent)

            recyclerView {
                lparams(width = matchParent, height = matchParent)
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(context)
                _adapter.into(this)
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

    override fun provideOverridingModule() = Kodein.Module {
        bind<CategoryActivity>() with instance(this@CategoryActivity)
    }

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle(R.string.main_menu_category)
    }

    private fun View.addCategory() {
        with(Category()) {
            CategoryEditDialog(context, this) {
                _adapter.add(this@with)
            }
        }
    }
}
