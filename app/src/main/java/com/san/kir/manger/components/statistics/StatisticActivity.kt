package com.san.kir.manger.components.statistics

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.core.view.updatePadding
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.san.kir.ankofork.matchParent
import com.san.kir.ankofork.recyclerview.recyclerView
import com.san.kir.ankofork.sdk28._LinearLayout
import com.san.kir.manger.R
import com.san.kir.manger.components.drawer.DrawerActivity
import com.san.kir.manger.utils.TimeFormat
import com.san.kir.manger.utils.extensions.doOnApplyWindowInstets
import com.san.kir.manger.view_models.StatisticViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StatisticActivity : DrawerActivity() {
    private val titleObserver by lazy {
        Observer<Long> { l ->
            l?.let {
                lifecycleScope.launch(Dispatchers.Main) {
                    supportActionBar?.setTitle(R.string.main_menu_statistic)
                    supportActionBar?.subtitle = withContext(Dispatchers.Default) {
                        val time = TimeFormat(it)
                        getString(
                            R.string.statistic_subtitle,
                            time.toString(this@StatisticActivity)
                        )
                    }
                }
            }
        }
    }
    val mViewModel by viewModels<StatisticViewModel>()

    override val _LinearLayout.customView: View
        get() = recyclerView {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
            setHasFixedSize(true)
            StatisticRecyclerPresenter(this@StatisticActivity).into(this)
            lparams(matchParent, matchParent)

            clipToPadding = false

            doOnApplyWindowInstets { view, insets, padding ->
                view.updatePadding(bottom = padding.bottom + insets.systemWindowInsetBottom)
                insets
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel.getStatisticAllTime().observe(this, titleObserver)
    }
}
