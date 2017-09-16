package com.san.kir.manger.components.Main

import android.Manifest
import android.arch.lifecycle.LifecycleRegistry
import android.arch.lifecycle.LifecycleRegistryOwner
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.view.GravityCompat
import com.san.kir.manger.Extending.AnkoExtend.compatCheckSelfPermission
import com.san.kir.manger.Extending.AnkoExtend.compatRequestPermissions
import com.san.kir.manger.R
import com.san.kir.manger.components.Category.CategoryFragment
import com.san.kir.manger.components.DownloadManager.DownloadManagerFragment
import com.san.kir.manger.components.LatestChapters.LatestChaptersFragment
import com.san.kir.manger.components.Library.LibraryFragment
import com.san.kir.manger.components.Parsing.ManageSites
import com.san.kir.manger.components.Settings.PrefFragment
import com.san.kir.manger.components.SitesCatalog.SiteCatalogFragment
import com.san.kir.manger.components.Storage.StorageMainDirFragment
import com.san.kir.manger.utils.DIR
import com.san.kir.manger.utils.MainRouter
import com.san.kir.manger.utils.createDirs
import com.san.kir.manger.utils.getFullPath
import com.san.kir.manger.utils.name_SHOW_CATEGORY
import dagger.android.support.DaggerAppCompatActivity
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.anko.longToast
import org.jetbrains.anko.setContentView
import org.jetbrains.anko.toast
import javax.inject.Inject


class MainActivity : DaggerAppCompatActivity(), LifecycleRegistryOwner {
    private val _lifecycle = LifecycleRegistry(this)

    @Inject lateinit var router: MainRouter
    @Inject lateinit var mView: MainView
    @Inject lateinit var updateApp: ManageSites.UpdateApp

    override fun getLifecycle() = _lifecycle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Проверяем есть ли разрешения на запись, если нет спрашиваем об этом
        val writeToExtStorage = Manifest.permission.WRITE_EXTERNAL_STORAGE
        if (compatCheckSelfPermission(writeToExtStorage) != PackageManager.PERMISSION_GRANTED) {
            compatRequestPermissions(arrayOf(writeToExtStorage), 200)
        }

        // Востановление настроек приложения
        with(defaultSharedPreferences) {
            if (!contains(name_SHOW_CATEGORY))
                edit().putBoolean(name_SHOW_CATEGORY, true).apply()
        }

        mView.setContentView(this)

        when (intent.getStringExtra("launch")) {
            "download" -> router.showScreen(DownloadManagerFragment())
            "catalog" -> router.showScreen(SiteCatalogFragment())
            else -> router.showScreen(LibraryFragment(), true)
        }

        updateApp.checkNewVersion()
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<out String>,
                                            grantResults: IntArray) {
        if (requestCode == 200)
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                applyChanges()
            else {
                longToast("Без этого разрешения от приложения мало толку")
                finishAffinity()
            }
    }

    private var back_pressed: Long = 0
    override fun onBackPressed() {
        if (mView.drawer_layout.isDrawerOpen(GravityCompat.START)) {
            mView.drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            if (router.isMainScreen()) {
                if (back_pressed + 2000 > System.currentTimeMillis())
                    super.onBackPressed()
                else
                    toast(R.string.first_run_exit_text)
                back_pressed = System.currentTimeMillis()
            } else {
                if (router.isMangaDir())
                    router.showScreen(StorageMainDirFragment())
                else
                    router.showScreen(LibraryFragment(), true)
            }
        }
    }

    fun onNavigationItemSelected(id: Int) = with(MainView._id) {
        if (id == library)
            router.showScreen(LibraryFragment(), true)
        else
            router.showScreen(when (id) {
                                  category -> CategoryFragment()
                                  storage -> StorageMainDirFragment()
                                  catalogs -> SiteCatalogFragment()
                                  downloader -> DownloadManagerFragment()
                                  latest -> LatestChaptersFragment()
                                  settings -> PrefFragment()
                                  else -> LibraryFragment()
                              })
    }

    private fun applyChanges() = DIR.ALL.forEach { dir -> createDirs(getFullPath(dir)) }
}

