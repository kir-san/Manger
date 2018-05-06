package com.san.kir.manger.components.schedule

import android.support.v4.view.PagerTabStrip
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import com.san.kir.manger.R
import com.san.kir.manger.components.drawer.DrawerActivity
import com.san.kir.manger.extending.views.showAlways
import com.san.kir.manger.room.models.PlannedAddEdit
import org.jetbrains.anko.include
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.support.v4.viewPager
import org.jetbrains.anko.verticalLayout

class ScheduleActivity : DrawerActivity() {
    override val LinearLayout.customView: View
        get() = verticalLayout {
            viewPager {
                include<PagerTabStrip>(R.layout.page_tab_strip)
                adapter = SchedulePageAdapter(this@ScheduleActivity)
                setTitle(R.string.main_menu_schedule)
            }
        }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(0, 0, 0, "Добавить")
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

