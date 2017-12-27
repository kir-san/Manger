package com.san.kir.manger.components.CatalogForOneSite

import android.annotation.SuppressLint
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
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.instance
import com.san.kir.manger.EventBus.negative
import com.san.kir.manger.EventBus.positive
import com.san.kir.manger.Extending.BaseActivity
import com.san.kir.manger.Extending.Views.showAlways
import com.san.kir.manger.Extending.Views.showNever
import com.san.kir.manger.R
import com.san.kir.manger.components.Parsing.ManageSites
import org.jetbrains.anko.alert
import org.jetbrains.anko.appcompat.v7.coroutines.onQueryTextListener
import org.jetbrains.anko.find
import org.jetbrains.anko.imageResource
import org.jetbrains.anko.setContentView
import org.jetbrains.anko.startService
import org.jetbrains.anko.support.v4.onRefresh
import org.jetbrains.anko.textColor


class CatalogForOneSiteActivity : BaseActivity() {
    private val mSite by lazy {
        val id = intent.getIntExtra("id", -1)
        ManageSites.CATALOG_SITES[id]
    }

    // Сохраняем название окна
    val mOldTitle: CharSequence by lazy { title }

    private val adapter = CatalogForOneSiteRecyclerPresenter(injector)
    private val view = CatalogForOneSiteView(injector, adapter)
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val id = intent.getIntExtra(CatalogForOneSiteUpdaterService.EXTRA_KEY_OUT, -1)
            if (id != -1 && id == mSite.ID)
                updateCatalog()
        }
    }

    /* перезаписанные функции */
    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        // Если id сайта не существует, то выйти из активити
        if (mSite.ID < 0)
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
                CatalogForOneSiteUpdaterService.ACTION_CATALOGUPDATERSERVICE)
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT)

        registerReceiver(receiver, intentFilter)
    }

    private fun updateCatalog() {
        adapter.setSite(mSite.ID,
                        { view.isAction.positive() },
                        { size ->
                            // Изменяем заголовок окна
                            title = "$mOldTitle: $size"

                            // Закрываем окно
                            view.isAction.negative()
                        })
    }

    @SuppressLint("MissingSuperCall")
    override fun onDestroy() {
        unregisterReceiver(receiver)
        super.onDestroy()
    }

    override fun provideOverridingModule() = Kodein.Module {
        bind<CatalogForOneSiteActivity>() with instance(this@CatalogForOneSiteActivity)
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
                    true
                }
            }
        }

        // Обновление каталога
        menu.add(0, 0, 2, R.string.catalog_for_one_site_update)
                .showNever()

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
            0 -> {
                reloadCatalogDialog()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        // Если открыто боковое меню, то сперва закрыть его
        if (view.drawer_layout.isDrawerOpen(GravityCompat.START)) {
            view.drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    /* приватные функции */

    private fun reloadCatalogDialog() {
        alert {
            titleResource = R.string.catalog_fot_one_site_warning
            messageResource = R.string.catalog_fot_one_site_redownload_text
            positiveButton(R.string.catalog_fot_one_site_redownload_ok) {
                if (!CatalogForOneSiteUpdaterService.isContain(mSite.ID))
                    startService<CatalogForOneSiteUpdaterService>("id" to mSite.ID)
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
