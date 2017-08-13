package com.san.kir.manger.components.Main

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v7.app.AppCompatActivity
import android.view.ActionMode
import android.view.View
import com.san.kir.manger.BuildConfig
import com.san.kir.manger.R
import com.san.kir.manger.components.Category.CategoryFragment
import com.san.kir.manger.components.DownloadManager.DownloadManagerFragment
import com.san.kir.manger.components.FirstRun.FirstRunActivity
import com.san.kir.manger.components.LatestChapters.LatestChaptersFragment
import com.san.kir.manger.components.Library.LibraryFragment
import com.san.kir.manger.components.Parsing.ManageSites
import com.san.kir.manger.components.Settings.PrefFragment
import com.san.kir.manger.components.SitesCatalog.SiteCatalogFragment
import com.san.kir.manger.components.Storage.StorageFragment
import com.san.kir.manger.dbflow.wrapers.MangaWrapper
import com.san.kir.manger.dbflow.wrapers.SettingsWrapper
import com.san.kir.manger.utils.DIR
import com.san.kir.manger.utils.FIRST_RUN
import com.san.kir.manger.utils.SET_MEMORY
import com.san.kir.manger.utils.createDirs
import com.san.kir.manger.utils.getShortPath
import com.san.kir.manger.utils.log
import com.san.kir.manger.utils.name_LAND_SPAN
import com.san.kir.manger.utils.name_PORT_SPAN
import com.san.kir.manger.utils.name_SET_MEMORY
import com.san.kir.manger.utils.name_SHOW_CATEGORY
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.alert
import org.jetbrains.anko.browse
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.anko.longToast
import org.jetbrains.anko.setContentView
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import java.io.File
import java.util.regex.Pattern


class MainActivity : AppCompatActivity() {

    private var isMain = true

    private val mView = MainView(this)

    companion object {
        lateinit var context: Context
        // функция проверки новой версии приложения на сайте 4pda.ru
        fun checkNewVersion(user: Boolean = false) {
            val url = "http://4pda.ru/forum/index.php?showtopic=772886&st=0#entry53336845"

            launch(CommonPool) {
                try {
                    val doc = ManageSites.asyncGetDocument(url)
                    val matcher = Pattern.compile("[0-9]\\.[0-9]\\.[0-9]")
                            .matcher(doc.select("#post-53336845 span > b").text())
                    if (matcher.find()) {
                        val version = matcher.group()
                        var message = ""
                        if (version != BuildConfig.VERSION_NAME)
                            message = com.san.kir.manger.App.context.getString(R.string.main_check_app_ver_find,
                                                                               version,
                                                                               BuildConfig.VERSION_NAME)
                        else
                            if (user)
                                message = com.san.kir.manger.App.context.getString(R.string.main_check_app_ver_no_find)


                        if (message.isNotEmpty())
                            launch(UI) {
                                Companion.context.alert {
                                    this.message = message
                                    positiveButton(R.string.main_check_app_ver_close) {}
                                    negativeButton(R.string.main_check_app_ver_go_to) {
                                        com.san.kir.manger.App.context.browse(url)
                                    }
                                }.show()
                            }
                    }
                } catch (ex: Throwable) {
                    launch(UI) {
                        com.san.kir.manger.App.context.longToast(R.string.main_check_app_ver_error)
                        log = ex.toString()
                    }
                }

            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = this

        // Проверка прав доступа
        val writeToExtStorage = Manifest.permission.WRITE_EXTERNAL_STORAGE
        // Проверяем есть ли разрешения на запись, если нет спрашиваем об этом
        if (ContextCompat.checkSelfPermission(this, writeToExtStorage)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(writeToExtStorage), 200)
        }

        // Первый запуск приложения
        if (!File(getExternalFilesDir(FIRST_RUN), FIRST_RUN).exists())
            startActivity<FirstRunActivity>()


        // Востановление настроек приложения
        with(defaultSharedPreferences) {
            if (contains(name_SET_MEMORY)) {
                SET_MEMORY = getString(name_SET_MEMORY, "")
            } else {
                SettingsWrapper.getSettings().forEach {
                    val name = it.name
                    val value = it.value
                    when (name) {
                        name_SET_MEMORY -> SET_MEMORY = value
                        name_PORT_SPAN -> edit().putString(name_PORT_SPAN, value).apply()
                        name_LAND_SPAN -> edit().putString(name_LAND_SPAN, value).apply()
                    }
                }
            }
            if (!contains(name_SHOW_CATEGORY))
                edit().putBoolean(name_SHOW_CATEGORY, true).apply()

        }


        mView.setContentView(this)

        checkNewManga()

        when (intent.getStringExtra("launch")) {
            "download" -> {
                isMain = false
                title = getString(R.string.main_menu_downloader)
                DownloadManagerFragment().replace
            }
            "catalog" -> {
                isMain = false
                title = getString(R.string.main_menu_catalogs)
                SiteCatalogFragment().replace
            }
            else -> {
                if (savedInstanceState == null)
                    LibraryFragment(this).add
            }
        }
    }

    private var back_pressed: Long = 0
    override fun onBackPressed() {
        if (mView.drawer_layout.isDrawerOpen(GravityCompat.START)) {
            mView.drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            if (isMain) {
                if (back_pressed + 2000 > System.currentTimeMillis())
                    super.onBackPressed()
                else
                    toast(R.string.first_run_exit_text)
                back_pressed = System.currentTimeMillis()
            } else {
                LibraryFragment(this).replace
                isMain = true
                title = getString(R.string.main_menu_library)
            }
        }
    }

    fun onNavigationItemSelected(id: Int) {
        with(MainView._id) {
            isMain = id == library

            when (id) {
                library -> LibraryFragment(this@MainActivity)
                category -> CategoryFragment()
                storage -> StorageFragment()
                catalogs -> SiteCatalogFragment()
                downloader -> DownloadManagerFragment()
                latest -> LatestChaptersFragment()
                settings -> PrefFragment()
                else -> LibraryFragment(this@MainActivity)
            }.replace

            LibraryFragment.actionMode?.let(ActionMode::finish)

            title = getString(
                    when (id) {
                        library -> com.san.kir.manger.R.string.main_menu_library
                        category -> com.san.kir.manger.R.string.main_menu_category
                        storage -> com.san.kir.manger.R.string.main_menu_storage
                        catalogs -> com.san.kir.manger.R.string.main_menu_catalogs
                        downloader -> com.san.kir.manger.R.string.main_menu_downloader
                        latest -> com.san.kir.manger.R.string.main_menu_latest
                        settings -> com.san.kir.manger.R.string.action_settings
                        else -> com.san.kir.manger.R.string.main_menu_library
                    })
        }
    }

    private fun checkNewManga() {
        launch(CommonPool) {
            delay(2000L)
            val view = findViewById<View>(android.R.id.content)
            val check = File(SET_MEMORY, DIR.LOCAL)
            if (check.exists()) {
                val checkableDirs = check.listFiles()
                if (checkableDirs.isNotEmpty()) {
                    val mangaPath = checkableDirs.filter {
                        getShortPath(it.path) !in MangaWrapper.getAllPath()
                    }
                    if (mangaPath.isNotEmpty())
                        launch(UI) {
                            Snackbar.make(view,
                                          R.string.main_check_new_manga_find,
                                          Snackbar.LENGTH_LONG)
                                    .setAction(R.string.main_check_new_manga_go_to, {
                                        StorageFragment().replace
                                        isMain = false
                                    }).show()
                        }
                }
            } else {
                launch(UI) {
                    Snackbar.make(view,
                                  R.string.main_check_new_manga_no_dirs,
                                  Snackbar.LENGTH_LONG).show()
                }
                createDirs(check.path)
            }
        }
    }

    private val Fragment.add: Int
        get() {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            return transaction.add(mView.fragment.id, this).commit()
        }

    private val Fragment.replace: Int
        get() {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            return transaction.replace(mView.fragment.id, this).commit()
        }
}

