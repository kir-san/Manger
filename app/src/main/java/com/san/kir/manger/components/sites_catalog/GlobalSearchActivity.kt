package com.san.kir.manger.components.sites_catalog

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import com.san.kir.ankofork.Binder
import com.san.kir.ankofork.appcompat.toolbar
import com.san.kir.ankofork.design.themedAppBarLayout
import com.san.kir.ankofork.dip
import com.san.kir.ankofork.doFromSdk
import com.san.kir.ankofork.horizontalProgressBar
import com.san.kir.ankofork.matchParent
import com.san.kir.ankofork.negative
import com.san.kir.ankofork.positive
import com.san.kir.ankofork.recyclerview.recyclerView
import com.san.kir.ankofork.sdk28.onQueryTextListener
import com.san.kir.ankofork.verticalLayout
import com.san.kir.ankofork.wrapContent
import com.san.kir.manger.R
import com.san.kir.manger.utils.extensions.BaseActivity
import com.san.kir.manger.utils.extensions.doOnApplyWindowInstets
import com.san.kir.manger.utils.extensions.setButton
import com.san.kir.manger.utils.extensions.setCloseButton
import com.san.kir.manger.utils.extensions.setTextColor
import com.san.kir.manger.utils.extensions.showAlways
import com.san.kir.manger.utils.extensions.visibleOrGone
import com.san.kir.manger.view_models.CatalogForOneSiteViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GlobalSearchActivity : BaseActivity() {

    private val mAdapter = GlobalSearchRecyclerPresenter(this)
    private var mJob: Job = Job()
    private val mOldTitle: CharSequence by lazy { title }
    private val isAction = Binder(true)

    val mViewModel by viewModels<CatalogForOneSiteViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        doFromSdk(Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.transparent_dark)
            window.navigationBarColor = ContextCompat.getColor(this, R.color.transparent_dark2)
        }

        verticalLayout {
            lparams(matchParent, matchParent)

            systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)

            doOnApplyWindowInstets { v, insets, _ ->
                v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    // Получаем размер выреза, если есть
                    val cutoutRight = insets.displayCutout?.safeInsetRight ?: 0
                    val cutoutLeft = insets.displayCutout?.safeInsetLeft ?: 0
                    // Вычитаем из WindowInsets размер выреза, для fullscreen
                    rightMargin = insets.systemWindowInsetRight - cutoutRight
                    leftMargin = insets.systemWindowInsetLeft - cutoutLeft
                }
                insets
            }

            themedAppBarLayout(R.style.ThemeOverlay_AppCompat_DayNight_ActionBar) {
                lparams(width = matchParent, height = wrapContent)

                doOnApplyWindowInstets { v, insets, _ ->
                    v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                        topMargin = insets.systemWindowInsetTop
                    }
                    insets
                }

                toolbar {
                    lparams(width = matchParent, height = wrapContent)
                    setSupportActionBar(this)
                }
            }

            horizontalProgressBar {
                isIndeterminate = true
                visibleOrGone(isAction)
            }.lparams(width = matchParent, height = dip(10))

            recyclerView {
                layoutManager =
                    androidx.recyclerview.widget.LinearLayoutManager(this@GlobalSearchActivity)
                mAdapter.into(this)

                clipToPadding = true

                doOnApplyWindowInstets { v, insets, padding ->
                    v.updatePadding(
                        bottom = padding.bottom + insets.systemWindowInsetBottom
                    )
                    insets
                }
            }.lparams(matchParent, matchParent)
        }

        mAdapter.loadSites { size ->
            lifecycleScope.launch(Dispatchers.Main) {
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
                    mJob = lifecycleScope.launch {
                        delay(1500L)
                        // Фильтрация при каждом изменении текста
                        mAdapter.changeOrder(searchText = it!!)
                        isAction.negative()
                    }
                    true
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
