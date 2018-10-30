package com.san.kir.manger.components.catalogForOneSite

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.support.v4.view.GravityCompat
import android.support.v7.widget.SearchView
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import com.san.kir.manger.R
import com.san.kir.manger.components.main.Main
import com.san.kir.manger.components.parsing.ManageSites
import com.san.kir.manger.eventBus.negative
import com.san.kir.manger.extending.BaseActivity
import com.san.kir.manger.extending.ankoExtend.onQueryTextListener
import com.san.kir.manger.extending.ankoExtend.startForegroundService
import com.san.kir.manger.extending.views.showAlways
import com.san.kir.manger.room.dao.updateAsync
import org.jetbrains.anko.alert
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.anko.find
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.setContentView
import org.jetbrains.anko.support.v4.onRefresh
import org.jetbrains.anko.textColor


class CatalogForOneSiteActivity : BaseActivity() {
    private val mSite by lazy {
        val id = intent.getIntExtra("id", -1)
        ManageSites.CATALOG_SITES[id]
    }

    // Сохраняем название окна
    val mOldTitle: CharSequence by lazy { title }

    private val adapter = CatalogForOneSiteRecyclerPresenter()
    private val view = CatalogForOneSiteView(this, adapter)
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val id = intent.getIntExtra(CatalogForOneSiteUpdaterService.EXTRA_KEY_OUT, -1)
            if (id != -1 && id == mSite.id || id == -2)
                updateCatalog()
        }
    }

    /* перезаписанные функции */
    override fun onCreate(savedInstanceState: android.os.Bundle?) {

        val key = getString(R.string.settings_app_dark_theme_key)
        val default = getString(R.string.settings_app_dark_theme_default) == "true"
        val isDark = defaultSharedPreferences.getBoolean(key, default)
        setTheme(if (isDark) R.style.AppThemeDark else R.style.AppTheme)

        super.onCreate(savedInstanceState)
        // Если id сайта не существует, то выйти из активити
        if (mSite.id < 0)
            onBackPressed()

        view.setContentView(this)
        // Присвоение адаптера
        view.swipe.onRefresh {
            reloadCatalogDialog()
            view.swipe.isRefreshing = false
        }

        updateCatalog()

        title = mSite.name

        // регистрируем BroadcastReceiver
        val intentFilter = IntentFilter(
            CatalogForOneSiteUpdaterService.ACTION_CATALOG_UPDATER_SERVICE
        )

        registerReceiver(receiver, intentFilter)
    }

    private fun updateCatalog() {
        adapter.setSite(mSite.id) { size ->
            // Изменяем заголовок окна
            title = "$mOldTitle: $size"

            // Убираем прогрессБар
            view.isAction.negative()

            Main.db.siteDao.loadSite(mSite.name)?.let {
                it.oldVolume = size
                Main.db.siteDao.updateAsync(it)
            }
        }
    }

    override fun onDestroy() {
        unregisterReceiver(receiver)
        adapter.close()
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Текстовое поле для поиска по названию
        menu!!.add(R.string.catalog_for_one_site_search)
            .showAlways()
            .actionView = SearchView(this).apply {
            setButton(R.drawable.ic_action_search)
            setCloseButton(R.drawable.ic_action_close)
            setTextColor(Color.WHITE)
            onQueryTextListener {
                onQueryTextChange {
                    // Фильтрация при каждом изменении текста
                    adapter.changeOrder(searchText = it!!)
                }
            }
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        // Если открыто боковое меню, то сперва закрыть его
        if (view.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            view.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    /* приватные функции */

    fun reloadCatalogDialog() {
        alert {
            titleResource = R.string.catalog_fot_one_site_warning
            messageResource = R.string.catalog_fot_one_site_redownload_text
            positiveButton(R.string.catalog_fot_one_site_redownload_ok) {
                if (!CatalogForOneSiteUpdaterService.isContain(mSite.id))
                    startForegroundService<CatalogForOneSiteUpdaterService>("id" to mSite.id)
            }
            negativeButton(getString(R.string.catalog_fot_one_site_redownload_cancel)) {}
        }.show()
    }

    private fun SearchView.setButton(resId: Int) {
        find<ImageView>(android.support.v7.appcompat.R.id.search_button).imageResource = resId
    }

    private fun SearchView.setCloseButton(resId: Int) {
        find<ImageView>(android.support.v7.appcompat.R.id.search_close_btn).imageResource = resId
    }

    private fun SearchView.setTextColor(color: Int) {
        find<TextView>(android.support.v7.appcompat.R.id.search_src_text).textColor = color
    }
}
