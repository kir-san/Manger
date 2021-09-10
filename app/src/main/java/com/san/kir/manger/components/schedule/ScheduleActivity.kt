package com.san.kir.manger.components.schedule

import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.viewpager.widget.PagerTabStrip
import com.san.kir.ankofork.include
import com.san.kir.ankofork.sdk28._LinearLayout
import com.san.kir.ankofork.startActivity
import com.san.kir.ankofork.support.viewPager
import com.san.kir.ankofork.verticalLayout
import com.san.kir.manger.R
import com.san.kir.manger.components.drawer.DrawerActivity
import com.san.kir.manger.utils.enums.PlannedAddEdit
import com.san.kir.manger.utils.extensions.showAlways

class ScheduleActivity : DrawerActivity() {
    val mViewModel by viewModels<ScheduleViewModel>()
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
            .setIcon(R.drawable.ic_add)
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

