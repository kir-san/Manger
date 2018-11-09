package com.san.kir.manger.components.schedule

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.san.kir.manger.R
import com.san.kir.manger.components.main.Main
import com.san.kir.manger.extending.ThemedActionBarActivity
import com.san.kir.manger.extending.views.showAlways
import com.san.kir.manger.room.models.PlannedAddEdit
import com.san.kir.manger.room.models.PlannedTask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.setContentView

// TODO добавить кнопку удалить
class AddEditPlannedTaskActivity : ThemedActionBarActivity() {

    private val mView = AddEditPlannedTaskView()
    private var isEditMode = false
    private val manager by lazy { ScheduleManager(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mView.setContentView(this)

        when {
            intent.hasExtra(PlannedAddEdit.add) -> {
                setTitle(R.string.planned_task_title_create)
                isEditMode = false
                mView.setTask(PlannedTask())
            }
            intent.hasExtra(PlannedAddEdit.edit) -> {
                setTitle(R.string.planned_task_title_change)
                isEditMode = true
                mView.setTask(intent.getParcelableExtra(PlannedAddEdit.edit))
            }
        }

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(0, 1, 1, R.string.planned_task_button_ready).showAlways()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
            1 -> {
                GlobalScope.launch(Dispatchers.Default) {
                    val task = mView.getTask()

                    if (isEditMode) {
                        task.isEnabled = false
                        Main.db.plannedDao.update(task)
                        manager.cancel(task)
                    } else {
                        task.addedTime = System.currentTimeMillis()
                        Main.db.plannedDao.insert(task)
                    }
                }
                onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
