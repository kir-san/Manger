package com.san.kir.manger.components.statistics

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.san.kir.ankofork.sdk28._LinearLayout
import com.san.kir.manger.R
import com.san.kir.manger.components.drawer.DrawerActivity
import com.san.kir.manger.utils.TimeFormat
import com.san.kir.manger.view_models.StatisticViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StatisticActivity : DrawerActivity() {
    private val mAdapter = StatisticRecyclerPresenter(this@StatisticActivity)
    val mViewModel by viewModels<StatisticViewModel>()

    override val _LinearLayout.customView: View
        get() = StatisticView(mAdapter).view(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch(Dispatchers.Main) {
            mViewModel.getStatisticAllTime().collect { l ->
                supportActionBar?.setTitle(R.string.main_menu_statistic)
                supportActionBar?.subtitle = withContext(Dispatchers.Default) {
                    val time = TimeFormat(l)
                    getString(R.string.statistic_subtitle, time.toString(this@StatisticActivity))
                }
            }
        }
    }
}
