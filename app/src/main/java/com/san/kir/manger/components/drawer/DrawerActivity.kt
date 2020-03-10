package com.san.kir.manger.components.drawer

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.san.kir.ankofork.dialogs.longToast
import com.san.kir.ankofork.dialogs.toast
import com.san.kir.ankofork.doFromSdk
import com.san.kir.ankofork.sdk28._LinearLayout
import com.san.kir.ankofork.startActivity
import com.san.kir.manger.R
import com.san.kir.manger.components.library.LibraryActivity
import com.san.kir.manger.components.parsing.ManageSites
import com.san.kir.manger.components.schedule.ScheduleManager
import com.san.kir.manger.repositories.CategoryRepository
import com.san.kir.manger.repositories.MainMenuRepository
import com.san.kir.manger.repositories.MangaRepository
import com.san.kir.manger.repositories.PlannedRepository
import com.san.kir.manger.repositories.SiteRepository
import com.san.kir.manger.repositories.StatisticRepository
import com.san.kir.manger.room.entities.Category
import com.san.kir.manger.room.entities.MainMenuItem
import com.san.kir.manger.room.entities.MangaStatistic
import com.san.kir.manger.room.entities.Site
import com.san.kir.manger.utils.CATEGORY_ALL
import com.san.kir.manger.utils.enums.DIR
import com.san.kir.manger.utils.enums.MainMenuType
import com.san.kir.manger.utils.extensions.BaseActivity
import com.san.kir.manger.utils.extensions.compatCheckSelfPermission
import com.san.kir.manger.utils.extensions.compatRequestPermissions
import com.san.kir.manger.utils.extensions.createDirs
import com.san.kir.manger.utils.extensions.getFullPath
import com.san.kir.manger.utils.extensions.log
import com.san.kir.manger.workmanager.FirstInitAppWorker
import com.san.kir.manger.workmanager.MigrateLatestChapterToChapterWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class DrawerActivity : BaseActivity() {
    private val mView by lazy { DrawerView(this) }

    abstract val _LinearLayout.customView: View

    open fun onPermissionGetting() {
        mView.init()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Проверяем есть ли разрешения на запись, если нет спрашиваем об этом
        val writeToExtStorage = Manifest.permission.WRITE_EXTERNAL_STORAGE
        if (compatCheckSelfPermission(writeToExtStorage) != PackageManager.PERMISSION_GRANTED) {
            compatRequestPermissions(arrayOf(writeToExtStorage), 200)
            log("дайте права")
        } else {
            init()
            log("права былм")
        }

        doFromSdk(Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.transparent_dark)
            window.navigationBarColor = ContextCompat.getColor(this, R.color.transparent_dark2)
        }

        mView.createView(this@DrawerActivity) {
            customView
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        if (requestCode == 200)
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                init()
            } else {
                longToast(R.string.main_permission_error)
                finishAffinity()
            }
    }

    private var backPressed: Long = 0
    override fun onBackPressed() {
        if (mView.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            if (backPressed + 2000 > System.currentTimeMillis()) {
                finishAffinity()
            } else {
                toast(R.string.first_run_exit_text)
            }
            backPressed = System.currentTimeMillis()
        } else {
            mView.drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    private fun init() = lifecycleScope.launch(Dispatchers.Main) {
        log("права получены")

        val sp = getPreferences(Context.MODE_PRIVATE)
        val prefKeyFirstLaunch = "pref_first_launch"

        if (sp.getBoolean(prefKeyFirstLaunch, true)) {
            sp.edit().putBoolean(prefKeyFirstLaunch, false).apply()
            FirstInitAppWorker.addTask(this@DrawerActivity)
            val task = OneTimeWorkRequestBuilder<MigrateLatestChapterToChapterWorker>()
                .addTag("migrate")
                .build()
            WorkManager.getInstance(applicationContext).enqueue(task)
        }

        onPermissionGetting()
    }


}
