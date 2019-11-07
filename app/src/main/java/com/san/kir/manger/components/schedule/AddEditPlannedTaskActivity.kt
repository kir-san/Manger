package com.san.kir.manger.components.schedule

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.san.kir.ankofork.setContentView
import com.san.kir.manger.R
import com.san.kir.manger.room.entities.PlannedTask
import com.san.kir.manger.utils.enums.PlannedAddEdit
import com.san.kir.manger.utils.extensions.ThemedActionBarActivity
import com.san.kir.manger.utils.extensions.showAlways
import com.san.kir.manger.view_models.AddEditPlannedTaskViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// TODO добавить кнопку удалить
class AddEditPlannedTaskActivity : ThemedActionBarActivity() {
    val mViewModel by viewModels<AddEditPlannedTaskViewModel>()
    private val mView by lazy { AddEditPlannedTaskView(this) }
    private var isEditMode = false
    private val manager = ScheduleManager()

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
                lifecycleScope.launch(Dispatchers.Default) {
                    val task = mView.getTask()

                    if (isEditMode) {
                        task.isEnabled = false
                        mViewModel.plannedUpdate(task)
                        manager.cancel(this@AddEditPlannedTaskActivity, task)
                    } else {
                        task.addedTime = System.currentTimeMillis()
                        mViewModel.plannedInsert(task)
                    }
                }
                onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
