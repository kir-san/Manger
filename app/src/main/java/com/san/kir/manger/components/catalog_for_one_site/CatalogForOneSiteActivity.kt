package com.san.kir.manger.components.catalog_for_one_site

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Build
import android.view.Menu
import androidx.activity.viewModels
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
import com.san.kir.ankofork.dialogs.alert
import com.san.kir.ankofork.dialogs.okButton
import com.san.kir.ankofork.doFromSdk
import com.san.kir.ankofork.negative
import com.san.kir.ankofork.positive
import com.san.kir.ankofork.sdk28.onQueryTextListener
import com.san.kir.ankofork.setContentView
import com.san.kir.manger.R
import com.san.kir.manger.components.parsing.ManageSites
import com.san.kir.manger.utils.extensions.BaseActivity
import com.san.kir.manger.utils.extensions.setButton
import com.san.kir.manger.utils.extensions.setCloseButton
import com.san.kir.manger.utils.extensions.setTextColor
import com.san.kir.manger.utils.extensions.showAlways
import com.san.kir.manger.utils.extensions.startForegroundService
import com.san.kir.manger.view_models.CatalogForOneSiteViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class CatalogForOneSiteActivity : BaseActivity() {
    private val mSite by lazy {
        val id = intent.getStringExtra("site")
        ManageSites.CATALOG_SITES.first { it.catalogName == id }
    }

    // Сохраняем название окна
    val mOldTitle: CharSequence by lazy { title }

    val mViewModel by viewModels<CatalogForOneSiteViewModel>()

    private val adapter = CatalogForOneSiteRecyclerPresenter(this)
    private val view = CatalogForOneSiteView(this, adapter)
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val catalogName = intent.getStringExtra(CatalogForOneSiteUpdaterService.EXTRA_KEY_OUT)
            when (catalogName) {
                mSite.catalogName -> updateCatalog()
                "destroy" -> hideAction()
                "error" -> showErrorDialog()
            }
        }
    }

    /* перезаписанные функции */
    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)

        doFromSdk(Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.transparent_dark)
            window.navigationBarColor = ContextCompat.getColor(this, R.color.transparent_dark2)
        }

        view.setContentView(this)

        updateCatalog()

        title = mSite.name

        // регистрируем BroadcastReceiver
        IntentFilter(CatalogForOneSiteUpdaterService.ACTION_CATALOG_UPDATER_SERVICE).apply {
            registerReceiver(receiver, this)
        }
    }

    private fun hideAction() {
        // Убираем прогрессБар
        if (!CatalogForOneSiteUpdaterService.isContain(mSite.catalogName)) {
            view.isAction.negative()
        }
    }

    private fun updateCatalog() {
        adapter.setSite(mSite) { size ->
            lifecycleScope.launch(Dispatchers.Main) {
                // Изменяем заголовок окна
                title = "$mOldTitle: $size"

                hideAction()

                withContext(Dispatchers.Default) {
                    mViewModel.getSiteItem(mSite.name)?.let {
                        it.oldVolume = size
                        mViewModel.siteUpdate(it)
                    }
                }
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
                    lifecycleScope.launch {
                        adapter.changeOrder(searchText = it!!)
                    }
                    true
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
                if (!CatalogForOneSiteUpdaterService.isContain(mSite.catalogName)) {
                    startForegroundService<CatalogForOneSiteUpdaterService>("catalogName" to mSite.catalogName)
                    view.isAction.positive()
                }
            }
            negativeButton(getString(R.string.catalog_fot_one_site_redownload_cancel)) {}
        }.show()
    }

    private fun showErrorDialog() {
        alert {
            titleResource = R.string.manga_error_dialog_title
            messageResource = R.string.manga_error_dialog_message
            okButton { hideAction() }
            show()
        }
    }
}
