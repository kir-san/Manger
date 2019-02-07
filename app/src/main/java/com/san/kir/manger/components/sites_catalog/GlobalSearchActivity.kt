package com.san.kir.manger.components.sites_catalog

import android.arch.lifecycle.ViewModelProviders
import android.graphics.Color
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.view.Menu
import android.view.MenuItem
import com.san.kir.manger.R
import com.san.kir.manger.eventBus.Binder
import com.san.kir.manger.eventBus.negative
import com.san.kir.manger.eventBus.positive
import com.san.kir.manger.extending.ThemedActionBarActivity
import com.san.kir.manger.extending.ankoExtend.onQueryTextListener
import com.san.kir.manger.extending.ankoExtend.visibleOrGone
import com.san.kir.manger.extending.launchUI
import com.san.kir.manger.extending.views.setButton
import com.san.kir.manger.extending.views.setCloseButton
import com.san.kir.manger.extending.views.setTextColor
import com.san.kir.manger.extending.views.showAlways
import com.san.kir.manger.view_models.CatalogForOneSiteViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.anko.dip
import org.jetbrains.anko.horizontalProgressBar
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.jetbrains.anko.verticalLayout

class GlobalSearchActivity : ThemedActionBarActivity() {

    private val mAdapter = GlobalSearchRecyclerPresenter(this)
    private var mJob = Job()
    private val mOldTitle: CharSequence by lazy { title }
    private val isAction = Binder(true)

    val mViewModel by lazy {
        ViewModelProviders.of(this).get(CatalogForOneSiteViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        verticalLayout {
            horizontalProgressBar {
                isIndeterminate = true
                visibleOrGone(isAction)
            }.lparams(width = matchParent, height = dip(10))
            recyclerView {
                layoutManager = LinearLayoutManager(this@GlobalSearchActivity)
                mAdapter.into(this)
            }
        }

        mAdapter.loadSites { size ->
            launchUI {
                // Изменяем заголовок окна
                title = "$mOldTitle: $size"

                // Убираем прогрессБар
                isAction.negative()
            }
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
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
                    isAction.positive()
                    mJob.cancel()
                    mJob = launch {
                        delay(1500L)
                        // Фильтрация при каждом изменении текста
                        mAdapter.changeOrder(searchText = it!!)
                        isAction.negative()
                    }
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

    override fun onDestroy() {
        mAdapter.close()
        super.onDestroy()
    }
}
