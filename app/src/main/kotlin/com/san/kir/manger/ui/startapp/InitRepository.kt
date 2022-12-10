package com.san.kir.manger.ui.startapp

import android.app.Application
import android.content.Context
import androidx.core.content.edit
import com.san.kir.background.works.ScheduleWorker
import com.san.kir.core.utils.coroutines.withIoContext
import com.san.kir.data.db.dao.PlannedDao
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class InitRepository @Inject constructor(
    private val ctx: Application,
    private val plannedDao: PlannedDao,
) {

    private val sharedPreferences by lazy {
        ctx.getSharedPreferences("startup", Context.MODE_PRIVATE)
    }

    fun isFirstLaunch(): Boolean {
        if (sharedPreferences.contains("firstLaunch"))
            return false

        sharedPreferences.edit {
            putBoolean("startup", false)
        }
        return true
    }

    suspend fun restoreSchedule() = withIoContext {
        plannedDao.loadSimpleItems()
            .first()
            .filter { it.isEnabled }
            .forEach { ScheduleWorker.addTask(ctx, it) }
    }
}
