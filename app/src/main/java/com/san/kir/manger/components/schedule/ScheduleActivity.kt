package com.san.kir.manger.components.schedule

import android.arch.lifecycle.ViewModelProviders
import android.support.v4.view.PagerTabStrip
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.san.kir.manger.R
import com.san.kir.manger.components.drawer.DrawerActivity
import com.san.kir.manger.extending.views.showAlways
import com.san.kir.manger.room.models.PlannedAddEdit
import com.san.kir.manger.view_models.ScheduleViewModel
import org.jetbrains.anko._LinearLayout
import org.jetbrains.anko.include
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.support.v4.viewPager
import org.jetbrains.anko.verticalLayout

class ScheduleActivity : DrawerActivity() {
    val mViewModel by lazy {
        ViewModelProviders.of(this).get(ScheduleViewModel::class.java)
    }

    override val _LinearLayout.customView: View
        get() = verticalLayout {
            viewPager {
                include<PagerTabStrip>(R.layout.page_tab_strip)
                adapter = SchedulePageAdapter(this@ScheduleActivity)
                setTitle(R.string.main_menu_schedule)
            }
        }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(0, 0, 0, R.string.schedule_activity_menu_add)
            .setIcon(R.drawable.ic_add_white)
            .showAlways()
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == 0) {
            startActivity<AddEditPlannedTaskActivity>(PlannedAddEdit.add to true)
        }
        return super.onOptionsItemSelected(item)
    }

}

