package com.san.kir.manger.components.catalog_for_one_site

import android.arch.lifecycle.ViewModelProviders
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.support.v4.view.GravityCompat
import android.support.v7.widget.SearchView
import android.view.Menu
import com.san.kir.manger.R
import com.san.kir.manger.components.parsing.ManageSites
import com.san.kir.manger.eventBus.negative
import com.san.kir.manger.extending.BaseActivity
import com.san.kir.manger.extending.anko_extend.onQueryTextListener
import com.san.kir.manger.extending.anko_extend.startForegroundService
import com.san.kir.manger.extending.launchUI
import com.san.kir.manger.extending.views.setButton
import com.san.kir.manger.extending.views.setCloseButton
import com.san.kir.manger.extending.views.setTextColor
import com.san.kir.manger.extending.views.showAlways
import com.san.kir.manger.view_models.CatalogForOneSiteViewModel
import org.jetbrains.anko.alert
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.anko.setContentView
import org.jetbrains.anko.support.v4.onRefresh


class CatalogForOneSiteActivity : BaseActivity() {
    private val mSite by lazy {
        val id = intent.getStringExtra("site")
        ManageSites.CATALOG_SITES.first { it.catalogName == id }
    }

    // Сохраняем название окна
    val mOldTitle: CharSequence by lazy { title }

    val mViewModel by lazy {
        ViewModelProviders.of(this).get(CatalogForOneSiteViewModel::class.java)
    }

    private val adapter = CatalogForOneSiteRecyclerPresenter(this)
    private val view = CatalogForOneSiteView(this, adapter)
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val catalogName = intent.getStringExtra(CatalogForOneSiteUpdaterService.EXTRA_KEY_OUT)
            if (catalogName == mSite.catalogName || catalogName == "destroy")
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
        adapter.setSite(mSite) { size ->
            launchUI {
                // Изменяем заголовок окна
                title = "$mOldTitle: $size"

                // Убираем прогрессБар
                view.isAction.negative()
            }

            mViewModel.getSiteItem(mSite.name)?.let {
                it.oldVolume = size
                mViewModel.siteUpdate(it)
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
                if (!CatalogForOneSiteUpdaterService.isContain(mSite.catalogName))
                    startForegroundService<CatalogForOneSiteUpdaterService>("catalogName" to mSite.catalogName)
            }
            negativeButton(getString(R.string.catalog_fot_one_site_redownload_cancel)) {}
        }.show()
    }
}
